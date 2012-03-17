package be.maartendecat.kotnetlogin;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import be.maartendecat.kotnetlogin.AccountManager.AccountDataListener;
import be.maartendecat.kotnetlogin.LoginManager.LoginProcedureListener;

public class KotnetLoginActivity extends Activity implements AccountDataListener, LoginProcedureListener {
	
	/************************************
	 * INSTANCE FIELDS
	 ************************************/
	
	/**
	 * 	The account data manager
	 */
	private AccountManager am;
	
	/**
	 * The central login button. Null if this is not initialized yet
	 * (for example when the user has not set his username and password yet).
	 */
	private LoginButton loginBtn = null;
	
	/**
	 * The info text shown when the user has not set his account details yet.
	 */
	private TextView infoTxt = null;
	
	/**
	 * The login manager for this activity.
	 */
	private LoginManager lm;
	
	/**
	 * The progress dialog used when logging in.
	 */
	private ProgressDialog pd;
	
    /************************************
     * GENERAL ACTIVITY FUNCTIONALITY
     ************************************/  
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        
		///////////////////////////////
		// INITIALIZE DATA
		///////////////////////////////
        
        AccountManager.initialize(this);
        am = AccountManager.getInstance();
        am.registerAccountDataListener(this);
        lm = LoginManager.getInstance();
        lm.registerLoginProcedureListener(this);
        
        ///////////////////////////////
        // INITIALIZE LAYOUT
        ///////////////////////////////
        setContentView(R.layout.main);
        
        if(am.isUsernameSet() && am.isPasswordSet()) {
        	showLoginBtn(am.getUsername());
        } else {
        	final LinearLayout layout = (LinearLayout) findViewById(R.id.wholeScreen);
        	infoTxt = new TextView(this);
        	infoTxt.setText("In order to log in, please specify your account details in the Preferences menu first.");
        	layout.addView(infoTxt);
        	//openOptionsMenu();
        }
        
        checkConnection();
    }
    
    /**
     * Create the menu: use the main menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.mainmenu, menu);
    	return true;
    }

    /**
     * Called when the menu is selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
	    	// We have only one menu option for now
	    	case R.id.preferences:
	    		// Launch Preference activity
	    		Intent i = new Intent(this, MyPreferencesActivity.class);
	    		startActivity(i);
	    		break;
    	}
    	return true;
    }
    
    /**
     * Called when the login btn is pressed.
     */
    public void login() {
    	lm.startLoginProcedure(am.getUsername(), am.getPassword());
    }

    /*************************************
     * ACCOUNT DATA LISTENER METHODS
     *************************************/
    
    /**
     * Called when the username is updated in the account manager.
     */
	public void onUsernameUpdated(String username) {
		// When the username and password are set for the first time, 
		// show the login button.
		if(loginBtn == null && am.isPasswordSet() && am.isUsernameSet()) {
			showLoginBtn(username);
		}
	}

	/**
	 * Called when the password is updated in the account manager.
	 */
	public void onPasswordUpdated(String username) {
		// When the username and password are set for the first time, 
		// show the login button.
		if(loginBtn == null && am.isPasswordSet() && am.isUsernameSet()) {
			showLoginBtn(am.getUsername());
		}
	}

    /*************************************
     * LOGIN PROCEDURE LISTENER METHODS
     *************************************/

	public void onLoginProcedureStarted(String username) {
		if(pd != null) {
			pd.dismiss();
		}
		pd = ProgressDialog.show(this, "", 
                "Logging in as " + username + "...", true);
	}

	public void onNewStageReached(String description) {
		pd.setMessage(description);
	}

	public void onProcedureError(String description) {
		pd.setMessage("Error: " + description);
		dismissProgressDialog(3000);
	}

	public void onProcedureSuccess() {
		pd.setMessage("Login successful");
		dismissProgressDialog(1000);
	}

	public void onProcedureFailure(String description) {
		pd.setMessage("Login failed: " + description);
		dismissProgressDialog(3000);
	}
	
    /************************************
     * HELPER METHODS
     ************************************/  
    
    /**
     * Show the login button on screen.
     */
    private void showLoginBtn(String username) {
    	final LinearLayout layout = (LinearLayout) findViewById(R.id.wholeScreen);
    	layout.removeView(infoTxt);
        loginBtn = new LoginButton(this, username);
        am.registerAccountDataListener(loginBtn);
        layout.addView(loginBtn);
    }
    
    /**
     * Check that the user can be connected to Kotnet: show a toast if it
     * seems unlikely the user is connected to Kotnet.
     */
    private void checkConnection() {
    	ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    	State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
    	if (!(wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING)) {
    		Toast.makeText(this, "No wireless connections available, are you sure you are connected to Kotnet?",	Toast.LENGTH_LONG).show();
    	}
    }
    
    /**
     * Dismisses the progress dialog after delay milliseconds.
     */
    private void dismissProgressDialog(long delay) {
		final Timer t = new Timer();
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				pd.dismiss();
			}
		}, delay);
    }
}