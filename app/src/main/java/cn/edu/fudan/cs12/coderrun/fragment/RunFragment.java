package cn.edu.fudan.cs12.coderrun.fragment;

import android.Manifest;
import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Animatable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.SaveCallback;
import com.bumptech.glide.Glide;
import com.lantouzi.wheelview.WheelView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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


public class RunFragment extends Fragment {
	enum State {STOP, PAUSE, RUNNING};
	User user;
	ImageView mRunImage;
	FancyButton mRunButton;
	FancyButton mPauseButton;
	FancyButton mStopButton;
	FancyButton mResumeButton;
	View mDataDisplay;
	Chronometer ch;  //计时器
	TextView distanceText;//记录距离的文本框
	TextView speedText;//记录速度的文本框
	LocationManager locationManager;
	PackageManager packageManager;
	Location initialLocation;
	Location lastLocation;
	List<Location> locationList;
	double totalDist;
	double avgSpeed;
	long timeFix;
	State state;


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
		BusProvider.getInstance().register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		BusProvider.getInstance().unregister(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		user = UserAction.getCurrentUser();
		View v = inflater.inflate(R.layout.fragment_run, container, false);

		mRunButton = (FancyButton) v.findViewById(R.id.button_run);
		mPauseButton = (FancyButton) v.findViewById(R.id.button_pause);
		mStopButton = (FancyButton) v.findViewById(R.id.button_stop);
		mResumeButton = (FancyButton) v.findViewById(R.id.button_resume);
		mRunImage = (ImageView) v.findViewById(R.id.image_run);
		mDataDisplay = v.findViewById(R.id.set_data);
		displayCorrespondingView();


		//jiao adds on 2015/11/29
		ch = (Chronometer) v.findViewById(R.id.chronometer1);
		ch.setFormat("时长：%s");
		distanceText=(TextView)v.findViewById(R.id.distance_value);
		speedText=(TextView)v.findViewById(R.id.speed_value);

		mRunButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ch.setBase(SystemClock.elapsedRealtime());
				ch.setFormat("时长：%s");
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
			}
		});
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