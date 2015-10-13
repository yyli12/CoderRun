package cn.edu.fudan.cs12.coderrun.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import butterknife.ButterKnife;
import butterknife.Bind;
import cn.edu.fudan.cs12.coderrun.R;
import cn.edu.fudan.cs12.coderrun.entity.User;

public class MainActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);

		if (User.getCurrentUser() != null) {
			Intent intent = new Intent(MainActivity.this, MyNavigationDrawer.class);
			startActivity(intent);
			this.finish();
		} else {
			Intent intent = new Intent(MainActivity.this, LoginActivity.class);
			startActivity(intent);
			this.finish();
		}

	}
}
