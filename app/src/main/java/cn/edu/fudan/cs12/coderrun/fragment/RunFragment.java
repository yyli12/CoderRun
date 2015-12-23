package cn.edu.fudan.cs12.coderrun.fragment;

import android.Manifest;
import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Animatable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.SaveCallback;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.DotOptions;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.radar.RadarNearbyResult;
import com.baidu.mapapi.radar.RadarNearbySearchOption;
import com.baidu.mapapi.radar.RadarSearchError;
import com.baidu.mapapi.radar.RadarSearchListener;
import com.baidu.mapapi.radar.RadarSearchManager;
import com.baidu.mapapi.radar.RadarUploadInfo;
import com.baidu.mapapi.radar.RadarUploadInfoCallback;
import com.bumptech.glide.Glide;
import com.lantouzi.wheelview.WheelView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import cn.edu.fudan.cs12.coderrun.Config;
import cn.edu.fudan.cs12.coderrun.R;
import cn.edu.fudan.cs12.coderrun.action.RunAction;
import cn.edu.fudan.cs12.coderrun.action.UserAction;
import cn.edu.fudan.cs12.coderrun.entity.User;
import cn.edu.fudan.cs12.coderrun.event.ProfileEvent;
import cn.edu.fudan.cs12.coderrun.event.RunEvent;
import cn.edu.fudan.cs12.coderrun.provider.BusProvider;
import de.halfbit.tinybus.Subscribe;
import mehdi.sakout.fancybuttons.FancyButton;


public class RunFragment extends Fragment implements RadarSearchListener{
	enum State {STOP, PAUSE, RUNNING};
	User user;
	ImageView mRunImage;
	FancyButton mPlayMusicButton;
	FancyButton mRunButton;
	FancyButton mPauseButton;
	FancyButton mStopButton;
	FancyButton mResumeButton;
	FancyButton mMapButton;
	View mDataDisplay;
	Chronometer ch;  //计时器
	TextView distanceText;//记录距离的文本框
	TextView speedText;//记录速度的文本框
	TextView consumeEnergyText;//记录消耗卡路里的文本框
	LocationManager locationManager;
	PackageManager packageManager;
	Location initialLocation;
	Location lastLocation;
	List<Location> locationList;
	double totalDist;
	double avgSpeed;
	double consumeEnergy;
	long timeFix;
	State state;

	//the map control variables
	MapView mMapView = null;
	BaiduMap baiduMap = null;
	ImageButton loc = null;
	private Toast mToast;								//显示消息
	public LocationClient locationClient = null;		// 定位相关声明
	boolean isFirstLoc = true;							// 是否首次定位
	boolean isRequst=false;								// 是否手动定位
	LatLng point = null;
	List<LatLng> points = new ArrayList<LatLng>();		//运动总轨迹
	List<LatLng> points_tem = new ArrayList<LatLng>();
	BitmapDescriptor ff3 = BitmapDescriptorFactory.fromResource(R.drawable.icon_mark);
	Handler handler = new Handler();					//检查GPS开启
	boolean isStopLocClient = false;					// 是否停止定位服务
	boolean isTrack = false;							// 是否开始绘制轨迹
	boolean isShowMap = false;							//是否显示地图
	RadarSearchManager mManager = RadarSearchManager.getInstance(); //周边雷达
	Vector<Marker> listResult = new Vector<>();				//周边雷达的标记存储
	//位置监听器
	public BDLocationListener myListener = new BDLocationListener() {
		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			if (location == null || mMapView == null)
				return;
			//获取当前位置信息
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
							// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			baiduMap.setMyLocationData(locData);    //设置定位数据
			point = new LatLng(location.getLatitude(), location.getLongitude());
			//如果首次定位或者要求定位，则地图拉到当前位置
			if (isFirstLoc||isRequst) {
				isFirstLoc = false;
				isRequst = false;
				LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(ll, 16);   //设置地图中心点以及缩放
				// MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				baiduMap.animateMapStatus(u);
			}
			//请求周边人信息
			RadarNearbySearchOption option = new RadarNearbySearchOption().centerPt(point).radius(2000);
			mManager.nearbyInfoRequest(option);
			//若显示要画地图
			if(isTrack){
				points.add(point);
				if (points.size() == 5) {

					// 这里绘制起点
					drawStart(points);
				} else if (points.size() > 7) {
					points_tem = points.subList(points.size() - 4, points.size());
					OverlayOptions options = new PolylineOptions().color(0xAAFF0000).width(15)
							.points(points_tem);
					baiduMap.addOverlay(options);
				}
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		user = UserAction.getCurrentUser();
		state = State.STOP;
		timeFix = 0;
		locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
		packageManager = getActivity().getPackageManager();
	}

	@Override
	public void onResume() {
		super.onResume();
		mMapView.onResume();
		BusProvider.getInstance().register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		mMapView.onPause();
		BusProvider.getInstance().unregister(this);
	}
	@Override
	public void onDestroy(){
		//移除监听
		mManager.removeNearbyInfoListener(this);
		//清除用户信息
		mManager.clearUserInfo();
		//释放资源
		mManager.destroy();
		ff3.recycle();
		mManager = null;
		//退出时销毁定位
		locationClient.stop();
		isStopLocClient = true;
		baiduMap.setMyLocationEnabled(false);
		mMapView.onDestroy();
		mMapView = null;
		super.onDestroy();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		user = UserAction.getCurrentUser();
		View v = inflater.inflate(R.layout.fragment_run, container, false);

		mPlayMusicButton = (FancyButton) v.findViewById(R.id.button_playMusic);
		mRunButton = (FancyButton) v.findViewById(R.id.button_run);
		mPauseButton = (FancyButton) v.findViewById(R.id.button_pause);
		mStopButton = (FancyButton) v.findViewById(R.id.button_stop);
		mResumeButton = (FancyButton) v.findViewById(R.id.button_resume);
		mMapButton = (FancyButton) v.findViewById(R.id.button_map);
		mRunImage = (ImageView) v.findViewById(R.id.image_run);
		mDataDisplay = v.findViewById(R.id.set_data);
		displayCorrespondingView();


		//jiao adds on 2015/11/29
		ch = (Chronometer) v.findViewById(R.id.chronometer1);
		ch.setFormat("时长：%s");

		distanceText=(TextView)v.findViewById(R.id.distance_value);
		speedText=(TextView)v.findViewById(R.id.speed_value);
		consumeEnergyText=(TextView)v.findViewById(R.id.consumeEnergy_units);
		TextPaint tp1 = consumeEnergyText.getPaint();
		tp1.setFakeBoldText(true);
		TextView distance = (TextView)v.findViewById(R.id.distance_units);
		TextPaint tp2 = distance.getPaint();
		tp2.setFakeBoldText(true);
		TextView speed = (TextView)v.findViewById(R.id.speed_units);
		TextPaint tp3 = speed.getPaint();
		tp3.setFakeBoldText(true);
		mPlayMusicButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				//File fileMusic=new File(fileNamePath);
				String fileNamePath = new String("storage/emulated/0");
				try {
					Uri uri = Uri.parse("file://" + fileNamePath);
					i.setDataAndType(uri, "audio/*");
					startActivity(i);

				} catch (Exception e) {
					System.out.println("There is an error when playing music!!");
				}


			}
		});

		mRunButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ch.setBase(SystemClock.elapsedRealtime());
				ch.setFormat("时长：%s");
				TextPaint tp = ch.getPaint();
				tp.setFakeBoldText(true);
				ch.start();
				timeFix = 0;
				state = State.RUNNING;
				displayCorrespondingView();

				Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_right_out);
				anim.setAnimationListener(new Animation.AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						mRunImage.setVisibility(View.INVISIBLE);
						Toast.makeText(getActivity(), "run", Toast.LENGTH_SHORT).show();
						startRun();
					}
				});
				mRunImage.startAnimation(anim);
				//后台绘制地图

				points.clear();
				points_tem.clear();
				isStopLocClient = false;
				isTrack = true;
				if (!locationClient.isStarted()) {
					locationClient.start();
				}

				//开启自动后台上传地理位置
				mManager.startUploadAuto(new RadarUploadInfoCallback() {
					@Override
					public RadarUploadInfo onUploadInfoCallback() {
						// TODO Auto-generated method stub
						RadarUploadInfo info = new RadarUploadInfo();
						info.pt = point;
						//Log.e("hjtest", "OnUploadInfoCallback");
						return info;
					}
				}, 5000);
			}
		});

		mMapButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				isShowMap = !isShowMap;
				if(isShowMap){
					mMapButton.setText(getString(R.string.button_map).toString());
					mMapView.setVisibility(View.VISIBLE);
					loc.setVisibility(View.VISIBLE);
				}else {
					mMapButton.setText(getString(R.string.button_mainScreen).toString());
					mMapView.setVisibility(View.INVISIBLE);
					loc.setVisibility(View.INVISIBLE);
				}
			}
		});
		mResumeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ch.setBase(SystemClock.elapsedRealtime() - timeFix);
				ch.start();
				state = State.RUNNING;
				displayCorrespondingView();
				Toast.makeText(getActivity(), "resume", Toast.LENGTH_SHORT).show();
				//后台绘制地图
				isStopLocClient = false;
				isTrack=true;
				if (!locationClient.isStarted()) {
					locationClient.start();
				}
			}
		});
		mPauseButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				timeFix = SystemClock.elapsedRealtime() - ch.getBase();
				ch.stop();
				state = State.PAUSE;
				displayCorrespondingView();
				Toast.makeText(getActivity(), "pause", Toast.LENGTH_SHORT).show();
				//后台绘制地图
				isTrack = false;
			}
		});
		mStopButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ch.stop();
				state = State.STOP;
				displayCorrespondingView();
				if (totalDist > 0 && avgSpeed > 0) {
					stopRun();
				} else {
					Toast.makeText(getActivity(), "别偷懒", Toast.LENGTH_SHORT).show();
				}
				Toast.makeText(getActivity(), "stop", Toast.LENGTH_SHORT).show();
				//后台绘制地图
				if (locationClient.isStarted()) {
					// 绘制终点
					isTrack = false;
					drawEnd(points);
					//locationClient.stop();
				}
				mManager.stopUploadAuto();	//停止自动位置上传
				mManager.clearUserInfo();
			}
		});

		// Baidu Map control
		mMapView = (MapView) v.findViewById(R.id.bmapView);
		baiduMap = mMapView.getMap();
		loc = (ImageButton) v.findViewById(R.id.request);//手动定位
		loc.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				requestLocation();
			}
		});
		this.setLocationOption();   //设置定位参数
		locationClient.start(); // 开始定位
		handler.postDelayed(new MyRunable(), 1000);
		mManager.addNearbyInfoListener(this);   //设置雷达监听器

		return v;
	}

	private void startRun() {
		if (packageManager.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, getActivity().getPackageName()) == PackageManager.PERMISSION_GRANTED) {
			StringBuilder recordLocation = new StringBuilder();
			lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			totalDist = 0.0;
			locationList = new ArrayList<>();
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, new LocationListener() {
				@Override
				public void onLocationChanged(Location location) {
					if (state == State.RUNNING) {
						Location presentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
						float distance[] = new float[1];
						Location.distanceBetween(presentLocation.getLatitude(), presentLocation.getLongitude(), lastLocation.getLatitude(), lastLocation.getLongitude(), distance);
						totalDist += distance[0];
						double totalTime = (SystemClock.elapsedRealtime() - ch.getBase()) / 1000; //使单位为秒
						avgSpeed = (totalTime / 60) / (totalDist / 1000);
						speedText.setText(String.format("%.2f min/km", avgSpeed));
						distanceText.setText(String.format("%.2f km", totalDist / 1000));
						consumeEnergy=55*(totalTime/3600)*(30/(avgSpeed*2.5));
						consumeEnergyText.setText(String.format("卡路里：%.2f kcal", consumeEnergy));
						locationList.add(lastLocation);
						lastLocation = presentLocation;
					}
				}

				@Override
				public void onStatusChanged(String provider, int status, Bundle extras) {

				}

				@Override
				public void onProviderEnabled(String provider) {

				}

				@Override
				public void onProviderDisabled(String provider) {

				}
			});
		} else {
			Toast.makeText(getActivity(), "没有获取地理位置的权限", Toast.LENGTH_SHORT).show();
		}

	}

	private void stopRun() {
		final View content = getActivity().getLayoutInflater().inflate(R.layout.component_stop_run, null);
		((TextView) content.findViewById(R.id.final_speed)).setText("速度：" + avgSpeed + " min/km");
		((TextView) content.findViewById(R.id.final_distance)).setText("距离：" + totalDist + " km");
		final WheelView score = (WheelView) content.findViewById(R.id.score_picker);
		score.setItems(Arrays.asList("1", "2", "3", "4", "5"));

		MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
				.title(R.string.dialog_title_record_run)
				.contentColorRes(R.color.gray_dark)
				.customView(content, false)
				.positiveText(R.string.dialog_button_submit_run)
				.positiveColorRes(R.color.app_green)
				.onPositive(new MaterialDialog.SingleButtonCallback() {
					public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
						int s = score.getSelectedPosition() + 1;
						uploadRun(System.currentTimeMillis() / 1000, totalDist, s, (long) (avgSpeed * totalDist), null);
					}
				})
				.negativeText(R.string.dialog_button_cancel_run)
				.negativeColorRes(R.color.gray_light)
				.show();
	}

	public void uploadRun(long fin_time, double dist, int score, long run_time, String loc) {
		if (loc == null) loc = "-";
		AVObject history = new AVObject("history_run");
		history.put("finish_time", fin_time);
		history.put("running_distance", dist);
		history.put("score", score);
		history.put("running_time", run_time);
		history.put("location", loc);
		history.put("user", User.getCurrentUser());
		history.saveInBackground(new SaveCallback() {
			@Override
			public void done(AVException e) {
				if (e == null) {
					Toast.makeText(getActivity(), "记录成功", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getActivity(), "记录失败", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	//Set up Map options
	private void setLocationOption() {
		//开启定位图层
		baiduMap.setMyLocationEnabled(true);
		locationClient = new LocationClient(this.getActivity()); // 实例化LocationClient类
		locationClient.registerLocationListener(myListener); // 注册监听函数
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true); // 打开GPS
		option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式
		option.setCoorType("bd09ll"); // 返回的定位结果是百度经纬度,默认值gcj02
		option.setScanSpan(1000); // 设置发起定位请求的间隔时间为2000ms
		option.setIsNeedAddress(true); // 返回的定位结果包含地址信息
		option.setNeedDeviceDirect(true); // 返回的定位结果包含手机机头的方向
		option.setPriority(LocationClientOption.GpsFirst);
		locationClient.setLocOption(option);
	}
	//manually request location
	public void requestLocation() {

		if(locationClient != null && locationClient.isStarted()){
			showToast("正在定位......");
			isRequst = true;
			locationClient.requestLocation();
		}else{
			Log.d("log", "locClient is null or not started");
		}
	}
	//show message
	private void showToast(String msg){
		if(mToast == null){
			mToast = Toast.makeText(this.getActivity(), msg, Toast.LENGTH_SHORT);
		}else{
			mToast.setText(msg);
			mToast.setDuration(Toast.LENGTH_SHORT);
		}
		mToast.show();
	}

	class MyRunable implements Runnable {

		public void run() {
			if (!locationClient.isStarted()) {
				locationClient.start();
			}
			if (!isStopLocClient) {
				handler.postDelayed(this, 1000);
			}

		}

	}
	//search result process
	@Override
	public void onGetNearbyInfoList(RadarNearbyResult result, RadarSearchError error) {
		if (error == RadarSearchError.RADAR_NO_ERROR) {
			// 获取成功
			for(int i=0;i<listResult.size();i++){
				listResult.get(i).remove();
			}
			listResult.clear();
			if (result != null && result.infoList != null && result.infoList.size() > 0) {
				for (int i = 0; i < result.infoList.size() && i< 10; i++) {
					MarkerOptions option = new MarkerOptions().icon(ff3).position(result.infoList.get(i).pt);
					listResult.add((Marker)(baiduMap.addOverlay(option)));//保存或者添加
				}
			}

		} else {
			// 获取失败
			showToast("查询周边失败");
		}

	}
	@Override
	public void onGetClearInfoState(RadarSearchError error) {
		// TODO Auto-generated method stub
		if (error == RadarSearchError.RADAR_NO_ERROR) {
			showToast("清除成功");
		} else {
			showToast("清除失败");
		}
	}
	@Override
	public void onGetUploadState(RadarSearchError error) {
		// TODO Auto-generated method stub

	}
	/**
	 * 绘制起点，取前n个点坐标的平均值绘制起点
	 */
	public void drawStart(List<LatLng> points2) {
		double myLat = 0.0;
		double myLng = 0.0;

		for (LatLng ll : points2) {
			myLat += ll.latitude;
			myLng += ll.longitude;
		}
		LatLng avePoint = new LatLng(myLat / points2.size(), myLng
				/ points2.size());
		points.add(avePoint);
		OverlayOptions options = new DotOptions().center(avePoint).color(0xAA00ff00)
				.radius(15);
		baiduMap.addOverlay(options);
	}

	/**
	 * 绘制终点。
	 */
	protected void drawEnd(List<LatLng> points2) {
		double myLat = 0.0;
		double myLng = 0.0;
		if (points2.size() > 5) {// points肯定大于5，其实不用判断
			for (int i = points2.size() - 5; i < points2.size(); i++) {
				LatLng ll = points2.get(i);
				myLat += ll.latitude;
				myLng += ll.longitude;

			}
			LatLng avePoint = new LatLng(myLat / 5, myLng / 5);
			OverlayOptions options = new DotOptions().center(avePoint).color(0xAAff00ff)
					.radius(15);
			baiduMap.addOverlay(options);
		}

	}

	private void displayCorrespondingView() {
		switch (state) {
			case STOP:
				mRunButton.setVisibility(View.VISIBLE);
				mResumeButton.setVisibility(View.INVISIBLE);
				mPauseButton.setVisibility(View.INVISIBLE);
				mStopButton.setVisibility(View.INVISIBLE);
				mDataDisplay.setVisibility(View.INVISIBLE);
				mRunImage.setVisibility(View.VISIBLE);
				break;
			case RUNNING:
				mRunButton.setVisibility(View.INVISIBLE);
				mResumeButton.setVisibility(View.INVISIBLE);
				mPauseButton.setVisibility(View.VISIBLE);
				mStopButton.setVisibility(View.VISIBLE);
				mDataDisplay.setVisibility(View.VISIBLE);
				mRunImage.setVisibility(View.INVISIBLE);
				break;
			case PAUSE:
				mRunButton.setVisibility(View.INVISIBLE);
				mResumeButton.setVisibility(View.VISIBLE);
				mPauseButton.setVisibility(View.INVISIBLE);
				mStopButton.setVisibility(View.VISIBLE);
				mDataDisplay.setVisibility(View.VISIBLE);
				mRunImage.setVisibility(View.INVISIBLE);
				break;
		}
	}

}