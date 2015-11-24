package cn.edu.fudan.cs12.coderrun.fragment;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
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
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.GetDataCallback;
import com.avos.avoscloud.SaveCallback;
import com.bumptech.glide.Glide;
import com.lantouzi.wheelview.WheelView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.edu.fudan.cs12.coderrun.Config;
import cn.edu.fudan.cs12.coderrun.R;
import cn.edu.fudan.cs12.coderrun.Util;
import cn.edu.fudan.cs12.coderrun.action.UserAction;
import cn.edu.fudan.cs12.coderrun.entity.User;
import cn.edu.fudan.cs12.coderrun.event.DataEvent;
import cn.edu.fudan.cs12.coderrun.event.ProfileEvent;
import cn.edu.fudan.cs12.coderrun.provider.BusProvider;
import de.halfbit.tinybus.Subscribe;
import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import mehdi.sakout.fancybuttons.FancyButton;

public class UserFragment extends Fragment {

	User user;
	ImageView mAvatarImageView;
	ImageView mTmpAvatarImageView;
	TextView mMobileTextView;
	TextView mInitPasswordView;
	TextView mAreaTextView;
	TextView mSloganTextView;
	WheelView mProvinceWheelView;
	WheelView mCityWheelView;
	EditText mSloganInput;

	Button mResetInitPasswordButton;
	String avatarPath;



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
		mAreaTextView = (TextView) v.findViewById(R.id.user_area);
		mSloganTextView = (TextView) v.findViewById(R.id.user_slogan);


		String url;
		if (user.getAVFile("avatar") == null) {
			url = "http://ac-sbt4q1yt.clouddn.com/b35d8aedb12edbfc.jpeg";
		} else {
			url = user.getAVFile("avatar").getUrl();
		}
		Glide.with(this)
				.load(url)
				.centerCrop()
				.crossFade()
				.into(mAvatarImageView);

		mMobileTextView.setText(UserAction.getCurrentUser().getMobilePhoneNumber());
		mAreaTextView.setText(UserAction.getCurrentUser().getArea());
		mSloganTextView.setText(UserAction.getCurrentUser().getSlogan());
		mInitPasswordView.setText(getString(R.string.title_init_password) + user.getInitPassword());
		mAvatarImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showAvatarDialog();
			}
		});
		mAreaTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showAreaDialog();
			}
		});
		mSloganTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showSloganDialog();
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

	public void showSloganDialog() {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.component_slogan_input, null);
		mSloganInput = (EditText) v.findViewById(R.id.slogan_input);

		String origin = mSloganTextView.getText().toString();
		mSloganInput.setText(origin);

		MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
				.title("编辑Slogan")
				.contentColorRes(R.color.gray_dark)
				.customView(v, false)
				.positiveText("确定")
				.positiveColorRes(R.color.primary)
				.onPositive(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
						String slogan = mSloganInput.getText().toString();
						if (slogan.length() == 0) {
							Toast.makeText(getActivity(), "Slogan为空", Toast.LENGTH_SHORT).show();
						} else if (slogan.length() > 25) {
							Toast.makeText(getActivity(), "Slogan太长啦", Toast.LENGTH_SHORT).show();
						} else {
							updateSlogan();
						}
					}
				})
				.negativeText("取消")
				.negativeColorRes(R.color.gray_light)
				.show();
	}

	public void showAreaDialog() {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.component_area_picker, null);


		mProvinceWheelView = (WheelView) v.findViewById(R.id.province_picker);
		mCityWheelView = (WheelView) v.findViewById(R.id.city_picker);
		mProvinceWheelView.setItems(Arrays.asList(Util.provinceArray));
		mCityWheelView.setItems(Arrays.asList(Util.cityArray[0]));
		mProvinceWheelView.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener() {
			@Override
			public void onWheelItemSelected(int i) {
				mCityWheelView.setItems(Arrays.asList(Util.cityArray[i]));
				mCityWheelView.selectIndex(0);
			}
		});

		MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
				.title("设置区域")
				.contentColorRes(R.color.gray_dark)
				.customView(v, false)
				.positiveText("确定")
				.positiveColorRes(R.color.primary)
				.onPositive(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
						updateArea();
					}
				})
				.negativeText("取消")
				.negativeColorRes(R.color.gray_light)
				.show();
	}

	public void showAvatarDialog() {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.component_avatar_uploader, null);
		FancyButton mPickButton = (FancyButton) v.findViewById(R.id.button_pick);
		mTmpAvatarImageView = (ImageView) v.findViewById(R.id.user_tmp_avatar);

		mPickButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showFileChooser();
			}
		});

		MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
				.title("上传新头像")
				.contentColorRes(R.color.gray_dark)
				.customView(v, false)
				.positiveText("上传")
				.positiveColorRes(R.color.primary)
				.onPositive(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
						uploadAvatar();
					}
				})
				.negativeText("取消")
				.negativeColorRes(R.color.gray_light)
				.show();
	}

	private static final int FILE_SELECT_CODE = 0;

	private void showFileChooser() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		try {
			startActivityForResult(
					Intent.createChooser(intent, "Select a File to Upload"),
					FILE_SELECT_CODE);
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(getActivity(), "Please install a File Manager.",
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data == null) {
			return;
		}
		switch (requestCode) {
			case FILE_SELECT_CODE:
				final Uri uri = data.getData();
				avatarPath = Util.getPath(getActivity(), uri);
				Glide.with(this)
						.load(avatarPath)
						.centerCrop()
						.crossFade()
						.into(mTmpAvatarImageView);
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void updateArea() {
		int prov = mProvinceWheelView.getSelectedPosition();
		int city = mCityWheelView.getSelectedPosition();
		String area = Util.provinceArray[prov] + " " + Util.cityArray[prov][city];
		mAreaTextView.setText(area);
		User u = UserAction.getCurrentUser();
		u.put("area", area);
		u.saveInBackground(new SaveCallback() {
			@Override
			public void done(AVException e) {
				if (e == null) {
					Toast.makeText(getActivity(), "地区更新成功", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getActivity(), "地区更新失败", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	public void updateSlogan() {
		String slogan = mSloganInput.getText().toString();
		mSloganTextView.setText(slogan);
		User u = UserAction.getCurrentUser();
		u.put("slogan", slogan);
		u.saveInBackground(new SaveCallback() {
			@Override
			public void done(AVException e) {
				if (e == null) {
					Toast.makeText(getActivity(), "Slogan更新成功", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getActivity(), "Slogan更新失败", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	public void uploadAvatar() {
		if (avatarPath == null) {
			Toast.makeText(getActivity(), "尚未选择头像", Toast.LENGTH_SHORT).show();
		} else {
			try {
				String filename = Util.getTime() + "_" + UserAction.getCurrentUser().getMobilePhoneNumber();
				final AVFile file = AVFile.withAbsoluteLocalPath(filename, avatarPath);
				try {
					User u = UserAction.getCurrentUser();
					u.put("avatar", file);
					u.saveInBackground(new SaveCallback() {
						@Override
						public void done(AVException e) {
							if (e == null) {
								Toast.makeText(getActivity(), "头像更新成功", Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(getActivity(), "头像更新失败", Toast.LENGTH_SHORT).show();
							}
						}
					});
				} catch (Exception e) {
					Toast.makeText(getActivity(), "头像上传出错", Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
				Toast.makeText(getActivity(), "头像文件出错", Toast.LENGTH_SHORT).show();
			}
			Glide.with(this)
					.load(avatarPath)
					.centerCrop()
					.crossFade()
					.into(mAvatarImageView);
		}
	}

}
