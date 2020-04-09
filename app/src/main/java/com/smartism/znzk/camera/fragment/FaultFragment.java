package com.smartism.znzk.camera.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartism.znzk.R;


public class FaultFragment extends Fragment {
	TextView text,cleck_refresh;
	String error_text = "";
	boolean isCanRefresh = false;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_fault, container, false);
		view.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(isCanRefresh){
//					Intent i = new Intent();
//					i.setAction(Constants.Action.REPLACE_LOADING);
//					MyApp.app.sendBroadcast(i);
				}
			}
			
		});
		initComponent(view);
		return view;
	}
	
	public void initComponent(View view){
		text = (TextView) view.findViewById(R.id.default_text);
		cleck_refresh = (TextView) view.findViewById(R.id.click_refresh);
		if(isCanRefresh){
			cleck_refresh.setVisibility(RelativeLayout.VISIBLE);
		}
		text.setText(error_text);
	}
	
	
	public void setErrorText(String error){
		this.error_text = error;
	}
	
	
	public void setClickRefresh(){
		if(!isCanRefresh){
			isCanRefresh = true;
		}
	}
}
