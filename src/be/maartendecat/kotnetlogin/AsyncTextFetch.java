package be.maartendecat.kotnetlogin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import android.os.AsyncTask;

/**
 * 
 * @author maartend
 *
 */
public class AsyncTextFetch extends AsyncTask<URL, Long, Boolean> {
	
	private String result;
	private IOException exception;
	private Type type;

	/****************************
	 * FETCH TYPE
	 ****************************/
	
	public enum Type {
		GET, POST;
	}
	
	/****************************
	 * CALLBACK
	 ****************************/
	
	public interface AsyncTextFetchCallback {
		
		/**
		 * Called on success.
		 */
		public void onSuccess(String result);
		
		/**
		 * Called on IOException.
		 */
		public void onIOException(IOException e);
		
	}
	private AsyncTextFetchCallback callback;
	
	/****************************
	 * CONSTRUCTOR
	 ****************************/
	
	public AsyncTextFetch(Type type, AsyncTextFetchCallback callback) {
		this.type = type;
		this.callback = callback;
	}

	@Override
	/**
	 * Called when the fetch is started (in a separate thread).
	 * Only the first URL will be fetched!
	 */
	protected Boolean doInBackground(URL... urls) {
		URL url = urls[0];
		BufferedReader reader = null;
	    StringBuilder builder = new StringBuilder();
	    try {
	        reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
	        for (String line; (line = reader.readLine()) != null;) {
	            builder.append(line.trim());
	        }
	        result = builder.toString();
	        try {
	        	reader.close(); 
        	} catch (IOException logOrIgnore) {}
	        return true;
	    } catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
	        if (reader != null) try { reader.close(); } catch (IOException logOrIgnore) {}
	    }
        return false;
	}
	
	@Override
	/**
	 * Called when the fetch finished successfully (in the original/UI thread).
	 */
	protected void onPostExecute(Boolean success) {
		if(success) {
			callback.onSuccess(result);
		} else {
			callback.onIOException(exception);
		}
	}

}
