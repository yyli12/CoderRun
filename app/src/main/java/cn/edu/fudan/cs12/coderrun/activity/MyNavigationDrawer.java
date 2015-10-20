package cn.edu.fudan.cs12.coderrun.activity;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.edu.fudan.cs12.coderrun.R;
import cn.edu.fudan.cs12.coderrun.action.UserAction;
import cn.edu.fudan.cs12.coderrun.entity.User;
import cn.edu.fudan.cs12.coderrun.fragment.HistoryItemFragment;
import cn.edu.fudan.cs12.coderrun.fragment.RunFragment;
import cn.edu.fudan.cs12.coderrun.fragment.UserFragment;
import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.elements.MaterialAccount;
import it.neokree.materialnavigationdrawer.elements.MaterialSection;
import it.neokree.materialnavigationdrawer.elements.listeners.MaterialAccountListener;
import it.neokree.materialnavigationdrawer.elements.listeners.MaterialSectionListener;

/**
 * Created by Li on 2015/10/11.
 */

/**
 * how to use?
 * see https://github.com/neokree/MaterialNavigationDrawer/wiki
 */
public class MyNavigationDrawer extends MaterialNavigationDrawer {
	MaterialAccount account;
	String userName;
	String userMail;

	@Override
	public void init(Bundle savedInstanceState) {
		User user = UserAction.getCurrentUser();
		User.getCurrentUser();
		userName = user.get("mobilePhoneNumber").toString();
		userMail = user.get("mobilePhoneNumber").toString();

		account = new MaterialAccount(this.getResources(), userName, userMail, null, R.drawable.bamboo);

		this.addAccount(account);

		Bundle params = new Bundle();
		params.putString("fields", "cover");

		// create sections
		this.addSection(newSection("Coder, Run!", R.drawable.ic_run, new RunFragment()).setSectionColor(getResources().getColor(R.color.app_green)));
		this.addSection(newSection("历史记录", R.drawable.ic_history, new HistoryItemFragment()).setSectionColor(getResources().getColor(R.color.app_blue)));
		this.addSection(newSection("我的账号", R.drawable.ic_profile, new UserFragment()).setSectionColor(getResources().getColor(R.color.primary)));

		// create bottom section
		this.addBottomSection(newSection("退出账号", R.drawable.ic_logout, new MaterialSectionListener() {
			@Override
			public void onClick(MaterialSection materialSection) {
				UserAction.logout();

				// FLAG_ACTIVITY_CLEAR_TASK only works on API 11, so if the user
				// logs out on older devices, we'll just exit.
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					Intent intent = new Intent(MyNavigationDrawer.this, MainActivity.class);
					startActivity(intent);
					finish();
				} else {
					finish();
				}
			}
		}));
	}

	public void onFragmentInteraction(String id) {

	}

}
