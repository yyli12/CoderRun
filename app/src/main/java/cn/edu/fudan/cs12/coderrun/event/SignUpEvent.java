package cn.edu.fudan.cs12.coderrun.event;

import com.avos.avoscloud.AVException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Li on 2015/10/2.
 */
public class SignUpEvent extends GeneralEvent {

	public final static int NICKNAME_TAKEN = 1000;
	public final static int EXISTED_MOBILE = 1001;

	// Register Error Code
	public final static int ACCOUNT_ALREADY_LINKED = AVException.ACCOUNT_ALREADY_LINKED;
	public final static int EMAIL_TAKEN = AVException.EMAIL_TAKEN;
	public final static int USER_MOBILE_PHONENUMBER_TAKEN = AVException.USER_MOBILE_PHONENUMBER_TAKEN;
	public final static int USERNAME_TAKEN = AVException.USERNAME_TAKEN;


	public final static Set<Integer> errorSet = new HashSet<>();

	static {
		errorSet.addAll(Arrays.asList(ACCOUNT_ALREADY_LINKED, EMAIL_TAKEN, USER_MOBILE_PHONENUMBER_TAKEN, USERNAME_TAKEN));
	}

	public SignUpEvent() {
	}

	public SignUpEvent(int code) {
		super(code);
	}

	public SignUpEvent(String errorMessage) {
		super(errorMessage);
	}
}