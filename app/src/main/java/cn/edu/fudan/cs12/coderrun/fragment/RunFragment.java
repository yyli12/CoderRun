package cn.edu.fudan.cs12.coderrun.fragment;

import android.animation.Animator;
import android.app.Activity;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
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
import com.baidu.mapapi.map.MapView;
import com.bumptech.glide.Glide;

import cn.edu.fudan.cs12.coderrun.Config;
import cn.edu.fudan.cs12.coderrun.R;
import cn.edu.fudan.cs12.coderrun.action.RunAction;
import cn.edu.fudan.cs12.coderrun.action.UserAction;
import cn.edu.fudan.cs12.coderrun.entity.User;
import cn.edu.fudan.cs12.coderrun.event.ProfileEvent;
import cn.edu.fudan.cs12.coderrun.provider.BusProvider;
import de.halfbit.tinybus.Subscribe;
import mehdi.sakout.fancybuttons.FancyButton;


public class RunFragment extends Fragment {
	User user;
	ImageView mRunImage;
	FancyButton mRunButton;
	FancyButton mPauseButton;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		user = UserAction.getCurrentUser();
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
		mRunImage = (ImageView) v.findViewById(R.id.image_run);
		mRunButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_right_out);
				anim.setAnimationListener(new Animation.AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {}
					@Override
					public void onAnimationRepeat(Animation animation) {}
					@Override
					public void onAnimationEnd(Animation animation) {
						mRunImage.setVisibility(View.GONE);
						Toast.makeText(getActivity(), "run", Toast.LENGTH_SHORT).show();
					}
				});
				mRunImage.startAnimation(anim);
			}
		});
		mPauseButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(getActivity(), "pause", Toast.LENGTH_SHORT).show();
			}
		});
		return v;
	}

}
