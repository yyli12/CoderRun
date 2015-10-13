package cn.edu.fudan.cs12.coderrun.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.avos.avoscloud.AVObject;

import java.util.List;

import cn.edu.fudan.cs12.coderrun.Config;
import cn.edu.fudan.cs12.coderrun.R;
import cn.edu.fudan.cs12.coderrun.adapter.HistoryListAdapter;
import cn.edu.fudan.cs12.coderrun.event.DataEvent;
import cn.edu.fudan.cs12.coderrun.fragment.dummy.DummyContent;
import cn.edu.fudan.cs12.coderrun.provider.BusProvider;
import de.halfbit.tinybus.Subscribe;


public class HistoryItemFragment extends ListFragment {
	private HistoryListAdapter adapter = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		adapter = new HistoryListAdapter(getActivity());
		setListAdapter(adapter);
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
		View v = inflater.inflate(R.layout.fragment_history, container, false);
		return v;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		System.out.println("Click On List Item!!!");
		super.onListItemClick(l, v, position, id);
	}

	@Subscribe
	public void updateListView(DataEvent e) {
		if (adapter != null && e.isTypeEvent(DataEvent.type.history_item) && e.code == Config.SUCCESS) {
			adapter.notifyDataSetChanged();
		} else {
			// todo
		}
	}
}
