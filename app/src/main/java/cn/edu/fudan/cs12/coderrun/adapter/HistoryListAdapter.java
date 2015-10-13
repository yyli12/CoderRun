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
			holder.distance = (TextView) convertView.findViewById(R.id.history_item_begin_end);
			holder.score = (RatingBar) convertView.findViewById(R.id.history_item_ratingBar);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		System.out.println(item.toString());
		holder.position.setText(Integer.toString(position));
		holder.begin_end.setText(item.getInt("finish_time") + " " + item.getInt("running_time"));
		String spped = new DecimalFormat("#.00").format(item.getInt("running_time") / item.getDouble("running_distance"));
		holder.speed.setText(spped + " min/km");
		holder.score.setRating(item.getInt("score"));

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
