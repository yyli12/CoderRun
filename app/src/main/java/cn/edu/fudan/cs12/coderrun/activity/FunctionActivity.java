package cn.edu.fudan.cs12.coderrun.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.edu.fudan.cs12.coderrun.R;
import cn.edu.fudan.cs12.coderrun.entity.User;

public class FunctionActivity extends AppCompatActivity {
	@Bind(R.id.action_button_logout)
	Button mLogoutButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_function);
		ButterKnife.bind(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_function, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@OnClick(R.id.action_button_logout)
	void logout() {
		User.logOut();
		Intent mainIntent = new Intent(FunctionActivity.this, MainActivity.class);
		startActivity(mainIntent);
		FunctionActivity.this.finish();
	}
}
