package cn.edu.fudan.cs12.coderrun.entity;

import android.text.TextUtils;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;

import java.io.IOException;
import java.util.List;

import cn.edu.fudan.cs12.coderrun.Config;
import cn.edu.fudan.cs12.coderrun.event.DataEvent;
import cn.edu.fudan.cs12.coderrun.event.SignUpEvent;
import cn.edu.fudan.cs12.coderrun.provider.BusProvider;


/**
 * Created by Li on 2015/10/2.
 */
public class User extends AVUser {

	public final static String ICON_STRING = "icon";
	public final static String GENDER_STRING = "gender";
	public final static String NICKNAME_STRING = "nickname";
	public final static String INTRO_STRING = "introduction";
	public final static String AREA_STRING = "area";
	public final static String SLOGAN_STRING = "slogan";

	public User() {
	}

	public User(String password, String phoneNum) {
		setPassword(password);
		setMobilePhoneNumber(phoneNum);
	}

	public String getIcon() {
		AVFile img = getAVFile(ICON_STRING);
		if (img != null)
			return img.getUrl();
		return null;
	}

	public void setIcon(String iconPath) {
		if (TextUtils.isEmpty(iconPath))
			return;
		try {
			AVFile avFile = AVFile.withAbsoluteLocalPath("head.png", iconPath);
			put(ICON_STRING, avFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getGender() {
		return getInt(GENDER_STRING);
	}

	public void setGender(int gender) {
		put(GENDER_STRING, gender);
	}

	public String getGenderString() {
		return Config.GENDER_STRINGS[getGender()];
	}

	public String getNickName() {
		return getString(NICKNAME_STRING);
	}

	public String getArea() { return getString(AREA_STRING); }

	public String getSlogan() { return getString(SLOGAN_STRING); }

	public void setNickName(String nickName) {
		put(NICKNAME_STRING, nickName);
	}

	public String getIntro() {
		return getString(INTRO_STRING);
	}

	public void setIntro(String intro) {
		put(INTRO_STRING, intro);
	}

	public boolean isPasswordSet() { return getString("initPassword") == null || getString("initPassword").equals("-1"); }

	public String getInitPassword() {
		return getString("initPassword");
	}
}
