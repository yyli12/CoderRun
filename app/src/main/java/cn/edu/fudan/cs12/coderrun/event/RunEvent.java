package cn.edu.fudan.cs12.coderrun.event;

/**
 * Created by Li on 2015/10/19.
 */
public class RunEvent extends GeneralEvent {
	public enum type {running};
	public type eventType;
	public int dist = 0;
	public int time = 0;
	public RunEvent() {
	}

	public RunEvent(int code, type T, int d, int t) {
		super(code);
		eventType = T;
		dist = d;
		time = t;
	}

	public RunEvent(String errorMessage, type T, int d, int t) {
		super(errorMessage);
		eventType = T;
		dist = d;
		time = t;
	}

	public boolean isTypeEvent(type T) {
		return eventType == T;
	}
}
