package cn.edu.fudan.cs12.coderrun.action;

import android.content.Intent;
import android.widget.ProgressBar;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVMobilePhoneVerifyCallback;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVRelation;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.CountCallback;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.LogInCallback;
import com.avos.avoscloud.RequestMobileCodeCallback;
import com.avos.avoscloud.RequestPasswordResetCallback;
import com.avos.avoscloud.SaveCallback;
import com.avos.avoscloud.SignUpCallback;
import com.avos.avoscloud.UpdatePasswordCallback;

import cn.edu.fudan.cs12.coderrun.Config;
import cn.edu.fudan.cs12.coderrun.activity.MainActivity;
import cn.edu.fudan.cs12.coderrun.entity.User;
import cn.edu.fudan.cs12.coderrun.event.LoginEvent;
import cn.edu.fudan.cs12.coderrun.event.ProfileEvent;
import cn.edu.fudan.cs12.coderrun.event.SignUpEvent;
import cn.edu.fudan.cs12.coderrun.event.SmsEvent;
import cn.edu.fudan.cs12.coderrun.provider.BusProvider;

/**
 * Created by Li on 2015/10/2.
 */
public class UserAction {

	public static User getCurrentUser() {
		return AVUser.getCurrentUser(User.class);
	}

	public static void logout() {
		AVUser.logOut();
	}

	public static void getRegSMSCode(String mobile) {
		System.out.println(mobile);
		AVOSCloud.requestSMSCodeInBackground(mobile, new RequestMobileCodeCallback() {
			public void done(AVException e) {
				if (e == null) {
					BusProvider.getInstance().post(new SmsEvent(Config.SUCCESS));
				} else {
					SmsEvent event = (SmsEvent) ErrorHandler.getErrorEvent(e, SmsEvent.class);
					if (event != null)
						BusProvider.getInstance().post(event);
					else
						BusProvider.getInstance().post(new SmsEvent());
				}
			}
		});
	}

	public static void signUpOrLoginWithSMSCode(String mobile, String smsCode) {
		AVUser.signUpOrLoginByMobilePhoneInBackground(mobile, smsCode, new LogInCallback<AVUser>() {
			public void done(AVUser user, AVException e) {
				if (e == null && user != null) {
					if (user.get("password") != null) {
						user.put("initPassword", user.get("password"));
						user.saveInBackground();
					}
					BusProvider.getInstance().post(new SignUpEvent(Config.SUCCESS));
				} else {
					SignUpEvent event = (SignUpEvent) ErrorHandler.getErrorEvent(e, SignUpEvent.class);
					if (event != null)
						BusProvider.getInstance().post(event);
					else
						BusProvider.getInstance().post(new SignUpEvent());
				}
			}
		});
	}

	public static void logInWithMobileAndPassword(String mobile, String password) {
		AVUser.logInInBackground(mobile, password, new LogInCallback<AVUser>() {
			@Override
			public void done(AVUser user, AVException e) {
				if (e == null && user != null) {
					BusProvider.getInstance().post(new LoginEvent(Config.SUCCESS));
				} else {
					LoginEvent event = (LoginEvent) ErrorHandler.getErrorEvent(e, LoginEvent.class);
					if (event != null)
						BusProvider.getInstance().post(event);
					else
						BusProvider.getInstance().post(new LoginEvent());
				}
			}
		});
	}

	public static void resetInitialPassword(String password) {
		final User user = getCurrentUser();
		if (user.get("initPassword") != null) {
			user.updatePasswordInBackground(user.get("initPassword").toString(), password, new UpdatePasswordCallback() {
				@Override
				public void done(AVException e) {
					if (e == null) {
						user.put("initPassword", null);
						user.saveInBackground();
						BusProvider.getInstance().post(new ProfileEvent(Config.SUCCESS));
					} else {
						ProfileEvent event = (ProfileEvent) ErrorHandler.getErrorEvent(e, ProfileEvent.class);
						if (event != null)
							BusProvider.getInstance().post(event);
						else
							BusProvider.getInstance().post(new ProfileEvent("设置初始密码失败"));
					}
				}
			});
		} else {
			BusProvider.getInstance().post(new ProfileEvent("已经设置过密码"));
		}
	}

	public static boolean hasUserSetPassword() {
		User user = getCurrentUser();
		return user.get("initPassword") == null;
	}
}
