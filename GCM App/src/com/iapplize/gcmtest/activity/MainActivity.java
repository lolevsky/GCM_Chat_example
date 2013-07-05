package com.iapplize.gcmtest.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBarDrawerToggle;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.facebook.Session;
import com.google.android.gms.plus.PlusClient;
import com.iapplize.gcmtest.MainApplication;
import com.iapplize.gcmtest.R;
import com.iapplize.gcmtest.fragment.MainDrawerFragment;
import com.iapplize.gcmtest.fragment.MainFragment;
import com.iapplize.gcmtest.gcm.GCMListener;
import com.iapplize.gcmtest.gcm.MainGCM;
import com.iapplize.gcmtest.google.plus.MomentUtil;
import com.iapplize.gcmtest.google.plus.PlusClientFragment;
import com.iapplize.gcmtest.google.plus.PlusClientFragment.OnSignedInListener;
import com.iapplize.gcmtest.http.HTTPResponceListener;
import com.iapplize.gcmtest.http.HttpConnect;
import com.iapplize.gcmtest.http.Parsing.xmlParser;
import com.iapplize.gcmtest.http.object.User;

public class MainActivity extends SherlockFragmentActivity implements
		OnSignedInListener, GCMListener , HTTPResponceListener, MainActivityUserSelectListener{

	static final String TAG = "MainActivity";

	public static final String NAME = "user_name";
	public static final String MAIL = "user_email";
	
	private PlusClientFragment mSignInFragment;

	DrawerLayout mDrawerLayout;
	FrameLayout mDrawerFrameLayout;
	ActionBarDrawerToggle mDrawerToggle;

	private MainFragment mMainFragment;
	private MainDrawerFragment mMainDrawerFragment;

	public boolean isPublicMassege = true;
	public User mSelectedUser;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initView();

		MainGCM.getInstance(this).setGCMListener(this);
		HttpConnect.getInstance().sendGcmGetUserList(this);
		
		((MainApplication)getApplicationContext()).setMainActivity(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		((MainApplication)getApplicationContext()).setMainActivity(null);
	}

	private void initView() {

		mSignInFragment = PlusClientFragment.getPlusClientFragment(this,
				MomentUtil.VISIBLE_ACTIVITIES);

		mMainFragment = MainFragment.newInstance(getIntent().getExtras());

		FragmentTransaction t = this.getSupportFragmentManager()
				.beginTransaction();

		mMainDrawerFragment = new MainDrawerFragment();
		
		t.replace(R.id.content_frame, mMainFragment);
		t.replace(R.id.left_drawer, mMainDrawerFragment);
		t.commit();

		mMainDrawerFragment.setMainActivityUserSelect(this);
		
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		// mDrawerLayout.setDrawerShadow(R.drawable.ic_action_drawer_shadow,GravityCompat.START);

		mDrawerFrameLayout = (FrameLayout) findViewById(R.id.left_drawer);

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, getSupportActionBar(),
				mDrawerLayout, R.drawable.ic_action_ic_drawer,
				R.string.drawer_open, R.string.drawer_close) {

			public void onDrawerClosed(View view) {
				// TODO Auto-generated method stub
				super.onDrawerClosed(view);
			}

			public void onDrawerOpened(View drawerView) {
				// TODO Auto-generated method stub
				super.onDrawerClosed(drawerView);
			}
		};

		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
		
		if(isPublicMassege && mMainFragment != null){
			mMainFragment.setSesionName(getString(R.string.session_with, getString(R.string.chat_with_all)));
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggles
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater mi = getSupportMenuInflater();
		mi.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.exit:
			mSignInFragment.signOut();

			Session.getActiveSession().closeAndClearTokenInformation();

			((MainApplication)getApplicationContext()).setSignedIn(false,getIntent().getExtras().getString(MainActivity.MAIL),getIntent().getExtras().getString(MainActivity.NAME));
			
			finish();
			break;
		case android.R.id.home:

			if (mDrawerLayout.isDrawerOpen(mDrawerFrameLayout)) {
				mDrawerLayout.closeDrawer(mDrawerFrameLayout);
			} else {
				mDrawerLayout.openDrawer(mDrawerFrameLayout);
			}
			break;
		}

		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		
		if (mDrawerLayout.isDrawerOpen(mDrawerFrameLayout)) {
			mDrawerLayout.closeDrawer(mDrawerFrameLayout);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void onSignedIn(PlusClient plusClient) {
		// TODO Auto-generated method stub

	}

	@Override
	public void recivedMessage(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void recivedRegistration(String message) {

		// MainGCM.getInstance(this).sendMassege("test from android");
		if (message.equalsIgnoreCase("OK")) {
			HttpConnect.getInstance().sendGcmSaveClient(
					MainGCM.getInstance(this).getRegistrationId(this),getIntent().getExtras().getString(MainActivity.MAIL),
					getIntent().getExtras().getString(MainActivity.NAME),null);
			
//			new Handler().postDelayed(new Runnable() {
//				
//				@Override
//				public void run() {
//					HttpConnect.getInstance().sendGcmSendMessage(
//							MainGCM.getInstance(MainActivity.this).getRegistrationId(MainActivity.this),
//							getIntent().getExtras().getString(MainActivity.NAME), null);
//					
//				}
//			}, 1000);
		}
	}

	@Override
	public void onResponce(String res, int Type) {
		switch (Type) {
		case HttpConnect.GCM_GET_USER_LIST:
			if(mMainDrawerFragment != null){
				//mMainDrawerFragment.setText(res);
				
				xmlParser xp = new xmlParser();
				
				mMainDrawerFragment.updateList(xp.parseUserData(res));
				
			}
			break;
		default:
			Toast.makeText(this, "Unknown http :" + res, Toast.LENGTH_SHORT).show();
			break;
		}
	}

	@Override
	public void onError(String err, int Type) {
		Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
		
	}

	@Override
	public void onUserSelect(int pos) {
		if(mMainDrawerFragment != null && mMainFragment != null){
			if(pos >= 0){
				synchronized (mMainDrawerFragment.user) {
					isPublicMassege = false;
					mSelectedUser = mMainDrawerFragment.user.get(pos);
					
					String name = "";
					
					if (mSelectedUser.UserName.equalsIgnoreCase("")) {
						name = mSelectedUser.Email;
					} else {
						name = mSelectedUser.UserName;
					}
					
					mMainFragment.setSesionName(getString(R.string.session_with, name));
		
				}
			}else{
				isPublicMassege = true;
				mSelectedUser = null;
				mMainFragment.setSesionName(getString(R.string.session_with, getString(R.string.chat_with_all)));
			}
		}
		
		if (mDrawerLayout.isDrawerOpen(mDrawerFrameLayout)) {
			mDrawerLayout.closeDrawer(mDrawerFrameLayout);
		}
	}
}
