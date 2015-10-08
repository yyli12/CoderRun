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

	// UI references.
	@Bind(R.id.button_main) Button mMainButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);

		if (User.getCurrentUser() != null) {
			Intent intent = new Intent(MainActivity.this, FunctionActivity.class);
			startActivity(intent);
			this.finish();
		} else {
			mMainButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(MainActivity.this, LoginActivity.class);
					startActivity(intent);
				}
			});
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
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
}
