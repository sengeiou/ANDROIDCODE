package com.smartism.znzk.activity.camera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.macrovideo.sdk.custom.DeviceInfo;
import com.p2p.core.P2PHandler;
import com.p2p.core.P2PValue;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.FragmentParentActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.domain.camera.RecordFile;
import com.smartism.znzk.fragment.AlarmControlFrag;
import com.smartism.znzk.fragment.MainControlFrag;
import com.smartism.znzk.fragment.NetControlFrag;
import com.smartism.znzk.fragment.PlayBackFrag;
import com.smartism.znzk.fragment.RecordControlFrag;
import com.smartism.znzk.fragment.RecordFilesFrag;
import com.smartism.znzk.fragment.SecurityControlFrag;
import com.smartism.znzk.fragment.TimeControlFrag;
import com.smartism.znzk.fragment.UtilsFrag;
import com.smartism.znzk.fragment.VideoControlFrag;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.global.VList;
import com.smartism.znzk.util.camera.T;
import com.smartism.znzk.widget.HeaderView;
import com.smartism.znzk.widget.NormalDialog;

/**
 * 设备设置
 *
 * @author 王建
 */
public class MainControlActivity extends BaseActivity implements
        OnClickListener {
    public static final int FRAG_MAIN = 0;
    public static final int FRAG_TIME_CONTROL = 1;
    public static final int FRAG_REMOTE_CONTROL = 2;
    public static final int FRAG_UTILS_CONTROL = 3;
    public static final int FRAG_PLAYBACK_CAMERALIST = 4;
    public static final int FRAG_ALARM_CONTROL = 5;
    public static final int FRAG_VIDEO_CONTROL = 6;
    public static final int FRAG_RECORD_CONTROL = 7;
    public static final int FRAG_SECURITY_CONTROL = 8;
    public static final int FRAG_PLAY_BACK_CAMERA = 9;
    public static final int FRAG_NET_CONTROL = 10;
    public static final int FRAG_SD_CARD_CONTROL = 11;
    public static final int FRAG_LANGUAGE_CONTROL = 12;
    public static final int FRAG_SMART_DEVICE = 13;
    public static final int FRAG_MODIFY_WIFIPWD_CONTROL = 14;


    private ImageView back;
    private TextView contactName;
    private NormalDialog dialog;
    boolean isCancelCheck = false;
    boolean isCancelDoUpdate = false;
    public int current_frag = -1;

    private int connectType = 0;

    private String[] fragTags = new String[]{"mainFrag", "timeFrag",
            "remoteFrag", "loadFrag", "faultFrag", "alarmFrag", "videoFrag",
            "recordFrag", "securityFrag", "netFrag", "defenceAreaFrag",
            "sdCardFrag", "languegeFrag", "smartDeviceFrag", "modifyWifiFrag"};
    boolean isRegFilter = false;
    private int device_type;
    private Contact contact;
    MainControlFrag mainFrag;
    NetControlFrag netFrag;
    AlarmControlFrag alarmFrag; //报警设置
    VideoControlFrag videoFrag; //录像设置
    UtilsFrag utilsFrag;
    SecurityControlFrag securityFrag;//安全设置
    RecordFilesFrag recordFilesFrag;//录像列表
    PlayBackFrag playBackFrag;//暂时用不到，空的
    RecordControlFrag recordFrag;//录像设置
    TimeControlFrag timeFrag;//时间设置
    HeaderView header_img;
    Button viewDeviceVersionBtn;
    TextView tv_setting;
    String device;
    int device_id;
    String idOrIp = "";
    DeviceInfo info;
    public com.smartism.znzk.domain.DeviceInfo deviceInfo;
    int languegecount;
    int curlanguege;
    int[] langueges;

    public Context mContext;

    @Override
    protected void onCreate(Bundle arg0) {
        // TODO Auto-generated method stub
        super.onCreate(arg0);
        setContentView(R.layout.activity_control_main);
        contact = (Contact) getIntent().getSerializableExtra("contact");
        if (contact != null) {
            idOrIp = contact.contactId;
            if (contact.ipadressAddress != null && !contact.ipadressAddress.equals("")) {
                String mark = contact.ipadressAddress.getHostAddress();
                idOrIp = mark.substring(mark.lastIndexOf(".") + 1,
                        mark.length());
            }
        }
        device_type = getIntent().getIntExtra("type", -1);
        mContext = this;
        connectType = getIntent().getIntExtra("connectType", 0);
        deviceInfo = (com.smartism.znzk.domain.DeviceInfo) getIntent().getSerializableExtra("deviceInfo");
        device = getIntent().getStringExtra("device");
        device_id = getIntent().getIntExtra("deviceid", 0);
        if (device_id != 0) {
            info = VList.getInstance().findById(device_id);
        }

        initComponent();
        regFilter();
        replaceFragment(FRAG_MAIN, false, true);
    }

    public void initComponent() {
        tv_setting = (TextView) findViewById(R.id.tv_setting);
        viewDeviceVersionBtn = (Button) findViewById(R.id.viewDeviceVersionBtn);
        contactName = (TextView) findViewById(R.id.contactName);
        header_img = (HeaderView) findViewById(R.id.header_img);
        if (contact != null) {
            header_img.updateImage(contact.contactId, false);
        }
        back = (ImageView) findViewById(R.id.back_btn);
        back.setOnClickListener(this);
        viewDeviceVersionBtn.setOnClickListener(this);
        if (device != null && device.equals("v380")) {
            contactName.setText(info.getnDevID() + "");
        } else {
            contactName.setText(contact.contactName);
            if (contact.contactType == P2PValue.DeviceType.NPC) {
                viewDeviceVersionBtn.setVisibility(RelativeLayout.GONE);
            } else {
                viewDeviceVersionBtn.setVisibility(RelativeLayout.VISIBLE);
            }
        }

    }

    public void regFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(FINISH_MIANCONTR);
        filter.addAction(Constants.Action.PLAY_BACK_CAMERA);
        filter.addAction(Constants.Action.FRAG_PLAYBACK_CAMERALIST);
        filter.addAction(Constants.Action.REPLACE_SETTING_TIME);
        filter.addAction(Constants.Action.REPLACE_UTILS_CONTROL);
        filter.addAction(Constants.Action.REPLACE_ALARM_CONTROL);
        filter.addAction(Constants.Action.REPLACE_REMOTE_CONTROL);
        filter.addAction(Constants.Action.REFRESH_CONTANTS);
        filter.addAction(Constants.Action.REPLACE_VIDEO_CONTROL);
        filter.addAction(Constants.Action.REPLACE_RECORD_CONTROL);
        filter.addAction(Constants.Action.REPLACE_SECURITY_CONTROL);
        filter.addAction(Constants.Action.REPLACE_NET_CONTROL);
        filter.addAction(Constants.Action.REPLACE_DEFENCE_AREA_CONTROL);
        filter.addAction(Constants.Action.REPLACE_SD_CARD_CONTROL);
        filter.addAction(Constants.Action.REPLACE_MAIN_CONTROL);
        filter.addAction(Constants.Action.REPLACE_LANGUAGE_CONTROL);
        filter.addAction(Constants.Action.REPLACE_MODIFY_WIFI_PWD_CONTROL);
        filter.addAction(Constants.Action.CONTROL_SETTING_PWD_ERROR);
        filter.addAction(Constants.P2P.ACK_RET_GET_DEVICE_INFO);
        filter.addAction(Constants.P2P.RET_GET_DEVICE_INFO);
        filter.addAction(Constants.Action.CONTROL_BACK);
        filter.addAction(Constants.P2P.RET_DEVICE_NOT_SUPPORT);
        filter.addAction(Constants.P2P.RET_GET_SENSOR_WORKMODE);
        this.registerReceiver(mReceiver, filter);
        isRegFilter = true;
    }
    boolean isPause = false;
    @Override
    protected void onPause() {
        super.onPause();
        isPause = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPause = false;
    }

    public static final String FINISH_MIANCONTR = MainApplication.app.getPackageName() + "FINISH_MIANCONTR";

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            boolean isEnforce = intent.getBooleanExtra("isEnforce", false);

            if (intent.getAction().equals(FINISH_MIANCONTR)) {
                finish();
            } else if (intent.getAction().equals(
                    Constants.Action.CONTROL_SETTING_PWD_ERROR)) {
                T.showShort(mContext, getString(R.string.password_error));
                finish();
            } else if (intent.getAction().equals(
                    Constants.Action.REFRESH_CONTANTS)) {
                Contact c = (Contact) intent.getSerializableExtra("contact");
                if (null != c) {
                    contact = c;
                    contactName.setText(contact.contactName);
                }
            } else if (intent.getAction().equals(
                    Constants.Action.PLAY_BACK_CAMERA)) {
                //录像回放界面
//				recordFile = (RecordFile) intent.getSerializableExtra("file");
//				if (recordFile==null) return;
//				tv_setting.setText("录像回放界面");
//				replaceFragment(FRAG_PLAY_BACK_CAMERA, true, true);
            } else if (intent.getAction().equals(
                    Constants.Action.FRAG_PLAYBACK_CAMERALIST)) {
                //回放列表
                tv_setting.setText(getResources().getString(R.string.record_file_list));
                replaceFragment(FRAG_PLAYBACK_CAMERALIST, true, true);
            } else if (intent.getAction().equals(
                    Constants.Action.REPLACE_MAIN_CONTROL)) {
                replaceFragment(FRAG_MAIN, true, true);
            } else if (intent.getAction().equals(
                    Constants.Action.REPLACE_SETTING_TIME)) {
                tv_setting.setText(R.string.time_set);
                replaceFragment(FRAG_TIME_CONTROL, true, isEnforce);
            } else if (intent.getAction().equals(
                    Constants.Action.REPLACE_UTILS_CONTROL)) {
                tv_setting.setText(R.string.network_shot);
                Log.e("MainControl", "screen_shot2");
                replaceFragment(FRAG_UTILS_CONTROL, true, isEnforce);
            } else if (intent.getAction().equals(
                    Constants.Action.REPLACE_NET_CONTROL)) {
                tv_setting.setText(R.string.network_set);
                replaceFragment(FRAG_NET_CONTROL, true, isEnforce);
            } else if (intent.getAction().equals(
                    Constants.Action.REPLACE_ALARM_CONTROL)) {
                tv_setting.setText(R.string.alarm_set);
                replaceFragment(FRAG_ALARM_CONTROL, true, isEnforce);
            } else if (intent.getAction().equals(
                    Constants.Action.REPLACE_VIDEO_CONTROL)) {
                tv_setting.setText(R.string.media_set);
                replaceFragment(FRAG_VIDEO_CONTROL, true, isEnforce);
            } else if (intent.getAction().equals(
                    Constants.Action.REPLACE_SECURITY_CONTROL)) {
                tv_setting.setText(R.string.security_set);
                replaceFragment(FRAG_SECURITY_CONTROL, true, isEnforce);
            } else if (intent.getAction().equals(
                    Constants.Action.REPLACE_REMOTE_CONTROL)) {
                replaceFragment(FRAG_REMOTE_CONTROL, true, isEnforce);
            } else if (intent.getAction().equals(
                    Constants.Action.REPLACE_SD_CARD_CONTROL)) {
                tv_setting.setText(R.string.sd_card_set);
                replaceFragment(FRAG_SD_CARD_CONTROL, true, isEnforce);
            } else if (intent.getAction().equals(
                    Constants.Action.REPLACE_RECORD_CONTROL)) {
                tv_setting.setText(R.string.video_set);
                replaceFragment(FRAG_RECORD_CONTROL, true, isEnforce);
            } else if (intent.getAction().equals(
                    Constants.Action.REPLACE_LANGUAGE_CONTROL)) {
                languegecount = intent.getIntExtra("languegecount", -1);
                curlanguege = intent.getIntExtra("curlanguege", -1);
                langueges = intent.getIntArrayExtra("langueges");
                tv_setting.setText(R.string.language_set);
                replaceFragment(FRAG_LANGUAGE_CONTROL, true, isEnforce);
            } else if (intent.getAction().equals(
                    Constants.Action.REPLACE_MODIFY_WIFI_PWD_CONTROL)) {
                tv_setting.setText(R.string.set_wifi_pwd);
                replaceFragment(FRAG_MODIFY_WIFIPWD_CONTROL, true, isEnforce);

            } else if (intent.getAction().equals(
                    Constants.P2P.ACK_RET_GET_DEVICE_INFO)) {
                int result = intent.getIntExtra("result", -1);
                if (result == Constants.P2P_SET.ACK_RESULT.ACK_PWD_ERROR) {
                    if (null != dialog) {
                        dialog.dismiss();
                        dialog = null;
                    }
                    T.showShort(mContext, R.string.password_error);
                } else if (result == Constants.P2P_SET.ACK_RESULT.ACK_NET_ERROR) {
                    Log.e("my", "net error resend:get device info");
                    P2PHandler.getInstance().getDeviceVersion(idOrIp,
                            contact.contactPassword,MainApplication.GWELL_LOCALAREAIP);
                }
            } else if (intent.getAction().equals(
                    Constants.P2P.RET_GET_DEVICE_INFO)) {
                if (isPause) return;//跳转到修改密码，查询信息，防止这个界面也弹出
                int result = intent.getIntExtra("result", -1);
                String cur_version = intent.getStringExtra("cur_version");
                int iUbootVersion = intent.getIntExtra("iUbootVersion", 0);
                int iKernelVersion = intent.getIntExtra("iKernelVersion", 0);
                int iRootfsVersion = intent.getIntExtra("iRootfsVersion", 0);
                if (null != dialog) {
                    dialog.dismiss();
                    dialog = null;
                }

                if (isCancelCheck) {
                    return;
                }

                NormalDialog deviceInfo = new NormalDialog(mContext);
                deviceInfo.showDeviceInfoDialog(cur_version,
                        String.valueOf(iUbootVersion),
                        String.valueOf(iKernelVersion),
                        String.valueOf(iRootfsVersion));
            } else if (intent.getAction().equals(Constants.Action.CONTROL_BACK)) {
                tv_setting.setText(R.string.device_set);
            } else if (intent.getAction().equals(Constants.P2P.RET_GET_SENSOR_WORKMODE)) {
                byte boption = intent.getByteExtra("boption", (byte) -1);
                isEnforce = true;
                if (dialog != null && dialog.isShowing() && current_frag == FRAG_MAIN) {
                    dialog.dismiss();
                    dialog = null;
                    tv_setting.setText(R.string.defense_zone_set);
                    replaceFragment(FRAG_SMART_DEVICE, true, isEnforce);
                }
                //获取传感器防护计划返回
                if (boption == Constants.FishBoption.MESG_GET_OK) {

                } else {
                    T.showLong(mContext, "获取错误");
                }
            }

        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            back();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        switch (view.getId()) {
            case R.id.back_btn:
                back();
                break;
            case R.id.viewDeviceVersionBtn:
                if (null != dialog && dialog.isShowing()) {
                    Log.e("my", "isShowing");
                    return;
                }
                dialog = new NormalDialog(mContext);
                dialog.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface arg0) {
                        // TODO Auto-generated method stub
                        isCancelCheck = true;
                    }

                });
                dialog.setTitle(mContext.getResources().getString(
                        R.string.device_info));
                dialog.showLoadingDialog2();
                dialog.setCanceledOnTouchOutside(false);
                isCancelCheck = false;
                P2PHandler.getInstance().getDeviceVersion(idOrIp,
                        contact.contactPassword,MainApplication.GWELL_LOCALAREAIP);
                break;
        }

    }

    public void back() {
        if (current_frag != FRAG_MAIN) {
            replaceFragment(FRAG_MAIN, true, true);
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    public boolean isReplace(int type, boolean isEnforce) {
        if (isEnforce || current_frag != FRAG_MAIN) {
            return true;
        } else {
            return false;
        }
    }

    public void replaceFragment(int type, boolean isAnim, boolean isEnforce) {
        Log.e("摄像头", "replaceFragment");
        if (type == current_frag) {
            return;
        }

        if (!isReplace(type, isEnforce)) {
            return;
        }
        Fragment fragment = newFragInstance(type);

        Log.e("摄像头", fragment + "");
        try {
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            if (isAnim) {
                transaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                switch (type) {
                    case FRAG_REMOTE_CONTROL:
                        if (current_frag == FRAG_MAIN || current_frag == -1) {
                            transaction.setCustomAnimations(R.anim.slide_in_right,
                                    R.anim.slide_out_left);
                        }
                        break;
                    case FRAG_MAIN:
                        if (current_frag == FRAG_REMOTE_CONTROL
                                || current_frag == FRAG_TIME_CONTROL
                                || current_frag == FRAG_ALARM_CONTROL
                                || current_frag == FRAG_VIDEO_CONTROL
                                || current_frag == FRAG_SECURITY_CONTROL
                                || current_frag == FRAG_NET_CONTROL
                                || current_frag == FRAG_SD_CARD_CONTROL
                                || current_frag == FRAG_SMART_DEVICE
                                || current_frag == FRAG_UTILS_CONTROL
                                || current_frag == FRAG_PLAYBACK_CAMERALIST
                                || current_frag == FRAG_PLAY_BACK_CAMERA
                                || current_frag == FRAG_RECORD_CONTROL) {
                            transaction.setCustomAnimations(R.anim.slide_in_left,
                                    R.anim.slide_out_right);
                        }

                        break;
                    case FRAG_TIME_CONTROL:
                        if (current_frag == FRAG_MAIN || current_frag == -1) {
                            transaction.setCustomAnimations(R.anim.slide_in_right,
                                    R.anim.slide_out_left);
                        }
                        break;
                    case FRAG_ALARM_CONTROL:
                        if (current_frag == FRAG_MAIN || current_frag == -1) {
                            transaction.setCustomAnimations(R.anim.slide_in_right,
                                    R.anim.slide_out_left);
                        }
                        break;
                    case FRAG_PLAY_BACK_CAMERA:
                        if (current_frag == FRAG_MAIN || current_frag == -1) {
                            transaction.setCustomAnimations(R.anim.slide_in_right,
                                    R.anim.slide_out_left);
                        }
                    case FRAG_PLAYBACK_CAMERALIST:
                        if (current_frag == FRAG_MAIN || current_frag == -1) {
                            transaction.setCustomAnimations(R.anim.slide_in_right,
                                    R.anim.slide_out_left);
                        }
                    case FRAG_VIDEO_CONTROL:
                        if (current_frag == FRAG_MAIN || current_frag == -1) {
                            transaction.setCustomAnimations(R.anim.slide_in_right,
                                    R.anim.slide_out_left);
                        }
                    case FRAG_SECURITY_CONTROL:
                        if (current_frag == FRAG_MAIN || current_frag == -1) {
                            transaction.setCustomAnimations(R.anim.slide_in_right,
                                    R.anim.slide_out_left);
                        }
                        break;
                    case FRAG_NET_CONTROL:
                        if (current_frag == FRAG_MAIN || current_frag == -1) {
                            transaction.setCustomAnimations(R.anim.slide_in_right,
                                    R.anim.slide_out_left);
                        }
                        Log.e("替换", "FRAG_NET_CONTROL");
                        break;
                    case FRAG_UTILS_CONTROL:
                        if (current_frag == FRAG_MAIN || current_frag == -1) {
                            transaction.setCustomAnimations(R.anim.slide_in_right,
                                    R.anim.slide_out_left);
                        }
                        Log.e("MainControl", "screen_shot2");
                        break;

                    case FRAG_SD_CARD_CONTROL:
                        if (current_frag == FRAG_MAIN || current_frag == -1) {
                            transaction.setCustomAnimations(R.anim.slide_in_right,
                                    R.anim.slide_out_left);
                        }
                        break;
                    case FRAG_LANGUAGE_CONTROL:
                        if (current_frag == FRAG_MAIN || current_frag == -1) {
                            transaction.setCustomAnimations(R.anim.slide_in_right,
                                    R.anim.slide_out_left);
                        }
                        break;
                    case FRAG_SMART_DEVICE:
                        if (current_frag == FRAG_MAIN || current_frag == -1) {
                            transaction.setCustomAnimations(R.anim.slide_in_right,
                                    R.anim.slide_out_left);
                        }
                        break;
                    case FRAG_MODIFY_WIFIPWD_CONTROL:
                        if (current_frag == FRAG_MAIN || current_frag == -1) {
                            transaction.setCustomAnimations(R.anim.slide_in_right,
                                    R.anim.slide_out_left);
                        }
                        break;
                    case FRAG_RECORD_CONTROL:
                        if (current_frag == FRAG_MAIN || current_frag == -1) {
                            transaction.setCustomAnimations(R.anim.slide_in_right,
                                    R.anim.slide_out_left);
                        }
                        break;
                }
            }
            current_frag = type;
            transaction.replace(R.id.fragContainer, fragment,
                    fragTags[current_frag]);
            transaction.commit();
            Log.e("摄像头", "replaceFragment2");
//			manager.executePendingTransactions();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("my", "replaceFrag error--main");
        }
    }

    RecordFile recordFile = null;

    public Fragment newFragInstance(int type) {
        Bundle args = new Bundle();
        args.putSerializable("contact", contact);
        args.putInt("type", device_type);
        args.putInt("connectType", connectType);
        args.putInt("languegecount", languegecount);
        args.putInt("curlanguege", curlanguege);
        args.putIntArray("langueges", langueges);
        args.putInt("deviceid", device_id);
        args.putString("device", device);
        args.putSerializable("file", recordFile);
        args.putBoolean("isEnforce", true);
        Bundle bundle = new Bundle();

        switch (type) {
            case FRAG_MAIN:
                if (null == mainFrag) {
                    mainFrag = new MainControlFrag();
                }
                mainFrag.setArguments(args);
                return mainFrag;
            case FRAG_REMOTE_CONTROL:
            /*if (null == remoteFrag) {
                remoteFrag = new RemoteControlFrag();
				remoteFrag.setArguments(args);
			}
			return remoteFrag;*/
            case FRAG_TIME_CONTROL:
                timeFrag = new TimeControlFrag();
                timeFrag.setArguments(args);
                return timeFrag;
            case FRAG_ALARM_CONTROL:
                alarmFrag = new AlarmControlFrag();
                alarmFrag.setArguments(args);
                return alarmFrag;

            case FRAG_PLAY_BACK_CAMERA:
                //录像回放界面
                playBackFrag = new PlayBackFrag();
                args.putSerializable("file", recordFile);
                playBackFrag.setArguments(args);
                return playBackFrag;
            case FRAG_PLAYBACK_CAMERALIST:
                //回放列表
                if (recordFilesFrag == null) {
                    recordFilesFrag = new RecordFilesFrag();
                }
                recordFilesFrag.setArguments(args);
                return recordFilesFrag;
            case FRAG_UTILS_CONTROL:
                utilsFrag = new UtilsFrag();
                utilsFrag.setArguments(args);
                return utilsFrag;
            case FRAG_VIDEO_CONTROL:
                videoFrag = new VideoControlFrag();
                videoFrag.setArguments(args);
                return videoFrag;
            case FRAG_SECURITY_CONTROL:

                securityFrag = new SecurityControlFrag();
                securityFrag.setArguments(args);
                return securityFrag;
            case FRAG_NET_CONTROL:
                netFrag = new NetControlFrag();
                netFrag.setArguments(args);
                Log.e("替换", "NetControlFrag");
                return netFrag;
            case FRAG_RECORD_CONTROL:
                recordFrag = new RecordControlFrag();
                recordFrag.setArguments(args);
                return recordFrag;
            case FRAG_LANGUAGE_CONTROL:
			/*languegeFrag = new LanguageControlFrag();
			languegeFrag.setArguments(args);
			return languegeFrag;*/
            case FRAG_SMART_DEVICE:
			/*smartDeviceFrag=new SmartDeviceFrag();
			smartDeviceFrag.setArguments(args);
			return smartDeviceFrag;*/
            case FRAG_MODIFY_WIFIPWD_CONTROL:
			/*modifyWifiFrag=new ModifyApWifiFrag();
			modifyWifiFrag.setArguments(args);
			return modifyWifiFrag;*/
            default:
                return null;
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (isRegFilter) {
            isRegFilter = false;
            this.unregisterReceiver(mReceiver);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (null != netFrag && current_frag == FRAG_NET_CONTROL) {
                if (netFrag.IsInputDialogShowing()) {
                    Intent close_input_dialog = new Intent();
                    close_input_dialog
                            .setAction(Constants.Action.CLOSE_INPUT_DIALOG);
                    mContext.sendBroadcast(close_input_dialog);
                    return true;
                }
            }
            if (current_frag != FRAG_MAIN) {
                replaceFragment(FRAG_MAIN, true, true);
                return true;
            }

        }

        return super.dispatchKeyEvent(event);
    }

    public Contact getContact() {
        return contact;
    }

    @Override
    public int getActivityInfo() {
        // TODO Auto-generated method stub
        return Constants.ActivityInfo.ACTIVITY_MAINCONTROLACTIVITY;
    }

    @Override
    protected int onPreFinshByLoginAnother() {
        return 0;
    }

    public com.smartism.znzk.domain.DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }
}
