package com.smartism.znzk.activity.camera;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.macrovideo.sdk.custom.RecordFileInfo;
import com.macrovideo.sdk.media.LibContext;
import com.macrovideo.sdk.media.LoginHandle;
import com.macrovideo.sdk.media.NVMediaPlayer;
import com.macrovideo.sdk.media.Player;
import com.smartism.znzk.R;
import com.smartism.znzk.global.TempDefines;
import com.smartism.znzk.util.WeakRefHandler;

/**
 * 录像回放
 * @author 2016年08月08日  update by 王建
 *
 */
@SuppressLint("NewApi")
public class PlayBackV380Activity extends Activity implements View.OnClickListener, OnTouchListener , OnSeekBarChangeListener{
	
	public final static int PLAYING_STAT_STOP = -1;
	public final static int PLAYING_STAT_PAUSE = 0;
	public final static int PLAYING_STAT_PLAYING = 1;
	public static final int MSG_HIDE_PROGRESSBAR  = 0x00;
	public static final int MSG_SHOW_PROGRESSBAR  = 0x01;
	public static final int MSG_SET_SEEKBAR_VALUE = 0x02;
	public static final int MSG_HIDE_TOOLVIEW = 0x03;
	public static final int MSG_SCREENSHOT = 0x04;
	static final short SHOWCODE_LOADING=1001;//正在加载
	static final short SHOWCODE_NEW_IMAGE=1002;//新图片
	static final short SHOWCODE_VIDEO=1004;//提示
	static final short SHOWCODE_STOP=2001;//停止播放
	static final short SHOWCODE_HAS_DATA=3001;//有数据
	static final short STAT_CONNECTING=2001;//正在连接服务器
	static final short STAT_LOADING=2002;//正在加载视频
	static final short STAT_DECODE=2003;//解码
	static final short STAT_STOP=2004;//停止
	static final short STAT_DISCONNECT=2005;//连接断开
	static final short STAT_RESTART=2006;//重新连接
	static final short STAT_MR_BUSY=2007;//重新连接
	static final short STAT_MR_DISCONNECT=2008;//重新连接
	private LoginHandle deviceParam = null;
	private boolean mPlaySound=true;
	private ImageView mBtnBack, mBtnBackHorizontal; // alter by mai 2015-4-29
	private Button mBtnStopAndPlay, mBtnStopAndPlayHorizontal;// alter by mai 2015-4-29
	private Button mBtnSound, mBtnSoundHorizontal; // alter by mai 2015-4-29
 	private TextView mTVTopServer=null;
	private LinearLayout layoutTopBar=null;
  	private boolean mIsPlaying=false;
  	private boolean mIsToPlay=false;
	private LinearLayout layoutBottomBar;
	private LinearLayout layoutCenter=null;
    private boolean mIsOnDropUp=true;
    private boolean mIsFinish=false;
	private boolean m_bFinish = false;
	int mScreenWidth = 0;//屏幕宽   
	int mScreenHeight = 0;//屏幕高 
	private boolean mQLHD=true;//是否支持高清
	private int mStreamType=0;//当前播放的质量
	private TextView tvStartTime, tvStopTime, tvStartTimeHorizontal, tvStopTimeHorizontal;
	private Button btnLastFile, btnNextFile, btnCatpure;
	private Button btnLastFileHorizontal, btnNextFileHorizontal, btnCatpureHorizontal; // add by mai 2015-4-29
	private int listID = 0;
	private int nPlayerFileTime;
	private int nPlayerTime = 0;
	private Dialog iamgeViewDialog=null;
	private View iamgeViewConctentView=null;
	private Dialog screenshotDialog=null;
	Bitmap bm = null; 
	String folderName="iCamSeeImages";
	NVMediaPlayer mvMediaPlayer = null;
	LinearLayout container =null;
 	private View parentContainer  = null;
	private View backgroundContainer = null;
	private ProgressBar loadingBar = null;
	private ImageView img_v[] = new ImageView[Player.PLAYER_NUM];
	private int nScreenOrientation = Configuration.ORIENTATION_PORTRAIT;
	private Button btnRepeat = null;
	private SeekBar seekBarPlayProgress=null, seekBarPlayProgressHorizontal=null; // alter by mai 2015-4-29
	private RelativeLayout relativeLayoutCenter = null;
	private Activity relateAtivity=null;
	private LinearLayout bottomButtonHorizontal, bottomButton;
	private boolean bAnyway = true;
	private LinearLayout llVideoPalyBakc, llVideoPalyBakcHorizontal, llPlayerDevice;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 如果是返回键,直接返回到桌面
		if(keyCode == KeyEvent.KEYCODE_BACK ){
			if(mIsPlaying==false){
				m_bFinish = true;
				  stopPlay(false);
	 			  Intent intent = new Intent(PlayBackV380Activity.this, RecordFileActivity.class);
			      Bundle data = new Bundle(); 
			  	  data.putParcelable("login_handle", deviceParam);
				  data.putInt("play_index", listID);					 
			      intent.putExtras(data);
			      startActivity(intent);
				  finish();
				  return false;
			  }
			
			  new AlertDialog.Builder(PlayBackV380Activity.this)
				.setTitle("停止播放")
				.setIcon(R.drawable.ic_launcher)
				.setNegativeButton(getString(R.string.cancel), null)
				.setPositiveButton(getString(R.string.sure), new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int whichButton) {
					stopPlay(false);
				  mIsPlaying = false;
				  Intent intent = new Intent(PlayBackV380Activity.this, RecordFileActivity.class);
			      m_bFinish = true;
			      Bundle data = new Bundle(); 
			  	  data.putParcelable("login_handle", deviceParam);
				  data.putInt("play_index", listID);
					 
			      intent.putExtras(data); 
			      startActivity(intent);

			      finish();
				}

				}).show();
		}
		return false;
	}	
	@Override
	public void onPause(){
        if(mIsPlaying){
        	PausePlay();
        }
		OnPlayersPuase();
		super.onPause();
	}
	@Override
	public void onResume(){
		super.onResume();
		nToolsViewShowTickCount = 8;
 
		timerThreadID++;
		new TimerThread(timerThreadID).start();
		
		if(!mIsFinish){
			if(mIsPlaying){
				 ResumePlay();
		    }else{
		    	if(mIsToPlay){    		
		    		startPlay();
		    	}else{
		    		stopPlay(true);
		    	}
		    }
		}
    	OnPlayersResume();	 
		m_bFinish = false;
		NotificationManager notiManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);		
        notiManager.cancelAll();      
	}	
	@Override
	public void onDestroy(){
		mvMediaPlayer = null;
		super.onDestroy();
	}	
	@SuppressWarnings("deprecation")
	@Override
	public void onStop(){	 
		timerThreadID++;
		if(!m_bFinish){//如果是按下了home键导致的停止，就保存当期的数据
			LibContext.stopAll();
		}else{
			LibContext.stopAll();
			LibContext.ClearContext();
		}
		m_bFinish = true;
		super.onStop();
	}
	private void ShowLandscapeView(){
		synchronized(this)
		{
			nToolsViewShowTickCount=5;
			bAnyway = false; // add by mai 2015-4-29 
			showToolsViews(); 
		    int nWidth = mScreenWidth;
   	        int nHeight = mScreenHeight;
   	        double dWidth = nHeight*1.7777777;
   	        if(dWidth<nWidth)nWidth=(int) dWidth; 
   	     if(layoutCenter!=null){
			 RelativeLayout.LayoutParams rlp=new RelativeLayout.LayoutParams(nWidth, nHeight);
			 rlp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
			 layoutCenter.setLayoutParams(rlp);
			 layoutCenter.setPadding(0, 0, 0, 0);		 
		 }
   	     nScreenOrientation = Configuration.ORIENTATION_LANDSCAPE;
   	     mvMediaPlayer.onOreintationChange(nScreenOrientation);
 		}
 	}
	private void ShowPortrailView(){
 		synchronized(this)
		{
 			//dip转化为px
 			int padding_in_dp = 80;  // 6 dps
 		    final float scale = getResources().getDisplayMetrics().density;
 		    int padding_in_px = (int) (padding_in_dp * scale + 0.5f);
			bAnyway = true; // add by mai 2015-4-29 
 			showToolsViews();
			int nWidth = mScreenWidth;
			int nHeight = (int) (nWidth*0.8);
			if(layoutCenter!=null){
				 RelativeLayout.LayoutParams rlp=new RelativeLayout.LayoutParams(nWidth, nWidth);
				 rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
				 layoutCenter.setLayoutParams(rlp);
				 layoutCenter.setPadding(0, padding_in_px, 0, 0);
			 }
 			 nScreenOrientation = Configuration.ORIENTATION_PORTRAIT;
 			mvMediaPlayer.onOreintationChange(nScreenOrientation);
  		}
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
			ShowLandscapeView();
			
		}
		else if(config.orientation == Configuration.ORIENTATION_PORTRAIT)
		{
			ShowPortrailView();
		}
     }
    /**
     * 设置关闭按钮的先是与否
     * @param stat	true表示显示，false表示不显示
     */
    void SetCloseButtonVisible(boolean isVisible)
    {
    	if(isVisible)
    	{
	    	img_v[Player.WND_ID_0].setVisibility(View.VISIBLE);
	    	img_v[Player.WND_ID_1].setVisibility(View.VISIBLE);
	    	img_v[Player.WND_ID_2].setVisibility(View.VISIBLE);
	    	img_v[Player.WND_ID_3].setVisibility(View.VISIBLE);
    	}
    	else
    	{
	    	img_v[Player.WND_ID_0].setVisibility(View.GONE);
	    	img_v[Player.WND_ID_1].setVisibility(View.GONE);
	    	img_v[Player.WND_ID_2].setVisibility(View.GONE);
	    	img_v[Player.WND_ID_3].setVisibility(View.GONE);
    	}
    }
    
    /**
     * 创建 GLES2SurfaceView的实例
     */
    public void ReleaseGLViewPlayer()
    {
    	if(mvMediaPlayer!=null){
    		mvMediaPlayer.DisableRender();
        	mvMediaPlayer = null;
    	}
    	
    }
    
    public void OnPlayersPuase()
    {
    	if(mvMediaPlayer!=null){
    		mvMediaPlayer.onPause();
    	}
    	
    	 
    }
    
    public void OnPlayersResume()
    {
		mvMediaPlayer.onResume();
    }

	private Handler.Callback mCallback = new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			if(msg.arg1 == MSG_SCREENSHOT){
				return true;
			}
			if(msg.arg1 == MSG_HIDE_TOOLVIEW){
				if(nScreenOrientation== Configuration.ORIENTATION_LANDSCAPE){
					hideToolsViews();
				}
			}else if(msg.arg1==MSG_SET_SEEKBAR_VALUE){
				mIsOnDropUp=false;
				seekBarPlayProgress.setProgress(msg.arg2);
				seekBarPlayProgressHorizontal.setProgress(msg.arg2); // add by mai 2015-4-29
				nPlayerTime++;
				String strPlayer = null;
				if(nPlayerTime >= 60)
				{
					strPlayer = (nPlayerTime/60)+":"+(nPlayerTime%60);
				}else{
					strPlayer = "00:"+nPlayerTime;
				}
				tvStartTime.setText(strPlayer);
				tvStartTimeHorizontal.setText(strPlayer);  // add by mai 2015-4-29
			}
			return false;
		}
	};
	private Handler handler = new WeakRefHandler(mCallback);
	static final int SIZE_CMDPACKET=128;
	static final int SEND_BUFFER_SIZE=512;
	static final int SESSION_FRAME_BUFFER_SIZE=65536;
	static final int SEND_BUFFER_DATA_SIZE=504; // 512 - 8 (header)
	static final int SEND_BUFFER_HEADER_SIZE=8;
	static final int SP_DATA = 0x7f;
	static final int  CMD_REQUEST=0x9101;
	static final int  CMD_AFFIRM=0x9102;
	static final int  CMD_EXIT=0x9103;
	static final int  CMD_ACCEPT=0x9104;
	static final int  CMD_CONNECTINFO=0x9105;
	static final int  CMD_STREAMHEAD=0x9106;
	static final int  CMD_UDPSHAKE=0x9107;
	static final int  CMD_ASKFORCNLNUM=0x9108;
	static final int  CMD_CHECKPSW=0x9109;
	//获取图像
	//获取图像
	 /** Called when the activity is first created. */
   @SuppressWarnings("deprecation")
   @Override
   public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
	     getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
       System.out.println("onCreate 1");//add for test
       requestWindowFeature(Window.FEATURE_NO_TITLE);
       setContentView(R.layout.activity_nvplayer_playbackview);
       // 创建新的图片  
       DisplayMetrics dm = getResources().getDisplayMetrics();   
       mScreenWidth = dm.widthPixels;//屏幕宽   
       mScreenHeight = dm.heightPixels;//屏幕高 
     	init();
        Bundle data = this.getIntent().getExtras();
       if(data!=null){
    	 listID = data.getInt("play_index");
    	 deviceParam = data.getParcelable("login_handle"); 
    	 System.out.println("onCreate deviceParam :"+deviceParam.getlHandle());//add for test
       }
       mTVTopServer =(TextView)findViewById(R.id.tvPlayerDevice);
       layoutTopBar = (LinearLayout)findViewById(R.id.linearLayoutTopBar);
       layoutCenter =  (LinearLayout)findViewById(R.id.playbackContainer);
       layoutBottomBar = (LinearLayout)findViewById(R.id.linearLayoutBottomBar);
       relativeLayoutCenter = (RelativeLayout)findViewById(R.id.relativeLayoutCenter);
       parentContainer = findViewById(R.id.playbackContainerParent1);
   	   backgroundContainer = findViewById(R.id.playbackContainer1background);
   	   container = (LinearLayout)findViewById(R.id.playbackContainer1);
   	   Player.GetProgressBars((ProgressBar) findViewById(R.id.spinner_0), 
			Player.WND_ID_0);
       //创建播放器
       mvMediaPlayer  = new NVMediaPlayer(getApplication(), nScreenOrientation, 0);
   	   mvMediaPlayer.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
   	   mvMediaPlayer.GetHandler(handler);
       //添加播放器到容器
       container.addView(mvMediaPlayer);
       // 添加 OpenGL ES2.0表面到容器  		
       //	设置环境变量
       LibContext.SetContext(mvMediaPlayer,null,null,null);
       ShowPortrailView();
       Player.SelectWindow(Player.WND_ID_0);
	    btnRepeat =  (Button)findViewById(R.id.btnRepeat1);
	    btnRepeat.setOnClickListener(this);
	    btnRepeat.setVisibility(View.GONE);
   	mBtnBack = (ImageView)findViewById(R.id.btnPBBackToLogin);
   	mBtnBack.setOnClickListener(this);
   	
   	mBtnBackHorizontal = (ImageView)findViewById(R.id.btnPBBackToLoginHprizontal);
   	mBtnBackHorizontal.setOnClickListener(this);
   	
   	mBtnStopAndPlay = (Button)findViewById(R.id.btnPBStopAndPlay);
   	mBtnStopAndPlay.setOnClickListener(this);
   	mBtnStopAndPlayHorizontal = (Button)findViewById(R.id.btnPBStopAndPlayHorizontal);
   	mBtnStopAndPlayHorizontal.setOnClickListener(this);
   	
   	mBtnSound =  (Button)findViewById(R.id.btnPBAudio);
   	mBtnSound.setOnClickListener(this);
	mBtnSoundHorizontal =  (Button)findViewById(R.id.btnPBAudioHorizontal);
	mBtnSoundHorizontal.setOnClickListener(this);
   
   	seekBarPlayProgress = (SeekBar)findViewById(R.id.seekBarPlayProgress);
   	seekBarPlayProgress.setOnSeekBarChangeListener(this);
   	
   	seekBarPlayProgress.setMax(100);
   	seekBarPlayProgress.setProgress(0);
   	
   	seekBarPlayProgressHorizontal = (SeekBar)findViewById(R.id.seekBarPlayProgressHorizontal); // add by mai 2015-4-29
   	seekBarPlayProgressHorizontal.setOnSeekBarChangeListener(this); // add by mai 2015-4-29
   	
   	seekBarPlayProgressHorizontal.setMax(100); // add by mai 2015-4-29
   	seekBarPlayProgressHorizontal.setProgress(0); // add by mai 2015-4-29
   	
   	mIsPlaying = false;
   	mIsToPlay = true;
  
   }

   /**
    * 初始化数据
    */
   private void init()
   {
	   tvStartTime = (TextView) findViewById(R.id.tvStartTime);
	   tvStopTime = (TextView) findViewById(R.id.tvStopTime);
	   tvStartTimeHorizontal = (TextView) findViewById(R.id.tvStartTimeHorizontal); // add by mai 2015-4-29
	   tvStopTimeHorizontal = (TextView) findViewById(R.id.tvStopTimeHorizontal); // add by mai 2015-4-29
	   btnLastFile = (Button) findViewById(R.id.btnLastFlie);
	   btnLastFile.setOnClickListener(this);
	   btnLastFileHorizontal = (Button) findViewById(R.id.btnLastFlieHorizontal);
	   btnLastFileHorizontal.setOnClickListener(this);
	   
	   btnNextFile = (Button) findViewById(R.id.btnNextFile);
	   btnNextFile.setOnClickListener(this);
	   btnNextFileHorizontal = (Button) findViewById(R.id.btnNextFileHorizontal);
	   btnNextFileHorizontal.setOnClickListener(this);
	   
	   btnCatpure = (Button) findViewById(R.id.btnCatpure);
	   btnCatpure.setOnClickListener(this);
	   btnCatpureHorizontal = (Button) findViewById(R.id.btnCatpureHorizontal);
	   btnCatpureHorizontal.setOnClickListener(this);
	   
	   bottomButtonHorizontal = (LinearLayout) findViewById(R.id.bottomButtonHorizontal); // add by mai 2015-4-29
	   bottomButton = (LinearLayout) findViewById(R.id.bottomButton); //add by mai 2015-4-29
	   
	   llVideoPalyBakc = (LinearLayout) findViewById(R.id.llVideoPalyBakc); //add by mai 2015-4-29
	   llVideoPalyBakcHorizontal = (LinearLayout) findViewById(R.id.llVideoPalyBakcHorizontal); //add by mai 2015-4-29
	   llPlayerDevice = (LinearLayout) findViewById(R.id.llPlayerDevice); //add by mai 2015-4-29
   } 
  
    @SuppressWarnings("deprecation")
	private void stopPlay(boolean bFlag){
    	if(seekBarPlayProgress!=null){
    		seekBarPlayProgress.setProgress(0);
        	seekBarPlayProgress.setEnabled(false);
        
    	}
    	if(seekBarPlayProgressHorizontal!=null){ // add by mai 2015-4-29
        	seekBarPlayProgressHorizontal.setProgress(0); 
        	seekBarPlayProgressHorizontal.setEnabled(false); 
    	}
    	  mIsFinish=false;
    	  mIsPlaying = false;
    	  mTVTopServer.setText("PLAYBACK");
		  mvMediaPlayer.StopPlayBack();
		 
		  mvMediaPlayer.pauseAudio();
		  mIsFinish = true;
    }
@SuppressWarnings("deprecation")
private void startPlay(){

	mIsFinish=false;
	if(btnRepeat!=null){
		btnRepeat.setVisibility(View.GONE);
	}
     	if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
    		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
			synchronized(this)
			{
	 
			}
		} 
    	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    	mvMediaPlayer.pauseAudio();
		if(Player.CurrentSelPlayer() < 4)
		{
    		Player.setPlaying(Player.CurrentSelPlayer(), true);
    		mvMediaPlayer.EnableRender();
    		 seekBarPlayProgress.setProgress(0);
    		 seekBarPlayProgressHorizontal.setProgress(0);
    		 RecordFileInfo recFile=TempDefines.listMapPlayerBackFile.get(listID);
    		 if(recFile==null){
    			 return;
    		 }
    		 mTVTopServer.setText(recFile.getStrFileName()); 		 
    		nPlayerFileTime = recFile.getuFileTimeLen();
    		String strTime = "";
    		nPlayerTime = 0;
    		if(nPlayerFileTime >= 60)
    		{
    			strTime = (nPlayerFileTime/60)+":"+(nPlayerFileTime%60);
    		}else{
    			strTime = "00:"+ nPlayerFileTime;
    		}
    		tvStopTime.setText(strTime);
    		tvStopTimeHorizontal.setText(strTime); 
    		 if(mvMediaPlayer.StartPlayBack(0,  deviceParam,  recFile,mPlaySound)){
    		    mvMediaPlayer.playAudio();		 
 		    	mBtnStopAndPlay.setBackgroundDrawable(getResources().getDrawable(R.drawable.play_back_stop_btn));
 		    	mBtnStopAndPlayHorizontal.setBackgroundDrawable(getResources().getDrawable(R.drawable.play_back_stop_btn));
 		 		mIsPlaying = true;
 		    }

		} 
	    
		if(seekBarPlayProgress!=null){
        	seekBarPlayProgress.setEnabled(true);       	
    	}		
		if(seekBarPlayProgressHorizontal!=null){ 
        	seekBarPlayProgressHorizontal.setEnabled(true);       	
    	}
    }

	private void PausePlay(){
	
	}    
	private void ResumePlay(){
		
	}
	@SuppressWarnings("deprecation")
	public void onClick(View v) {
	     nToolsViewShowTickCount=5;
		 if(v==null)return;
		 switch (v.getId()) {
		    case R.id.btnRepeat1:
		    	stopPlay(true);
		    	startPlay();
		    	break;
		 	case R.id.btnPBBackToLogin:
		 		finish();
		 	case R.id.btnPBBackToLoginHprizontal:
		 		if(mIsPlaying==false){
					  stopPlay(false);
		 			  Intent intent = new Intent(PlayBackV380Activity.this, RecordFileActivity.class);
				      m_bFinish = true;
				      Bundle data = new Bundle(); 
				  	  data.putParcelable("login_handle", deviceParam);
					  data.putInt("play_index", listID);
				      intent.putExtras(data); 
				      startActivity(intent);
 					  PlayBackV380Activity.this.finish();
					  return;
				  }
				  new AlertDialog.Builder(PlayBackV380Activity.this)
					.setTitle("停止播放")
					.setIcon(R.drawable.ic_launcher)
					.setNegativeButton(getString(R.string.cancel), null)
					.setPositiveButton(getString(R.string.sure), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						stopPlay(false);
					  setResult(RESULT_OK);
					  mIsPlaying = false;
					  Intent intent = new Intent(PlayBackV380Activity.this, RecordFileActivity.class);
				      m_bFinish = true;
				     Bundle data = new Bundle(); 
				  	  data.putParcelable("login_handle", deviceParam);
					  data.putInt("play_index", listID);						 
				      intent.putExtras(data);  
				      startActivity(intent);  
				      finish();
					}
					}).show();
				  break;
	     	case R.id.btnPBStopAndPlay:
	     	case R.id.btnPBStopAndPlayHorizontal: // add by mai 2015-4-29
 	     		 mIsPlaying = !mIsPlaying;
	     		 if(mIsPlaying){
 	     			 startPlay();
	     		 }else{
	     			 stopPlay(true);
	     		 }
	     		break;
	     	case R.id.btnPBAudio:
	     	case R.id.btnPBAudioHorizontal: // add by mai 2015-4-29
	     		 mPlaySound = !mPlaySound;
	     		 	int nChn = Player.CurrentSelPlayer();
	     		 	if(nChn>=0 && nChn<4){
	     		 		mvMediaPlayer.SetAudioParam(mPlaySound);
	     		 	}
	     		break;
	     	case R.id.btnLastFlie:
	     	case R.id.btnLastFlieHorizontal: // add by mai 2015-4-29
	     		if((listID-1) >= 0)
	     		{ //如果上一个存在
	     			listID = listID-1;
	     			stopPlay(false);
	     			Runnable runn = new Runnable() {
						
						@Override
						public void run() {
							
							if(mIsFinish)
							{ //如果已经停止
								startPlay();
								
							}else{
								
								handler.postDelayed(this, 1000);
							}
							
						}
					};
					
					handler.postDelayed(runn, 1000);
	     		}else{
	     			//Toast.makeText(PlaybackActivity.this, getString(R.string.FileFirst), Toast.LENGTH_SHORT).show();
	     		}
	     		break;
	     	
	     	case R.id.btnNextFile:
	     	case R.id.btnNextFileHorizontal: // add by mai 2015-4-29	
	     		if(TempDefines.listMapPlayerBackFile!=null && (listID+1) < TempDefines.listMapPlayerBackFile.size())
	     		{ //如果上一个存在
	     			listID = listID+1;
	     			stopPlay(true);
	     			Runnable runn = new Runnable() {
						
						@Override
						public void run() {
							
							if(mIsFinish)
							{ //如果已经停止
								startPlay();
								
							}else{
								
								handler.postDelayed(this, 1000);
							}
							
						}
					};
					handler.postDelayed(runn, 1000);
	     		}else{
	     			//Toast.makeText(PlaybackActivity.this, getString(R.string.FileFinally), Toast.LENGTH_SHORT).show();
	     		}
	     		break;
	     		
	     	case R.id.btnCatpure:
	     	case R.id.btnCatpureHorizontal: // add by mai 2015-4-29	
	     		break;
	     	default:break;
		 }
 
 
	}
	@Override
	public boolean onTouch(View v, MotionEvent event) {
        return true;   
	}
	@SuppressWarnings("unused")
	private boolean piontsInView(float x, float y, View v){
		if(x>=v.getLeft() && x<=(v.getWidth()+v.getLeft()) && y>=v.getTop() && y<=(v.getHeight()+v.getTop())){
			return true;
		}
		return false;
	}
	@SuppressWarnings("deprecation")
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if(!mIsOnDropUp && mIsPlaying && progress>=100){
			btnRepeat.setVisibility(View.VISIBLE);
			mIsPlaying=false;
	    	mIsPlaying = false;
	    	mTVTopServer.setText("PLAYBACK");
			mvMediaPlayer.pauseAudio();
			mBtnStopAndPlay.setBackgroundDrawable(getResources().getDrawable(R.drawable.play_back_stop_btn));
			mBtnStopAndPlayHorizontal.setBackgroundDrawable(getResources().getDrawable(R.drawable.play_back_stop_btn));
			mvMediaPlayer.FinishPlayback();
			mIsFinish=true;
 		}
		mIsOnDropUp=true;
	}
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		int nValue = seekBar.getProgress();
		mvMediaPlayer.SetPlayIndex(nValue);		 
	}
	private void showToolsViews(){
		nToolsViewShowTickCount=5;
		layoutBottomBar.setVisibility(View.VISIBLE);
		layoutTopBar.setVisibility(View.VISIBLE);
		if(bAnyway) // 如果是竖屏
		{
			bottomButton.setVisibility(View.VISIBLE);
			bottomButtonHorizontal.setVisibility(View.GONE);
			llVideoPalyBakc.setVisibility(View.VISIBLE);
		    llVideoPalyBakcHorizontal.setVisibility(View.GONE);
			llPlayerDevice.setVisibility(View.VISIBLE);
		}else{
			bottomButtonHorizontal.setVisibility(View.VISIBLE);
			bottomButton.setVisibility(View.GONE);
			llVideoPalyBakc.setVisibility(View.GONE);
		    llVideoPalyBakcHorizontal.setVisibility(View.VISIBLE);
			llPlayerDevice.setVisibility(View.GONE);
		}
	}
	
	private void hideToolsViews(){
		nToolsViewShowTickCount=0;
		layoutBottomBar.setVisibility(View.GONE);
		layoutTopBar.setVisibility(View.GONE);
		bottomButtonHorizontal.setVisibility(View.GONE);
		bottomButton.setVisibility(View.GONE);
	}
		private int nToolsViewShowTickCount = 8;
		private int timerThreadID=0;
		class TimerThread extends Thread{
			int mThreadID=0;
			 public TimerThread(int nThreadID){
				 mThreadID = nThreadID;
			 }
			 public void run(){
				 while(mThreadID == timerThreadID){
					 
					 nToolsViewShowTickCount-=1;
			    	  if(nToolsViewShowTickCount<=0){
				    	  Message message = new Message();      
				    	  message.arg1 = MSG_HIDE_TOOLVIEW;     
				    	  handler.sendMessage(message); 
			    		  nToolsViewShowTickCount=0;
			    	  } 
			    	  
			    	 try {
						sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				 }
			 }
		}
}
