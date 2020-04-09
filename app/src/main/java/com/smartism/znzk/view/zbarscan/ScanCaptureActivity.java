package com.smartism.znzk.view.zbarscan;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.amazonaws.amplify.generated.graphql.CreateCtrUserDeviceRelationsMutation;
import com.amazonaws.amplify.generated.graphql.ListCtrUserDeviceRelationsQuery;
import com.amazonaws.amplify.generated.graphql.ListCtrUserDeviceSharesQuery;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.dtr.zbar.build.ZBarDecoder;
import com.p2p.core.P2PHandler;
import com.p2p.core.P2PValue;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.camera.MainActivity;
import com.smartism.znzk.activity.device.DeviceMainActivity;
import com.smartism.znzk.activity.device.ZhujiListFragment;
import com.smartism.znzk.activity.device.add.AddZhujiActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.awsClient.AWSClients;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.db.camera.DataManager;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.domain.camera.CameraInfo;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.global.FList;
import com.smartism.znzk.global.NpcCommon;
import com.smartism.znzk.hipermission.HiPermission;
import com.smartism.znzk.hipermission.PermissionCallback;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.BaiduLBSUtils;
import com.smartism.znzk.util.CollectionsUtils;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.SecurityUtil;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.util.camera.T;
import com.smartism.znzk.util.camera.Utils;
import com.smartism.znzk.view.alertview.AlertView;

import java.lang.reflect.Field;
import java.util.List;

import javax.annotation.Nonnull;

import cn.bingoogolapple.photopicker.activity.BGAPhotoPickerActivity;
import cn.bingoogolapple.qrcode.zxing.QRCodeDecoder;
import type.CreateCtrUserDeviceRelationsInput;
import type.TableCtrUserDeviceRelationsFilterInput;
import type.TableCtrUserDeviceShareFilterInput;
import type.TableStringFilterInput;

public class ScanCaptureActivity extends ActivityParentActivity {
    public static final String TAG = ScanCaptureActivity.class.getSimpleName();
    private Camera mCamera;
    private CameraPreview mPreview;
    private CameraManager mCameraManager;

    private TextView scanResult, choose_local_photo;
    private FrameLayout scanPreview;
    private Button scanRestart;
    private RelativeLayout scanContainer;
    private RelativeLayout scanCropView, rl_bottom;
    private ImageView scanLine;

    private Rect mCropRect = null;
    private boolean barcodeScanned = false;
    private boolean previewing = true;
    private int requestCode = 0;
    private String sqr = "";

    Contact mSaveContact; //摄像头配网后发送过来的
    boolean isCreatePassword = false; //是否需要设置初始密码
    boolean isfactory; //连接模式
    String ipFlag;//
    private int type;
    private boolean isZhujiFragment;
    private static final int REQUEST_CODE_CHOOSE_QRCODE_FROM_GALLERY = 666;

    //临时全局变量 mac地址
    private String tmpMac = "";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.zbar_scan_capture);
        isScan = getIntent().getBooleanExtra("isScan", false);
        mSaveContact = (Contact) getIntent().getSerializableExtra("contact");
        isCreatePassword = getIntent().getBooleanExtra("isCreatePassword", false);
        isfactory = getIntent().getBooleanExtra("isfactory", false);
        ipFlag = getIntent().getStringExtra("ipFlag");
        i = getIntent().getIntExtra("int", 0);
        type = getIntent().getIntExtra("type", 0);
        isCameraList = getIntent().getIntExtra("isCameraList", 0);
        isZhujiFragment = getIntent().getBooleanExtra("isZhujiFragment", false);

//        info = DatabaseOperator.getInstance(getApplicationContext())
//                .queryDeviceZhuJiInfo(DataCenterSharedPreferences
//                        .getInstance(MainApplication.app, DataCenterSharedPreferences.Constant.CONFIG)
//                        .getString(DataCenterSharedPreferences.Constant.APP_MASTERID, ""));
        //替换
        info = DatabaseOperator.getInstance(getApplicationContext()).queryDeviceZhuJiInfo(ZhujiListFragment.getMasterId());

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setTitle(getString(R.string.capture_activity_title));
        requestCode = getIntent().getIntExtra(Constant.CAPUTRE_REQUESTCOE, 0);
        findViewById();
        addEvents();
        checkPermissionAndInitView();
        BaiduLBSUtils.location(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void findViewById() {
        scanPreview = (FrameLayout) findViewById(R.id.capture_preview);
        scanResult = (TextView) findViewById(R.id.capture_scan_result);
        choose_local_photo = (TextView) findViewById(R.id.choose_local_photo);
        scanRestart = (Button) findViewById(R.id.capture_restart_scan);
        scanContainer = (RelativeLayout) findViewById(R.id.capture_container);
        scanCropView = (RelativeLayout) findViewById(R.id.capture_crop_view);
        rl_bottom = (RelativeLayout) findViewById(R.id.rl_bottom);
        scanLine = (ImageView) findViewById(R.id.capture_scan_line);

    }

    private void checkPermissionAndInitView(){
        if (HiPermission.checkPermission(mContext, Manifest.permission.CAMERA)) {
            initViews();
        }else{
            HiPermission.create(mContext)
                    .animStyle(R.style.PermissionAnimFade)//设置动画
                    .checkSinglePermission(Manifest.permission.CAMERA,new PermissionCallback() {
                        @Override
                        public void onClose() {}

                        @Override
                        public void onFinish() {}

                        @Override
                        public void onDeny(String permission, int position) {initViews();}

                        @Override
                        public void onGuarantee(String permission, int position) {
                            initViews();
                        }
                    });
        }
    }

    private void addEvents() {
        if (isZhujiFragment)
            //此按钮不再打开
//            rl_bottom.setVisibility(View.VISIBLE);
        scanRestart.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                restartCamera();
            }
        });
        choose_local_photo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(BGAPhotoPickerActivity.newIntent(ScanCaptureActivity.this, null, 1, null, false), REQUEST_CODE_CHOOSE_QRCODE_FROM_GALLERY);
            }
        });
        rl_bottom.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(mContext, AddZhujiActivity.class);
                startActivity(intent);
                finish();
            }
        });
        if (Actions.VersionType.CHANNEL_AIERFUDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                || Actions.VersionType.CHANNEL_UHOME.equals(MainApplication.app.getAppGlobalConfig().getVersion()))
            rl_bottom.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        initViews();
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_CHOOSE_QRCODE_FROM_GALLERY) {
            final String picturePath = BGAPhotoPickerActivity.getSelectedImages(data).get(0);

            /*
            这里为了偷懒，就没有处理匿名 AsyncTask 内部类导致 Activity 泄漏的问题
            请开发在使用时自行处理匿名内部类导致Activity内存泄漏的问题，处理方式可参考 https://github.com/GeniusVJR/LearningNotes/blob/master/Part1/Android/Android%E5%86%85%E5%AD%98%E6%B3%84%E6%BC%8F%E6%80%BB%E7%BB%93.md
             */
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    return QRCodeDecoder.syncDecodeQRCode(picturePath);
                }

                @Override
                protected void onPostExecute(String result) {
                    if (result != null && !"".equals(result)) {
                        previewing = false;
                        if (mCamera != null) {
                            mCamera.setPreviewCallback(null);
                            mCamera.stopPreview();
                        }
                        barcodeScanned = true;
                        if (result.toLowerCase().startsWith("http://") || result.toLowerCase().startsWith("https://")) {
                            Intent intent = new Intent();
                            intent.setAction("android.intent.action.VIEW");
                            Uri content_url = Uri.parse(result);
                            intent.setData(content_url);
                            startActivity(intent);
                            finish();
                        } else if (result.toLowerCase().startsWith("ctrfluid")) { // 扫描获得熏香液类型
                            Intent intent = new Intent();
                            intent.putExtra("value", result.substring(8,12));
                            intent.putExtra("pattern", "ctrfluid");
                            setResult(Constant.CAPUTRE_ADDRESULT, intent);
                            finish();
                        } else if (result.toLowerCase().startsWith("ctrshare")) { // 扫描分享码
                            showInProgress(getString(R.string.operationing), false, false);
                            addZhujiFromScan(result.substring(8));
                        } else if (result.toLowerCase().matches("^[0-9]*$")) { // 纯数字为商品条形码
                            Toast.makeText(ScanCaptureActivity.this, getString(R.string.capture_activity_unknown), Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            AlertDialog myAlertDialog = new AlertDialog.Builder(ScanCaptureActivity.this)
                                    .setMessage(getString(R.string.capture_activity_unknown))
                                    .setPositiveButton(getString(R.string.sure), null).show();
                            myAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    restartCamera();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(ScanCaptureActivity.this, getString(R.string.qrcode_scan_photo_fail), Toast.LENGTH_SHORT).show();
                    }

                }
            }.execute();
        }
//        else {
//            restartCamera();
//        }
    }

    /**
     * 重新开启扫描
     */
    private void restartCamera() {
        if (barcodeScanned) {
            barcodeScanned = false;
            scanResult.setText(getString(R.string.capture_activity_ing));
            if (mCamera == null)
                finish();
            mCamera.setPreviewCallback(previewCb);
            mCamera.startPreview();
            previewing = true;
            mCamera.autoFocus(autoFocusCB);
        }
    }

    private void initViews() {
        mCameraManager = new CameraManager(this);
        try {
            mCameraManager.openDriver();

            // 调整扫描框大小,自适应屏幕
            Display display = this.getWindowManager().getDefaultDisplay();
            int width = display.getWidth();
            int height = display.getHeight();

            RelativeLayout.LayoutParams linearParams = (RelativeLayout.LayoutParams) scanCropView.getLayoutParams();
            linearParams.height = (int) (width * 0.7);
            linearParams.width = (int) (width * 0.7);
            scanCropView.setLayoutParams(linearParams);

            mCamera = mCameraManager.getCamera();
            mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
            scanPreview.addView(mPreview);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f,
                            Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
                            Animation.RELATIVE_TO_PARENT, 0.75f);
                    animation.setDuration(8 * 1000);
                    animation.setRepeatCount(-1);
//					animation.
                    animation.setRepeatMode(Animation.RESTART); // 是否返回，循环开始
                    final TranslateAnimation animation1 = animation;
                    if (defaultHandler != null) {
                        defaultHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                scanLine.startAnimation(animation1);

                            }
                        });
                    } else {
                        defaultHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                scanLine.startAnimation(animation1);
                            }
                        });
                    }
                }
            }).start();
            /*defaultHandler.post(new Runnable() {
                @Override
				public void run() {
				}
			});*/

        } catch (Exception e) {
            new AlertView(getString(R.string.tips),
                    getString(R.string.capture_activity_opendiverfail), getString(R.string.capture_activity_setcamera_yes),
                    new String[]{getString(R.string.capture_activity_setcamera_no)}, null, ScanCaptureActivity.this,
                    AlertView.Style.Alert, new com.smartism.znzk.view.alertview.OnItemClickListener() {

                @Override
                public void onItemClick(Object o, int position) {
                    if (position == -1) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.fromParts("package",getPackageName(),null));
                        startActivity(intent);
                    }
                    finish();
                }
            }).show();
        }
    }

    public void onPause() {
        super.onPause();
        releaseCamera();
//        finish();
    }

    private void releaseCamera() {
        if (mCamera != null) {
            synchronized (ScanCaptureActivity.class) {
                if (mCamera != null) {
                    previewing = false;
                    mCamera.setPreviewCallback(null);
                    mCamera.stopPreview();
                    mCamera.release();
                    mCamera = null;
                }
            }
        }
    }

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (previewing)
                mCamera.autoFocus(autoFocusCB);
        }
    };

    PreviewCallback previewCb = new PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            Size size = camera.getParameters().getPreviewSize();

            // 这里需要将获取的data翻转一下，因为相机默认拿的的横屏的数据
            byte[] rotatedData = new byte[data.length];
            for (int y = 0; y < size.height; y++) {
                for (int x = 0; x < size.width; x++)
                    rotatedData[x * size.height + size.height - y - 1] = data[x + y * size.width];
            }

            // 宽高也要调整
            int tmp = size.width;
            size.width = size.height;
            size.height = tmp;

            initCrop();
            ZBarDecoder zBarDecoder = new ZBarDecoder();
            String result = zBarDecoder.decodeCrop(rotatedData, size.width, size.height, mCropRect.left, mCropRect.top,
                    mCropRect.width(), mCropRect.height());

            if (!TextUtils.isEmpty(result)) {
                previewing = false;
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                barcodeScanned = true;
                if (result != null && !"".equals(result)) {
                    Log.d(TAG, "scanResult:" + result.toString());
                    if (result.toLowerCase().startsWith("http://") || result.toLowerCase().startsWith("https://")) {
                        Intent intent = new Intent();
                        intent.setAction("android.intent.action.VIEW");
                        Uri content_url = Uri.parse(result);
                        intent.setData(content_url);
                        startActivity(intent);
                        finish();
                    } else if (result.toLowerCase().startsWith("ctrfluid")) { // 扫描获得熏香液类型
                        Intent intent = new Intent();
                        intent.putExtra("value", result.substring(8,12));
                        intent.putExtra("pattern", "ctrfluid");
                        setResult(Constant.CAPUTRE_ADDRESULT, intent);
                        finish();
                    } else if (result.toLowerCase().startsWith("ctrshare")) { // 扫描分享码
                        showInProgress(getString(R.string.operationing), false, false);
                        addZhujiFromScan(result.substring(8));
                    } else if (result.toLowerCase().matches("^[0-9]*$")) { // 纯数字为商品条形码
                        Toast.makeText(ScanCaptureActivity.this, getString(R.string.capture_activity_unknown), Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        AlertDialog myAlertDialog = new AlertDialog.Builder(ScanCaptureActivity.this)
                                .setMessage(getString(R.string.capture_activity_unknown))
                                .setPositiveButton(getString(R.string.sure), null).show();
                        myAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                restartCamera();
                            }
                        });
                    }
                }
            }
        }
    };

    /**
     * 扫描到摄像头的条码
     *
     * @param result
     */
    public void readCamera(String result) {

        try {
            String code = result.replace("jdmipc://", "");
            result = "jdmipc://" + SecurityUtil.decrypt(code, DataCenterSharedPreferences.Constant.SCAN_CONTEXT);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String[] strs = result.substring(9).split("\\/");
        if (strs != null && strs.length == 2) {
            //判断是直接扫码还是配网后进入
            if (!isScan) {
                next(strs[0], "Camera" + strs[0], strs[1]);
            } else if (mSaveContact != null && strs[0].equals(mSaveContact.contactId)) {
                next(strs[0], "Camera" + strs[0], strs[1]);
            } else {
                showDialogMsg();
                return;
            }
        } else {
            showDialogMsg();
            return;
        }
    }

    /**
     * 添加摄像头操作
     */
    private ZhujiInfo info;
    DeviceInfo deviceInfo = null;
    private int isCameraList;
    private int i;
    boolean isScan;//配网分两种（1、false，类似手动 2、true 配网之后扫码添加）

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //完成服务器添加，再将数据保存在本地
                    FList.getInstance().insert(saveContact);
                    FList.getInstance().updateLocalDeviceWithLocalFriends();
                    T.showShort(mContext, R.string.add_success);
                    //解决
                    String flag = "";
                    if (i == 3) {
                        flag = "juwei";
                        Intent intent = new Intent();
                        if (isCameraList == 1) {
                            intent.setClass(mContext, MainActivity.class);
                        } else {
                            intent.setClass(mContext, DeviceMainActivity.class);
                            intent.putExtra("isNotCamera", false);    //跳转之后会提示更新，为了避免从摄像头列表跳转弹出，添加一个判断
                        }
                        startActivity(intent);
                        finish();
                        return true;
                    }
                    finish();

                    break;
                case 0:
                    com.macrovideo.sdk.custom.DeviceInfo info = (com.macrovideo.sdk.custom.DeviceInfo) msg.obj;
                    Log.e("摄像头", info.getnDevID() + "");
                    addDevice(info);
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    private Handler defaultHandler = new WeakRefHandler(mCallback);

    /**
     * 添加摄像头到服务器
     *
     * @param device
     */
    private void addDevice(com.macrovideo.sdk.custom.DeviceInfo device) {
        String c1 = null, id1 = null, n1 = null, p1 = null;
        final long did = info.getId();
        if (i == 5) {
            c1 = "v380";
            id1 = device.getnDevID() + "";
            n1 = device.getStrName();
            p1 = device.getStrPassword();
        } else {
            c1 = "jiwei";
            id1 = saveContact.contactId;
            n1 = saveContact.contactName;
            p1 = saveContact.contactPassword;
        }
        final String c = c1;
        final String id = id1;
        final String n = n1;
        final String p = p1;
        JavaThreadPool.getInstance().excute(new Runnable() {

            @Override
            public void run() {
                DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(ScanCaptureActivity.this,
                        DataCenterSharedPreferences.Constant.CONFIG);
                String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("did", did);
                pJsonObject.put("c", c);
                pJsonObject.put("id", id);
                pJsonObject.put("n", n);
                pJsonObject.put("p", p);
                String http = server + "/jdm/s3/ipcs/add";
                String result = HttpRequestUtils.requestoOkHttpPost(http, pJsonObject, dcsp);
                // -1参数为空 -2校验失败 -10服务器不存在
                if ("0".equals(result)) {
                    //清空推送用户
                    P2PHandler.getInstance().setBindAlarmId(id, p, 0, new String[]{}, MainApplication.GWELL_LOCALAREAIP);
//                    P2PHandler.getInstance().setDefenceAreaState(id,p,0,0,0);//防区通道固定0，防区固定0 ，type 0表示学习 智慧主机没有可以不支持

                    defaultHandler.sendEmptyMessage(1);
                } else {
                    defaultHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            T.showShort(mContext, R.string.addfailed);
                        }
                    });
                }

            }
        });
    }

    public void next(String cid, String cname, String cpwd) {
        String input_id = cid;
        String input_name = cname;
        String input_pwd = cpwd;
        if (input_id != null && input_id.trim().equals("")) {
            showErroMsg(getString(R.string.capture_activity_unknown));
            return;
        }
        if (input_id.charAt(0) == '0' || input_id.length() > 9 || !Utils.isNumeric(input_id)) {
            showErroMsg(getString(R.string.capture_activity_unknown));
            return;
        }

        List<DeviceInfo> deviceInfos = DatabaseOperator.getInstance(ScanCaptureActivity.this).queryAllDeviceInfos(info.getId());
        for (DeviceInfo d : deviceInfos) {
            if (d.getCak().equals("surveillance")) {
                deviceInfo = d;
                break;
            }
        }

        List<CameraInfo> camera = null;
        if (deviceInfo != null && deviceInfo.getCak().equals("surveillance")) {
            camera = (List<CameraInfo>) JSON.parseArray(deviceInfo.getIpc(), CameraInfo.class);
        }
        if (null != FList.getInstance().isContact(input_id)) {
            if (camera != null && camera.size() > 0) {
                for (int i = 0; i < camera.size(); i++) {
                    if (camera.get(i).getId().equals(input_id)) {
                        showErroMsg(getString(R.string.contact_already_exist));
                        return;
                    }
                }

            }
            /*
             * T.showShort(mContext, R.string.contact_already_exist); return;
			 */

        }
        int type;
        if (input_id.charAt(0) == '0') {
            type = P2PValue.DeviceType.PHONE;
        } else {
            type = P2PValue.DeviceType.UNKNOWN;
        }
        if (input_name != null && input_name.trim().equals("")) {
            showErroMsg(getString(R.string.capture_activity_unknown));
            return;
        }
        saveContact.contactId = input_id;
        saveContact.contactType = type;
        saveContact.activeUser = NpcCommon.mThreeNum;
        saveContact.messageCount = 0;
        List<Contact> lists = DataManager.findContactByActiveUser(mContext, NpcCommon.mThreeNum);

        for (Contact c : lists) {
            if (c.contactName.equals(input_name)) {
                if (camera != null && camera.size() > 0) {
                    for (int i = 0; i < camera.size(); i++) {
                        if (camera.get(i).getN().equals(input_name)) {
                            showErroMsg(getString(R.string.capture_activity_unknown));
                            return;
                        }
                    }
                }
            }

			/*
             * Log.e("密码", pwd+":"+c.contactPassword);
			 * if(pwd!=null&&!c.contactPassword.equals(pwd)){
			 *
			 * T.showShort(mContext, R.string.password_error); return; }
			 */
        }
        if (input_pwd == null || input_pwd.trim().equals("")) {
            T.showShort(this, R.string.input_password);
            return;
            // input_pwd = "";
        }
        if (saveContact.contactType != P2PValue.DeviceType.PHONE) {
            if (input_pwd != null && !input_pwd.trim().equals("")) {
                if (input_pwd.charAt(0) == '0' || input_pwd.length() > 30) {
                    showErroMsg(getString(R.string.capture_activity_unknown));
                    return;
                }
            }
        }

        List<Contact> contactlist = DataManager.findContactByActiveUser(mContext, NpcCommon.mThreeNum);
        for (Contact contact : contactlist) {
            if (contact.contactId.equals(saveContact.contactId)) {
                if (camera != null && camera.size() > 0) {
                    for (int i = 0; i < camera.size(); i++) {
                        if (camera.get(i).getId().equals(input_id)) {
                            showErroMsg(getString(R.string.capture_activity_unknown));
                            return;
                        }
                    }
                }

            }
        }

        saveContact.contactName = input_name;
        saveContact.userPassword = input_pwd;
        String pwd = P2PHandler.getInstance().EntryPassword(input_pwd);
        saveContact.contactPassword = pwd;
        String[] contactIds = new String[]{input_id};
        P2PHandler.getInstance().getFriendStatus(contactIds);
        P2PHandler.getInstance().checkPassword(input_id, pwd, MainApplication.GWELL_LOCALAREAIP);
        P2PHandler.getInstance().getDefenceStates(input_id, pwd, MainApplication.GWELL_LOCALAREAIP);
        P2PHandler.getInstance().checkDeviceUpdate(input_id, pwd, MainApplication.GWELL_LOCALAREAIP);
//		Log.e("摄像头", saveContact.toString());
        if (saveContact.defenceState == Constants.DefenceState.DEFENCE_STATE_WARNING_PWD) {
            showErroMsg(getString(R.string.capture_activity_unknown));
            return;
        }
        addDevice(null);
    }

    private AlertDialog myAlertDialog = null;
    private Contact saveContact = new Contact();

    /**
     * 提示信息
     *
     * @param msg
     */
    public void showErroMsg(String msg) {
        if (myAlertDialog == null) {
            myAlertDialog = new AlertDialog.Builder(ScanCaptureActivity.this)
                    .setMessage(msg)
                    .setPositiveButton(getString(R.string.sure), null).show();
            myAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    restartCamera();
                    myAlertDialog = null;
                }
            });
        }
    }

    /**
     * 扫码失败之后提示信息
     */
    public void showDialogMsg() {
        AlertDialog myAlertDialog = new AlertDialog.Builder(ScanCaptureActivity.this)
                .setMessage(getString(R.string.capture_activity_unknown))
                .setPositiveButton(getString(R.string.sure), null).show();
        myAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                restartCamera();
            }
        });
    }

    // Mimic continuous auto-focusing
    AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            defaultHandler.postDelayed(doAutoFocus, 1000);
        }
    };

    /**
     * 初始化截取的矩形区域
     */
    private void initCrop() {
        int cameraWidth = mCameraManager.getCameraResolution().y;
        int cameraHeight = mCameraManager.getCameraResolution().x;

        /** 获取布局中扫描框的位置信息 */
        int[] location = new int[2];
        scanCropView.getLocationInWindow(location);

        int cropLeft = location[0];
        int cropTop = location[1] - getStatusBarHeight();

        int cropWidth = scanCropView.getWidth();
        int cropHeight = scanCropView.getHeight();

        /** 获取布局容器的宽高 */
        int containerWidth = scanContainer.getWidth();
        int containerHeight = scanContainer.getHeight();

        /** 计算最终截取的矩形的左上角顶点x坐标 */
        int x = cropLeft * cameraWidth / containerWidth;
        /** 计算最终截取的矩形的左上角顶点y坐标 */
        int y = cropTop * cameraHeight / containerHeight;

        /** 计算最终截取的矩形的宽度 */
        int width = cropWidth * cameraWidth / containerWidth;
        /** 计算最终截取的矩形的高度 */
        int height = cropHeight * cameraHeight / containerHeight;

        /** 生成最终的截取的矩形 */
        mCropRect = new Rect(x, y, width + x, height + y);
    }

    private int getStatusBarHeight() {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            return getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void back(View v) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private GraphQLCall.Callback<ListCtrUserDeviceSharesQuery.Data> querySharesCallback = new GraphQLCall.Callback<ListCtrUserDeviceSharesQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListCtrUserDeviceSharesQuery.Data> response) {
            if (response.data()!=null && response.data().listCtrUserDeviceShares() != null && !CollectionsUtils.isEmpty(response.data().listCtrUserDeviceShares().items())){
                ListCtrUserDeviceSharesQuery.Item item = response.data().listCtrUserDeviceShares().items().get(0);

                if (item != null){
                    if(item.validityTime() < System.currentTimeMillis()/1000){
                        runOnUiThread(() -> {
                            ToastUtil.longMessage("QR code has expired");
                            finish();
                        });
                        return;
                    }

                    tmpMac = item.mac();

                    AWSClients.getInstance().getmAWSAppSyncClient().query(ListCtrUserDeviceRelationsQuery.builder()
                            .filter(TableCtrUserDeviceRelationsFilterInput.builder()
                                    .uid(TableStringFilterInput.builder().eq(AWSMobileClient.getInstance().getUsername()).build())
                                    .mac(TableStringFilterInput.builder().eq(item.mac()).build())
                                    .build())
                            .build())
                            .responseFetcher(AppSyncResponseFetchers.NETWORK_ONLY)
                            .enqueue(queryRelatioinsCallback);
                }
            }
            Log.i("Results", response.data().listCtrUserDeviceShares().items().toString());
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("ERROR", e.toString());
            runOnUiThread(()->{
                ToastUtil.longMessage(getString(R.string.addfailed));
                finish();
            });
        }
    };

    private GraphQLCall.Callback<ListCtrUserDeviceRelationsQuery.Data> queryRelatioinsCallback = new GraphQLCall.Callback<ListCtrUserDeviceRelationsQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListCtrUserDeviceRelationsQuery.Data> response) {
            Log.i("Results", response.data().listCtrUserDeviceRelations().items().toString());
            if (response.data()!=null && response.data().listCtrUserDeviceRelations() != null && !CollectionsUtils.isEmpty(response.data().listCtrUserDeviceRelations().items())){
                runOnUiThread(() -> {
                    ToastUtil.longMessage("Device already exists");
                    finish();
                });
            }else{
                CreateCtrUserDeviceRelationsInput createCtrUserDeviceRelationsInput = CreateCtrUserDeviceRelationsInput.builder()
                        .uid(AWSMobileClient.getInstance().getUsername())
                        .mac(tmpMac)
                        .name("熏香机")
                        .type(ZhujiInfo.CtrDeviceType.INSECTICIDE_SINGLE_REFILL)
                        .build();

                AWSClients.getInstance().getmAWSAppSyncClient().mutate(CreateCtrUserDeviceRelationsMutation.builder().input(createCtrUserDeviceRelationsInput).build())
                        .enqueue(mutationCallback);
            }
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("ERROR", e.toString());
            runOnUiThread(()->{
                ToastUtil.longMessage(getString(R.string.addfailed));
                finish();
            });
        }
    };

    private GraphQLCall.Callback<CreateCtrUserDeviceRelationsMutation.Data> mutationCallback = new GraphQLCall.Callback<CreateCtrUserDeviceRelationsMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<CreateCtrUserDeviceRelationsMutation.Data> response) {
            Log.i(TAG,"Results GraphQL Add:" + JSON.toJSONString(response));
            runOnUiThread(()->{
                ToastUtil.longMessage(getString(R.string.activity_add_zhuji_havezhu_addsuccess));
                finish();
            });
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("Error","GraphQL Add Exception", e);
            runOnUiThread(()->{
                ToastUtil.longMessage(getString(R.string.addfailed));
                finish();
            });
        }
    };

    private void addZhujiFromScan(final String value) {
        AWSClients.getInstance().getmAWSAppSyncClient().query(ListCtrUserDeviceSharesQuery.builder()
                .filter(TableCtrUserDeviceShareFilterInput.builder()
                        .key(TableStringFilterInput.builder().eq(value).build())
                        .build())
                .build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_ONLY)
                .enqueue(querySharesCallback);

    }

}