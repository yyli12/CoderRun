package cn.edu.fudan.cs12.coderrun.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.edu.fudan.cs12.coderrun.Config;
import cn.edu.fudan.cs12.coderrun.R;
import cn.edu.fudan.cs12.coderrun.action.UserAction;
import cn.edu.fudan.cs12.coderrun.entity.User;
import cn.edu.fudan.cs12.coderrun.pedometer.PedometerSettings;
import cn.edu.fudan.cs12.coderrun.pedometer.StepService;
import cn.edu.fudan.cs12.coderrun.pedometer.Utils;
import cn.edu.fudan.cs12.coderrun.provider.BusProvider;
import mehdi.sakout.fancybuttons.FancyButton;


public class RunFragment extends Fragment {
	View v;
	User user;
	ImageView mRunImage;
	FancyButton mRunButton;
	FancyButton mPauseButton;
	FancyButton mStopButton;
	RelativeLayout mRunButtonSet;
	RelativeLayout mPauseButtonSet;
	RelativeLayout mDataSet;
	private static final String TAG = "RunFragment";
	private SharedPreferences mSettings;
	private PedometerSettings mPedometerSettings;
	private Utils mUtils;

	private TextView mStepValueView;
	private TextView mPaceValueView;
	private TextView mDistanceValueView;
	private TextView mSpeedValueView;
	private TextView mCaloriesValueView;
	TextView mDesiredPaceView;
	private int mStepValue;
	private int mPaceValue;
	private float mDistanceValue;
	private float mSpeedValue;
	private int mCaloriesValue;
	private float mDesiredPaceOrSpeed;
	private int mMaintain;
	private boolean mIsMetric;
	private float mMaintainInc;
	private boolean mQuitting = false; // Set when user selected Quit from menu, can be used by onPause, onStop, onDestroy


	/**
	 * True, when service is running.
	 */
	private boolean mIsRunning;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		user = UserAction.getCurrentUser();

		mStepValue = 0;
		mPaceValue = 0;

		mUtils = Utils.getInstance();
	}

	@Override
	public void onResume() {
		super.onResume();
		BusProvider.getInstance().register(this);

		mSettings = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mPedometerSettings = new PedometerSettings(mSettings);

		mUtils.setSpeak(mSettings.getBoolean("speak", false));

		// Read from preferences if the service was running on the last onPause
		mIsRunning = mPedometerSettings.isServiceRunning();

		// Start the service if this is considered to be an application start (last onPause was long ago)
		if (!mIsRunning && mPedometerSettings.isNewStart()) {
			startStepService();
			bindStepService();
		}
		else if (mIsRunning) {
			bindStepService();
		}

		mPedometerSettings.clearServiceRunning();



		mIsMetric = mPedometerSettings.isMetric();
		((TextView) v.findViewById(R.id.distance_units)).setText(getString(
				mIsMetric
						? R.string.kilometers
						: R.string.miles
		));
		((TextView) v.findViewById(R.id.speed_units)).setText(getString(
				mIsMetric
						? R.string.kilometers_per_hour
						: R.string.miles_per_hour
		));

		mRunButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showPauseAndStop();
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
						hidePic();
						showDataSet();
						showPauseAndStop();
						startStepService();
						bindStepService();
					}
				});
				mRunImage.startAnimation(anim);
			}
		});
		mPauseButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showStartButton();
				unbindStepService();
				stopStepService();
			}
		});
		mStopButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showPic();
				hideDataSet();
				showStartButton();
				resetValues(true);
				unbindStepService();
				stopStepService();
			}
		});

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
		v = inflater.inflate(R.layout.fragment_run, container, false);
		mRunButtonSet = (RelativeLayout) v.findViewById(R.id.button_set_run);
		mPauseButtonSet = (RelativeLayout) v.findViewById(R.id.button_set_pause);
		mDataSet = (RelativeLayout) v.findViewById(R.id.set_data);

		mStepValueView     = (TextView) v.findViewById(R.id.step_value);
		mPaceValueView     = (TextView) v.findViewById(R.id.pace_value);
		mDistanceValueView = (TextView) v.findViewById(R.id.distance_value);
		mSpeedValueView    = (TextView) v.findViewById(R.id.speed_value);
		mCaloriesValueView = (TextView) v.findViewById(R.id.calories_value);
		mRunButton = (FancyButton) v.findViewById(R.id.button_run);
		mPauseButton = (FancyButton) v.findViewById(R.id.button_pause);
		mStopButton = (FancyButton) v.findViewById(R.id.button_stop);
		mRunImage = (ImageView) v.findViewById(R.id.image_run);
		showStartButton();
		hideDataSet();

		return v;
	}

	private void showPic() {
		mRunImage.setVisibility(View.VISIBLE);
	}
	private void hidePic() {
		mRunImage.setVisibility(View.GONE);
	}
	private void showDataSet() {
		mDataSet.setVisibility(View.VISIBLE);
	}
	private void hideDataSet() {
		mDataSet.setVisibility(View.GONE);
	}
	private void showStartButton() {
		mRunButtonSet.setVisibility(View.VISIBLE);
		mPauseButtonSet.setVisibility(View.GONE);
	}

	private void showPauseAndStop() {
		mPauseButtonSet.setVisibility(View.VISIBLE);
		mRunButtonSet.setVisibility(View.GONE);
	}


	private void setDesiredPaceOrSpeed(float desiredPaceOrSpeed) {
		if (mService != null) {
			if (mMaintain == PedometerSettings.M_PACE) {
				mService.setDesiredPace((int)desiredPaceOrSpeed);
			}
			else
			if (mMaintain == PedometerSettings.M_SPEED) {
				mService.setDesiredSpeed(desiredPaceOrSpeed);
			}
		}
	}

	private void savePaceSetting() {
		mPedometerSettings.savePaceOrSpeedSetting(mMaintain, mDesiredPaceOrSpeed);
	}

	private StepService mService;

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = ((StepService.StepBinder)service).getService();

			mService.registerCallback(mCallback);
			mService.reloadSettings();

		}

		public void onServiceDisconnected(ComponentName className) {
			mService = null;
		}
	};


	private void startStepService() {
		if (! mIsRunning) {
			Log.i(TAG, "[SERVICE] Start");
			mIsRunning = true;
			getActivity().startService(new Intent(getActivity(),
					StepService.class));
		}
	}


	private void bindStepService() {
		Log.i(TAG, "[SERVICE] Bind");
		getActivity().bindService(new Intent(getActivity(),
				StepService.class), mConnection, Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
	}

	private void unbindStepService() {
		Log.i(TAG, "[SERVICE] Unbind");
		getActivity().unbindService(mConnection);
	}

	private void stopStepService() {
		Log.i(TAG, "[SERVICE] Stop");
		if (mService != null) {
			Log.i(TAG, "[SERVICE] stopService");
			getActivity().stopService(new Intent(getActivity(),
					StepService.class));
		}
		mIsRunning = false;
	}

	private void resetValues(boolean updateDisplay) {
		if (mService != null && mIsRunning) {
			mService.resetValues();
		}
		else {
			mStepValueView.setText("0");
			mPaceValueView.setText("0");
			mDistanceValueView.setText("0");
			mSpeedValueView.setText("0");
			mCaloriesValueView.setText("0");
			SharedPreferences state = getActivity().getSharedPreferences("state", 0);
			SharedPreferences.Editor stateEditor = state.edit();
			if (updateDisplay) {
				stateEditor.putInt("steps", 0);
				stateEditor.putInt("pace", 0);
				stateEditor.putFloat("distance", 0);
				stateEditor.putFloat("speed", 0);
				stateEditor.putFloat("calories", 0);
				stateEditor.commit();
			}
		}
	}

	private static final int MENU_SETTINGS = 8;
	private static final int MENU_QUIT     = 9;

	private static final int MENU_PAUSE = 1;
	private static final int MENU_RESUME = 2;
	private static final int MENU_RESET = 3;


	// TODO: unite all into 1 type of message
	private StepService.ICallback mCallback = new StepService.ICallback() {
		public void stepsChanged(int value) {
			mHandler.sendMessage(mHandler.obtainMessage(STEPS_MSG, value, 0));
		}
		public void paceChanged(int value) {
			mHandler.sendMessage(mHandler.obtainMessage(PACE_MSG, value, 0));
		}
		public void distanceChanged(float value) {
			mHandler.sendMessage(mHandler.obtainMessage(DISTANCE_MSG, (int)(value*1000), 0));
		}
		public void speedChanged(float value) {
			mHandler.sendMessage(mHandler.obtainMessage(SPEED_MSG, (int)(value*1000), 0));
		}
		public void caloriesChanged(float value) {
			mHandler.sendMessage(mHandler.obtainMessage(CALORIES_MSG, (int)(value), 0));
		}
	};

	private static final int STEPS_MSG = 1;
	private static final int PACE_MSG = 2;
	private static final int DISTANCE_MSG = 3;
	private static final int SPEED_MSG = 4;
	private static final int CALORIES_MSG = 5;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case STEPS_MSG:
					mStepValue = (int)msg.arg1;
					mStepValueView.setText("" + mStepValue);
					break;
				case PACE_MSG:
					mPaceValue = msg.arg1;
					if (mPaceValue <= 0) {
						mPaceValueView.setText("0");
					}
					else {
						mPaceValueView.setText("" + (int)mPaceValue);
					}
					break;
				case DISTANCE_MSG:
					mDistanceValue = ((int)msg.arg1)/1000f;
					if (mDistanceValue <= 0) {
						mDistanceValueView.setText("0");
					}
					else {
						mDistanceValueView.setText(
								("" + (mDistanceValue + 0.000001f)).substring(0, 5)
						);
					}
					break;
				case SPEED_MSG:
					mSpeedValue = ((int)msg.arg1)/1000f;
					if (mSpeedValue <= 0) {
						mSpeedValueView.setText("0");
					}
					else {
						mSpeedValueView.setText(
								("" + (mSpeedValue + 0.000001f)).substring(0, 4)
						);
					}
					break;
				case CALORIES_MSG:
					mCaloriesValue = msg.arg1;
					if (mCaloriesValue <= 0) {
						mCaloriesValueView.setText("0");
					}
					else {
						mCaloriesValueView.setText("" + (int)mCaloriesValue);
					}
					break;
				default:
					super.handleMessage(msg);
			}
		}

	};


}
