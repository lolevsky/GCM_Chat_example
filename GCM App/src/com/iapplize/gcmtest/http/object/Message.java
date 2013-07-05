package com.iapplize.gcmtest.http.object;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.iapplize.gcmtest.database.TableChat;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

public class Message {

	private static final String TAG = "Message";
	
	public String message = "";
	public String fromEmail = "";
	public boolean toAll = false;
	public String toEmail = "";
	public Date Updated_at;
	public String fromName = "";
	
	public static Message messageFromJson(JSONObject json){
		Message mMessage = new Message();
		
		mMessage.message = json.optString("message", "");
		mMessage.fromEmail = json.optString("fromEmail", "");
		if(json.optString("toAll", "false").equalsIgnoreCase("true")){
			mMessage.toAll = true;
		}
		mMessage.toEmail = json.optString("toEmail");
		mMessage.Updated_at = new Date(Long.valueOf(json.optString("Updated_at")));
		mMessage.fromName = json.optString("UserName");
		
		return mMessage;
	}
	
	public static Message messageFromCursor(Cursor cursor){
		Message mMessage = new Message();
		
		mMessage.message = cursor.getString(cursor.getColumnIndex(TableChat.COLUMN_MESSAGE));
		mMessage.fromEmail = cursor.getString(cursor.getColumnIndex(TableChat.COLUMN_FROM_USER));
		
		mMessage.fromName = cursor.getString(cursor.getColumnIndex(TableChat.COLUMN_FROM_NAME));

		if(cursor.getString(cursor.getColumnIndex(TableChat.COLUMN_MULTYCHAT)).equalsIgnoreCase("true")){
			mMessage.toAll = true;
		}
		mMessage.toEmail = cursor.getString(cursor.getColumnIndex(TableChat.COLUMN_TO_USER));
		mMessage.Updated_at = new Date( cursor.getLong(cursor.getColumnIndex(TableChat.COLUMN_UPDATED_AT)));
		
		return mMessage;
	}
	
	public static ContentValues[]  messageListFromJsonArrey(JSONArray jsonArray){
		List<ContentValues> messages = new ArrayList<ContentValues>();
		
		if (jsonArray.length() > 0) {
			for (int i = 0; i < jsonArray.length(); i++) {

				JSONObject jobJsonObject;
				try {
					jobJsonObject = (JSONObject) jsonArray
							.get(i);

					messages.add(Message.messageFromJson(jobJsonObject).toContentValues());

				} catch (JSONException e) {
					Log.i(TAG, e.getMessage());
				}
			}
		}
		
		ContentValues[] valArrey = new ContentValues[messages.toArray().length];
		
		for(int i = 0; i < messages.toArray().length ; i++){
			valArrey[i] = messages.get(i);
		}
		
		return valArrey;
	}
	
	public ContentValues toContentValues(){
		ContentValues contentValues = new ContentValues();
		contentValues.put(TableChat.COLUMN_MESSAGE,message);
		contentValues.put(TableChat.COLUMN_FROM_USER,fromEmail);
		contentValues.put(TableChat.COLUMN_MULTYCHAT, String.valueOf(toAll));
		contentValues.put(TableChat.COLUMN_TO_USER,toEmail);
		contentValues.put(TableChat.COLUMN_FROM_NAME,fromName);
		contentValues.put(TableChat.COLUMN_UPDATED_AT,Updated_at.getTime());
		
		return contentValues;
	}
}
