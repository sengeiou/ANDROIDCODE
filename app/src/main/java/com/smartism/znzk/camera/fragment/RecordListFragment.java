package com.smartism.znzk.camera.fragment;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.p2p.core.MediaPlayer;
import com.p2p.core.P2PHandler;
import com.smartism.znzk.R;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.camera.P2PConnect;
import com.smartism.znzk.activity.camera.PlayBackListActivity;
import com.smartism.znzk.camera.adapter.RecordAdapter;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.util.camera.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RecordListFragment extends Fragment implements OnScrollListener{
	Context mContext;
	ListView list_record;
	Contact contact;
	String[] names;
	AlertDialog load_record;
	View load_view;
	LayoutInflater inflater;
	RecordAdapter adapter;
	boolean isDialogShowing = false;
	private boolean mIsReadyCall   = false;
    private int visibleLastIndex = 0;   //最后的可视项索引   
    private int visibleItemCount;       // 当前窗口可见项总数   
    RelativeLayout layout_loading;
    boolean isRegFilter=false;
    public ArrayList<String> list;
	public int position;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		this.inflater = inflater;
		mContext = getActivity();
		View view = inflater.inflate(R.layout.fragment_record, container, false);
		initComponent(view);
		regFilter();
		return view;
	}
	public void regFilter(){
		isRegFilter=true;
		IntentFilter filter=new IntentFilter();
		filter.addAction(Constants.P2P.RET_GET_PLAYBACK_FILES);
		filter.addAction(Constants.P2P.ACK_RET_GET_PLAYBACK_FILES);
		filter.addAction(Constants.Action.REPEAT_LOADING_DATA);
		mContext.registerReceiver(br, filter);
	}
	BroadcastReceiver br=new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context arg0, Intent intent) {
			if(intent.getAction().equals(Constants.P2P.RET_GET_PLAYBACK_FILES)){
		}else if(intent.getAction().equals(Constants.Action.REPEAT_LOADING_DATA)){
			layout_loading.setVisibility(RelativeLayout.GONE);
		}
	  }
	};
	public void initComponent(View view){
		list_record = (ListView) view.findViewById(R.id.list_record);
		adapter = new RecordAdapter(mContext,list);
		list_record.setAdapter(adapter);
		layout_loading=(RelativeLayout)view.findViewById(R.id.layout_loading);
		list_record.setOnScrollListener(this);
		list_record.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				String filename=adapter.getList().get(arg2);
				load_view = inflater.inflate(R.layout.dialog_load_record, null);
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				
				load_record = builder.create();
				//load_record.setCancelable(false);
				load_record.show();
				isDialogShowing = true;
				load_record.setContentView(load_view);
				load_record.setOnKeyListener(new OnKeyListener(){

					@Override
					public boolean onKey(DialogInterface arg0, int arg1,
							KeyEvent event) {
						// TODO Auto-generated method stub
						if (event.getAction() == KeyEvent.ACTION_DOWN
								&& event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
							//createExitDialog(this);
							if(isDialogShowing){
								
								load_record.cancel();
								isDialogShowing = false;
								P2PHandler.getInstance().finish();
							}
							return true;
						}
						return false;
					}
					
				});
				
				FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(
						Utils.dip2px(getActivity(), 222), Utils.dip2px(
								getActivity(), 130));
				load_view.setLayoutParams(layout);
			    final AnimationDrawable anim;  
			    ImageView img = (ImageView) load_view.findViewById(R.id.load_record_img);
			     
			    anim = (AnimationDrawable) img.getDrawable();  
			    OnPreDrawListener opdl = new OnPreDrawListener(){  
			           @Override  
			            public boolean onPreDraw() {  
			        	   anim.start();  
			               return true; 
			           }  
			      
			    };  
			    img.getViewTreeObserver().addOnPreDrawListener(opdl);
			    
				P2PConnect.setCurrent_state(P2PConnect.P2P_STATE_CALLING);
				P2PConnect.setCurrent_call_id(contact.contactId);
				P2PHandler.getInstance().playbackConnect(contact.contactId,contact.contactId, contact.contactPassword,filename, arg2, 0, 0, 0, 0, 0,0,0);
				position = arg2;
			    PlayBackListActivity.fileName=filename;
				Log.e("playback", filename);
			}
			
		});
	}
	
	
	
	public void cancelDialog(){
		load_record.cancel();
		isDialogShowing = false;
		MediaPlayer.getInstance().native_p2p_hungup();
		Log.e("my","hungup");
	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.e("my","onDestroy");
		if(isRegFilter==true){
			mContext.unregisterReceiver(br);
			isRegFilter=false;
		}
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.e("my","onPause");
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.e("my","onResume");
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.e("my","onStart");
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	
	public void setList(ArrayList<String> list){
		this.list = list;
	}
	
	public void setUser(Contact contact){
		this.contact = contact;
		
	}
	
	public void closeDialog(){
		if(null!=load_record){
			load_record.cancel();
			isDialogShowing = false;
		}
	}
	
	public void scrollOn(){
		list_record.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				return false;
			}
			
		});
	}
	
	public void scrollOff(){
		list_record.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				return true;
			}
			
		});
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		 this.visibleItemCount = visibleItemCount;  
	     visibleLastIndex = firstVisibleItem + visibleItemCount; 
	     Log.e("length", visibleLastIndex+"visibleLastIndex");
	     Log.e("total", "totalItemCount"+totalItemCount);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		  int itemsLastIndex = adapter.getCount();    //数据集最后一项的索引   
	      int lastIndex = itemsLastIndex + 1;             //加上底部的loadMoreView项   
	        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && visibleLastIndex == lastIndex) {  
	            //如果是自动加载,可以在这里放置异步加载数据的代码   
	            Log.e("loading", "loading...");  
	        }  
		  Log.e("length", itemsLastIndex+"itemsLastIndex");  
		  if(itemsLastIndex==visibleLastIndex){
			  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			  try {
				Date nextStartTime=RecordAdapter.startTime;
				String lasttime=adapter.getLastItem();
				if(lasttime==null||nextStartTime==null){
					return;
				}
				Date nextEndTime=sdf.parse(adapter.getLastItem());
				if(nextEndTime==null||nextEndTime.equals("")||nextStartTime==null||nextStartTime.equals("")){
					return;
				}
//				layout_loading.setVisibility(RelativeLayout.VISIBLE);
				P2PHandler.getInstance().getRecordFiles(contact.contactId, contact.contactPassword, nextStartTime, nextEndTime,MainApplication.GWELL_LOCALAREAIP);
				Log.e("time1", nextStartTime.toString());
				Log.e("time2", nextEndTime.toString());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  }	  
   }
 
}
