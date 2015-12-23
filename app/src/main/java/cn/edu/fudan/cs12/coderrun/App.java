package cn.edu.fudan.cs12.coderrun;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDexApplication;
import android.support.multidex.MultiDex;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;

public class App extends MultiDexApplication {

	@Override
	public void onCreate() {
		super.onCreate();
		AVOSCloud.initialize(this, Config.APPID, Config.APPKEY);
	}
	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}
}
