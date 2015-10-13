package cn.edu.fudan.cs12.coderrun.fragment;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.edu.fudan.cs12.coderrun.R;

public class UserFragment extends Fragment {
	int t;
	Button testButton;
	TextView tv;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		t = 0;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_user, container, false);
		ButterKnife.bind(v);
		testButton = (Button) v.findViewById(R.id.test_button);
		tv = (TextView) v.findViewById(R.id.test_tv);
		testButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				test();
			}
		});
		return v;
	}

	void test() {
		t++;
		tv.setText(Integer.toString(t));
	}

}
