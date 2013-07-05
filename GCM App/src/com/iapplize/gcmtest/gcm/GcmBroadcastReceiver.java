package com.iapplize.gcmtest.gcm;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.iapplize.gcmtest.MainApplication;
import com.iapplize.gcmtest.R;
import com.iapplize.gcmtest.activity.MainActivity;
import com.iapplize.gcmtest.http.HttpConnect;

public class GcmBroadcastReceiver extends BroadcastReceiver {
	static final String TAG = "GcmBroadcastReceiver";
	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	Context ctx;

	@Override
	public void onReceive(Context context, Intent intent) {
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
		ctx = context;
		String messageType = gcm.getMessageType(intent);
		if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
			sendNotification("Send error: " + intent.getExtras().toString());
		} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
				.equals(messageType)) {
			sendNotification("Deleted messages on server: "
					+ intent.getExtras().toString());
		} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
				.equals(messageType)) {

			Bundle extra = intent.getExtras();
			if (extra != null) {

				MainApplication mainApp = (MainApplication) context
						.getApplicationContext();
				MainActivity main = mainApp.getMainActivity();

				if(main != null){
					if(main.mSelectedUser != null){
						HttpConnect.getInstance().sendGcmGetMessage(mainApp.getMe().Email, main.mSelectedUser.Email, false, null);
					}else if(main.isPublicMassege){
						HttpConnect.getInstance().sendGcmGetMessage(mainApp.getMe().Email, null, true, null);
					}
				}
				
				sendNotification(extra.getString("message"));

			} else {
				sendNotification("ERROR");
			}
		} else {
			sendNotification("Received: " + intent.getExtras().toString());
		}
		setResultCode(Activity.RESULT_OK);
	}

	// Put the GCM message into a notification and post it.
	private void sendNotification(String msg) {
		mNotificationManager = (NotificationManager) ctx
				.getSystemService(Context.NOTIFICATION_SERVICE);

		// PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
		// new Intent(ctx, SignInActivity.class), 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				ctx).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("GCM Notification")
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg);

		// mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}
}
