package be.maartendecat.kotnetlogin;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import be.maartendecat.kotnetlogin.AsyncJSoupFetch.AsyncJSoupFetchCallback;
import be.maartendecat.kotnetlogin.AsyncJSoupFetch.Type;

/**
 * Class used for executing the login procedure.
 * 
 * @author maartend
 *
 */
public class LoginManager {
	
	private static final String TAG = "LoginManager";

	private static Context context;
	
	/************************************
	 * STATIC SINGLETON STUFF
	 ************************************/
	
	private static LoginManager instance;
	
	public static LoginManager getInstance(KotnetLoginActivity context) {
		if(instance == null) {
			LoginManager.context = context;
			instance = new LoginManager();
		}
		return instance;
	}
	
	/****************************
	 * FIELDS
	 ****************************/
	
	private String username;
	private String password;
	
	/****************************
	 * CONSTRUCTOR
	 ****************************/
	
	private LoginManager() {
		
	}
	
	/************************************
	 * LISTENERS
	 ************************************/
	
	/**
	 * Interface used for classes following up on the login procedure.
	 */
	public interface LoginProcedureListener {
		
		/**
		 * Called when the login procedure is started.
		 */
		public void onLoginProcedureStarted(String username);
		
		/**
		 * Called when a new stage in the procedure is reached.
		 * This method is purposely kept high-level (as opposed to multiple
		 * specific methods for each procedure stage).
		 */
		public void onNewStageReached(String description);
		
		/**
		 * Called when the procedure failed because of an error during the 
		 * procedure (e.g., a page could not be fetched). The procedure does 
		 * not proceed after this.
		 */
		public void onProcedureError(String description);
		
		/**
		 * Called when the procedure ended successfully.
		 */
		public void onProcedureSuccess();
		
		/**
		 * Called when the procedure ended in a failure (e.g., credentials
		 * not correct).
		 */
		public void onProcedureFailure(String description);

		/**
		 * Called when the procedure is aborted.
		 */
		public void onProcedureAborted();
	}
	
	/**
	 * Internal class used for easy notification of login procedure listeners (readable code!).
	 */
	private class LoginProcedureListeners extends ArrayList<LoginProcedureListener> {
		
		private static final long serialVersionUID = 8773932103001806230L;

		public void notifyLoginProcedureStarted(String username) {
			for(LoginProcedureListener l: this) { 
				l.onLoginProcedureStarted(username); 
			}
		}

		public void notifyNewStageReached(String description) {
			for(LoginProcedureListener l: this) { 
				l.onNewStageReached(description);
			}
		}

		public void notifyProcedureError(String description) {
			for(LoginProcedureListener l: this) { 
				l.onProcedureError(description);
			}
		}

		public void notifyProcedureSuccess() {
			for(LoginProcedureListener l: this) { 
				l.onProcedureSuccess();
			}
		}

		public void notifyProcedureFailure(String description) {
			for(LoginProcedureListener l: this) { 
				l.onProcedureFailure(description);
			}
		}

		public void notifyProcedureAborted() {
			for(LoginProcedureListener l: this) {
				l.onProcedureAborted();
			}
		}
	}
	
	private LoginProcedureListeners listeners = new LoginProcedureListeners();
	
	/**
	 * Register a listener.
	 */
	public void registerLoginProcedureListener(LoginProcedureListener listener) {
		this.listeners.add(listener);
	}
	
	/************************************
	 * CORE FUNCTIONALITY
	 ************************************/
	
	/**
	 * Starts the login procedure with given username and password.
	 * 
	 * @param username
	 * @param password
	 * @return boolean	Successful login or not.
	 */
	public void startLoginProcedure(String username, String password) {
		this.username = username;
		this.password = password;
		
		Log.i(TAG, "Starting new session for " + username);
		listeners.notifyLoginProcedureStarted(username);
		
		// fetch the login page
		listeners.notifyNewStageReached("Fetching login page...");
		Connection loginConnection = Jsoup.connect("https://netlogin.kuleuven.be/cgi-bin/wayf2.pl?inst=kuleuven&lang=nl&submit=Ga+verder+%2F+Continue");
		
		new AsyncJSoupFetch(Type.GET, new AsyncJSoupFetchCallback() {
			
			public void onSuccess(Document result) {
				LoginManager.this.onLoginPageFetched(result);
			}
			
			public void onIOException(IOException e) {
				String description = "Failed to fetch the login page (" + e.getClass().getName() + ": " + e.getMessage() + ")";
				Log.e(TAG, description);
				listeners.notifyProcedureError(description);
				// procedure finished
			}
		}).execute(loginConnection);
	}	
	
	/**
	 * Called after the async login page fetch when the document fetch was successful.
	 * 
	 * @param loginPage
	 */
	private void onLoginPageFetched(Document loginPage) {
		performLoginLogout(loginPage, true);
	}

	/**
	 * Called after the async logout page fetch when the document fetch was successful.
	 * 
	 * @param logoutPage
	 */
	private void onLogoutPageFetched(Document logoutPage) {
		performLoginLogout(logoutPage, false);
	}

	/**
	 * Perform login and logout.
	 * 
	 * @param loginPage
	 * @param isLogin
	 */
	private void performLoginLogout(Document loginPage, final Boolean isLogin) {
		Log.i(TAG, "Fetched netlogin page");
		
		Elements forms;
		if (isLogin)
			forms = loginPage.select("form[name=netlogin]");
		else
			forms = loginPage.select("form[name=netlogout]");

		if(forms.size() == 0) {
			Log.e(TAG, "No forms with name \"netlogin\" in the page. Quitting.");
			listeners.notifyProcedureError("No forms with name \"netlogin\" in the page.");
			return;
		} else if (forms.size() > 1) {
			Log.e(TAG, "Multiple forms with name \"netlogin\" in the page. Quitting.");
			listeners.notifyProcedureError("Multiple forms with name \"netlogin\" in the page.");
			return;
		}
		Element form = forms.first();
		Log.e(TAG, "Found login form.");
		
		// process the login form
		// target page
		String target = form.attr("action");
		if(target.startsWith("/")) {
			target = "https://netlogin.kuleuven.be" + target;
		}
		Log.i(TAG, "Form target: " + target);
		// set up connection to the target
		Connection resultConnection = Jsoup.connect(target);
		// hidden inputs
		Elements hiddenInputs = form.select("input[type=hidden]");
		for(Element input: hiddenInputs) {
			resultConnection.data(input.attr("name"), input.attr("value"));
		}
		Log.i(TAG, "Found " + hiddenInputs.size() + " hidden inputs.");
		// determine name of the password field (this changes for every request, but always starts with "pwd")
		Elements passwordFields = form.select("input[type=password]");
		if(passwordFields.size() == 0) {
			Log.e(TAG, "No password fields in the form. Quitting.");
			listeners.notifyProcedureError("No password fields in the form.");
			return;
		} else if (passwordFields.size() > 1) {
			Log.e(TAG, "Multiple password fields in the form. Quitting.");
			listeners.notifyProcedureError("Multiple password fields in the form. Quitting.");
			return;
		}
		Element passwordField = passwordFields.first();
		// add the password to the connection
		resultConnection.data(passwordField.attr("name"), password);
		// finally, also add the username if we are logging in
		if (isLogin)
			resultConnection.data("uid", username);
		
		// post the form and retrieve the result
		listeners.notifyNewStageReached("Submitting login information...");
		new AsyncJSoupFetch(Type.POST, new AsyncJSoupFetchCallback() {
			
			public void onSuccess(Document result) {
				if (isLogin)
					LoginManager.this.onResultPageFetched(result);
				else
					LoginManager.this.startLoginProcedure(username, password);
			}
			
			public void onIOException(IOException e) {
				String description = "Failed to fetch the result page (" + e.getClass().getName() + ": " + e.getMessage() + ")";
				Log.e(TAG, description);
				listeners.notifyProcedureError(description);
				// procedure finished
			}
			
		}).execute(resultConnection);
	}	
		
		/**
		 * Called after the async login page fetch when the document fetch was successful.
		 * 
		 * @param loginPage
		 */
	private void onResultPageFetched(Document resultPage) {		
		// process result
		Elements errorMsgs = resultPage.getElementsMatchingText("Login NIET geslaagd").select("font");
		if(errorMsgs.isEmpty()) {
			// TODO is this robust enough?
			Log.i(TAG, "Woohoo, login successfull.");
			listeners.notifyProcedureSuccess();
		} else {
			// The first sibling of the font element should be the <p> containing the error.
			Element p = errorMsgs.first().nextElementSibling();
			if (p.text().indexOf("206 : maximum") != -1) {
				Log.i(TAG, "Another device is connected");
				abortLoginDialog(resultPage);
			} else {
				Log.e(TAG, "Eek, login failed: " + p.text());
				listeners.notifyProcedureFailure(p.text());
			}
		}
	}

	/**
	 * Show dialog to ask the user whether the other device has to be disconnected or not.
	 * 
	 * @param page
	 */
	private void abortLoginDialog(final Document page) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(R.string.disconnect_dialog)
			.setCancelable(false)
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					disconnectDevice(page);
				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
					listeners.notifyProcedureAborted();
				}
			});
		AlertDialog alert = builder.create();
		alert.show();
	}

	/**
	 * Logout from the other device.
	 * 
	 * @param page
	 */
	private void disconnectDevice(Document page) {
		Elements forms = page.select("form[action~=.*/cgi-bin/wayf2.pl]");
		Element form = forms.first();
		String target = form.attr("action");
		if (target.startsWith("/")) {
			target = "https://netlogin.kuleuven.be" + target;
		}
		Connection resultConnection = Jsoup.connect(target);

		Elements hiddenInputs = form.select("input[type=hidden]");
		for(Element input: hiddenInputs) {
			resultConnection.data(input.attr("name"), input.attr("value"));
		}

		new AsyncJSoupFetch(Type.POST, new AsyncJSoupFetchCallback() {

			public void onSuccess(Document result) {
				LoginManager.this.onLogoutPageFetched(result);
			}

			public void onIOException(IOException e) {
				String description = "Failed to fetch the result page (" + e.getClass().getName() + ": " + e.getMessage() + ")";
				Log.e(TAG, description);
				listeners.notifyProcedureError(description);
				// procedure finished
			}

		}).execute(resultConnection);
	}
}
