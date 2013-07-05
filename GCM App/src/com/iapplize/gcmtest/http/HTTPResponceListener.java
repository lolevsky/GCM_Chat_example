package com.iapplize.gcmtest.http;

public interface HTTPResponceListener {
	public void onResponce(String res, int Type);
	public void onError(String err, int Type);
}
