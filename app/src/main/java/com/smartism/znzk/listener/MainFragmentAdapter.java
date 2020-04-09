package com.smartism.znzk.listener;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

public class MainFragmentAdapter extends FragmentPagerAdapter {

	private static final String TAG = "MainFragmentAdapter";
	private List<Fragment> list;

	public MainFragmentAdapter(FragmentManager fragmentManager,
			List<Fragment> list) {
		super(fragmentManager);
		this.list = list;
	}

	@Override
	public Fragment getItem(int arg0) {
		Log.i(TAG, "" + arg0);
		return list.get(arg0);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

}
