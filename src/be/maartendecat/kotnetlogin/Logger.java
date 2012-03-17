package be.maartendecat.kotnetlogin;

import android.util.Log;

/**
 * Class used for logging during the login procedure.
 * 
 * @author maartend
 *
 */
public class Logger {
	
	private static final String TAG = "Logger";

	public Logger() {
		
	}
	
	public void startNewSession(String username) {
		Log.i(TAG, "Starting new session for " + username);
	}
}
