package com.iapplize.gcmtest;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.iapplize.gcmtest.activity.MainActivity;
import com.iapplize.gcmtest.gcm.MainGCM;
import com.iapplize.gcmtest.http.HttpConnect;
import com.iapplize.gcmtest.http.object.User;

public class MainApplication extends Application{

	private SharedPreferences prefs;
	public static final String IS_SIGNED_IN = "isSignedIn";
	public static final String USER_EMAIL = "user_email";
	public static final String USER_NAME = "user_name";
	
	private MainActivity mMainActivity;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		HttpConnect.getInstance().initHttpConnect(this);
		
		prefs = getSharedPreferences(MainApplication.class.getSimpleName(), Context.MODE_PRIVATE);
		
	}
	
	public void setMainActivity(MainActivity main){
		mMainActivity = main;
	}
	
	public MainActivity getMainActivity(){
		return mMainActivity;
	}
	
	public boolean isSignedIn(){
		return prefs.getBoolean(IS_SIGNED_IN, false);
	}
	
	public void setSignedIn(boolean signedIn, String Email, String Name) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(IS_SIGNED_IN, signedIn);
		
		if(signedIn){
			editor.putString(USER_EMAIL, Email);
			editor.putString(USER_NAME, Name);
		}else{
			editor.putString(USER_EMAIL, "");
			editor.putString(USER_NAME, "");
		}
		
		editor.commit();
	}
	
	public User getMe(){
		User user = new User();
		
		user.Email = prefs.getString(USER_EMAIL, "");
		user.UserName = prefs.getString(USER_NAME, "");
		user.MacAddress = Installation.id(this).replace("-", "");
		user.RegId = MainGCM.getInstance(this).getRegistrationId(this);
		
		return user;
	}
}
