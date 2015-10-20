package cn.edu.fudan.cs12.coderrun;

import android.app.Application;

import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;

public class App extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		AVOSCloud.initialize(this, Config.APPID, Config.APPKEY);
	}
}
