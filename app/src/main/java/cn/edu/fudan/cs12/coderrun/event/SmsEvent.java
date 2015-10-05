package cn.edu.fudan.cs12.coderrun.event;

/**
 * Created by Li on 2015/10/5.
 */
public class SmsEvent extends GeneralEvent {
	public SmsEvent() {
	}

	public SmsEvent(int code) {
		super(code);
	}

	public SmsEvent(String errorMessage) {
		super(errorMessage);
	}
}