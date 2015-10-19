package cn.edu.fudan.cs12.coderrun.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.edu.fudan.cs12.coderrun.Config;
import cn.edu.fudan.cs12.coderrun.R;
import cn.edu.fudan.cs12.coderrun.action.UserAction;
import cn.edu.fudan.cs12.coderrun.entity.User;
import cn.edu.fudan.cs12.coderrun.event.DataEvent;
import cn.edu.fudan.cs12.coderrun.event.ProfileEvent;
import cn.edu.fudan.cs12.coderrun.provider.BusProvider;
import de.halfbit.tinybus.Subscribe;
import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;

public class UserFragment extends Fragment {

	User user;
	ImageView mAvatarImageView;
	TextView mMobileTextView;
	TextView mInitPasswordView;
	Button mResetInitPasswordButton;



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
		View v = inflater.inflate(R.layout.fragment_user, container, false);

		mAvatarImageView = (ImageView) v.findViewById(R.id.user_avatar);
		mMobileTextView = (TextView) v.findViewById(R.id.user_mobile);
		mInitPasswordView = (TextView) v.findViewById(R.id.user_init_password);
		mResetInitPasswordButton = (Button) v.findViewById(R.id.button_reset_init_password);

		Glide.with(this)
				.load(user.getString("avatar"))
				.centerCrop()
				.crossFade()
				.into(mAvatarImageView);

		mMobileTextView.setText(UserAction.getCurrentUser().getMobilePhoneNumber());
		mInitPasswordView.setText(getString(R.string.title_init_password) + user.getInitPassword());
		mAvatarImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				System.out.println(user.getString("avatar"));
			}
		});

		if (!user.isPasswordSet()) {
			mResetInitPasswordButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final View inputs = getActivity().getLayoutInflater().inflate(R.layout.component_set_init_password_inputs, null);

					MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
							.title(R.string.dialog_title_reset_init_password)
							.contentColorRes(R.color.gray_dark)
							.customView(inputs, false)
							.positiveText(R.string.dialog_button_reset_init_password_submit)
							.positiveColorRes(R.color.primary)
							.onPositive(new MaterialDialog.SingleButtonCallback() {
								@Override
								public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
									EditText pwd = (EditText) inputs.findViewById(R.id.init_reset_password);
									EditText pwd2 = (EditText) inputs.findViewById(R.id.init_reset_password_confirm);
									if (!pwd.getText().toString().equals(pwd2.getText().toString())) {
										Toast.makeText(getActivity(), "密码不一致", Toast.LENGTH_SHORT).show();
									} else if (pwd.getText().toString().length() < 6) {
										Toast.makeText(getActivity(), "密码过短", Toast.LENGTH_SHORT).show();
									} else {
										UserAction.resetInitialPassword(pwd.getText().toString());
									}
								}
							})
							.negativeText(R.string.dialog_button_reset_init_password_cancel)
							.negativeColorRes(R.color.gray_light)
							.show();
				}
			});
		} else {
			mInitPasswordView.setVisibility(View.GONE);
			mResetInitPasswordButton.setVisibility(View.GONE);
		}

		return v;
	}

	@Subscribe
	public void updateListView(ProfileEvent e) {
		if (e.isTypeEvent(ProfileEvent.type.reset_init_password) && e.code == Config.SUCCESS) {
			Toast.makeText(getActivity(), "设置成功", Toast.LENGTH_SHORT).show();
			mInitPasswordView.setVisibility(View.GONE);
			mResetInitPasswordButton.setVisibility(View.GONE);
		} else {
			Toast.makeText(getActivity(), e.errorMessage, Toast.LENGTH_SHORT).show();
		}
	}

}
