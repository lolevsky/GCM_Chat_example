package com.iapplize.gcmtest.fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.iapplize.gcmtest.Installation;
import com.iapplize.gcmtest.R;
import com.iapplize.gcmtest.activity.MainActivityUserSelectListener;
import com.iapplize.gcmtest.http.object.User;

public class MainDrawerFragment extends SherlockFragment implements
		OnItemClickListener {

	private MainActivityUserSelectListener mMainActivityUserSelect;
	
	private View mRootView;

	public List<User> user = new ArrayList<User>();

	ListView lv;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_drawer_main, container,
				false);
		
		lv = (ListView) mRootView.findViewById(android.R.id.list);
		
		TextView header = new TextView(getActivity());
		header.setText(R.string.chat_with_all);
		header.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));
		header.setTextColor(getResources().getColor(android.R.color.white));
		header.setTextSize(TypedValue.COMPLEX_UNIT_SP , 18);
		
		lv.addHeaderView(header);
		
		
		lv.setOnItemClickListener(this);
		return mRootView;
	}
	
	public int dpToPx(int dp){
		DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
		return (int)((dp * displayMetrics.density) + 0.5);
	}

	public void setMainActivityUserSelect(MainActivityUserSelectListener mainActivityUserSelect){
		mMainActivityUserSelect = mainActivityUserSelect;
	}
	
	@SuppressLint("SimpleDateFormat")
	public void updateList(List<User> user) {

		this.user = user;

		for (int i = 0; i < user.size(); i++) {
			if (user.get(i).MacAddress.equalsIgnoreCase(Installation.id(
					getActivity()).replace("-", ""))) {
				user.remove(i);
			}
		}

		List<Map<String, Object>> resourceNames = new ArrayList<Map<String, Object>>();

		Map<String, Object> data;

		SimpleDateFormat postFormater = new SimpleDateFormat("dd/MM/yyyy HH:mm");

		for (int i = 0; i < user.size(); i++) {
			data = new HashMap<String, Object>();

			if (user.get(i).UserName.equalsIgnoreCase("")) {
				data.put("text1", user.get(i).Email);
			} else {
				data.put("text1", user.get(i).UserName);
			}

			try {
				data.put(
						"text2",
						getString(R.string.Last_time_visit)
								+ postFormater.format((new Date(Long
										.valueOf(user.get(i).Updated_at)))));
			} catch (Exception ex) {
				data.put("text2", getString(R.string.Error_Date));
			}

			resourceNames.add(data);
		}

		SimpleAdapter notes = new SimpleAdapter(getActivity(), resourceNames,
				R.layout.userrow, new String[] { "text1", "text2" }, new int[] {
						R.id.Text1, R.id.Text2 });

		lv.setAdapter(notes);
		notes.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> adaper, View v, int location,
			long arg3) {
		
		location-=1;
		
		if(mMainActivityUserSelect != null){
			mMainActivityUserSelect.onUserSelect(location);
		}else{
			Toast.makeText(getActivity(), R.string.Error, Toast.LENGTH_SHORT).show();
		}		
	}
}
