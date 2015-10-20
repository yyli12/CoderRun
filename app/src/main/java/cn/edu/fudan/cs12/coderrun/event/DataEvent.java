package cn.edu.fudan.cs12.coderrun.event;

/**
 * Created by Li on 2015/10/13.
 */
public class DataEvent extends GeneralEvent {
	public enum type {history_item};
	public type dataType;

	public DataEvent() {
	}

	public DataEvent(int code, type T) {
		super(code);
		dataType = T;
	}

	public DataEvent(String errorMessage, type T) {
		super(errorMessage);
		dataType = T;
	}

	public boolean isTypeEvent(type T) {
		return dataType == T;
	}
}