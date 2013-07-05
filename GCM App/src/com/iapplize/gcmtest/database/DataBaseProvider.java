package com.iapplize.gcmtest.database;

import java.util.HashMap;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class DataBaseProvider extends ContentProvider {

	private static final String TAG = "DataBaseProvider";

	public static final String AUTHORITY = "com.iapplize.gcmtest.databaseprovider";
	public static final String VND_PATH = "vnd.iapplize.gcmtest.databaseprovider";

	public static final String CONTENT_URI_STRING = "content://" + AUTHORITY;
	public static final Uri CONTENT_URI = Uri.parse(CONTENT_URI_STRING);

	private static final int URI_CODE_CHAT = 10;

	private MySQLiteHelper mDbHelper;

	public static String getContentURIString() {
		return CONTENT_URI_STRING;
	}

	public static String getVndPath() {
		return VND_PATH;
	}

	private UriMatcher mUriMatcher;

	private UriMatcher getUriMatcher() {
		if (mUriMatcher == null) {
			UriMatcher newMatcher = new UriMatcher(UriMatcher.NO_MATCH);

			newMatcher.addURI(AUTHORITY, TableChat.TABLE_NAME, URI_CODE_CHAT);
			newMatcher.addURI(AUTHORITY, TableChat.TABLE_NAME + "/#",
					URI_CODE_CHAT);

			mUriMatcher = newMatcher;
		}

		return mUriMatcher;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count = 0;

		try {
			SQLiteDatabase db = mDbHelper.getWritableDatabase();

			int uriCode = getUriMatcher().match(uri);

			switch (uriCode) {
			case URI_CODE_CHAT:
				count = db.delete(TableChat.TABLE_NAME, selection,
						selectionArgs);
				break;

			default:
				throw new UnsupportedOperationException("The URI "
						+ uri.toString() + " is not supported for delete.");
			}

		} catch (Exception e) {
			Log.e(TAG, "Exception while deleting", e);
		}

		if (count > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}

		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (getUriMatcher().match(uri)) {
		case URI_CODE_CHAT:
			return TableChat.CONTENT_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {

		Uri baseUri = null;
		Uri newObjectUri = null;
		long rowId = -1;

		try {
			SQLiteDatabase db = mDbHelper.getWritableDatabase();

			int uriCode = getUriMatcher().match(uri);

			switch (uriCode) {
			case URI_CODE_CHAT:
				rowId = db.insertWithOnConflict(TableChat.TABLE_NAME, null,
						values, SQLiteDatabase.CONFLICT_REPLACE);
				baseUri = TableChat.CONTENT_URI;
				break;
			default:
				throw new UnsupportedOperationException("The URI "
						+ uri.toString() + " is not supported for insert.");

			}
		} catch (Exception e) {
			Log.e(TAG, "Exception during insert", e);
		}

		if (rowId >= 0 && baseUri != null) {
			newObjectUri = ContentUris.withAppendedId(baseUri, rowId);
			getContext().getContentResolver().notifyChange(baseUri, null);
		}

		return newObjectUri;
	}

	@Override
	public boolean onCreate() {
		mDbHelper = new MySQLiteHelper(getContext());
		getUriMatcher();
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		String groupBy = null;
		String having = null;
		List<String> segments = uri.getPathSegments();

		try {
			SQLiteDatabase db = mDbHelper.getReadableDatabase();
			int uriCode = getUriMatcher().match(uri);
			SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
			Uri notificationUri = uri;

			switch (uriCode) {
			case URI_CODE_CHAT:
				standardQuery(qb, segments, TableChat.TABLE_NAME,
						TableChat.sProjectionMap, TableChat.COLUMN_ID);
				break;

			default:
				throw new UnsupportedOperationException("The URI "
						+ uri.toString() + " is not supported for query.");
			}

			Cursor c = qb.query(db, projection, selection, selectionArgs,
					groupBy, having, sortOrder);
			c.setNotificationUri(getContext().getContentResolver(),
					notificationUri);
			return c;

		} catch (Exception e) {
			Log.e(TAG, "Exception executing query", e);
		}

		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int count = 0;
		String table = null;
		try {
			SQLiteDatabase db = mDbHelper.getWritableDatabase();

			int uriCode = getUriMatcher().match(uri);
			switch (uriCode) {
			case URI_CODE_CHAT:
				table = "MomentTable";
				count = db.updateWithOnConflict(TableChat.TABLE_NAME, values,
						selection, selectionArgs,
						SQLiteDatabase.CONFLICT_IGNORE);
				break;
			default:
				throw new UnsupportedOperationException("The URI "
						+ uri.toString() + " is not supported for update.");
			}
		} catch (Exception e) {
			Log.e(TAG, "Exception during update", e);
		}
		if (count > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		} else {
			Log.i("Error", "Update " + table + " is not updated");
		}

		return count;
	}

	private static void standardQuery(SQLiteQueryBuilder qb,
			List<String> segments, String tableName,
			HashMap<String, String> projectionMap, String idColumn) {
		if (segments.size() == 2) {
			qb.appendWhere(idColumn + "=" + segments.get(1));
		}
		qb.setTables(tableName);
		qb.setProjectionMap(projectionMap);
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		int count = 0;

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.beginTransaction();

		try {
			int uriCode = getUriMatcher().match(uri);

			switch (uriCode) {
			case URI_CODE_CHAT:
				count = standardBulkInsert(db, values, TableChat.TABLE_NAME,
						null, SQLiteDatabase.CONFLICT_REPLACE);
				break;
			default:
				throw new UnsupportedOperationException("The URI "
						+ uri.toString() + " is not supported for bulkInsert.");
			}

		} catch (Exception e) {
			Log.e(TAG, "Exception during bulk insert", e);
		} finally {
			db.endTransaction();
		}

		if (count > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}

		return count;
	}

	private int standardBulkInsert(SQLiteDatabase db,
			ContentValues[] valuesArray, String tableName,
			String nullColumnHack, int conflictAlgorithm) {
		int count = 0;
		long rowId;

		for (ContentValues values : valuesArray) {
			if (values != null) {
				rowId = db.insertWithOnConflict(tableName, nullColumnHack,
						values, conflictAlgorithm);

				if (rowId != -1)
					count++;
			}
		}

		if (count > 0) {
			db.setTransactionSuccessful();
		}
		return count;
	}
}
