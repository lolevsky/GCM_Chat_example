package com.iapplize.gcmtest.fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.iapplize.gcmtest.MainApplication;
import com.iapplize.gcmtest.R;
import com.iapplize.gcmtest.activity.MainActivity;
import com.iapplize.gcmtest.database.TableChat;
import com.iapplize.gcmtest.http.HTTPResponceListener;
import com.iapplize.gcmtest.http.HttpConnect;

public class MainFragment extends SherlockFragment implements OnClickListener,
		LoaderManager.LoaderCallbacks<Cursor>, HTTPResponceListener {

	private static final String TAG = "MainFragment";

	private static final int LOADER_MESSAGE = 1;

	private View mRoot;
	private TextView mSesionName;
	private ListView mListView;
	private EditText massege;

	private ChatAdapter mAdapter;
	
	private MainApplication mApp;

	public static MainFragment newInstance(Bundle extra) {
		MainFragment f = new MainFragment();

		f.setArguments(extra);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRetainInstance(true);
		
		mApp = (MainApplication)getActivity().getApplicationContext();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mRoot = inflater.inflate(R.layout.fragment_main, null);

		mListView = (ListView) mRoot.findViewById(android.R.id.list);
		mSesionName = (TextView) mRoot.findViewById(R.id.sessionName);
		massege = (EditText) mRoot.findViewById(R.id.massege);
		((Button) mRoot.findViewById(R.id.send)).setOnClickListener(this);

		return mRoot;
	}

	public void setSesionName(String name) {
		mSesionName.setText(name);
		getLoaderManager().restartLoader(LOADER_MESSAGE, null, this);
		getMessages();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// String[] dataColumns = { TableChat.COLUMN_FROM_USER,
		// TableChat.COLUMN_MESSAGE ,TableChat.COLUMN_UPDATED_AT};
		// int[] viewIDs = { R.id.Text1, R.id.Text2 , R.id.Text3 };
		//
		// mAdapter = new SimpleCursorAdapter(getActivity(),
		// R.layout.messagerow,
		// null, dataColumns, viewIDs, 0);

		mAdapter = new ChatAdapter(getActivity(), null, 0);

		mListView.setAdapter(mAdapter);

		getLoaderManager().initLoader(LOADER_MESSAGE, null, this);

	}

	public void getMessages() {
		MainApplication mainApp = (MainApplication) getActivity()
				.getApplicationContext();
		MainActivity main = mainApp.getMainActivity();

		// getActivity().getContentResolver().delete(TableChat.CONTENT_URI,
		// null, null);

		if (main != null) {
			if (main.mSelectedUser != null) {
				HttpConnect.getInstance().sendGcmGetMessage(
						mainApp.getMe().Email, main.mSelectedUser.Email, false,
						null);
			} else if (main.isPublicMassege) {
				HttpConnect.getInstance().sendGcmGetMessage(
						mainApp.getMe().Email, null, true, null);
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.send:
			if (massege.getText().toString().equalsIgnoreCase("")) {

			} else {
				if (getActivity() instanceof MainActivity) {

					MainActivity main = (MainActivity) getActivity();
					if (main.isPublicMassege) {
						HttpConnect.getInstance().sendGcmSendMessage("", "",
								massege.getText().toString(), true, this);
					} else {
						synchronized (main.mSelectedUser) {

							// String userName = "";

							// if (main.mSelectedUser.UserName
							// .equalsIgnoreCase("")) {
							// userName = main.mSelectedUser.Email;
							// } else {
							// userName = main.mSelectedUser.UserName;
							// }

							HttpConnect.getInstance().sendGcmSendMessage(
									main.mSelectedUser.RegId,
									main.mSelectedUser.Email,
									massege.getText().toString(), false, this);
						}
					}

				}
			}

			massege.setText("");

			break;
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
		Log.d(TAG, "onCreateLoader " + loaderId);
		switch (loaderId) {
		case LOADER_MESSAGE:

			String selection = null;
			String[] arg = null;
			
			String sortOrder = TableChat.COLUMN_UPDATED_AT + " DESC";

			if (getActivity() instanceof MainActivity) {

				MainActivity main = (MainActivity) getActivity();
				if (!main.isPublicMassege) {
					synchronized (main.mSelectedUser) {

						selection = "(" + TableChat.COLUMN_FROM_USER + " = ? AND "
								+ TableChat.COLUMN_TO_USER + " = ? ) OR (" + TableChat.COLUMN_FROM_USER + " = ? AND "
								+ TableChat.COLUMN_TO_USER + " = ? )";
						arg = new String[] { main.mSelectedUser.Email,
								mApp.getMe().Email, mApp.getMe().Email,
								main.mSelectedUser.Email };

					}
				}else{
					selection = TableChat.COLUMN_MULTYCHAT + " = ? ";
							
					arg = new String[] {"true"};
				}
			}

			return new CursorLoader(getActivity(), TableChat.CONTENT_URI, null,
					selection, arg, sortOrder);
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		switch (loader.getId()) {
		case LOADER_MESSAGE:
			mAdapter.swapCursor(cursor);
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

	@Override
	public void onResponce(String res, int Type) {

		switch (Type) {
		case HttpConnect.GCM_SEND_MESSAGE:
			getMessages();
			break;

		}
	}

	@Override
	public void onError(String err, int Type) {

	}

	private class ChatAdapter extends CursorAdapter {

		private LayoutInflater mInflater;
		private SimpleDateFormat postFormater;

		public ChatAdapter(Context context, Cursor c, int flags) {
			super(context, c, flags);
			postFormater = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
		}

		/**
		 * @see android.widget.ListAdapter#getView(int, View, ViewGroup)
		 */
		public View getView(int position, View convertView, ViewGroup parent) {
			if (!mDataValid) {
				throw new IllegalStateException(
						"this should only be called when the cursor is valid");
			}
			if (!mCursor.moveToPosition(position)) {
				throw new IllegalStateException(
						"couldn't move cursor to position " + position);
			}
			View v;
			if (convertView == null) {
				v = newView(mContext, mCursor, parent);
			} else {
				v = convertView;
			}
			bindView(v, mContext, mCursor);
			return v;
		}

		@Override
		public void bindView(View convertView, Context context, Cursor cursor) {
			// Message message = removeMessageFromCacheOrRehydrate(cursor);
			Holder holder = (Holder) convertView.getTag();

			String name = cursor.getString(cursor
					.getColumnIndex(TableChat.COLUMN_FROM_NAME));
			
			if(name.equalsIgnoreCase("")){
				holder.text1.setText(cursor.getString(cursor
						.getColumnIndex(TableChat.COLUMN_FROM_USER)));
			}else{
				holder.text1.setText(name);
			}
			
			holder.text2.setText(cursor.getString(cursor
					.getColumnIndex(TableChat.COLUMN_MESSAGE)));

			String date = getString(R.string.time_send)
					+ postFormater.format((new Date(Long.valueOf(cursor.getString(cursor
							.getColumnIndex(TableChat.COLUMN_UPDATED_AT))))));

			holder.text3.setText(date);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {

			if (mInflater == null) {
				mInflater = LayoutInflater.from(context);
			}

			Holder holder = null;

			holder = new Holder();

			View newView = mInflater.inflate(R.layout.messagerow, null);
			holder.text1 = (TextView) newView.findViewById(R.id.Text1);
			holder.text2 = (TextView) newView.findViewById(R.id.Text2);
			holder.text3 = (TextView) newView.findViewById(R.id.Text3);

			newView.setTag(holder);

			return newView;
		}

		public class Holder {
			public TextView text1;
			public TextView text2;
			public TextView text3;
		}

	}
}
