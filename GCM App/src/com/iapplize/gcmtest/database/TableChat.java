package com.iapplize.gcmtest.database;

import java.util.HashMap;

import android.net.Uri;

public class TableChat {

	public static final String TABLE_NAME = "chat";

	public static final Uri CONTENT_URI = Uri.parse(DataBaseProvider.getContentURIString() + "/" + TABLE_NAME);
    
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + DataBaseProvider.getVndPath() + "." + TABLE_NAME;
    
    public static HashMap<String, String> sProjectionMap;
    
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_MESSAGE = "message";
	public static final String COLUMN_FROM_USER = "fromUser";
	public static final String COLUMN_TO_USER = "toUser";
	public static final String COLUMN_MULTYCHAT = "multyChat";
	public static final String COLUMN_UPDATED_AT = "UpdatedAt";
	public static final String COLUMN_FROM_NAME = "UserName";
	
	

	// Database creation sql statement
	public static final String DATABASE_CREATE = "create table " + TABLE_NAME
			+ "(" + COLUMN_ID + " INTEGER PRIMARY KEY, "
			+ COLUMN_MESSAGE + " TEXT,"
			+ COLUMN_FROM_USER + " TEXT,"
			+ COLUMN_TO_USER + " TEXT,"
			+ COLUMN_UPDATED_AT + " INTEGER UNIQUE NOT NULL,"
			+ COLUMN_MULTYCHAT + " TEXT," 
			+ COLUMN_FROM_NAME + " TEXT" 
			+ " );";

}
