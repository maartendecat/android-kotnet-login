package be.maartendecat.kotnetlogin;

import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import android.os.AsyncTask;

/**
 * 
 * @author maartend
 *
 */
public class AsyncJSoupFetch extends AsyncTask<Connection, Long, Boolean> {
	
	private Document result;
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
	
	public interface AsyncJSoupFetchCallback {
		
		/**
		 * Called on success.
		 */
		public void onSuccess(Document result);
		
		/**
		 * Called on IOException.
		 */
		public void onIOException(IOException e);
		
	}
	private AsyncJSoupFetchCallback callback;
	
	/****************************
	 * CONSTRUCTOR
	 ****************************/
	
	public AsyncJSoupFetch(Type type, AsyncJSoupFetchCallback callback) {
		this.type = type;
		this.callback = callback;
	}

	@Override
	/**
	 * Called when the fetch is started (in a separate thread).
	 * Only the first connection will be executed!
	 */
	protected Boolean doInBackground(Connection... connections) {
		Connection connection = connections[0];
		try {
			if(type == Type.GET) {
				result = connection.get();
			} else {
				result = connection.post();
			}
			return true;
		} catch( IOException e) {
			exception = e;
			return false;
		}
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
