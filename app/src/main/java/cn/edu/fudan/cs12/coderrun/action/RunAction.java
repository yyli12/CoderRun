package cn.edu.fudan.cs12.coderrun.action;

import android.util.TimeUtils;

import java.util.Calendar;
import java.util.Date;

import cn.edu.fudan.cs12.coderrun.Config;
import cn.edu.fudan.cs12.coderrun.event.RunEvent;
import cn.edu.fudan.cs12.coderrun.provider.BusProvider;

/**
 * Created by Li on 2015/10/19.
 */
public class RunAction {

	private static RunThread runThread = null;

	private static RunThread getRunThread() {
		if (runThread == null) {
			runThread = new RunThread();
		}
		return runThread;
	}

	public static void startRun() {
		getRunThread().run();
	}

	public static void pauseRun() {
		System.out.println("pause");
		getRunThread().pause();
	}

}

class RunThread extends Thread {
	private long begin;
	private long now;
	private int dist;
	private int time;

	public void run() {
		begin = (new Date()).getTime();
		while (!Thread.currentThread().isInterrupted()) {
			now = (new Date()).getTime();
			BusProvider.getInstance().post(new RunEvent(Config.SUCCESS, RunEvent.type.running, dist, time));
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void pause() {
		try{
			this.wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public long getBegin() {
		return begin;
	}

	public long getNow() {
		return now;
	}
}
