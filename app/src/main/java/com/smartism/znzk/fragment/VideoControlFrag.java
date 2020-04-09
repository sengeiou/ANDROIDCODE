package com.smartism.znzk.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.p2p.core.P2PHandler;
import com.p2p.core.utils.DelayThread;
import com.smartism.znzk.R;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.util.camera.T;

public class VideoControlFrag extends BaseFragment implements View.OnClickListener {
	private Context mContext;
	private Contact contact;
	private boolean isRegFilter = false;
	RelativeLayout change_video_format,change_volume,video_voleme_seek,layout_reverse;
	LinearLayout video_format_radio;
	ProgressBar progressBar_video_format,progressBar_volume;
	RadioButton radio_one,radio_two;
	SeekBar seek_volume;
	int cur_modify_video_format;
	int cur_modify_video_volume;
	RelativeLayout change_image_reverse;
	ImageView img_image_reverse;
	ProgressBar progressbar_image_reverse;
	boolean isOpenReverse=true;
	boolean isOpenModify;

	private TextView tv_voice_persent;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mContext = getActivity();
		contact = (Contact) getArguments().getSerializable("contact");
		View view = inflater.inflate(R.layout.fragment_video_control, container, false);
		initComponent(view);
		regFilter();
		showProgress_video_format();
		showProgress_volume();
		showProgress_image_reverse();
		P2PHandler.getInstance().getNpcSettings(contact.contactId, contact.contactPassword, MainApplication.GWELL_LOCALAREAIP);
		return view;
	}


	public void initComponent(View view){
		tv_voice_persent = (TextView) view.findViewById(R.id.tv_voice_persent);

		change_video_format = (RelativeLayout) view.findViewById(R.id.change_video_format);
		video_format_radio = (LinearLayout) view.findViewById(R.id.video_format_radio);
		progressBar_video_format = (ProgressBar) view.findViewById(R.id.progressBar_video_format);

		change_volume = (RelativeLayout) view.findViewById(R.id.change_volume);
		video_voleme_seek = (RelativeLayout) view.findViewById(R.id.video_voleme_seek);
		progressBar_volume = (ProgressBar) view.findViewById(R.id.progressBar_volume);

		seek_volume = (SeekBar) view.findViewById(R.id.seek_volume);
		radio_one = (RadioButton)view.findViewById(R.id.radio_one);
		radio_two = (RadioButton)view.findViewById(R.id.radio_two);

		change_image_reverse=(RelativeLayout)view.findViewById(R.id.change_image_reverse);
		img_image_reverse=(ImageView)view.findViewById(R.id.image_reverse_img);
		progressbar_image_reverse=(ProgressBar)view.findViewById(R.id.progressBar_image_reverse);
		layout_reverse=(RelativeLayout)view.findViewById(R.id.change_image_reverse);
		radio_one.setOnClickListener(this);
		radio_two.setOnClickListener(this);
		change_image_reverse.setOnClickListener(this);
		seek_volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

			@Override
			public void onProgressChanged(SeekBar arg0,int arg1, boolean arg2) {
				// TODO Auto-generated method stub


			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				final int value = arg0.getProgress();
				progressBar_volume.setVisibility(RelativeLayout.VISIBLE);
				seek_volume.setEnabled(false);
				cur_modify_video_volume = value;
				int persent =  value*100/seek_volume.getMax();
				tv_voice_persent.setText(persent+"%");
				switchVideoVolume(value);
			}

		});
	}

	public void regFilter(){
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.P2P.ACK_RET_GET_NPC_SETTINGS);

		filter.addAction(Constants.P2P.ACK_RET_SET_VIDEO_FORMAT);
		filter.addAction(Constants.P2P.RET_SET_VIDEO_FORMAT);
		filter.addAction(Constants.P2P.RET_GET_VIDEO_FORMAT);

		filter.addAction(Constants.P2P.ACK_RET_SET_VIDEO_VOLUME);
		filter.addAction(Constants.P2P.RET_SET_VIDEO_VOLUME);
		filter.addAction(Constants.P2P.RET_GET_VIDEO_VOLUME);
		filter.addAction(Constants.P2P.RET_GET_IMAGE_REVERSE);
		filter.addAction(Constants.P2P.ACK_VRET_SET_IMAGEREVERSE);
		mContext.registerReceiver(mReceiver, filter);
		isRegFilter = true;
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context arg0, Intent intent) {
			if(intent.getAction().equals(Constants.P2P.RET_GET_VIDEO_FORMAT)){
				int type = intent.getIntExtra("type", -1);
				if(type== Constants.P2P_SET.VIDEO_FORMAT_SET.VIDEO_FORMAT_PAL){
					radio_one.setChecked(true);
				}else if(type== Constants.P2P_SET.VIDEO_FORMAT_SET.VIDEO_FORMAT_NTSC){
					radio_two.setChecked(true);
				}
				showVideoFormat();
				radio_one.setEnabled(true);
				radio_two.setEnabled(true);
			}else if(intent.getAction().equals(Constants.P2P.RET_SET_VIDEO_FORMAT)){
				int result = intent.getIntExtra("result", -1);
				if(result== Constants.P2P_SET.VIDEO_FORMAT_SET.SETTING_SUCCESS){
					if(cur_modify_video_format== Constants.P2P_SET.VIDEO_FORMAT_SET.VIDEO_FORMAT_PAL){
						radio_one.setChecked(true);
					}else if(cur_modify_video_format== Constants.P2P_SET.VIDEO_FORMAT_SET.VIDEO_FORMAT_NTSC){
						radio_two.setChecked(true);
					}
					showVideoFormat();
					radio_one.setEnabled(true);
					radio_two.setEnabled(true);
					T.showShort(mContext,  mContext.getString(R.string.camera_video_control));
				}else{
					showVideoFormat();
					radio_one.setEnabled(true);
					radio_two.setEnabled(true);
					T.showShort(mContext, R.string.operator_error);
				}
			}else if(intent.getAction().equals(Constants.P2P.RET_GET_VIDEO_VOLUME)){
				int value = intent.getIntExtra("value", 0);
				seek_volume.setProgress(value);
				seek_volume.setEnabled(true);
				int persent =  value*100/seek_volume.getMax();
				tv_voice_persent.setText(persent+"%");
				showVideoVolume();
			}else if(intent.getAction().equals(Constants.P2P.RET_SET_VIDEO_VOLUME)){
				int result = intent.getIntExtra("result", -1);
				if(result== Constants.P2P_SET.VIDEO_VOLUME_SET.SETTING_SUCCESS){
					seek_volume.setProgress(cur_modify_video_volume);
					seek_volume.setEnabled(true);
					int persent =  cur_modify_video_volume*100/seek_volume.getMax();
					tv_voice_persent.setText(persent+"%");
					showVideoVolume();
					T.showShort(mContext, mContext.getString(R.string.camera_video_control_vol));
				}else{
					seek_volume.setEnabled(true);
					showVideoVolume();
					T.showShort(mContext, R.string.operator_error);
				}
			}else if(intent.getAction().equals(Constants.P2P.ACK_RET_GET_NPC_SETTINGS)){
				int result = intent.getIntExtra("result", -1);
				if(result== Constants.P2P_SET.ACK_RESULT.ACK_PWD_ERROR){
					Intent i = new Intent();
					i.setAction(Constants.Action.CONTROL_SETTING_PWD_ERROR);
					mContext.sendBroadcast(i);
				}else if(result== Constants.P2P_SET.ACK_RESULT.ACK_NET_ERROR){
					Log.e("my","net error resend:get npc settings");
					P2PHandler.getInstance().getNpcSettings(contact.contactId, contact.contactPassword,MainApplication.GWELL_LOCALAREAIP);
				}
			}else if(intent.getAction().equals(Constants.P2P.ACK_RET_SET_VIDEO_FORMAT)){
				int result = intent.getIntExtra("result", -1);
				if(result== Constants.P2P_SET.ACK_RESULT.ACK_PWD_ERROR){
					Intent i = new Intent();
					i.setAction(Constants.Action.CONTROL_SETTING_PWD_ERROR);
					mContext.sendBroadcast(i);
				}else if(result== Constants.P2P_SET.ACK_RESULT.ACK_NET_ERROR){
					Log.e("my","net error resend:set npc settings video format");
					switchVideoFormat(cur_modify_video_format);
				}
			}else if(intent.getAction().equals(Constants.P2P.ACK_RET_SET_VIDEO_VOLUME)){

				int result = intent.getIntExtra("result", -1);
				if(result== Constants.P2P_SET.ACK_RESULT.ACK_PWD_ERROR){
					Intent i = new Intent();
					i.setAction(Constants.Action.CONTROL_SETTING_PWD_ERROR);
					mContext.sendBroadcast(i);
				}else if(result== Constants.P2P_SET.ACK_RESULT.ACK_NET_ERROR){
					Log.e("my","net error resend:set npc settings video volume");
					switchVideoVolume(cur_modify_video_volume);
				}
			}else if(intent.getAction().equals(Constants.P2P.RET_GET_IMAGE_REVERSE)){
				int type=intent.getIntExtra("type",-1);
				if(type==0){
					layout_reverse.setVisibility(RelativeLayout.VISIBLE);
					showImageview_image_reverse();
					img_image_reverse.setBackgroundResource(R.drawable.zhzj_switch_off);
					isOpenReverse=true;
				}else if(type==1){
					layout_reverse.setVisibility(RelativeLayout.VISIBLE);
					showImageview_image_reverse();
					img_image_reverse.setBackgroundResource(R.drawable.zhzj_switch_on);
					isOpenReverse=false;
				}
			}else if(intent.getAction().equals(Constants.P2P.ACK_VRET_SET_IMAGEREVERSE)){
				int result=intent.getIntExtra("result",-1);
				if(result== Constants.P2P_SET.ACK_RESULT.ACK_NET_ERROR){
					if(isOpenReverse=true){
						P2PHandler.getInstance().setImageReverse(contact.contactId, contact.contactPassword, 1,MainApplication.GWELL_LOCALAREAIP);
					}else{
						P2PHandler.getInstance().setImageReverse(contact.contactId, contact.contactPassword, 0,MainApplication.GWELL_LOCALAREAIP);
					}

				}else if(result== Constants.P2P_SET.ACK_RESULT.ACK_SUCCESS){
					if(isOpenReverse==true){
						showImageview_image_reverse();
						img_image_reverse.setBackgroundResource(R.drawable.zhzj_switch_on);
						isOpenReverse=false;
					}else{
						showImageview_image_reverse();
						img_image_reverse.setBackgroundResource(R.drawable.zhzj_switch_off);
						isOpenReverse=true;
					}
				}
			}
		}
	};


	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch(view.getId()){
			case R.id.radio_one:
				progressBar_video_format.setVisibility(RelativeLayout.VISIBLE);
				radio_one.setEnabled(false);
				radio_two.setEnabled(false);

				cur_modify_video_format = Constants.P2P_SET.VIDEO_FORMAT_SET.VIDEO_FORMAT_PAL;
				switchVideoFormat(Constants.P2P_SET.VIDEO_FORMAT_SET.VIDEO_FORMAT_PAL);
				break;
			case R.id.radio_two:
				progressBar_video_format.setVisibility(RelativeLayout.VISIBLE);
				radio_one.setEnabled(false);
				radio_two.setEnabled(false);

				cur_modify_video_format = Constants.P2P_SET.VIDEO_FORMAT_SET.VIDEO_FORMAT_NTSC;
				switchVideoFormat(Constants.P2P_SET.VIDEO_FORMAT_SET.VIDEO_FORMAT_NTSC);
				break;
			case R.id.change_image_reverse:
				showProgress_image_reverse();
				if(isOpenReverse==true){
					P2PHandler.getInstance().setImageReverse(contact.contactId, contact.contactPassword, 1,MainApplication.GWELL_LOCALAREAIP);
				}else{
					P2PHandler.getInstance().setImageReverse(contact.contactId, contact.contactPassword, 0,MainApplication.GWELL_LOCALAREAIP);
				}
				break;
		}
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		if(isRegFilter){
			mContext.unregisterReceiver(mReceiver);
			isRegFilter = false;
		}
	}


	public void switchVideoVolume(final int toggle){

		new DelayThread(Constants.SettingConfig.SETTING_CLICK_TIME_DELAY,new DelayThread.OnRunListener() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				P2PHandler.getInstance().setVideoVolume(contact.contactId, contact.contactPassword, toggle,MainApplication.GWELL_LOCALAREAIP);

			}
		}).start();
	}

	public void switchVideoFormat(final int toggle){
		new DelayThread(Constants.SettingConfig.SETTING_CLICK_TIME_DELAY,new DelayThread.OnRunListener() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				P2PHandler.getInstance().setVideoFormat(contact.contactId, contact.contactPassword, toggle,MainApplication.GWELL_LOCALAREAIP);
			}
		}).start();
	}
	public void showProgress_image_reverse(){
		progressbar_image_reverse.setVisibility(ProgressBar.VISIBLE);
		img_image_reverse.setVisibility(ImageView.GONE);
	}
	public void showImageview_image_reverse(){
		progressbar_image_reverse.setVisibility(ImageView.GONE);
		img_image_reverse.setVisibility(ImageView.VISIBLE);
	}
	public void showVideoFormat(){
		progressBar_video_format.setVisibility(RelativeLayout.GONE);
		video_format_radio.setVisibility(RelativeLayout.VISIBLE);
	}

	public void showProgress_video_format(){
		progressBar_video_format.setVisibility(RelativeLayout.VISIBLE);
		video_format_radio.setVisibility(RelativeLayout.GONE);
	}

	public void showVideoVolume(){
		video_voleme_seek.setVisibility(RelativeLayout.VISIBLE);
		progressBar_volume.setVisibility(RelativeLayout.GONE);
		seek_volume.setEnabled(true);
	}

	public void showProgress_volume(){
		progressBar_volume.setVisibility(RelativeLayout.VISIBLE);
		seek_volume.setEnabled(false);
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		Intent it=new Intent();
		it.setAction(Constants.Action.CONTROL_BACK);
		mContext.sendBroadcast(it);
	}
}
