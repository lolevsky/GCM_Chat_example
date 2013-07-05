package com.iapplize.gcmtest.gcm;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.iapplize.gcmtest.R;

public class MainGCM {

	static final String TAG = "MainGCM";

	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";

	private static final String PROPERTY_APP_VERSION = "appVersion";
	private static final String PROPERTY_ON_SERVER_EXPIRATION_TIME = "onServerExpirationTimeMs";

	AtomicInteger msgId = new AtomicInteger();

	GCMListener mGCMListener;
	
	/**
	 * Default lifespan (7 days) of a reservation until it is considered
	 * expired.
	 */
	public static final long REGISTRATION_EXPIRY_TIME_MS = 1000 * 3600 * 24 * 7;

	/**
	 * Substitute you own sender ID here.
	 */
	String SENDER_ID = "";

	GoogleCloudMessaging gcm;
	String regid;

	private static MainGCM instance;
	private Context mContext;

	public static MainGCM getInstance(Context context) {

		if (instance == null) {
			instance = new MainGCM();
			instance.mContext = context;
			instance.init();
		}

		return instance;
	}
	
	public void setGCMListener(GCMListener gcmListener){
		mGCMListener = gcmListener;
		
		if (regid.length() == 0) {
			registerBackground();
		}else{
			if(mGCMListener != null){
				mGCMListener.recivedRegistration("OK");
			}
		}
	}

	public void init() {
		SENDER_ID = mContext.getString(R.string.gcm_sender_id);

		regid = getRegistrationId(mContext);

		if (regid.length() == 0) {
			registerBackground();
		}
		
		gcm = GoogleCloudMessaging.getInstance(mContext);
	}

	public void sendMassege(String test) {

		SendMasssege task = new SendMasssege();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, test);
		} else {
			task.execute(test);
		}

	}

	public String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.length() == 0) {
			Log.v(TAG, "Registration not found.");
			return "";
		}
		// check if app was updated; if so, it must clear registration id to
		// avoid a race condition if GCM sends a message
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion || isRegistrationExpired()) {
			Log.v(TAG, "App version changed or registration expired.");
			return "";
		}
		return registrationId;
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	/**
	 * Checks if the registration has expired.
	 * 
	 * <p>
	 * To avoid the scenario where the device sends the registration to the
	 * server but the server loses it, the app developer may choose to
	 * re-register after REGISTRATION_EXPIRY_TIME_MS.
	 * 
	 * @return true if the registration has expired.
	 */
	private boolean isRegistrationExpired() {
		final SharedPreferences prefs = getGCMPreferences(mContext);
		// checks if the information is not stale
		long expirationTime = prefs.getLong(PROPERTY_ON_SERVER_EXPIRATION_TIME,
				-1);
		return System.currentTimeMillis() > expirationTime;
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGCMPreferences(Context context) {
		return mContext.getSharedPreferences(MainGCM.class.getSimpleName(),
				Context.MODE_PRIVATE);
	}

	private void registerBackground() {
		RegisterBackground task = new RegisterBackground();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			task.execute();
		}
	}

	/**
	 * Stores the registration id, app versionCode, and expiration time in the
	 * application's {@code SharedPreferences}.
	 * 
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration id
	 */
	private void setRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGCMPreferences(context);
		int appVersion = getAppVersion(context);
		Log.v(TAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		long expirationTime = System.currentTimeMillis()
				+ REGISTRATION_EXPIRY_TIME_MS;

		Log.v(TAG, "Setting registration expiry time to "
				+ new Timestamp(expirationTime));
		editor.putLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, expirationTime);
		editor.commit();
	}

	private class SendMasssege extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String msg = params[0];
			try {
				Bundle data = new Bundle();
				data.putString("hello", msg);
				String id = Integer.toString(msgId.incrementAndGet());
				gcm.send(SENDER_ID + "@gcm.googleapis.com", id, data);
				msg = "Sent message";
			} catch (IOException ex) {
				msg = "Error :" + ex.getMessage();
			}
			return msg;
		}

		@Override
		protected void onPostExecute(String result) {
			if(mGCMListener != null){
				mGCMListener.recivedMessage(result);
			}
		}
	}

	private class RegisterBackground extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			String msg = "";
			try {
				if (gcm == null) {
					gcm = GoogleCloudMessaging.getInstance(mContext);
				}
				regid = gcm.register(SENDER_ID);
				//msg = "Device registered, registration id=" + regid;
				msg = "OK";
				// You should send the registration ID to your server over
				// HTTP,
				// so it can use GCM/HTTP or CCS to send messages to your
				// app.

				// For this demo: we don't need to send it because the
				// device
				// will send upstream messages to a server that echo back
				// the message
				// using the 'from' address in the message.

				// Save the regid - no need to register again.
				setRegistrationId(mContext, regid);
			} catch (IOException ex) {
				msg = "Error :" + ex.getMessage();
			}
			return msg;
		}

		@Override
		protected void onPostExecute(String result) {
			if(mGCMListener != null){
				mGCMListener.recivedRegistration(result);
			}
		}

	}
}
