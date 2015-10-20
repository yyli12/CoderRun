package cn.edu.fudan.cs12.coderrun.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.edu.fudan.cs12.coderrun.R;
import cn.edu.fudan.cs12.coderrun.action.UserAction;

/**
 * Created by Li on 2015/10/13.
 */
public class HistoryListAdapter extends BaseAdapter {
	public List<AVObject> allHistory;
	private LayoutInflater mInflater = null;

	public HistoryListAdapter(Context context){
		super();
		UserAction.userHistoryList(this);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		if (allHistory == null) {
			return 0;
		} else {
			return allHistory.size();
		}
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if (allHistory == null) {
			return null;
		} else {
			return allHistory.get(position);
		}
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		AVObject item = (AVObject) getItem(position);
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.component_history_item, null);
			holder.position = (TextView) convertView.findViewById(R.id.history_item_position);
			holder.begin_end = (TextView) convertView.findViewById(R.id.history_item_begin_end);
			holder.speed = (TextView) convertView.findViewById(R.id.history_item_speed);
			holder.distance = (TextView) convertView.findViewById(R.id.history_item_total_distance);
			holder.location = (TextView) convertView.findViewById(R.id.history_item_location);
			holder.score = (RatingBar) convertView.findViewById(R.id.history_item_ratingBar);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.position.setText(Integer.toString(position));
		SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
		String end = format.format(item.getInt("finish_time") * 1000L);
		String begin = format.format(item.getInt("finish_time") * 1000L - item.getInt("running_time") * 60 * 1000L);
		holder.begin_end.setText(begin + " ~ " + end);
		String spped = new DecimalFormat("#.0").format(item.getInt("running_time") / item.getDouble("running_distance"));
		holder.speed.setText(spped);
		holder.score.setRating(item.getInt("score"));
		String loc = item.getString("location");
		holder.location.setText(loc == null ? "" : loc);
		holder.distance.setText(item.getDouble("running_distance") + "km");

		return convertView;
	}

}

class ViewHolder {
	public TextView position;
	public TextView begin_end;
	public TextView speed;
	public TextView distance;
	public TextView location;
	public RatingBar score;
}
