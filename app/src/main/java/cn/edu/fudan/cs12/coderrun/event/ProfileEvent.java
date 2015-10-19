package cn.edu.fudan.cs12.coderrun.event;

/**
 * Created by Li on 2015/10/10.
 */
public class ProfileEvent extends GeneralEvent {
	public enum type {reset_init_password};
	public type profileType;
	public ProfileEvent() {
	}

	public ProfileEvent(int code, type T) {
		super(code);
		profileType = T;
	}

	public ProfileEvent(String errorMessage, type T) {
		super(errorMessage);
		profileType = T;
	}

	public boolean isTypeEvent(type T) {
		return profileType == T;
	}
}
