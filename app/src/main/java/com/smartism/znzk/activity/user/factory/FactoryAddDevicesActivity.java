package com.smartism.znzk.activity.user.factory;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.device.ZhujiListFragment;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessage.CodeMenu;
import com.smartism.znzk.communication.protocol.SyncMessage.CommandMenu;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.DeviceInfo.ControlTypeMenu;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.DeviceFactoryPopupWindow;
import com.smartism.znzk.view.DeviceFactoryPopupWindow.OnCancelBeforeListener;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class FactoryAddDevicesActivity extends ActivityParentActivity implements OnClickListener {
    public static final String MODEL_FACTORY_TYPE = "factory_type";
    public static final String MODEL_FACTORY_TYPE_ACTIVATION = "factory_type_activation";
    public static final String MODEL_FACTORY_TYPE_FACTORY = "factory_type_factory";

    private final int initZhuji_handler = 100, submit_timeout = 101, quit_timeout = 102, factory_timeout = 103, failed_tip_back = 104, init_devicesuccess = 105;
    private EditText name, type, barcode;
    private ImageView logo;
    private String logo_encode;
    private Spinner ctype, szhuji;
    private LinearLayout szhujiLayout;
    private String ctype_key = ControlTypeMenu.shangxing_1.value();
    private List<ZhujiInfo> deviceZhujis = null;
    private ZhujiInfo zhuji = null;
    private DeviceFactoryPopupWindow factoryPopupWindow;
    private BroadcastReceiver broadcastReceiver;
    private OnCancelBeforeListener cancelBeforeListener;
    private Uri iDLogoUri;
    private boolean addSuccess = false;
    private String model = null;
    private DeviceInfo deviceInfo = null;

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case initZhuji_handler:
                    //获取主机成功
                    deviceZhujis = (List<ZhujiInfo>) msg.obj;
                    if (deviceZhujis != null && !deviceZhujis.isEmpty()) {
                        if (deviceZhujis.size() > 1) {
                            szhujiLayout.setVisibility(View.VISIBLE);
                            //这里为多主机需要考虑的逻辑
//						List<String> list = new ArrayList<String>();
//						for (DeviceInfo info : deviceZhujis) {
//
//						}
//						list.add(getString(R.string.activity_add_devicefactory_shangxing_1));
//						list.add(getString(R.string.activity_add_devicefactory_xiaxing_1));
//						list.add(getString(R.string.activity_add_devicefactory_xiaxing_2));
//						ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_item,list);
//						adapter.setDropDownViewResource(R.layout.spinner_item);
//						ctype.setAdapter(adapter);
//						ctype.setOnItemSelectedListener(new OnItemSelectedListener() {
//
//							@Override
//							public void onItemSelected(AdapterView<?> parent, View view,
//									int position, long id) {
//								switch (position) {
//								case 0:
//									ctype_key = ControlTypeMenu.shangxing_1.getValue();
//									break;
//								case 1:
//									ctype_key = ControlTypeMenu.xiaxing_1.getValue();
//									break;
//								case 2:
//									ctype_key = ControlTypeMenu.xiaxing_2.getValue();
//									break;
//
//								default:
//									break;
//								}
//							}
//
//							@Override
//							public void onNothingSelected(AdapterView<?> parent) {
//
//							}
//						});
                        } else {
                            zhuji = deviceZhujis.get(0);
                            szhujiLayout.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(FactoryAddDevicesActivity.this, getString(R.string.activity_add_devicefactory_nozhuji), Toast.LENGTH_LONG).show();
                    }
                    break;

                case submit_timeout:
                    cancelInProgress();
                    Toast.makeText(FactoryAddDevicesActivity.this, getString(R.string.timeout), Toast.LENGTH_SHORT).show();
                    break;
                case quit_timeout:
                    cancelInProgress();
                    Toast.makeText(FactoryAddDevicesActivity.this, getString(R.string.timeout), Toast.LENGTH_SHORT).show();
                    if (addSuccess) {
                        Toast.makeText(FactoryAddDevicesActivity.this, getString(R.string.activity_add_devicefactory_success_quitfailed), Toast.LENGTH_LONG).show();
                    }
                    break;
                case factory_timeout:
                    cancelInProgress();
                    Toast.makeText(FactoryAddDevicesActivity.this, getString(R.string.timeout), Toast.LENGTH_SHORT).show();
                    dismissProgressDialogIntoFactoryModel();
                    break;
                case failed_tip_back:
                    TextView tipsView = (TextView) msg.obj;
                    tipsView.setBackgroundResource(R.color.blue);
                    tipsView.setText(R.string.activity_add_devicefactory_trigger);
                    break;
                case init_devicesuccess:
                    cancelInProgress();
                    ImageLoader.getInstance().displayImage( dcsp.getString(Constant.HTTP_DATA_SERVERS, "") + "/devicelogo/" + deviceInfo.getLogo(), logo);
                    name.setText(deviceInfo.getName());
                    type.setText(deviceInfo.getType());
                    name.setEnabled(false);
                    type.setEnabled(false);
                    logo.setEnabled(false);
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    private Handler defHandler = new WeakRefHandler(mCallback);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device_factory);
        //启动activity时不自动弹出软键盘
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        model = getIntent().getStringExtra(MODEL_FACTORY_TYPE);
        initView();
        if (MODEL_FACTORY_TYPE_ACTIVATION.equals(model)) {
            deviceInfo = new DeviceInfo();
            deviceInfo.setType(getIntent().getStringExtra("type"));
            initActivationView();
            initDeviceInfo();//初始化设备信息
        } else if (MODEL_FACTORY_TYPE_FACTORY.equals(model)) {
            initCtype();
        }
        initZhuji();
        initTipsView();
        initBroadcastReceiver();
    }

    private void initView() {
        logo = (ImageView) findViewById(R.id.deviceinfo_logo);
        logo.setOnClickListener(this);
        szhujiLayout = (LinearLayout) findViewById(R.id.zhuji_selected);
        name = (EditText) findViewById(R.id.deviceinfo_name);
        type = (EditText) findViewById(R.id.deviceinfo_type);
        barcode = (EditText) findViewById(R.id.deviceinfo_barcode);
        File appDir = new File(Environment.getExternalStorageDirectory(),
                "jjm");
        if (!appDir.exists()) {
            if (!appDir.mkdir()) {
                return;
            }
        }
        File file = new File(appDir, "device_logo.jpg");
        iDLogoUri = Uri.fromFile(file);
    }

    private void initCtype() {
        ctype = (Spinner) findViewById(R.id.deviceinfo_ctype);
        List<String> list = new ArrayList<String>();
        list.add(getString(R.string.activity_add_devicefactory_shangxing_1));
        list.add(getString(R.string.activity_add_devicefactory_xiaxing_1));
        list.add(getString(R.string.activity_add_devicefactory_xiaxing_2));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, list) {
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.spinner_item, parent, false);
                }
                TextView label = (TextView) convertView.findViewById(R.id.label);
                label.setText(getItem(position));

                return convertView;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.select_dialog_item);
        ctype.setAdapter(adapter);
        ctype.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                switch (position) {
                    case 0:
                        ctype_key = ControlTypeMenu.shangxing_1.value();
                        break;
                    case 1:
                        ctype_key = ControlTypeMenu.xiaxing_1.value();
                        break;
                    case 2:
                        ctype_key = ControlTypeMenu.xiaxing_2.value();
                        break;

                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initZhuji() {
        szhuji = (Spinner) findViewById(R.id.deviceinfo_szhuji);
        JavaThreadPool.getInstance().excute(new loadAllZhujiInfo(initZhuji_handler));
    }

    private void initTipsView() {
        cancelBeforeListener = new OnCancelBeforeListener() {

            @Override
            public void onCancelBefore() {
                if (factoryPopupWindow != null && factoryPopupWindow.isShowing()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(FactoryAddDevicesActivity.this);
                    builder.setTitle(getString(R.string.activity_add_devicefactory_confirmfactory_title)).setMessage(R.string.activity_add_devicefactory_confirmfactory_message)
                            .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    SyncMessage message = new SyncMessage();
                                    message.setCommand(CommandMenu.rq_pfactory.value());
                                    message.setCode(CodeMenu.rq_pfactory_exit.value());
                                    message.setDeviceid(zhuji.getId());
                                    SyncMessageContainer.getInstance().produceSendMessage(message);
                                    showInProgress(getString(R.string.activity_add_devicefactory_quiting), false, false);
                                    defHandler.sendEmptyMessageDelayed(quit_timeout, 5000);
                                }
                            })
                            .setNegativeButton(R.string.cancel, null).create().show();
                }
            }
        };
        factoryPopupWindow = new DeviceFactoryPopupWindow(FactoryAddDevicesActivity.this, this, cancelBeforeListener);
    }

    private void initBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (Actions.ZHUJI_FACTORY.equals(intent.getAction())) {
                    int code = intent.getIntExtra("code", -100);
                    if (code == CodeMenu.rp_pfactory_into.value()) {
                        cancelInProgress();
                        defHandler.removeMessages(submit_timeout);
                        showProgressDialogIntoFactoryModel();
                    } else if (code == CodeMenu.rp_pfactory_into_type_have.value()) {
                        cancelInProgress();
                        defHandler.removeMessages(submit_timeout);
                        Toast.makeText(FactoryAddDevicesActivity.this, getString(R.string.activity_add_devicefactory_havetype), Toast.LENGTH_LONG).show();
                    } else if (code == CodeMenu.rp_pfactory_into_type_nothave.value()) {
                        cancelInProgress();
                        defHandler.removeMessages(submit_timeout);
                        Toast.makeText(FactoryAddDevicesActivity.this, getString(R.string.activity_add_devicefactory_nothavetype), Toast.LENGTH_LONG).show();
                    } else if (code == CodeMenu.rp_pfactory_exit.value()) {
                        cancelInProgress();
                        defHandler.removeMessages(quit_timeout);
                        dismissProgressDialogIntoFactoryModel();
                        if (addSuccess) {
                            Toast.makeText(FactoryAddDevicesActivity.this, getString(R.string.activity_add_devicefactory_addsuccess), Toast.LENGTH_LONG).show();
                            finish();
                        }
                    } else if (code == CodeMenu.ac_factory_1_1_1.value()) {
                        setKeySuccessOrFailed(true, 1, 1);
                    } else if (code == CodeMenu.ac_factory_1_1_0.value()) {
                        setKeySuccessOrFailed(false, 1, 1);
                    } else if (code == CodeMenu.ac_factory_1_2_1.value()) {
                        setKeySuccessOrFailed(true, 2, 1);
                    } else if (code == CodeMenu.ac_factory_1_2_0.value()) {
                        setKeySuccessOrFailed(false, 2, 1);
                    } else if (code == CodeMenu.ac_factory_1_3_1.value()) {
                        setKeySuccessOrFailed(true, 3, 1);
                    } else if (code == CodeMenu.ac_factory_1_3_0.value()) {
                        setKeySuccessOrFailed(false, 3, 1);
                    } else if (code == CodeMenu.ac_factory_2_1_1.value()) {
                        setKeySuccessOrFailed(true, 1, 2);
                    } else if (code == CodeMenu.ac_factory_2_1_0.value()) {
                        setKeySuccessOrFailed(false, 1, 2);
                    } else if (code == CodeMenu.ac_factory_2_2_1.value()) {
                        setKeySuccessOrFailed(true, 2, 2);
                    } else if (code == CodeMenu.ac_factory_2_2_0.value()) {
                        setKeySuccessOrFailed(false, 2, 2);
                    } else if (code == CodeMenu.ac_factory_2_3_1.value()) {
                        setKeySuccessOrFailed(true, 3, 2);
                    } else if (code == CodeMenu.ac_factory_2_3_0.value()) {
                        setKeySuccessOrFailed(false, 3, 2);
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Actions.ZHUJI_FACTORY);
        registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 200:  //相册选择
                if (data != null) {
                    Util.startPhotoZoom(this, data.getData(), iDLogoUri, 150, 150);
                }
                break;
            case 300:  //拍照
                if (resultCode == -1) {
                    Util.startPhotoZoom(this, iDLogoUri, iDLogoUri, 150, 150);
                }
                break;
            case 400: //剪裁后
                if (data != null) {
                    //方式1
//				try {
//					InputStream bitsStream = getContentResolver().openInputStream(iDLogoUri);
//					logo.setImageBitmap(BitmapFactory.decodeStream(bitsStream));
//					logo_encode = Util.convertBitmapToBase64String(Bitmap.CompressFormat.JPEG, bitsStream);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
                    //方式二
                    try {
                        Bitmap logoBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(iDLogoUri));
                        logo.setImageBitmap(logoBitmap);
                        logo_encode = Util.convertBitmapToBase64String(Bitmap.CompressFormat.JPEG, logoBitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }

    public void back(View v) {
        finish();
    }

    public void submitToAddDevice(View v) {
        if (zhuji == null) {
            Toast.makeText(FactoryAddDevicesActivity.this, getString(R.string.activity_add_devicefactory_nozhuji), Toast.LENGTH_LONG).show();
            return;
        }
        showInProgress(getString(R.string.activity_add_devicefactory_submiting), false, false);
        SyncMessage message = new SyncMessage();
        message.setCommand(CommandMenu.rq_pfactory.value());
        message.setDeviceid(zhuji.getId());
        if ("".equals(name.getText().toString())) {
            Toast.makeText(FactoryAddDevicesActivity.this, getString(R.string.activity_add_devicefactory_submitname), Toast.LENGTH_LONG).show();
            return;
        }
        if ("".equals(type.getText().toString())) {
            Toast.makeText(FactoryAddDevicesActivity.this, getString(R.string.activity_add_devicefactory_submittype), Toast.LENGTH_LONG).show();
            return;
        }
//		if ("".equals(name.getText().toString())) {
//			Toast.makeText(FactoryAddDevicesActivity.this, getString(R.string.activity_add_devicefactory_submitname), Toast.LENGTH_LONG).show();
//			return;
//		}
        JSONObject pro = new JSONObject();
        if (MODEL_FACTORY_TYPE_FACTORY.equals(model)) {
            message.setCode(CodeMenu.rq_pfactory_into.value());

            pro.put("logo_suffix", ".jpg");
            pro.put("logo_file", logo_encode);  //base64编码的图片文件
            pro.put("name", name.getText().toString());
            pro.put("type", type.getText().toString());
            pro.put("barcode", barcode.getText().toString());
            pro.put("ctype", ctype_key);
            pro.put("role", DataCenterSharedPreferences.getInstance(getApplicationContext(), Constant.CONFIG).getString(Constant.LOGIN_ROLE, ""));
            try {
                message.setSyncBytes(pro.toJSONString().getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "不支持utf-8");
            }
        } else {
            message.setCode(CodeMenu.rq_pfactory_into_activation.value());

            pro.put("type", type.getText().toString());
            try {
                message.setSyncBytes(pro.toJSONString().getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "不支持utf-8");
            }
        }
        SyncMessageContainer.getInstance().produceSendMessage(message);
        defHandler.sendEmptyMessageDelayed(submit_timeout, 5000);  //5秒超时
    }

    class loadAllZhujiInfo implements Runnable {
        private int what;

        public loadAllZhujiInfo() {
        }

        public loadAllZhujiInfo(int what) {
            this.what = what;
        }

        @Override
        public void run() {
            List<ZhujiInfo> deviceList = new ArrayList<ZhujiInfo>();
//            deviceList.add(DatabaseOperator.getInstance(FactoryAddDevicesActivity.this).queryDeviceZhuJiInfo(dcsp.getString(Constant.APP_MASTERID, "")));
            //替换
            deviceList.add(DatabaseOperator.getInstance(FactoryAddDevicesActivity.this).queryDeviceZhuJiInfo(ZhujiListFragment.getMasterId()));
            Message m = defHandler.obtainMessage(this.what);
            m.obj = deviceList;
            defHandler.sendMessage(m);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.deviceinfo_logo: //更换图片
                showLogoDialog();
                break;

            default:
                break;
        }
    }

    private void showProgressDialogIntoFactoryModel() {
        if (factoryPopupWindow == null) {
            factoryPopupWindow = new DeviceFactoryPopupWindow(this, this, cancelBeforeListener);
        }
        if (ctype_key.equals(ControlTypeMenu.xiaxing_2.value())) {
            factoryPopupWindow.getFactory_tips().setText(getString(R.string.activity_add_devicefactory_twoftips));
            factoryPopupWindow.getF_1_off().setVisibility(View.VISIBLE);
            factoryPopupWindow.getF_2_off().setVisibility(View.VISIBLE);
            factoryPopupWindow.getF_3_off().setVisibility(View.VISIBLE);
            factoryPopupWindow.getF_1_off().setText(getString(R.string.activity_add_devicefactory_triggeroff));
            factoryPopupWindow.getF_2_off().setText(getString(R.string.activity_add_devicefactory_triggeroff));
            factoryPopupWindow.getF_3_off().setText(getString(R.string.activity_add_devicefactory_triggeroff));
            factoryPopupWindow.getF_1_on().setText(getString(R.string.activity_add_devicefactory_triggeron));
            factoryPopupWindow.getF_2_on().setText(getString(R.string.activity_add_devicefactory_triggeron));
            factoryPopupWindow.getF_3_on().setText(getString(R.string.activity_add_devicefactory_triggeron));
        } else {
            factoryPopupWindow.getFactory_tips().setText(getString(R.string.activity_add_devicefactory_ontftips));
            factoryPopupWindow.getF_1_off().setVisibility(View.GONE);
            factoryPopupWindow.getF_2_off().setVisibility(View.GONE);
            factoryPopupWindow.getF_3_off().setVisibility(View.GONE);
            factoryPopupWindow.getF_1_on().setText(getString(R.string.activity_add_devicefactory_trigger));
            factoryPopupWindow.getF_2_on().setText(getString(R.string.activity_add_devicefactory_trigger));
            factoryPopupWindow.getF_3_on().setText(getString(R.string.activity_add_devicefactory_trigger));
        }

        if (!factoryPopupWindow.isShowing()) {
            defHandler.sendEmptyMessageDelayed(factory_timeout, 1000 * 180); //3分钟工厂模式超时
            factoryPopupWindow.showAtLocation(this.getWindow().getDecorView(), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
        }
    }

    /**
     * 取消主机搜索提示
     */
    private void dismissProgressDialogIntoFactoryModel() {
        if (factoryPopupWindow != null && factoryPopupWindow.isShowing()) {
            factoryPopupWindow.dismiss();
            factoryPopupWindow = null;
        }
    }

    @Override
    protected void onDestroy() {
        defHandler.removeMessages(factory_timeout);
        super.onDestroy();
    }

    /**
     * 设置成功还是失败
     *
     * @param success 成功还是失败
     * @param count   第几个设备
     * @param key     第几个键
     */
    private void setKeySuccessOrFailed(boolean success, int count, int key) {
        switch (count) {
            case 3:
                if (ControlTypeMenu.xiaxing_2.value().equals(ctype_key)) {
                    if (key == 1) {
                        setSuccessOrFailedTips(success, false, factoryPopupWindow.getF_3_on());
                    } else if (key == 2) {
                        setSuccessOrFailedTips(success, true, factoryPopupWindow.getF_3_off());
                    }
                } else {
                    setSuccessOrFailedTips(success, true, factoryPopupWindow.getF_3_on());
                }
                setKeySuccessOrFailed(true, 2, key);
                break;
            case 2:
                if (ControlTypeMenu.xiaxing_2.value().equals(ctype_key)) {
                    if (key == 1) {
                        setSuccessOrFailedTips(success, false, factoryPopupWindow.getF_2_on());
                    } else if (key == 2) {
                        setSuccessOrFailedTips(success, false, factoryPopupWindow.getF_2_off());
                    }
                } else {
                    setSuccessOrFailedTips(success, false, factoryPopupWindow.getF_2_on());
                }
                setKeySuccessOrFailed(true, 1, key);
                break;
            case 1:
                if (DataCenterSharedPreferences.getInstance(getApplicationContext(), Constant.CONFIG).getString(Constant.LOGIN_ROLE, "").equals("app_role_superadmin")) {
                    if (ControlTypeMenu.xiaxing_2.value().equals(ctype_key)) {
                        if (key == 1) {
                            setSuccessOrFailedTips(success, false, factoryPopupWindow.getF_1_on());
                        } else if (key == 2) {
                            setSuccessOrFailedTips(success, true, factoryPopupWindow.getF_1_off());
                        }
                    } else {
                        setSuccessOrFailedTips(success, true, factoryPopupWindow.getF_1_on());
                    }
                } else {
                    if (ControlTypeMenu.xiaxing_2.value().equals(ctype_key)) {
                        if (key == 1) {
                            setSuccessOrFailedTips(success, false, factoryPopupWindow.getF_1_on());
                        } else if (key == 2) {
                            setSuccessOrFailedTips(success, false, factoryPopupWindow.getF_1_off());
                        }
                    } else {
                        setSuccessOrFailedTips(success, false, factoryPopupWindow.getF_1_on());
                    }
                }
                break;
            default:
                break;
        }
    }

    private void setSuccessOrFailedTips(boolean success, boolean over, final TextView tipsView) {
        if (success) {
            tipsView.setBackgroundResource(R.color.greenyellow);
            tipsView.setText(R.string.activity_add_devicefactory_triggersucc);
            if (over) {
                defHandler.removeMessages(failed_tip_back);
                defHandler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (!addSuccess) {
                            addSuccess = true;
                            showInProgress(getString(R.string.activity_add_devicefactory_quiting), false, false);
                            SyncMessage message = new SyncMessage();
                            message.setCommand(CommandMenu.rq_pfactory.value());
                            message.setCode(CodeMenu.rq_pfactory_exit.value());
                            message.setDeviceid(zhuji.getId());
                            SyncMessageContainer.getInstance().produceSendMessage(message);
                            defHandler.sendEmptyMessageDelayed(quit_timeout, 5000);
                        }
                    }
                }, 3000);
            }
        } else {
            tipsView.setBackgroundResource(R.color.red);
            tipsView.setText(R.string.activity_add_devicefactory_triggerfailed);
            Message msg1 = defHandler.obtainMessage(failed_tip_back);
            msg1.obj = tipsView;
            defHandler.sendMessageDelayed(msg1, 3000);
        }
    }

    private void showLogoDialog() {
        new AlertDialog.Builder(this).setTitle(getString(R.string.activity_add_devicefactory_logo_title)).setMessage(getString(R.string.activity_add_devicefactory_logo_message))
                .setPositiveButton(getString(R.string.album), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //调用相册获取图片。不过如果是大图会有问题，只适合小图，因为大图可能是缩略图
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        startActivityForResult(intent, 200);
                    }
                })
                .setNegativeButton(getString(R.string.camera), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, iDLogoUri);
                        startActivityForResult(intent, 300);
                    }
                }).show();
    }

    private void initActivationView() {
        findViewById(R.id.barcode_layout).setVisibility(View.GONE);
        findViewById(R.id.ctype_layout).setVisibility(View.GONE);
        ((Button) findViewById(R.id.title_button)).setText(getString(R.string.activity_add_deviceactivation_title));
        ((Button) findViewById(R.id.deviceinfo_update_btn)).setText(getString(R.string.activity_add_deviceactivation_submit));
    }

    private void initDeviceInfo() {
        showInProgress(getString(R.string.loading), false, true);
        JavaThreadPool.getInstance().excute(new Runnable() {

            @Override
            public void run() {
                String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("type", deviceInfo.getType());
//					pJsonObject.put("lang",Locale.getDefault().getLanguage()+"-"+Locale.getDefault().getCountry());
//					String result = HttpRequestUtils.requestHttpServer(server+"/jdm/service/device?v="+URLEncoder.encode(SecurityUtil.crypt(pJsonObject.toJSONString(), Constant.KEY_HTTP)),FactoryAddDevicesActivity.this,defHandler);
                String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/t/device", pJsonObject, FactoryAddDevicesActivity.this);
                //-1参数为空
                if (result != null && result.length() > 5) {
                    JSONObject info = null;
                    try {
                        info = JSONObject.parseObject(result);
                    } catch (Exception e) {
                        if (info == null) {
                            defHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast.makeText(FactoryAddDevicesActivity.this, getString(R.string.net_error_response), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                    deviceInfo.setLogo(String.valueOf(info.get("deviceLogo")));
                    deviceInfo.setName(String.valueOf(info.get("deviceName")));
                    deviceInfo.setType(String.valueOf(info.get("deviceType")));
                    defHandler.sendEmptyMessage(init_devicesuccess);
                }

            }
        });
    }
}
