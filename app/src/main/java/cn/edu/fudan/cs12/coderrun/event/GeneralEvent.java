package cn.edu.fudan.cs12.coderrun.event;

import cn.edu.fudan.cs12.coderrun.Config;

/**
 * Created by Li on 2015/10/2.
 */
public abstract class GeneralEvent {

	public int code;
	public String errorMessage;

	GeneralEvent() {
		this.code = Config.DEFAULT_FAIL;
		this.errorMessage = "Unknown Error";
	}

	GeneralEvent(int code) {
		this.code = code;
	}

	GeneralEvent(String errorMessage) {
		this.code = Config.DEFAULT_FAIL;
		this.errorMessage = errorMessage;
	}

}