package cn.edu.fudan.cs12.coderrun.event;

/**
 * Created by Li on 2015/10/4.
 */
public class LoginEvent extends GeneralEvent {
	public LoginEvent() {
	}

	public LoginEvent(int code) {
		super(code);
	}

	public LoginEvent(String errorMessage) {
		super(errorMessage);
	}
}
