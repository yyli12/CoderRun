package cn.edu.fudan.cs12.coderrun.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.Bind;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import cn.edu.fudan.cs12.coderrun.Config;
import cn.edu.fudan.cs12.coderrun.R;
import cn.edu.fudan.cs12.coderrun.Util;
import cn.edu.fudan.cs12.coderrun.action.UserAction;
import cn.edu.fudan.cs12.coderrun.entity.User;
import cn.edu.fudan.cs12.coderrun.event.LoginEvent;
import cn.edu.fudan.cs12.coderrun.event.SignUpEvent;
import cn.edu.fudan.cs12.coderrun.event.SmsEvent;
import cn.edu.fudan.cs12.coderrun.provider.BusProvider;
import de.halfbit.tinybus.Subscribe;

public class LoginActivity extends AppCompatActivity {
	private final static int ACTION_LOGIN = 1;
	private final static int ACTION_SIGNUP = 0;
	int action = ACTION_LOGIN;

	// UI references.
	@Bind(R.id.input_mobile) EditText mMobileInput;
	@Bind(R.id.input_password) EditText mPasswordInput;
	@Bind(R.id.login_error) TextView mLoginError;
	@Bind(R.id.action_button_login) Button mLoginButton;
	@Bind(R.id.action_button_signup) Button mSignupButton;
	@Bind(R.id.login_progress) ProgressBar mProgressBar;
	EditText mSmsCodeInput;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		ButterKnife.bind(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		BusProvider.getInstance().register(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		BusProvider.getInstance().unregister(this);
	}

	boolean mobileCheck() {
		if (!Util.isMobileNum(mMobileInput.getText().toString())) {
			mMobileInput.setError("这不是手机号");
			mMobileInput.requestFocus();
			return false;
		}
		return true;
	}

	boolean passwdCheck() {
		if (!Util.validPassword(mPasswordInput.getText().toString())) {
			mPasswordInput.setError("密码太短啦");
			mPasswordInput.requestFocus();
			return false;
		}
		return true;
	}

	@OnClick(R.id.action_button_signup)
	void signUp() {
		if (mobileCheck()) {
			mLoginError.setText("");
			UserAction.getRegSMSCode(mMobileInput.getText().toString());
		}
	}

	@OnClick(R.id.action_button_login)
	void login() {
		if (mobileCheck() && passwdCheck()) {
			mLoginError.setText("");
			attemptLogin();
		}
	}

	@Subscribe
	public void afterLogin(LoginEvent event) {
		if (event.code == Config.SUCCESS) {
			Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
			startActivity(mainIntent);
			LoginActivity.this.finish();
		} else {
			mLoginError.setText(event.errorMessage);
		}
	}

	@Subscribe
	public void afterRequireSms(SmsEvent event) {
		if (event.code == Config.SUCCESS) {
			showSmsCodeDialog();
		} else {
			mLoginError.setText(event.errorMessage);
		}
	}

	@Subscribe
	public void afterSignUp(SignUpEvent event) {
		if (event.code == Config.SUCCESS) {
			Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
			startActivity(mainIntent);
			LoginActivity.this.finish();
		} else {
			mLoginError.setText(event.errorMessage);
			new Handler().postDelayed(new Runnable() {
				public void run() {
					showSmsCodeDialog();
				}
			}, 1000);
		}
	}

	private void attemptLogin() {
		String mobile = mMobileInput.getText().toString();
		String password = mPasswordInput.getText().toString();
		UserAction.logInWithMobileAndPassword(mobile, password);
	}

	private void attemptWithSmsCode() {
		String smsCode = mSmsCodeInput.getText().toString();
		String mobile = mMobileInput.getText().toString();
		UserAction.signUpOrLoginWithSMSCode(mobile, smsCode);
	}

	private void showSmsCodeDialog() {
		LayoutInflater inflater = getLayoutInflater();
		View v = inflater.inflate(R.layout.component_smscode_input, null);
		mSmsCodeInput = (EditText) v.findViewById(R.id.input_sms);

		MaterialDialog dialog = new MaterialDialog.Builder(this)
				.title("输入验证码")
				.contentColorRes(R.color.gray_dark)
				.customView(v, false)
				.positiveText("登录")
				.positiveColorRes(R.color.primary)
				.onPositive(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
						attemptWithSmsCode();
					}
				})
				.negativeText("取消")
				.negativeColorRes(R.color.gray_light)
				.show();
	}
}

