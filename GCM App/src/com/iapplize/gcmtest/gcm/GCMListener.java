package com.iapplize.gcmtest.gcm;

public interface GCMListener {

	public void recivedMessage(String message);
	
	public void recivedRegistration(String message);
}
