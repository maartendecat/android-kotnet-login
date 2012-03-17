package be.maartendecat.kotnetlogin;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;


public class AccountManager implements OnSharedPreferenceChangeListener {
	
	/************************************
	 * STATIC SINGLETON STUFF
	 ************************************/
	
	private static AccountManager instance;
	
	public static AccountManager getInstance() {
		if(instance == null) {
			throw new NullPointerException("initialize before requesting an instance!");
		}
		return instance;
	}
	
	public static void initialize(Context ctx) {
		if(instance == null) {
			instance = new AccountManager(ctx);
		}
	}
	
	/************************************
	 * LISTENERS
	 ************************************/
	
	public interface AccountDataListener {
		
		/**
		 * Called when the username is updated.
		 */
		public void onUsernameUpdated(String username);
		
		/**
		 * Called when the password is updated.
		 */
		public void onPasswordUpdated(String username);
		
	}
	
	private List<AccountDataListener> listeners = new ArrayList<AccountDataListener>();
	
	/**
	 * Register a listener.
	 */
	public void registerAccountDataListener(AccountDataListener listener) {
		this.listeners.add(listener);
	}
	
	/**
	 * Unregister a listener. Nothing is changed if the listener was not registered
	 * in the first place.
	 */
	public void unregisterAccountDataListener(AccountDataListener listener) {
		this.listeners.remove(listener);
	}	
	
	/************************************
	 * CONSTRUCTOR
	 ************************************/
	
	private AccountManager(Context ctx) {
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		prefs.registerOnSharedPreferenceChangeListener(this);
	}
	
	/************************************
	 * INSTANCE FIELDS
	 ************************************/
	
	private final SharedPreferences prefs;
	
	/************************************
	 * USERNAME
	 ************************************/
	
	/**
	 * Sets the username to the given value and notifies the listeners.
	 */
	private void setUsername(String username) {
		Editor edit = prefs.edit();
		edit.putString("username", username);
		edit.commit();
		// notify listeners
		for(AccountDataListener listener: this.listeners) {
			listener.onUsernameUpdated(username);
		}
	}

	/**
	 * Returns the user's username. Null if he has not set it yet.
	 */
	public String getUsername() {
		return prefs.getString("username", null);
	}

	/**
	 * 	Returns whether the user has set his username.
	 */
	public boolean isUsernameSet() {
		return prefs.contains("username");
	}
	
	/************************************
	 * 	PASSWORD
	 ************************************/
	
	/**
	 * Sets the password to the given value and notifies the listeners.
	 */
	private void setPassword(String password) {
		Editor edit = prefs.edit();
		prefs.edit().putString("password", password);
		edit.commit();
		// notify listeners
		for(AccountDataListener listener: this.listeners) {
			listener.onPasswordUpdated(password);
		}
	}
	
	/**
	 * Returns the user's password. Null if he has not set it yet.
	 */
	public String getPassword() {
		return prefs.getString("password", null);
	}
	
	/**
	 * Returns whether the user has set his password.
	 */
	public boolean isPasswordSet() {
		return prefs.contains("password");
	}
	
	/************************************
	 * SHARED PREFERENCE LISTENER METHODS
	 ************************************/

	/**
	 * Called when a shared preference is changed. For us, the only preferences
	 * are username and password.
	 */
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(key.equals("username")) {
			setUsername(sharedPreferences.getString(key, null));
		} else if(key.equals("password")) {
			setPassword(sharedPreferences.getString(key, null));
		}
	}

}
