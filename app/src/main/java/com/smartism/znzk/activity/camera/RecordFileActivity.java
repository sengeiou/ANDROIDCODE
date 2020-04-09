package com.smartism.znzk.activity.camera;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.macrovideo.sdk.custom.DeviceInfo;
import com.macrovideo.sdk.custom.RecordFileInfo;
import com.macrovideo.sdk.defines.Defines;
import com.macrovideo.sdk.defines.ResultCode;
import com.macrovideo.sdk.media.LoginHandle;
import com.macrovideo.sdk.media.RecordFileHelper;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.global.TempDefines;
import com.smartism.znzk.global.VList;
import com.smartism.znzk.util.WeakRefHandler;

import java.util.ArrayList;
import java.util.Calendar;
/**
 * 录音文件播放
 * @author 2016年08月08日 update 王建
 *
 */
public class RecordFileActivity extends ActivityParentActivity implements OnItemClickListener{
	private ListView lv_record;
	private int nSearchChn = 0;
	private int nSearchType = Defines.FILE_TYPE_ALL;
	private static DeviceInfo info;
	private Button btn_back;
	boolean isInit = false;
	// 搜索年月日（日期）
	private short nYear = 2000;
	private short nMonth = 0;
	private short nDay = 0;
	// 搜索开始时分秒（时间）
	private short nStartHour = 0;
	private short nStartMin = 0;
	private short nStartSec = 0;
	// 搜索结束时分秒（时间）
	private short nEndHour = 23;
	private short nEndMin = 59;
	private short nEndSec = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recorde_file);
		Calendar calendar = Calendar.getInstance();
		// 搜索年月日（日期）
		nYear = (short) calendar.get(Calendar.YEAR);
		nMonth = (short) calendar.get(Calendar.MONTH);
		nDay = (short) calendar.get(Calendar.DAY_OF_MONTH);
		// 搜索开始时分秒（时间）
		nStartHour = 0;
		nStartMin = 0;
		nStartSec = 0;
		// 搜索结束时分秒（时间）
		nEndHour = 23;
		nEndMin = 59;
		nEndSec = 0;
		if((VList.getInstance().findById(getIntent().getIntExtra("deviceid", 0)))!=null){
			info = VList.getInstance().findById(getIntent().getIntExtra("deviceid", 0));
		}
		
		initview();
	}
	private static final int  HANDLE_MSG_CODE_SEARCH = 0x8001;
	private static final int  HANDLE_MSG_CODE_SEARCH_RESULT = 0x8002;
	private static final int SEARCH_RESULT_START = 0x2001;
	private static final int SEARCH_RESULT_ENDT = 0x2002;
	private ArrayList<RecordFileInfo> fileList = new ArrayList<RecordFileInfo>();

	private Handler.Callback mCallback = new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			if (msg.arg1 == HANDLE_MSG_CODE_SEARCH_RESULT) {//获取句柄失败
				// 处理登录结果
				switch (msg.arg2) {


					case ResultCode.RESULT_CODE_FAIL_SERVER_CONNECT_FAIL: {// 登录失败-服务器不在线
					/*ShowAlert(
							getString(R.string.alert_title_login_failed)
									+ "Login fail","");*/
					}
					break;
					case ResultCode.RESULT_CODE_FAIL_VERIFY_FAILED: {// 登录失败-用户名或密码错误
					/*ShowAlert(getString(R.string.alert_title_login_failed),
							getString(R.string.notice_Result_VerifyFailed));*/
					}
					break;
					case ResultCode.RESULT_CODE_FAIL_USER_NOEXIST: {// 登录失败-用户名或密码错误
				/*	ShowAlert(getString(R.string.alert_title_login_failed),
							getString(R.string.notice_Result_UserNoExist));*/
					}
					break;
					case ResultCode.RESULT_CODE_FAIL_PWD_ERROR: {// 登录失败-用户名或密码错误
					/*ShowAlert(getString(R.string.alert_title_login_failed),
							getString(R.string.notice_Result_PWDError));*/
					}
					break;
					case ResultCode.RESULT_CODE_FAIL_OLD_VERSON: {
					/*ShowAlert(getString(R.string.alert_title_login_failed),
							getString(R.string.notice_Result_Old_Version));*/
					}
					break;
					default:
					/*ShowAlert(
							getString(R.string.alert_title_login_failed)
									+ "  ("
									+ getString(R.string.notice_Result_ConnectServerFailed)
									+ ")","");*/
						break;
				}
			} else if(msg.arg1 == HANDLE_MSG_CODE_SEARCH) {//搜索文件
				System.out.println("HANDLE_MSG_CODE_SEARCH ");//add for test
				if(msg.arg2==SEARCH_RESULT_ENDT){
					System.out.println("SEARCH_RESULT_ENDT "+fileList.size());//add for test
					//searchingProgressBar.setVisibility(View.GONE);
					if (fileList==null || fileList.size() <= 0) {
						// 隐藏列表
							/*isListVisible = false;
							layoutSearchParam.setVisibility(View.VISIBLE);
							layoutRecFileList.setVisibility(View.GONE); */
					}
				}
			} else if (msg.arg1 == Defines.HANDLE_MSG_CODE_RECORD_FILES_RECV) {

				System.out.println("HANDLE_MSG_CODE_RECORD_FILES_RECV ");//add for test
				Bundle data  = msg.getData();
				if(data==null){
					return true;
				}
				ArrayList<RecordFileInfo> recList= data.getParcelableArrayList(Defines.RECORD_FILE_RETURN_MESSAGE);
				System.out.println("HANDLE_MSG_CODE_RECORD_FILES_RECV "+fileList.size());//add for test
				if(recList!=null && recList.size()>0){

//					for(int i=0; i<recList.size(); i++)
//					{
//
//						fileList.add(recList.get(i));
//					}
					fileList.addAll(recList);
					mAdapter.notifyDataSetChanged();
					Log.e("摄像头", fileList.get(0).getStrFileName());
				}

				/*if (isActive) {
					try {
						refleshRecFileList();
					} catch (Exception e) {

					}

				}
*/
			}
			return false;
		}
	};
	private Handler defaultHandler = new WeakRefHandler(mCallback);

	private MyAdapter mAdapter;
	private void initview() {
		lv_record = (ListView) findViewById(R.id.lv_record);
		btn_back = (Button) findViewById(R.id.btn_back);
		mAdapter = new MyAdapter();
		lv_record.setAdapter(mAdapter);
		lv_record.setOnItemClickListener(this);
		btn_back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	class MyAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return fileList.size();
		}

		@Override
		public Object getItem(int position) {
			return fileList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if(convertView==null){
				holder = new ViewHolder();
				convertView = getLayoutInflater().inflate(R.layout.activity_record_file_item, null);
				holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tv_name.setText(fileList.get(position).getStrFileName());
			return convertView;
		}
		class ViewHolder{
			TextView tv_name;
		}
		
	}
	private LoginHandle login;
	public class getRecordOpThead extends Thread{
		@Override
		public void run() {
			//start
			Message msg = defaultHandler.obtainMessage();
			msg.arg1 = HANDLE_MSG_CODE_SEARCH;
			msg.arg2 = SEARCH_RESULT_START;
			defaultHandler.sendMessage(msg);	 
			login = RecordFileHelper.getRecordOPHandle(info);		
			if(login!=null && login.getnResult()==ResultCode.RESULT_CODE_SUCCESS){		
				int nResult = RecordFileHelper.getRecordFiles(login, defaultHandler, nSearchChn, nSearchType, nYear, nMonth, nDay, nStartHour, nStartMin, nStartSec, nEndHour, nEndMin, nEndSec); 
				System.out.println("**************FU");//add for test
			}else{
				if(login!=null){
					msg = defaultHandler.obtainMessage();
					msg.arg1 = HANDLE_MSG_CODE_SEARCH_RESULT;
					msg.arg2 = login.getnResult();
					defaultHandler.sendMessage(msg);
				}else{
					msg = defaultHandler.obtainMessage();
					msg.arg1 = HANDLE_MSG_CODE_SEARCH_RESULT;
					msg.arg2 = ResultCode.RESULT_CODE_FAIL_COMMUNICAT_FAIL;
					defaultHandler.sendMessage(msg);
				}
				
				return;
			
			}
			
			//end
			msg = defaultHandler.obtainMessage();
			msg.arg1 = HANDLE_MSG_CODE_SEARCH;
			msg.arg2 = SEARCH_RESULT_ENDT;
			 
			defaultHandler.sendMessage(msg);
 
	
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		fileList.clear();
		new getRecordOpThead().start();
	}
	private void StartPlayFile(int nIndex) {
		 
		Intent intent = new Intent(this,
				CameraPlayBackActivity.class);
		Bundle data = new Bundle(); 
		data.putParcelable("login_handle", login);
		data.putInt("play_index", nIndex);
		intent.putExtras(data);
		startActivity(intent);
		finish();
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (parent.getId() == R.id.lv_record) {
			Log.e("摄像头", "被点击了");
			if (position >= 0 && position < fileList.size()) {		 
				TempDefines.listMapPlayerBackFile = fileList;			
				StartPlayFile(position);
			}
		}
	}
}
