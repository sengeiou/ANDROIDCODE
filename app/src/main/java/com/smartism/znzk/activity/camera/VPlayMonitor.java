package com.smartism.znzk.activity.camera;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.macrovideo.sdk.custom.DeviceInfo;
import com.macrovideo.sdk.defines.Defines;
import com.macrovideo.sdk.defines.ResultCode;
import com.macrovideo.sdk.media.LibContext;
import com.macrovideo.sdk.media.LoginHandle;
import com.macrovideo.sdk.media.LoginHelper;
import com.macrovideo.sdk.media.NVMediaPlayer;
import com.macrovideo.sdk.setting.AlarmAndPromptInfo;
import com.macrovideo.sdk.setting.DeviceAlarmAndPromptSetting;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.adapter.HorizontalListViewAdapter;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.global.VList;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.HorizontalListView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/**
 * V380摄像头实时播放
 * @author 2016年08月08日 update 王建
 *
 */
public class VPlayMonitor extends ActivityParentActivity implements OnTouchListener, OnClickListener{
	private RelativeLayout rl,layout_title,rl_tools;
	NVMediaPlayer mvMediaPlayer = null;
	private HorizontalListView horizon_listview;
	private ImageView full_screen,iv_voice, iv_speak, iv_defence, iv_screenshot,iv_half_screen,hungup,close_voice,send_voice,screenshot,iv_file,iv_recode;
	private View view1, view2;
	private ProgressBar loadingBar = null;
	private ViewPager viewpager;
	private ArrayList<View> dots;
	private int ScrrenOrientation;
	private boolean mIsSpeaking=false;
	private int oldPosition = 0;// 记录上一次点的位置
	private int currentItem; // 当前页面
	private List<View> viewList;// view数组
	private int mStreamType=0;//当前播放的质量
	private boolean m_bFinish = false;
	private boolean mPlaySound=true;
	private int nScreenOrientation = Configuration.ORIENTATION_PORTRAIT;
	static final short SHOWCODE_LOADING=1001;//正在加载
	static final short SHOWCODE_NEW_IMAGE=1002;//新图片
	private LoginHandle _deviceParam=null;
//	static final short SHOWCODE_NOTICE=1003;//提示
	private boolean m_bReversePRI=true;
	private boolean mIsReverse=false;
	private int ptzTimerThreadID=0;
	static final short SHOWCODE_VIDEO=1004;//提示
	static final short SHOWCODE_STOP=2001;//停止播放
	static final short SHOWCODE_HAS_DATA=3001;//有数据
	private int FLING_MIN_DISTANCE = 10;  
    private int FLING_MIN_VELOCITY = 80;  
    private int FLING_MAX_DISTANCE = FLING_MIN_DISTANCE;
    private static int BTN_SCREENSHOT = 10010; // add by mai 2015-6-25 延迟更新截图
	static final short STAT_CONNECTING=2001;//正在连接服务器
	static final short STAT_LOADING=2002;//正在加载视频
	static final short STAT_DECODE=2003;//解码
	static final short STAT_STOP=2004;//停止
	static final short STAT_DISCONNECT=2005;//连接断开
	static final short STAT_RESTART=2006;//重新连接
	static final short STAT_MR_BUSY=2007;//重新连接
	static final short STAT_MR_DISCONNECT=2008;//重新连接9
	static final int HANDLE_MSG_CODE_LOGIN_RESULT = 0x10;// 消息处理标示吗：登录结果
	private int m_loginID = 0;
	private Button btn_mode;
	private String m_strName;
	private Button back_btn;
	private DeviceInfo deviceTest;
	private boolean mIsPlaying=false;
	private boolean bIsLeftPressed=false, bIsRightPressed=false, bIsUpPressed=false, bIsDownPressed=false;
	private ScaleGestureDetector mScaleGestureDetector = null;
	private Context context;
	int mScreenWidth = 0;//屏幕宽   
	int mScreenHeight = 0;//屏幕高 
	private LinearLayout ll_dot;
	//private AlarmAndPromptInfo alarm;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_vplay_monitor);
		Log.e("wxb", "-----------v camera open-----------------");
		ScrrenOrientation = Configuration.ORIENTATION_PORTRAIT;
		context = this;
		deviceTest = VList.getInstance().findById(getIntent().getIntExtra("id", 0));
		//alarm = DeviceAlarmAndPromptSetting.getAlarmAndPropmt(deviceTest);
		Log.e("数据", "开始");
		mIsPlaying = false;
		initView();
		new getAlarmAndPropmtThread(deviceTest).start();
	}
	AlarmAndPromptInfo info;
	private boolean ishasAlarmConfig;
	private boolean isAlarmSwitch;
	private boolean ishasVoiceSwitch;
	private boolean isVoicePromptsMainSwitch;
	private int nLanguage;

	private Handler.Callback mCallback = new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			if (msg.arg1 == HANDLE_MSG_CODE_LOGIN_RESULT) {// 是否来自登录结果的消息

				// 处理登录结果
				switch (msg.arg2) {
					case ResultCode.RESULT_CODE_SUCCESS: {// 登录成功
						Bundle data = msg.getData();
						_deviceParam = data.getParcelable("device_param");// data.putParcelable("device_param", _deviceParam);
						if(_deviceParam!=null){
							System.out.println("login ok: ");//add for test
							m_bReversePRI= _deviceParam.isbReversePRI();//data.getBoolean("reverse", false);//add  by luo 20141219
							startPlay();
						}
					}
					break;
					case ResultCode.RESULT_CODE_FAIL_SERVER_CONNECT_FAIL: {// 登录失败-服务器不在线
						Toast.makeText(mContext, "Login fail", Toast.LENGTH_SHORT).show();
					}
					break;
					case ResultCode.RESULT_CODE_FAIL_VERIFY_FAILED: {// 登录失败-用户名或密码错误
						Toast.makeText(mContext, "用户名或密码错误", Toast.LENGTH_SHORT).show();
					}
					break;
					case ResultCode.RESULT_CODE_FAIL_USER_NOEXIST: {// 登录失败-用户名或密码错误
						Toast.makeText(mContext, "用户名或密码错误", Toast.LENGTH_SHORT).show();
					}
					break;
					case ResultCode.RESULT_CODE_FAIL_PWD_ERROR: {// 登录失败-用户名或密码错误
						Toast.makeText(mContext, "用户名或密码错误", Toast.LENGTH_SHORT).show();
					}
					break;
					case ResultCode.RESULT_CODE_FAIL_OLD_VERSON: {
						Toast.makeText(mContext, "版本太旧", Toast.LENGTH_SHORT).show();
					}
					break;
					default:
						break;
				}

				return true;
			}

			if(msg.arg1 == 1)
			{
				loadingBar.setVisibility(View.VISIBLE);
			}
			else
			{
				loadingBar.setVisibility(View.GONE);
			}
			switch (msg.what) {
				case 2:
					info = (AlarmAndPromptInfo) msg.obj;
					ishasAlarmConfig = info.isHasAlarmConfig();
					isAlarmSwitch = info.isbMainAlarmSwitch();
					ishasVoiceSwitch = info.isbAlarmVoiceSwitch();
					isVoicePromptsMainSwitch = info.isbVoicePromptsMainSwitch();
					nLanguage = info.getnLanguage();
					Toast.makeText(context, getString(R.string.activity_editscene_set_success), Toast.LENGTH_SHORT).show();
					break;
				case 1:

					break;
				case 0:
					Bundle b = msg.getData();
					Log.e("摄像头", b.getBoolean("defence")+"app");
					if(b.getBoolean("defence")){
						iv_defence.setImageResource(R.drawable.zhzj_sxt_suoding);
					}else{
						iv_defence.setImageResource(R.drawable.zhzj_sxt_jiesuo);
					}
					break;
				default:
					break;
			}
			return false;
		}
	};
	private Handler handler = new WeakRefHandler(mCallback);

	private GestureDetector mGestureDetector;
	private void initView() {
		horizon_listview = (HorizontalListView) findViewById(R.id.horizon_listview);
		List<String> str = getSD();
		if(str!=null&&!str.isEmpty()){
			mAdapter = new HorizontalListViewAdapter(this,str,1);
		    horizon_listview.setAdapter(mAdapter);
		}
		back_btn = (Button) findViewById(R.id.back_btn);
		btn_mode = (Button) findViewById(R.id.btn_mode);
		screenshot = (ImageView) findViewById(R.id.screenshot);//全屏
		send_voice = (ImageView) findViewById(R.id.send_voice);//发言
		close_voice = (ImageView) findViewById(R.id.close_voice);//静音
		hungup = (ImageView) findViewById(R.id.hungup);//挂断
		ll_dot = (LinearLayout) findViewById(R.id.ll_dot);
		viewpager = (ViewPager) findViewById(R.id.viewpager);
		full_screen = (ImageView) findViewById(R.id.full_screen);//全屏
		iv_half_screen = (ImageView) findViewById(R.id.iv_half_screen);
		rl_tools = (RelativeLayout) findViewById(R.id.rl_tools);
		loadingBar = (ProgressBar) findViewById(R.id.spinner_0);//进度条
		dots = new ArrayList<View>();
		dots.add(findViewById(R.id.dot_1));
		dots.add(findViewById(R.id.dot_2));
		layout_title = (RelativeLayout) findViewById(R.id.layout_title);
		LayoutInflater inflater = getLayoutInflater();
//		view1 = inflater.inflate(R.layout.viewpager1, null);
//		view2 = inflater.inflate(R.layout.viewpager2, null);
		viewList = new ArrayList<View>();// 将要分页显示的View装入数组中
		viewList.add(view1);
		viewList.add(view2);
		 PagerAdapter pagerAdapter = new PagerAdapter() {

				@Override
				public boolean isViewFromObject(View arg0, Object arg1) {
					// TODO Auto-generated method stub
					return arg0 == arg1;
				}

				@Override
				public int getCount() {
					// TODO Auto-generated method stub
					return viewList.size();
				}

				@Override
				public void destroyItem(ViewGroup container, int position,
						Object object) {
					// TODO Auto-generated method stub
					container.removeView(viewList.get(position));
				}

				@Override
				public Object instantiateItem(ViewGroup container, int position) {
					// TODO Auto-generated method stub
					container.addView(viewList.get(position));

					return viewList.get(position);
				}
			};
		
		iv_voice = (ImageView) view2.findViewById(R.id.iv_vioce);
		iv_file = (ImageView) view2.findViewById(R.id.iv_file);
		iv_recode = (ImageView) view2.findViewById(R.id.iv_recode);
		iv_defence = (ImageView) view1.findViewById(R.id.iv_defence);
		iv_speak = (ImageView) view1.findViewById(R.id.iv_speak);
		iv_screenshot = (ImageView) view1.findViewById(R.id.iv_screenshot);
		rl = (RelativeLayout) findViewById(R.id.rl_monitor);
		/*if(alarm.isHasAlarmConfig()){
			iv_defence.setImageResource(R.drawable.portrait_arm_p);
		}else{
			iv_defence.setImageResource(R.drawable.portrait_arm);
		}*/
		iv_file.setOnClickListener(this);
		iv_defence.setOnClickListener(this);
		screenshot.setOnClickListener(this);
		iv_voice.setOnClickListener(this);  
		iv_speak.setOnTouchListener(this);
		iv_screenshot.setOnClickListener(this);
		iv_half_screen.setOnClickListener(this);
		hungup.setOnClickListener(this);
		close_voice.setOnClickListener(this);
		send_voice.setOnTouchListener(this);
		btn_mode.setOnClickListener(this);
		viewpager.setAdapter(pagerAdapter);
		dots.get(0).setBackgroundResource(R.drawable.about_bottom_p);
		viewpager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				// TODO Auto-generated method stub

				dots.get(oldPosition).setBackgroundResource(
						R.drawable.about_bottom);
				dots.get(position).setBackgroundResource(
						R.drawable.about_bottom_p);

				oldPosition = position;
				currentItem = position;

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
		m_strName = deviceTest.getStrName();
		
		
		//创建播放器
        mvMediaPlayer  = new NVMediaPlayer(getApplication(), nScreenOrientation, 0);
    	 mvMediaPlayer.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);      
        //添加播放器到容器
    	 rl.addView(mvMediaPlayer);
    	 SetGLViewPlayerMessageHandler();
    	 InitGLViewTouchEventEX();
    	  //	设置环境变量     
 	    LibContext.SetContext(mvMediaPlayer,null,null,null);  
 	   mGestureDetector = new GestureDetector(this, new PTZGestureListener(this));
       mScaleGestureDetector =new ScaleGestureDetector(this, new ScaleGestureListener());  
       full_screen.setOnClickListener(this);
       if(ishasAlarmConfig){
    	   iv_defence.setImageResource(R.drawable.zhzj_sxt_suoding);
       }else{
    	   iv_defence.setImageResource(R.drawable.zhzj_sxt_jiesuo);
       }
      
	}
	private void InitGLViewTouchEventEX() {
		if(rl==null)return;
		rl.setLongClickable(true);
		rl.setOnTouchListener(this);
	}
	public void SetGLViewPlayerMessageHandler()
	{
		if(mvMediaPlayer!=null){
			mvMediaPlayer.GetHandler(handler);
		}
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		OnPlayersResume();
		m_bFinish = false;
		this.loginDevice();
	}
	private void OnPlayersResume() {
		mvMediaPlayer.onResume();
	}
	class DeviceLoginThread extends Thread {
 		DeviceInfo info=null;
		public DeviceLoginThread(DeviceInfo info,int nLoginID){
			this.info = info;
		}

 
		public void run() {
			System.out.println("login :"+info.getnDevID()+", "+info.getStrDomain()+", "+info.getStrUsername()+", "+info.getStrPassword());//add for test
			
			LoginHandle deviceParam= null;
			if(info.getnSaveType() == Defines.SERVER_SAVE_TYPE_DEMO){
				_deviceParam = LoginHelper.getDeviceParam(info, info.getStrMRServer(), info.getnMRPort());
			}else{
				deviceParam = LoginHelper.getDeviceParam(info);
			}
			
			
			if(deviceParam!=null && deviceParam.getnResult()==ResultCode.RESULT_CODE_SUCCESS){
				Message msg = handler.obtainMessage();
				msg.arg1 = HANDLE_MSG_CODE_LOGIN_RESULT;
				msg.arg2 = ResultCode.RESULT_CODE_SUCCESS;
				Bundle data = new Bundle();
				data.putParcelable("device_param", deviceParam);
				msg.setData(data);
//				System.out.println("login result : "+deviceParam.isMRMode()+", "+deviceParam.getStrIP()+", "+deviceParam.getnPort());//add for test
				handler.sendMessage(msg);
			}else{
				if(deviceParam==null){
					Message msg = handler.obtainMessage();
					msg.arg1 = HANDLE_MSG_CODE_LOGIN_RESULT;
					msg.arg2 = ResultCode.RESULT_CODE_FAIL_SERVER_CONNECT_FAIL;
					
					handler.sendMessage(msg);
				}else{
					Message msg = handler.obtainMessage();
					msg.arg1 = HANDLE_MSG_CODE_LOGIN_RESULT;
					msg.arg2 = deviceParam.getnResult();

					handler.sendMessage(msg);
				}
			}
 
		}
	
	}
	
	private void loginDevice() {
		m_loginID++;
		new DeviceLoginThread(deviceTest, m_loginID).start();
	}
	 //停止播放
    private void stopPlay(boolean bFlag){
    	ptzTimerThreadID++;
    	 
    	//end add by luo 20141007
    	 
    	 mIsPlaying = false;
    	 if(mvMediaPlayer!=null){
    		 mvMediaPlayer.scale(1, 1);
   		     mvMediaPlayer.StopPlay();//add by luo 20141008
    	 }
    	 loadingBar.setVisibility(View.GONE);
    }
    
	//开始播放
    private void startPlay(){
    	 if(_deviceParam==null || mvMediaPlayer==null) return;
    		mvMediaPlayer.EnableRender(); 
    		mvMediaPlayer.StartPlay(0, 0, mStreamType, mPlaySound, _deviceParam);
    		mvMediaPlayer.setReverse(mIsReverse);//add by luo 20141219
			mvMediaPlayer.playAudio();
			ptzTimerThreadID++;
			mIsPlaying = true;
    }
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		 if(v==this.rl){
	 	    	mScaleGestureDetector.onTouchEvent(event);
	 	    	if(System.currentTimeMillis() - lScaleTime>500){
	 	    		mGestureDetector.onTouchEvent(event);
	 	    	}
		    	
		    	return true;
		    }else if(v==this.iv_speak||v==this.send_voice){
 	    		Log.e("摄像头", "开始说话");
	    		if(!mIsPlaying){
	    			return true;
	    		}
  
	    		switch(event.getAction())
				{
					case MotionEvent.ACTION_DOWN: 
						/*mBtnMic.setBackgroundResource(R.drawable.play_talkback_2);
						llPlayTalkback.setVisibility(View.VISIBLE);*/
						mIsSpeaking = true;
	 
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}	
						mvMediaPlayer.StartSpeak();
						
						break;
					case MotionEvent.ACTION_CANCEL: 
						mIsSpeaking = false; 
						
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
						mvMediaPlayer.StopSpeak();
						break;
					case MotionEvent.ACTION_MOVE: 
						mIsSpeaking = true;					 
						break;
					case MotionEvent.ACTION_UP:		 
						mIsSpeaking = false;	
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					mvMediaPlayer.StopSpeak();
						 
						break;
					default:break;
				}
	    		
	    		
				return true;
		    }
		return false;
	}
	//屏幕缩放手势处理器
		private float fScaleSize = 1.0f;
		private long lScaleTime = 0;
		private boolean isFullScreen;
		private boolean bAnyway;
		private PopupWindow popupWindow;
		class ScaleGestureListener implements ScaleGestureDetector.OnScaleGestureListener {


		 @Override
		 public boolean onScale(ScaleGestureDetector detector) 
		 {
			 System.out.println("Scale: "+detector.getScaleFactor()+", "+detector.getCurrentSpan());//add for test
			 
		              // TODO Auto-generated method stub
		              if(detector.getScaleFactor()>1){//放大
		            	  fScaleSize = fScaleSize-0.005f;
		            	  if(fScaleSize<0.2){
		            		  fScaleSize = 0.2f;
		            	  }else{
		            		  mvMediaPlayer.scale(fScaleSize, fScaleSize); 
		            	  }
		            	  
		              }else if(detector.getScaleFactor()<1){//缩小
		            	  fScaleSize = fScaleSize+0.025f;
		            	  if(fScaleSize>1){
		            		  fScaleSize = 1.0f;
		            	  }else{
		            		  mvMediaPlayer.scale(fScaleSize, fScaleSize); 
		            	  }
		              } 
		              lScaleTime = System.currentTimeMillis();
		              return false;
		}
		          @Override
		          public boolean onScaleBegin(ScaleGestureDetector detector) 
		         {
		             return true;
		         }
		 
		         @Override
		         public void onScaleEnd(ScaleGestureDetector detector) 
		         {
		           
		         }
		    
		      
		 }
		
		
		//触屏云台控制监控线程	
		class PTZGestureListener extends SimpleOnGestureListener {
			  
			 public static final int MOVE_TO_LEFT = 0;  
			 public static final int MOVE_TO_RIGHT =1;  
			 public static final int MOVE_TO_UP = 2;  
			 public static final int MOVE_TO_DOWN = 3;  
			  
			 
			 private int nStep = 0;  
			  
			 boolean bTouchLeft=false, bTouchRight=false,bTouchUp=false,bTouchDown=false;
	         double nVelocityX = 0;//水平移动的速度
	         double nMoveDistanceX = 0;//水平移动的距离
	         
	         double nVelocityY = 0;//垂直移动的速度
	         double nMoveDistanceY = 0;//垂直移动的距离
	         
			 float x1 = 0;  
			 float x2 = 0;  
			 float y1 = 0;  
			 float y2 = 0;  
			  PTZGestureListener(Context context) { 
			  }
		        @Override  
		        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,  
		                float distanceY) {  
	 
	 
			            
		            return false;  
		        }  
		      
		         
		        @Override  
		        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,  
		                float velocityY) {  
	 

		        	if(bIsLeftPressed || bIsRightPressed || bIsUpPressed || bIsDownPressed){
		        		return true;
		        	}
		             bTouchLeft=false;
		             bTouchRight=false;
		             bTouchUp=false;
		             bTouchDown=false;
		             nVelocityX = Math.abs(velocityX);//水平移动的速度
		             nMoveDistanceX = Math.abs(e1.getX() - e2.getX());//水平移动的距离
		            
		            nVelocityY = Math.abs(velocityY);//垂直移动的速度
		            nMoveDistanceY = Math.abs(e1.getY() - e2.getY());//垂直移动的距离
		            
		            if(nVelocityY>=nVelocityX){
		            	nVelocityX =0;
	            	}else{
	            		nVelocityY=0;
	            	}
		            
		            
		            if(nVelocityY < nVelocityX){
		            	nStep = 0;
		            	
		            	if(nMoveDistanceX>FLING_MIN_DISTANCE){
		            		nStep=1;
		            		if(nMoveDistanceX>FLING_MAX_DISTANCE){
		            			nStep = (int) (nMoveDistanceX/FLING_MAX_DISTANCE);
		            		}
		            	}
		            	if(nVelocityX > FLING_MIN_VELOCITY && nMoveDistanceX > FLING_MIN_DISTANCE){
			            	if(e1.getX()>e2.getX()){
			            		
			            		bTouchLeft=true;
			            	}else{
			            		bTouchRight=true;
			            	}
			            }
		            	
	            	}else if(nVelocityY > nVelocityX){

	            		nStep = 0;
		            	
		            	if(nMoveDistanceX>FLING_MIN_DISTANCE){
		            		nStep=1;
		            		if(nMoveDistanceX>FLING_MAX_DISTANCE){
		            			nStep = (int) (nMoveDistanceX/FLING_MAX_DISTANCE);
		            		}
		            	}
	            		
	            		if(nVelocityY > FLING_MIN_VELOCITY && nMoveDistanceY > FLING_MIN_DISTANCE){
	    	            	if(e1.getY()>e2.getY()){
	    	            		bTouchUp=true;
	    	            	}else{
	    	            		bTouchDown=true;
	    	            	}
	    	            	
	    	            }
	            	}else{
	            		
	            		if(nMoveDistanceY>=nMoveDistanceX){
	            			if(nVelocityY > FLING_MIN_VELOCITY && nMoveDistanceY > FLING_MIN_DISTANCE){
	        	            	if(e1.getY()>e2.getY()){
	        	            		bTouchUp=true;
	        	            	}else{
	        	            		bTouchDown=true;
	        	            	}
	        	            	
	        	            }
	                	}else{
	                		if(nVelocityX > FLING_MIN_VELOCITY && nMoveDistanceX > FLING_MIN_DISTANCE){
	    		            	if(e1.getX()>e2.getX()){
	    		            		//@@System.out.println("onFling: Left");
	    		            		bTouchLeft=true;
	    		            	}else{
	    		            		//@@System.out.println("onFling: Right");
	    		            		bTouchRight=true;
	    		            	}
	    		            }
	                	}
	    	            
	            	}
		            
		            if(nStep>5){
		            	nStep=5;
		            }
	 	            
	 	            mvMediaPlayer.SendPTZAction(bTouchLeft, bTouchRight, bTouchUp, bTouchDown, nStep);
		            return false;  
		        }  
		        /**
		         * 这个方法不同于onSingleTapUp，他是在GestureDetector确信用户在第一次触摸屏幕后，没有紧跟着第二次触摸屏幕，也就是不是“双击”的时候触发
		         * */
		        @Override
		        public boolean onSingleTapConfirmed(MotionEvent e) {
						Log.i("数据", "onSingleTapConfirmed"+rl_tools.getVisibility()+Configuration.ORIENTATION_LANDSCAPE); 
			        	if(rl_tools.getVisibility()==View.VISIBLE){
			        		hidTools();
							
						}else if(rl_tools.getVisibility()==View.GONE&&ScrrenOrientation == Configuration.ORIENTATION_LANDSCAPE){
							showTools();
							
						}     
		        	return false;
			  
			}

				

				
		}
			private void hidTools() {
				rl_tools.setVisibility(View.GONE);
				}
			private void showTools() {
				rl_tools.setVisibility(View.VISIBLE);
			}
			@Override
			    public void onConfigurationChanged(Configuration config)
			    {
			    	super.onConfigurationChanged(config);// 
			    	 
			    	 
			     	 
			    	DisplayMetrics dm = getResources().getDisplayMetrics();   
					
					mScreenWidth = dm.widthPixels;//屏幕宽   
					mScreenHeight = dm.heightPixels;//屏幕高 
			     	 
					if(config.orientation == Configuration.ORIENTATION_LANDSCAPE)
					{
						/*
						 * If the screen is switched from portait mode to landscape mode
						 */
						ShowLandscapeView();
						
					}
					else if(config.orientation == Configuration.ORIENTATION_PORTRAIT)
					{
						/*
						 * If the screen is switched from landscape mode to portrait mode
						 */
						ShowPortrailView();
					}
			      
		}
			//横屏显示
				private void ShowLandscapeView(){
					
					synchronized(this)
					{
						 bAnyway = false; //add by mai 2015-3-23	 				
					    int nWidth = mScreenWidth;
			   	        int nHeight = mScreenHeight;
			   	        double dWidth = nHeight*1.7777777;
			   	        if(dWidth<nWidth)nWidth=(int) dWidth;
			   	     
			   	  int height = (int) getResources().getDimension(
							R.dimen.p2p_monitor_bar_height);
					//setControlButtomHeight(height);
					
			   	     if(rl!=null){
			   	    	RelativeLayout.LayoutParams parames = new RelativeLayout.LayoutParams(
								LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
						 rl.setLayoutParams(parames);
					 }	
			   	     rl_tools.setVisibility(View.VISIBLE);		   	  
			   	     nScreenOrientation = Configuration.ORIENTATION_LANDSCAPE;   
			   	     mvMediaPlayer.onOreintationChange(nScreenOrientation);
			   	     full_screen.setVisibility(View.GONE);
			   	     layout_title.setVisibility(View.GONE);
			   	     ll_dot.setVisibility(View.GONE);
			   	     viewpager.setVisibility(View.GONE);
			   	     horizon_listview.setVisibility(View.GONE);
					}
					
				}
				public void setControlButtomHeight(int height) {
					LinearLayout.LayoutParams control_bottom_parames = new LinearLayout.LayoutParams(
							LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
					control_bottom_parames.height = height;
					rl_tools.setLayoutParams(control_bottom_parames);
				}
				//竖屏显示
				private void ShowPortrailView(){
					 
					synchronized(this)
					{
						if(mScreenWidth > mScreenHeight)
						{
							ShowLandscapeView();
						}else{
						//dip转化为px
			 			int padding_in_dp = 45;  // 6 dps
			 		    final float scale = getResources().getDisplayMetrics().density;
			 		    int padding_in_px = (int) (padding_in_dp * scale + 0.5f);
			 		    bAnyway = true; //add by mai 2015-3-23		 
						 int nWidth = mScreenWidth;
						 int nHeight = (int) (nWidth*0.95);	
						 //setControlButtomHeight(0);
						 rl_tools.setVisibility(View.GONE);
						 if(rl!=null){
							 RelativeLayout.LayoutParams parames = new RelativeLayout.LayoutParams(
										LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
								 rl.setLayoutParams(parames);
							 
						 }
						
			 			 nScreenOrientation = Configuration.ORIENTATION_PORTRAIT;
			 
			 			 this.mvMediaPlayer.onOreintationChange(nScreenOrientation);
			 			 full_screen.setVisibility(View.VISIBLE);
				   	     ll_dot.setVisibility(View.VISIBLE);
				   	     viewpager.setVisibility(View.VISIBLE);
				   	     horizon_listview.setVisibility(View.VISIBLE);
				   	     layout_title.setVisibility(View.VISIBLE);
						}
			 		}
				}
				
				 

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.full_screen:
				rl_tools.setVisibility(View.GONE);
				ScrrenOrientation = Configuration.ORIENTATION_LANDSCAPE;
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				break;
			case R.id.iv_vioce:
				if(mPlaySound){
					Log.e("摄像头", "停止声音");
		     		iv_voice.setImageResource(R.drawable.zhzj_sxt_jingyin);
		     		mvMediaPlayer.pauseAudio();
		     		mPlaySound = false;
		     		
		    	}else{
		    		Log.e("摄像头", "开始声音");
		    		iv_voice.setImageResource(R.drawable.zhzj_sxt_shengyin);
		    		mvMediaPlayer.playAudio();
		    		mPlaySound = true;
		    		
		    	}
				break;
			case R.id.close_voice:
				if(mPlaySound){
					Log.e("摄像头", "停止声音");
					close_voice.setImageResource(R.drawable.btn_call_sound_out_s);
		     		mvMediaPlayer.pauseAudio();
		     		mPlaySound = false;
		     		
		    	}else{
		    		Log.e("摄像头", "开始声音");
		    		close_voice.setImageResource(R.drawable.btn_call_sound_out);
		    		mvMediaPlayer.playAudio();
		    		mPlaySound = true;
		    		
		    	}
				break;
			case R.id.iv_screenshot:
				Log.e("摄像头", "点击截图");
				shotScreen();		
				break;
			case R.id.iv_half_screen:
				rl_tools.setVisibility(View.GONE);
				ScrrenOrientation = Configuration.ORIENTATION_PORTRAIT;
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				break;
			case R.id.hungup:
				finish();
				break;
			case R.id.screenshot:
				Log.e("摄像头", "点击截图");
				shotScreen();	
				break;
			case R.id.iv_defence:
				if(iv_defence.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.zhzj_sxt_jiesuo).getConstantState())){
					new setAlarmAndPropmtThread(deviceTest,true,isAlarmSwitch,ishasVoiceSwitch,isVoicePromptsMainSwitch,nLanguage).start();
					iv_defence.setImageResource(R.drawable.zhzj_sxt_suoding);
				}else{
					new setAlarmAndPropmtThread(deviceTest,false,isAlarmSwitch,ishasVoiceSwitch,isVoicePromptsMainSwitch,nLanguage).start();
					iv_defence.setImageResource(R.drawable.zhzj_sxt_jiesuo);
				}
				/*if(alarm.isHasAlarmConfig()){
					DeviceAlarmAndPromptSetting.setAlarmAndPropmt(deviceTest, false, false, false, false, false, 0, false, 1, false);
					iv_defence.setImageResource(R.drawable.portrait_arm);
				}else{
					DeviceAlarmAndPromptSetting.setAlarmAndPropmt(deviceTest, true, false, false, false, false, 0, false, 1, false);
					iv_defence.setImageResource(R.drawable.portrait_arm_p);
				}*/
				
				break;
			case R.id.iv_file:
				Log.e("摄像头", "图像倒置");
				 if(m_bReversePRI){
	 				 mvMediaPlayer.SetCamImageOrientation(Defines.NV_IPC_ORIENTATION_REVERSE);
				 }else{
					 mIsReverse=!mIsReverse;//add  by luo 20141124
					  mvMediaPlayer.setReverse(mIsReverse);
				 }
				//end modify by luo 20150106  
				break;
			case R.id.btn_mode:
				getPopupWindow();   
	            popupWindow.showAtLocation(v, Gravity.BOTTOM|Gravity.LEFT,0,2*btn_mode.getHeight());
				break;
			default:
				break;
			}
		}
		/** 
	     * 创建PopupWindow 
	     */  
	    protected void initPopuptWindow() {  
	        // TODO Auto-generated method stub  
	        // 获取自定义布局文件activity_popupwindow_left.xml的视图  
	        View popupWindow_view = getLayoutInflater().inflate(R.layout.activity_popwindow_top, null,  
	                false);  
	        TextView video_mode_hd = (TextView) popupWindow_view.findViewById(R.id.video_mode_hd);
	        TextView video_mode_sd = (TextView) popupWindow_view.findViewById(R.id.video_mode_sd);
	        // 创建PopupWindow实例,200,LayoutParams.MATCH_PARENT分别是宽度和高度  
	        popupWindow = new PopupWindow(popupWindow_view,LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);  
	        // 设置动画效果  
	        
	        //popupWindow.setAnimationStyle(R.style.AnimationFade);  
	        // 点击其他地方消失  
	        popupWindow_view.setOnTouchListener(new OnTouchListener() {  
	            @Override  
	            public boolean onTouch(View v, MotionEvent event) {  
	                // TODO Auto-generated method stub  
	                if (popupWindow != null && popupWindow.isShowing()) {  
	                    popupWindow.dismiss();  
	                    popupWindow = null;  
	                }  
	                return false;  
	            }  
	        });  
	        
	        video_mode_hd.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					onStreamTypeChange(1);
					btn_mode.setText(getString(R.string.video_mode_hd));
					popupWindow.dismiss();
				}
			});
	        video_mode_sd.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					onStreamTypeChange(0);
					btn_mode.setText(getString(R.string.video_mode_sd));
					popupWindow.dismiss();
				}
			});
	    }  
	  //流类型选择改变事件
	    private void onStreamTypeChange(int nType){
	    	if(mStreamType==nType)return;
	    	
	    	mStreamType = nType;
	    	if(mIsPlaying){
	      	  stopPlay(false);
		      	  try {
						Thread.sleep(200);
		      	  } catch (InterruptedException e) {
	 					e.printStackTrace();
		      	  }
		 
		      	  startPlay();
	    	 }
	    }
		/*** 
	     * 获取PopupWindow实例 
	     */  
	    private void getPopupWindow() {  
	        if (null != popupWindow) {  
	            popupWindow.dismiss();  
	            return;  
	        } else {  
	            initPopuptWindow();  
	        }  
	    }  
	    private HorizontalListViewAdapter mAdapter;
	    private List<String> pictrue;
	    private void shotScreen() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		Bitmap bm = mvMediaPlayer.Screenshot();
		String imagePath = Environment.getExternalStorageDirectory().toString() + "/v380/";//存放照片的文件夹
		String fileName = df.format(new Date())+".jpg";
		 File out = new File(imagePath);
         if (!out.exists()) {
                 out.mkdirs();
         }
         out = new File(imagePath, fileName);
         imagePath = imagePath + fileName;//该照片的绝对路径
         FileOutputStream fOut = null;		
 		try {  
 		            fOut = new FileOutputStream(out);  
 		    } catch (FileNotFoundException e) {  
 		            e.printStackTrace();  
 		    }  
 		    bm.compress(Bitmap.CompressFormat.JPEG, 100, fOut);  
 		    try {  
 		            fOut.flush();  
 		    } catch (IOException e) {  
 		            e.printStackTrace();  
 		    }  
 		    try {  
 		            fOut.close();  
 		    } catch (IOException e) {  
 		            e.printStackTrace();  
 		    } 
		    Toast.makeText(mContext, getString(R.string.activity_editscene_modify_success), Toast.LENGTH_SHORT).show();
		    getSD(); 
		    mAdapter = new HorizontalListViewAdapter(this,pictrue,1);
			horizon_listview.setAdapter(mAdapter);
		}
	 private List<String> getSD() {
         pictrue = new ArrayList<String>();
         File f = new File(Environment.getExternalStorageDirectory().toString() + "/v380/");
         if(!f.exists()){
        	 f.mkdirs();
         }
         File[] files = f.listFiles();
         for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (getImageFile(file.getPath()))
                	pictrue.add(file.getPath());
         }
         return pictrue;
	 }
	 @Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
				
					moveTaskToBack(true);
			
				return true;
			}
			return super.onKeyDown(keyCode, event);
		}
	//指定遍历文件类型
    private boolean getImageFile(String fName) {
           boolean re;
           String end = fName
                         .substring(fName.lastIndexOf(".") + 1, fName.length())
                         .toLowerCase();
           if (end.equals("jpg")) {
                  re = true;
           } else {
                  re = false;
           }
           return re;
    }
	@Override
	protected void onDestroy() {		
		stopPlay(true);
		mvMediaPlayer.StopPlay();
		mvMediaPlayer = null;
		rl = null;
		Intent in = new Intent();
		in.setAction(Constants.Action.REFRESH_DATA);
		sendBroadcast(in);
		super.onDestroy();
	}	 
	@Override
	protected void onStop() {
		System.out.println("onStop");//add for test
 		if(!m_bFinish){//如果是按下了home键导致的停止，就保存当期的数据
			LibContext.stopAll();//add by luo 20141219
		}else{
			LibContext.stopAll();//add by luo 20141219
			LibContext.ClearContext();
		}
 		System.out.println("onStop end");//add for test
		m_bFinish = true;
		super.onStop();
	}
	public class getAlarmAndPropmtThread extends Thread{
		DeviceInfo info;
		public getAlarmAndPropmtThread(DeviceInfo info) {
			this.info = info;
		}
		@Override
		public void run() {
			AlarmAndPromptInfo alarm = DeviceAlarmAndPromptSetting.getAlarmAndPropmt(info);
			Log.e("摄像头", alarm.isHasAlarmConfig()+"");
			if(alarm.getnResult()==ResultCode.RESULT_CODE_SUCCESS){
				Message msg = new Message();
				msg.what = 0;
				Bundle b = new Bundle();
				b.putBoolean("defence", alarm.isHasAlarmConfig());
				msg.setData(b);
				handler.sendMessage(msg);
			}
			
		}
	}
	
	public class setAlarmAndPropmtThread extends Thread{
		DeviceInfo deviceInfo;
		boolean ishasAlarmConfig;
		boolean isAlarmSwitch;
		boolean ishasVoiceSwitch;
		boolean isVoicePromptsMainSwitch;
		int nLanguage;
		public setAlarmAndPropmtThread(DeviceInfo deviceInfo,
				boolean ishasAlarmConfig, boolean isAlarmSwitch,
				boolean ishasVoiceSwitch, boolean isVoicePromptsMainSwitch,
				int nLanguage) {
			this.deviceInfo = deviceInfo;
			this.ishasAlarmConfig = ishasAlarmConfig;
			this.isAlarmSwitch = isAlarmSwitch;
			this.ishasVoiceSwitch= ishasVoiceSwitch;
			this.isVoicePromptsMainSwitch = isVoicePromptsMainSwitch;
			this.nLanguage = nLanguage;
		}

		@Override
		public void run() {
			AlarmAndPromptInfo info = DeviceAlarmAndPromptSetting.setAlarmAndPropmt(deviceInfo, ishasAlarmConfig, isAlarmSwitch, true, ishasVoiceSwitch,ishasVoiceSwitch,nLanguage,true, 1, isVoicePromptsMainSwitch);
			if(info.getnResult()==ResultCode.RESULT_CODE_SUCCESS){
				Message msg = new Message();
				msg.what = 2;
				msg.obj = info;
				handler.sendMessage(msg);
			}else{
				handler.sendEmptyMessage(1);
			}
		}
	}
}
