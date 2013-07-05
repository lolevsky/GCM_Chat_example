package com.iapplize.gcmtest.http;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.iapplize.gcmtest.Installation;
import com.iapplize.gcmtest.MainApplication;
import com.iapplize.gcmtest.database.TableChat;
import com.iapplize.gcmtest.gcm.MainGCM;
import com.iapplize.gcmtest.http.object.Message;

public class HttpConnect {

	private static final String TAG = "HttpConnect";

	private static final String BASE_URL = "https://gcmtestserver.appspot.com/";

	public final static int GCM_SEND_MESSAGE = 0;
	public final static int GCM_SAVE_CLIENT = 1;
	public final static int GCM_GET_USER_LIST = 2;
	public final static int GCM_GET_MESSAGE = 3;

	private final String STRING_GCM_SEND_MESSAGE = "gcm_send_message";
	private final String STRING_GCM_SAVE_CLIENT = "gcm_save_client";
	private final String STRING_GCM_GET_USER_LIST = "gcm_get_user_list";
	private final String STRING_GCM_GET_MESSAGE = "gcm_get_Message";

	private RequestQueue mRequestQueue;

	private Context mContext;

	private static class HttpConnectHolder {
		public static HttpConnect instance = new HttpConnect();
	}

	private HttpConnect() {

	}

	public static HttpConnect getInstance() {
		return HttpConnectHolder.instance;
	}

	public void initHttpConnect(Context context) {
		mRequestQueue = Volley.newRequestQueue(context);
		mContext = context;
	}

	public void sendGcmGetMessage(String userEmail, String withEmail,
			boolean isAll, HTTPResponceListener listener) {

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();

		nvps.add(new BasicNameValuePair("regId", Installation.id(mContext)));
		nvps.add(new BasicNameValuePair("userEmail", userEmail));
		if (!isAll) {
			nvps.add(new BasicNameValuePair("withEmail", withEmail));
		}

		sendRequest(nvps, GCM_GET_MESSAGE, listener);
	}

	public void sendGcmSaveClient(String regId, String user_email,
			String userName, HTTPResponceListener listener) {

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();

		nvps.add(new BasicNameValuePair("regId", regId));
		nvps.add(new BasicNameValuePair("installationID", Installation
				.id(mContext)));
		nvps.add(new BasicNameValuePair("user_email", user_email));
		nvps.add(new BasicNameValuePair("user_name", userName));

		sendRequest(nvps, GCM_SAVE_CLIENT, listener);
	}

	public void sendGcmSendMessage(String regId, String user_email,
			String message, Boolean isForAll, HTTPResponceListener listener) {

		MainApplication mApp = (MainApplication)mContext.getApplicationContext();
		
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();

		nvps.add(new BasicNameValuePair("regId", regId));
		nvps.add(new BasicNameValuePair("message", message));
		nvps.add(new BasicNameValuePair("fromEmail", mApp.getMe().Email));
		nvps.add(new BasicNameValuePair("toEmail", user_email));
		nvps.add(new BasicNameValuePair("fromRegId", mApp.getMe().RegId));

		if (isForAll) {
			nvps.add(new BasicNameValuePair("toAll", "true"));
		}

		sendRequest(nvps, GCM_SEND_MESSAGE, listener);
	}

	public void sendGcmGetUserList(HTTPResponceListener listener) {

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();

		sendRequest(nvps, GCM_GET_USER_LIST, listener);
	}

	private void sendRequest(List<NameValuePair> nvps, final int req, final HTTPResponceListener listener) {

		String url = BASE_URL;

		switch (req) {
		case GCM_SEND_MESSAGE:
			url += STRING_GCM_SEND_MESSAGE;
			url += "?" + URLEncodedUtils.format(nvps, "UTF-8");
			break;
		case GCM_SAVE_CLIENT:
			url += STRING_GCM_SAVE_CLIENT;
			url += "?" + URLEncodedUtils.format(nvps, "UTF-8");
			break;
		case GCM_GET_USER_LIST:
			url += STRING_GCM_GET_USER_LIST;
			break;
		case GCM_GET_MESSAGE:
			url += STRING_GCM_GET_MESSAGE;
			url += "?" + URLEncodedUtils.format(nvps, "UTF-8");
			break;
		}

		StringRequest sr = new StringRequest(Request.Method.POST, url,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						Log.i(TAG, response);

						switch (req) {
						case GCM_GET_MESSAGE:

							JSONArray jArrey;
							try {
								jArrey = new JSONArray(response);

								ContentValues[] messages = Message
										.messageListFromJsonArrey(jArrey);
								int count = 0;
								if (messages.length > 0) {
									count = mContext.getContentResolver()
											.bulkInsert(TableChat.CONTENT_URI,
													messages);
								}
								Log.i(TAG, "response - bulkInsert :" + count);
							} catch (JSONException e) {
								Log.i(TAG, "error", e);
							}
							break;
						default:
							Log.i(TAG, response.toString());
							break;
						}

						if (listener != null) {
							listener.onResponce(response, req);
						}
					};
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						if(error == null || error.getMessage() == null){
							Log.i(TAG, "error");
							if (listener != null) {
								listener.onError("error",
										req);
							}
						}else{
							Log.i(TAG, error.getMessage());
							if (listener != null) {
								listener.onError(error.getMessage(),
										req);
							}
						}

					}

				});

		if (mRequestQueue != null) {
			mRequestQueue.add(sr);
			Log.d(TAG, "add - " + sr.getUrl());
		} else {
			Log.e(TAG, "Error sending");
		}
	}
}
