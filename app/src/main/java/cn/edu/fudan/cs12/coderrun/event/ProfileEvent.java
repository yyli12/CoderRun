package cn.edu.fudan.cs12.coderrun.event;

/**
 * Created by Li on 2015/10/10.
 */
public class ProfileEvent extends GeneralEvent {
	public ProfileEvent() {
	}

	public ProfileEvent(int code) {
		super(code);
	}

	public ProfileEvent(String errorMessage) {
		super(errorMessage);
	}
}
