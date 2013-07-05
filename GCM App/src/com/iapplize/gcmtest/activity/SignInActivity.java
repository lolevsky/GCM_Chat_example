package com.iapplize.gcmtest.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.model.people.Person;
import com.iapplize.gcmtest.MainApplication;
import com.iapplize.gcmtest.R;
import com.iapplize.gcmtest.google.plus.MomentUtil;
import com.iapplize.gcmtest.google.plus.PlusClientFragment;
import com.iapplize.gcmtest.google.plus.PlusClientFragment.OnSignedInListener;

public class SignInActivity extends SherlockFragmentActivity implements
		OnClickListener, OnSignedInListener {

	private final String PENDING_ACTION_BUNDLE_KEY = "com.iapplize.gcmtest.SignInActivity:PendingAction";

	public static final int REQUEST_CODE_PLUS_CLIENT_FRAGMENT = 0;

	private LoginButton loginButton;

	private TextView mSignInStatus;
	private PlusClientFragment mSignInFragment;

	private UiLifecycleHelper uiHelper;

	private PendingAction pendingAction = PendingAction.NONE;

	private View splash;

	private MainApplication mApp;

	private enum PendingAction {
		NONE, POST_PHOTO, POST_STATUS_UPDATE
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApp = (MainApplication) getApplicationContext();

		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			String name = savedInstanceState
					.getString(PENDING_ACTION_BUNDLE_KEY);
			pendingAction = PendingAction.valueOf(name);
		}

		setContentView(R.layout.activity_signin);

		// for facebook use for getting the signature
		// try {
		// PackageInfo info =
		// getPackageManager().getPackageInfo("com.iapplize.gcmtest",
		// PackageManager.GET_SIGNATURES);
		// for (Signature signature : info.signatures) {
		// MessageDigest md = MessageDigest.getInstance("SHA");
		// md.update(signature.toByteArray());
		// Log.d("YOURHASH KEY:",
		// Base64.encodeToString(md.digest(), Base64.DEFAULT));
		// }
		// } catch (NameNotFoundException e) {
		//
		// } catch (NoSuchAlgorithmException e) {
		//
		// }

		splash = findViewById(R.id.splash);

		mSignInFragment = PlusClientFragment.getPlusClientFragment(this,
				MomentUtil.VISIBLE_ACTIVITIES);

		SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
		signInButton.setOnClickListener(this);
		// findViewById(R.id.sign_out_button).setOnClickListener(this);
		// findViewById(R.id.revoke_access_button).setOnClickListener(this);
		mSignInStatus = (TextView) findViewById(R.id.sign_in_status);

		loginButton = (LoginButton) findViewById(R.id.fb_login_button);
		loginButton
				.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
					@Override
					public void onUserInfoFetched(GraphUser user) {
						if (user != null) {
							
							mApp.setSignedIn(true, user.getId(), user.getUsername());
							
							// String greeting = getString(
							// R.string.greeting_status,
							// user.getFirstName());
							// mSignInStatus.setText(greeting);

							Intent intent = new Intent(SignInActivity.this,
									MainActivity.class);
							intent.putExtra(MainActivity.NAME,
									user.getUsername());
							intent.putExtra(MainActivity.MAIL, user.getId());
							startActivity(intent);
							finish();
						}else{
							mApp.setSignedIn(false, null, null);
							splash.setVisibility(View.GONE);
						}
					}
				});

		if (!mApp.isSignedIn()) {
			splash.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);

		outState.putString(PENDING_ACTION_BUNDLE_KEY, pendingAction.name());
	}

	@Override
	protected void onResume() {
		super.onResume();
		uiHelper.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		// case R.id.sign_out_button:
		// resetAccountState();
		// mSignInFragment.signOut();
		// break;
		case R.id.sign_in_button:
			mSignInFragment.signIn(REQUEST_CODE_PLUS_CLIENT_FRAGMENT);
			break;
		// case R.id.revoke_access_button:
		// resetAccountState();
		// mSignInFragment.revokeAccessAndDisconnect();
		// break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int responseCode,
			Intent intent) {

		mSignInFragment.handleOnActivityResult(requestCode, responseCode,
				intent);
		uiHelper.onActivityResult(requestCode, responseCode, intent);
		
		switch (responseCode) {
		case Activity.RESULT_CANCELED:
			mApp.setSignedIn(false, null, null);
			splash.setVisibility(View.GONE);
			break;
		}

	}

	@Override
	public void onSignedIn(PlusClient plusClient) {
		mSignInStatus.setText(getString(R.string.signed_in_status));

		// We can now obtain the signed-in user's profile information.
		Person currentPerson = plusClient.getCurrentPerson();
		if (currentPerson != null) {
			// String greeting = getString(R.string.greeting_status,
			// currentPerson.getDisplayName());
			// mSignInStatus.setText(greeting);

			mApp.setSignedIn(true, currentPerson.getId(), currentPerson.getDisplayName());
			
			Intent intent = new Intent(SignInActivity.this, MainActivity.class);
			intent.putExtra(MainActivity.NAME, currentPerson.getDisplayName());
			intent.putExtra(MainActivity.MAIL, currentPerson.getId());
			startActivity(intent);
			finish();
		} else {
			resetAccountState();
		}
	}

	private void resetAccountState() {
		mSignInStatus.setText(getString(R.string.signed_out_status));
		mApp.setSignedIn(false, null, null);
		splash.setVisibility(View.GONE);
	}

	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		if (pendingAction != PendingAction.NONE
				&& (exception instanceof FacebookOperationCanceledException || exception instanceof FacebookAuthorizationException)) {
			new AlertDialog.Builder(SignInActivity.this)
					.setTitle(R.string.cancelled)
					.setMessage(R.string.permission_not_granted)
					.setPositiveButton(R.string.ok, null).show();
			pendingAction = PendingAction.NONE;
		} else if (state == SessionState.OPENED_TOKEN_UPDATED) {
			// handlePendingAction();
		}
	}

}
