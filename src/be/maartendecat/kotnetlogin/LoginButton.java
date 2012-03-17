package be.maartendecat.kotnetlogin;

import android.view.View;
import android.widget.Button;
import be.maartendecat.kotnetlogin.AccountManager.AccountDataListener;

public class LoginButton extends Button implements AccountDataListener {
	
	private KotnetLoginActivity context;

	public LoginButton(KotnetLoginActivity context, String username) {
		super(context);
		this.context = context;
        setText(username);
        setHeight(40);
        setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	LoginButton.this.context.login();
            }
        });
	}

    /*************************************
     * ACCOUNT DATA LISTENER METHODS
     *************************************/
    
    /**
     * 	Called when the username is updated in the account manager.
     */
	public void onUsernameUpdated(String username) {
		// Update the button text
		this.setText(username);
	}

	/**
	 * 	Called when the password is updated in the account manager.
	 */
	public void onPasswordUpdated(String username) {
		// Nothing to do here
	}

    /*************************************
     * HELPER FUNCTIONS
     *************************************/
	
	/**
	 * 	Sets the button text using the given username.
	 */
	private void setText(String username) {
		super.setText("Log in as " + username);
	}

}
