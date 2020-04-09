package com.smartism.znzk.activity.scene;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.device.AllEquipmentListActivity;
import com.smartism.znzk.activity.device.TriggerEqipmentActivity;
import com.smartism.znzk.activity.device.ZhujiListFragment;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.FoundInfo;
import com.smartism.znzk.domain.Tips;
import com.smartism.znzk.domain.XiaXingInfo;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.DevicesMenuPopupWindow;
import com.smartism.znzk.view.MyListView;
import com.smartism.znzk.view.SceneTypeSpinnerPopWindow;
import com.smartism.znzk.view.SceneTypeSpinnerPopWindow.IOnItemSelectedListener;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.OnItemClickListener;
import com.smartism.znzk.view.pickerview.TimePickerView;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.smartism.znzk.activity.scene.SceneActivity.SecuritySceneType_Arming;
import static com.smartism.znzk.activity.scene.SceneActivity.SecuritySceneType_DesArming;
import static com.smartism.znzk.activity.scene.SceneActivity.SecuritySceneType_Home;
import static com.smartism.znzk.activity.scene.SceneActivity.SecuritySceneType_Normal;

public class AddSceneActivity extends ActivityParentActivity implements OnClickListener, IOnItemSelectedListener {
    private List<DeviceInfo> triggerDeviceInfos = new ArrayList<DeviceInfo>();
    private ArrayList<String> str;
    private ArrayList<String> str1;
    private List<XiaXingInfo> xiaXing;
    private List<Tips> mTip;
    private MyListView lv_operation_equipment, lv_operation_trigger;
    private OperationEquipmentAdapter mOperationAdapter;
    private EquipmentAdapter mmAdapter;

    private List<DeviceInfo> devices = new ArrayList<DeviceInfo>();
    private List<DeviceInfo> deviceinfos = new ArrayList<DeviceInfo>();
    private List<DeviceInfo> devicesTips = new ArrayList<DeviceInfo>();
    public List<DeviceInfo> deviceInfos;
    public DeviceInfo zhuji, deviceInfo, info;
    private final int dHandlerWhat_loadsuccess = 2;
    private static final int CONTROL = 0;
    private static final int TRIGGER = 10;
    private FoundInfo result = new FoundInfo();
    private static String[] arr = new String[]{"一", "二", "三", "四", "五", "六"};
    private String[] times = null;
    private View view;
    private SceneTypeSpinnerPopWindow type_spinPopW;
    DevicesMenuPopupWindow itemMenu;

    private int type;
    private EditText et;
    private TextView time, cycle, triggers, triggers_text, controls, controls_text, addscene_controls;
    private RelativeLayout time_layout, cycle_layout, triggers_layout, controls_layout, pop_type_layout;
    private LinearLayout ll_trigger, ll_control;
    private TimePickerView timePickerView;
    private AlertView cycleAlertView;
    private String[] i = new String[8];
    private String cycleTime = "00000001";
    private List<String> list;


    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Bundle b = msg.getData();
                    String obj = b.getString("obj");
                    String dec = b.getString("dec");
                    int pos = b.getInt("pos");
                    if (devices != null && devices.size() >= 1) {
                        devices.get(pos).setTip(obj);
                        devices.get(pos).setTipName(dec);
                        //添加到map中，当设备没有选择操作的时候
                        List<Tips> tipsList = new ArrayList<>();
                        Tips t = new Tips();
                        t.setC(obj);
                        t.setE(dec);
                        tipsList.add(t);
                        tipMap.put(devices.get(pos).getId(), tipsList);
                        mOperationAdapter.notifyDataSetChanged();
                    }
                    break;
                case 1:
                    Bundle bl = msg.getData();
                    String objs = bl.getString("objs");
                    String decs = bl.getString("decs");
                    int p = bl.getInt("p");
                    if (triggerDeviceInfos != null && triggerDeviceInfos.size() >= 1) {
                        triggerDeviceInfos.get(p).setTip(objs);
                        triggerDeviceInfos.get(p).setTipName(decs);
                        mmAdapter.notifyDataSetChanged();
                    }
                    break;
                case 2:
                    Bundle bs = msg.getData();
                    String obje = bs.getString("obje");
                    String dece = bs.getString("dece");
                    int ps = bs.getInt("pose");
                    if (triggerDeviceInfos != null && triggerDeviceInfos.size() >= 1) {
                        triggerDeviceInfos.get(ps).setTip(obje);
                        triggerDeviceInfos.get(ps).setTipName(dece);
                        mmAdapter.notifyDataSetChanged();
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    private Handler defaultHandler = new WeakRefHandler(mCallback);

    private void refreshData() {
        JavaThreadPool.getInstance().excute(new loadAllDevicesInfo(dHandlerWhat_loadsuccess));
    }

    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_scene);

        Intent intent = getIntent();
        result = (FoundInfo) intent.getSerializableExtra("result");
        times = new String[]{
                getResources().getString(R.string.monday),
                getResources().getString(R.string.tuesday),
                getResources().getString(R.string.wednesday),
                getResources().getString(R.string.thursday),
                getResources().getString(R.string.friday),
                getResources().getString(R.string.saturday),
                getResources().getString(R.string.sunday),
                getResources().getString(R.string.everyday)};
        initView();
        initDate();
    }

    ;

    /*
     * @Override protected void onRestart() { super.onRestart(); if (type == 2)
     * { if (triggerDeviceInfos.size() >= 1) {
     * ll_trigger.setVisibility(View.VISIBLE); }else{
     * ll_trigger.setVisibility(View.GONE); } if (devices.size() >= 1) {
     * ll_control.setVisibility(View.VISIBLE); }else{
     * ll_control.setVisibility(View.GONE); } if (result != null) { if
     * (result.getTriggerInfos() != null) { if
     * (result.getTriggerInfos().get(0).getDevice() != 0) {
     * ll_trigger.setVisibility(View.VISIBLE); }else{
     * ll_trigger.setVisibility(View.GONE); } } if (result.getControlInfos() !=
     * null) { if (result.getControlInfos().size() >= 1) {
     * ll_control.setVisibility(View.VISIBLE); }else{
     * ll_control.setVisibility(View.GONE); } }
     *
     * }
     *
     * } else { if (devices.size() >= 1) {
     * ll_control.setVisibility(View.VISIBLE); }else{
     * ll_control.setVisibility(View.GONE); } if (result != null &&
     * result.getControlInfos() != null) { if (result.getControlInfos().size()
     * >= 1) { ll_control.setVisibility(View.VISIBLE); }else{
     * ll_control.setVisibility(View.GONE); } } } }
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (type == 2) {
            if (result != null && result.getTriggerInfos() != null && result.getTriggerInfos().size() > 0) {
                if (result.getTriggerInfos().get(0).getDevice() != 0) {
                    ll_trigger.setVisibility(View.VISIBLE);
                } else {
                    if (triggerDeviceInfos != null) {
                        if (triggerDeviceInfos.size() >= 1) {
                            ll_trigger.setVisibility(View.VISIBLE);
                        } else {
                            ll_trigger.setVisibility(View.GONE);
                        }
                    }

                }
            } else {
                if (triggerDeviceInfos != null) {
                    if (triggerDeviceInfos.size() >= 1) {
                        ll_trigger.setVisibility(View.VISIBLE);
                    } else {
                        ll_trigger.setVisibility(View.GONE);
                    }
                }
            }

            if (result != null && result.getControlInfos() != null) {
                if (result.getControlInfos().size() >= 1) {
                    ll_control.setVisibility(View.VISIBLE);
                } else {
                    if (devices != null) {
                        if (devices.size() >= 1) {
                            ll_control.setVisibility(View.VISIBLE);
                        } else {
                            ll_control.setVisibility(View.GONE);
                        }
                    }
                }
            } else {
                if (devices != null) {
                    if (devices.size() >= 1) {
                        ll_control.setVisibility(View.VISIBLE);
                    } else {
                        ll_control.setVisibility(View.GONE);
                    }
                }
            }

        } else {

            if (result != null && result.getControlInfos() != null && !result.getControlInfos().isEmpty()) {
                ll_control.setVisibility(View.VISIBLE);
            } else if (devices != null && devices.size() >= 1) {
                ll_control.setVisibility(View.VISIBLE);
            } else {
                ll_control.setVisibility(View.GONE);
            }
            /*if (result != null && result.getControlInfos() != null) {
                if (result.getControlInfos().size() >= 1) {
					ll_control.setVisibility(View.VISIBLE);
				} else {
					ll_control.setVisibility(View.GONE);
				}
			}*/
        }
    }

    int p1 = 0;
    boolean[] chooseTip;

    /**
     * 多选对话框
     *
     * @param str
     * @param str1
     */
    public void showDialog5(final ArrayList<String> str, final ArrayList<String> str1) {
        final int size = str.size();
        final String[] items = (String[]) str.toArray(new String[size]);
        chooseTip = new boolean[size];

        if (devices != null && devices.size() > 0) {

            for (int i = 0; i < devices.size(); i++) {
                if (devices.get(i).getId() == deviceInfo.getId()) {
                    List<Tips> tipsList = tipMap.get(deviceInfo.getId());
                    if (tipsList != null) {
                        for (int j = 0; j < tipsList.size(); j++) {
                            for (int k = 0; k < str1.size(); k++) {
                                if (str1.get(k).equals(tipsList.get(j).getC())) {
//                                    devices.get(i).setFlag(true);
                                    chooseTip[k] = true;
                                }
                            }

                        }
                    }
                }
            }
        }
        final List<Integer> t = new ArrayList<Integer>();
        AlertDialog.Builder builder = new AlertDialog.Builder(AddSceneActivity.this);
        builder.setTitle(getString(R.string.activity_devices_list_choose_operation));
        builder.setMultiChoiceItems(items, chooseTip, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
            }
        });
        builder.setPositiveButton(getString(R.string.pickerview_submit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<Tips> tipList = new ArrayList<Tips>();
                if (deviceInfo != null && xiaXing != null && xiaXing.size() > 0) {
                    for (int i = 0; i < chooseTip.length; i++) {
                        if (chooseTip[i]) {
                            Tips tips = new Tips();
                            tips.setC(xiaXing.get(i).getS());
                            tips.setE(xiaXing.get(i).getN());
                            tipList.add(tips);
                        }
                    }
                }
                if (tipList != null && !tipList.isEmpty()) {
                    tipMap.put(deviceInfo.getId(), tipList);
                    if (mOperationAdapter != null) {
                        mOperationAdapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(AddSceneActivity.this, getResources().getString(R.string.activity_devices_no_choose_operation), Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(getString(R.string.pickerview_cancel), null);
        builder.show();
    }

    int p2 = 0;

    public void showDialog6(final ArrayList<String> str) {
        final int size = str.size();
        final String[] items = (String[]) str.toArray(new String[size]);
        boolean[] flag = new boolean[size];
        if (triggerDeviceInfos != null && triggerDeviceInfos.size() > 0) {
            for (int i = 0; i < triggerDeviceInfos.size(); i++) {
                if (triggerDeviceInfos.get(i).getId() == info.getId()) {
                    for (int j = 0; j < str.size(); j++) {
                        if (str.get(j).equals(triggerDeviceInfos.get(i).getTipName())) {
                            triggerDeviceInfos.get(i).setFlag(true);
                            flag[j] = true;
                        }
                    }
                }
            }
        }

        final List<Integer> t = new ArrayList<Integer>();
        AlertDialog.Builder builder = new AlertDialog.Builder(AddSceneActivity.this);
        builder.setTitle(getString(R.string.activity_devices_list_choose_operation));
        if (items != null && info != null) {
            for (int i = 0; i < items.length; i++) {
                if (info.getTipName().equals(items[i])) {
                    p2 = i;
                }
            }
        }
        builder.setSingleChoiceItems(items, p2, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                p2 = which;

            }
        });
        builder.setPositiveButton(getString(R.string.pickerview_submit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (info != null && xiaXing != null && xiaXing.size() > 0) {
                    info.setTip(xiaXing.get(p2).getS());
                    info.setTipName(xiaXing.get(p2).getN());
                }
                if (mmAdapter != null) {
                    mmAdapter.notifyDataSetChanged();
                }
            }
        });
        /*builder.setMultiChoiceItems(items, flag, new OnMultiChoiceClickListener() {
            @Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {

				if (isChecked) {
					t.add(which);

				} else {
					for (int i = 0; i < triggerDeviceInfos.size(); i++) {
						if (triggerDeviceInfos.get(i).getId() == info.getId()) {
							if (str.get(which).equals(triggerDeviceInfos.get(i).getTipName())) {
								triggerDeviceInfos.get(i).setFlag(false);
								triggerDeviceInfos.remove(i);
							}

						}
					}
				}

			}
		});
		builder.setPositiveButton(getString(R.string.pickerview_submit), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				for (int i = 0; i < t.size(); i++) {
					for (int j = 0; j < triggerDeviceInfos.size(); j++) {
						if (info.getId() == triggerDeviceInfos.get(j).getId()
								&& xiaXing.get(t.get(i)).getN().equals(triggerDeviceInfos.get(j).getTipName())
								&& !triggerDeviceInfos.get(j).isFlag()) {
							triggerDeviceInfos.remove(j);
							j--;
						}
					}

					DeviceInfo test = info.deepClone();
					test.setTip(xiaXing.get(t.get(i)).getS());
					test.setTipName(xiaXing.get(t.get(i)).getN());
					triggerDeviceInfos.add(test);

				}

				if (mmAdapter != null) {
					mmAdapter.notifyDataSetChanged();
				}
			}

		});*/
        builder.setNegativeButton(getString(R.string.pickerview_cancel), null);
        builder.show();
    }

    private int p = 0;
    Map<Long, List<Tips>> tipMap = new HashMap<>();

    public void showSingleDailog() {
        final List<String> item = new ArrayList<String>();
        item.add(getString(R.string.activity_scene_item_outside));
        item.add(getString(R.string.activity_scene_item_home));
        item.add(getString(R.string.devices_list_menu_dialog_jsbts));
        item.add(getString(R.string.activity_scene_item_inhome));
        final int size = item.size();
        final String[] items = (String[]) item.toArray(new String[size]);
        AlertDialog.Builder builder = new AlertDialog.Builder(AddSceneActivity.this);
        builder.setTitle(getString(R.string.activity_devices_list_choose_operation));
        if (item.size() > 0 && deviceInfo != null) {
            for (int i = 0; i < item.size(); i++) {
                if (deviceInfo.getTipName().equals(item.get(i))) {
                    p = i;
                }
            }
        }
        builder.setSingleChoiceItems(items, p, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                p = which;

            }
        });
        builder.setPositiveButton(getString(R.string.pickerview_submit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deviceInfo.setTipName(items[p]);
                mOperationAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton(getString(R.string.pickerview_cancel), null);
        builder.show();
    }

    public void showSingleDailog2(ArrayList<String> str) {
        final int size = str.size();
        final String[] items = (String[]) str.toArray(new String[size]);
        AlertDialog.Builder builder = new AlertDialog.Builder(AddSceneActivity.this);
        builder.setTitle(getString(R.string.activity_devices_list_choose_operation));
        if (items != null && info != null) {
            for (int i = 0; i < items.length; i++) {
                if (info.getTipName().equals(items[i])) {
                    p = i;
                }
            }
        }
        builder.setSingleChoiceItems(items, p, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                p = which;

            }
        });
        builder.setPositiveButton(getString(R.string.pickerview_submit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (info != null) {
                    info.setTip(mTip.get(p).getC());
                    info.setTipName(mTip.get(p).getE());
                }
                if (mmAdapter != null) {
                    mmAdapter.notifyDataSetChanged();
                }
            }
        });
        builder.setNegativeButton(getString(R.string.pickerview_cancel), null);
        builder.show();
    }

    private void initView() {
        view = findViewById(R.id.view);

        Cursor cursor = DatabaseOperator.getInstance(AddSceneActivity.this).getWritableDatabase()
                .rawQuery("select * from DEVICE_STATUSINFO order by sort desc", new String[]{});
        deviceInfos = new ArrayList<DeviceInfo>();
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                DeviceInfo info = DatabaseOperator.getInstance(getApplicationContext()).buildDeviceInfo(cursor);

                deviceInfos.add(info);

            }
            cursor.close();
        }
        ll_control = (LinearLayout) findViewById(R.id.ll_control);
        ll_trigger = (LinearLayout) findViewById(R.id.ll_trigger);
        itemMenu = new DevicesMenuPopupWindow(AddSceneActivity.this, this);
        et = (EditText) findViewById(R.id.addscene_name);
        et.addTextChangedListener(textWatcher);
        lv_operation_equipment = (MyListView) findViewById(R.id.lv_operation_equipment);
        lv_operation_trigger = (MyListView) findViewById(R.id.lv_operation_trigger);
        // pop_type_layout = (RelativeLayout)
        // findViewById(R.id.addscene_type_pop_layout);
        // type_layout.setOnClickListener(this);
        // pop_type_layout.setOnClickListener(this);
        controls_layout = (RelativeLayout) findViewById(R.id.addscene_controls_layout);
        controls_layout.setOnClickListener(this);
        controls = (TextView) findViewById(R.id.addscene_controls);
        controls_text = (TextView) findViewById(R.id.addscene_controls_text);
        addscene_controls = (TextView) findViewById(R.id.addscene_controls);
        time_layout = (RelativeLayout) findViewById(R.id.addscene_time_layout);
        time_layout.setOnClickListener(this);
        time = (TextView) findViewById(R.id.addscene_time);
        cycle_layout = (RelativeLayout) findViewById(R.id.addscene_cycle_layout);
        cycle_layout.setOnClickListener(this);
        cycle = (TextView) findViewById(R.id.addscene_cycle);
        triggers_layout = (RelativeLayout) findViewById(R.id.addscene_trigger_layout);
        triggers_layout.setOnClickListener(this);
        triggers = (TextView) findViewById(R.id.addscene_trigger);
        triggers_text = (TextView) findViewById(R.id.addscene_trigger_text);

        type = getIntent().getIntExtra("type", 0);

        list = new ArrayList<String>();
        list.add(getString(R.string.activity_editscene_type_0));
        list.add(getString(R.string.activity_editscene_type_1));
        list.add(getString(R.string.activity_editscene_type_2));

        if (result != null) {
            if (result.getTip() == 1) {
                et.setText(result.getName());
                if (result.getTriggerInfos() != null && result.getTriggerInfos().size() > 0) {
                    // triggerDeviceInfos.clear();
                    long hour = result.getTriggerInfos().get(0).getTime() / 60;
                    long minute = result.getTriggerInfos().get(0).getTime() % 60;
                    if (minute < 10) {
                        time.setText(hour + ":0" + minute);
                    } else {
                        time.setText(hour + ":" + minute);
                    }

                    cycleTime = result.getTriggerInfos().get(0).getCycle();
                    StringBuffer b = new StringBuffer();
                    if (cycleTime != null) {
                        if (cycleTime.equals("00000001")) {
                            b.append(getString(R.string.everyday) + " ");
                        } else if (cycleTime.equals("00000010")) {
                            b.append(getString(R.string.sundays));
                        } else {
                            for (int i = 0; i < cycleTime.length(); i++) {
                                if (cycleTime.charAt(i) == '1') {
                                    b.append(getString(R.string.week) + (i + 1) + " ");

                                }
                            }
                        }

                    }
                    cycle.setText(b);
                    if (deviceInfos != null && deviceInfos.size() > 0) {
                        for (int i = 0; i < deviceInfos.size(); i++) {
                            for (int j = 0; j < result.getTriggerInfos().size(); j++) {
                                if (deviceInfos.get(i).getId() == result.getTriggerInfos().get(j).getDevice()) {
                                    deviceInfos.get(i).setTip(result.getTriggerInfos().get(j).getCommand());
                                    final long did = deviceInfos.get(i).getId();
                                    final int pos = i;
                                    final String l = Locale.getDefault().getLanguage() + "-"
                                            + Locale.getDefault().getCountry();
                                    showInProgress(getString(R.string.loading), false, false);
                                    JavaThreadPool.getInstance().excute(new Runnable() {

                                        @Override
                                        public void run() {
                                            DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(
                                                    AddSceneActivity.this, Constant.CONFIG);
                                            String server = dcsp
                                                    .getString(Constant.HTTP_DATA_SERVERS, "");
                                            JSONObject pJsonObject = new JSONObject();
                                            pJsonObject.put("did", did);
                                            pJsonObject.put("lang", l);
//											final String result = HttpRequestUtils
//													.requestHttpServer(
//															 server + "/jdm/service/dcommands?v="
//																	+ URLEncoder.encode(SecurityUtil.crypt(
//																			pJsonObject.toJSONString(), Constant.KEY_HTTP)),
//																	AddSceneActivity.this, defaultHandler);
                                            final String result = HttpRequestUtils.requestoOkHttpPost( server
                                                    + "/jdm/s3/d/dcomms", pJsonObject, AddSceneActivity.this);

                                            defaultHandler.post(new Runnable() {

                                                @Override
                                                public void run() {
                                                    cancelInProgress();
                                                    String o = null;
                                                    try {
                                                        o = result;
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                    mTip = JSON.parseArray(o, Tips.class);
                                                    if (mTip != null) {
                                                        for (int k = 0; k < mTip.size(); k++) {
                                                            if (deviceInfos.get(pos).getTip()
                                                                    .equals(mTip.get(k).getC())) {
                                                                deviceInfos.get(pos).setTipName(mTip.get(k).getE());
                                                            }
                                                        }
                                                    }
                                                    triggerDeviceInfos.add(deviceInfos.get(pos));
                                                    mmAdapter.notifyDataSetChanged();
                                                }
                                            });
                                        }

                                    });

                                }
                            }

                        }
                    }
//                    triggerDeviceInfos.clear();
                    mmAdapter = new EquipmentAdapter(getApplicationContext(), triggerDeviceInfos);
                    lv_operation_trigger.setAdapter(mmAdapter);
                }
                type = result.getType();
                if (result.getControlInfos() != null) {
                    for (int j = 0; j < result.getControlInfos().size(); j++) {
                        if (deviceInfos != null && deviceInfos.size() > 0) {
                            for (int i = 0; i < deviceInfos.size(); i++) {
                                if (deviceInfos.get(i).getId() == Long
                                        .parseLong(result.getControlInfos().get(j).getDeviceId())) {
                                    final DeviceInfo info = deviceInfos.get(i).deepClone();
                                    info.setTip(result.getControlInfos().get(j).getCommand());
                                    if (info.getCak().equals("security")) {

                                        if (info.getTip().equals("0")) {
                                            info.setTipName(getString(R.string.activity_scene_item_home));
                                        } else if (info.getTip().equals("1")) {
                                            info.setTipName(getString(R.string.devices_list_menu_dialog_jsbts));
                                        } else if (info.getTip().equals("2")) {
                                            info.setTipName(getString(R.string.activity_scene_item_inhome));
                                        } else {
                                            info.setTipName(getString(R.string.activity_scene_item_outside));
                                        }

                                    }
                                    final long did = deviceInfos.get(i).getId();
                                    showInProgress(getString(R.string.loading), false, false);
                                    JavaThreadPool.getInstance().excute(new Runnable() {

                                        @Override
                                        public void run() {
                                            DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(
                                                    AddSceneActivity.this, Constant.CONFIG);
                                            String server = dcsp
                                                    .getString(Constant.HTTP_DATA_SERVERS, "");
                                            JSONObject pJsonObject = new JSONObject();
                                            pJsonObject.put("did", did);

//											final String result = HttpRequestUtils
//													.requestHttpServer(
//															 server + "/jdm/service/dkeycommands?v="
//																	+ URLEncoder.encode(SecurityUtil.crypt(
//																			pJsonObject.toJSONString(), Constant.KEY_HTTP)),
//																	AddSceneActivity.this, defaultHandler);
                                            final String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/d/dkeycomms", pJsonObject, AddSceneActivity.this);
                                            defaultHandler.post(new Runnable() {

                                                @Override
                                                public void run() {
                                                    cancelInProgress();
                                                    String o = null;
                                                    try {
                                                        o = result;
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }

                                                    xiaXing = JSON.parseArray(o, XiaXingInfo.class);

                                                    if (xiaXing != null) {
                                                        for (int k = 0; k < xiaXing.size(); k++) {
                                                            if (info.getCak().equals("control")
                                                                    && info.getTip().equals(xiaXing.get(k).getS())) {
                                                                info.setTipName(xiaXing.get(k).getN());
                                                            }
                                                        }
                                                    }
                                                    int index = -1;
                                                    for (int i = 0; i < devices.size(); i++) {
                                                        if (info.getId() == devices.get(i).getId()) {
                                                            index = i;
                                                            break;
                                                        }

                                                    }
                                                    Tips tips = new Tips();
                                                    tips.setC(info.getTip());
                                                    tips.setE(info.getTipName());
                                                    if (index == -1) {
                                                        info.tipsList.add(tips);
                                                        devices.add(info);
                                                        tipMap.put(info.getId(), info.getTipsList());
                                                    } else {
                                                        devices.get(index).tipsList.add(tips);
                                                        tipMap.put(info.getId(), devices.get(index).tipsList);
                                                    }
                                                    mOperationAdapter.notifyDataSetChanged();
                                                }
                                            });
                                        }

                                    });

                                }
                            }
                        }

                    }
//                    for (DeviceInfo dinfo : devices) {
//                        tipMap.put(dinfo.getId(), dinfo.getTipsList());
//                        //log.e("uuyyyyuuuuuuu-----","---》"+dinfo.getTipsList().size());
//                    }
                    mOperationAdapter = new OperationEquipmentAdapter(getApplicationContext(), devices);
                    lv_operation_equipment.setAdapter(mOperationAdapter);
                }
            }
        }

        type_spinPopW = new SceneTypeSpinnerPopWindow(this);
        type_spinPopW.refreshData(list, type);
        type_spinPopW.setItemListener(this);

        // et.setEnabled(true);
        if (type == SecuritySceneType_Normal || type == SecuritySceneType_Home || type == SecuritySceneType_DesArming || type == SecuritySceneType_Arming) { // 自定义类型和在家场景、设防和撤防场景
            time_layout.setVisibility(View.GONE);
            cycle_layout.setVisibility(View.GONE);
            view.setVisibility(View.GONE);
            triggers_layout.setVisibility(View.GONE);
            if (type == SecuritySceneType_Home || type == SecuritySceneType_DesArming || type == SecuritySceneType_Arming) {
                et.setEnabled(false);
            }
        } else if (type == 1) { // 定时类型
            timePickerView = new TimePickerView(this, TimePickerView.Type.HOURS_MINS);
            timePickerView.setTime(new Date());
            timePickerView.setCyclic(true);
            timePickerView.setCancelable(true);
            // 时间选择后回调
            timePickerView.setOnTimeSelectListener(new TimePickerView.OnTimeSelectListener() {

                @Override
                public void onTimeSelect(Date date) {
                    time.setText(getTime(date));
                }
            });

//            JSONObject o = new JSONObject();
//            o.put("name", getString(R.string.monday));
//            o.put("o", 0);
//            i[0] = o.toJSONString();
//            o = new JSONObject();
//            o.put("name", getString(R.string.tuesday));
//            o.put("o", 0);
//            i[1] = o.toJSONString();
//            o = new JSONObject();
//            o.put("name", getString(R.string.wednesday));
//            o.put("o", 0);
//            i[2] = o.toJSONString();
//            o = new JSONObject();
//            o.put("name", getString(R.string.thursday));
//            o.put("o", 0);
//            i[3] = o.toJSONString();
//            o = new JSONObject();
//            o.put("name", getString(R.string.friday));
//            o.put("o", 0);
//            i[4] = o.toJSONString();
//            o = new JSONObject();
//            o.put("name", getString(R.string.saturday));
//            o.put("o", 0);
//            i[5] = o.toJSONString();
//            o = new JSONObject();
//            o.put("name", getString(R.string.sunday));
//            o.put("o", 0);
//            i[6] = o.toJSONString();
//            o = new JSONObject();
//            o.put("name", getString(R.string.everyday));
//            o.put("o", 1);
//            i[7] = o.toJSONString();

            getCycleStrings(cycleTime);
            cycleAlertView = new AlertView(getString(R.string.activity_editscene_cycle_tytle), null,
                    getString(R.string.sure), null, i, this, AlertView.Style.Alert, new OnItemClickListener() {

                @Override
                public void onItemClick(Object o, int position) { // o的值为字符串
                    StringBuffer b = new StringBuffer();
                    if (position != -1) {
                        cycleTime = o.toString();
                    }
                    if (cycleTime.equals("00000001")) {
                        b.append(getString(R.string.everyday));
                    } else if (cycleTime.equals("00000010")) {
                        b.append(getString(R.string.sundays) + " ");
                    } else {
                        for (int i = 0; i < cycleTime.length(); i++) {

                            if (cycleTime.charAt(i) == '1') {
                                b.append(getString(R.string.week) + (i + 1) + " ");
                            }
                        }

                    }

                    cycle.setText(b);
                }

            });
            triggers_layout.setVisibility(View.GONE);
        } else if (type == 2) { // 联动场景
            if (triggerDeviceInfos.size() >= 1) {
                ll_trigger.setVisibility(View.VISIBLE);
            }
            time_layout.setVisibility(View.GONE);
            cycle_layout.setVisibility(View.GONE);
            view.setVisibility(View.GONE);
        }
    }

    private TextWatcher textWatcher = new TextWatcher() {
        private int editStart;
        private int editEnd;
        private int maxLen = 30; // the max byte

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            editStart = et.getSelectionStart();
            editEnd = et.getSelectionEnd();
            // 先去掉监听器，否则会出现栈溢出
            et.removeTextChangedListener(textWatcher);
            if (!TextUtils.isEmpty(et.getText())) {
                String etstring = et.getText().toString().trim();
                while (calculateLength(s.toString()) > maxLen) {
                    s.delete(editStart - 1, editEnd);
                    editStart--;
                    editEnd--;

                }
            }

            et.setText(s);
            et.setSelection(editStart);

            // 恢复监听器
            et.addTextChangedListener(textWatcher);
        }

        private int calculateLength(String etstring) {
            char[] ch = etstring.toCharArray();

            int varlength = 0;
            for (int i = 0; i < ch.length; i++) {
                if ((ch[i] >= 0x2E80 && ch[i] <= 0xFE4F) || (ch[i] >= 0xA13F && ch[i] <= 0xAA40) || ch[i] >= 0x80) { // 中文字符范围0x4e00
                    // 0x9fbb
                    varlength = varlength + 2;
                } else {
                    varlength++;
                }
            }
            // 这里也可以使用getBytes,更准确嘛
            // varlength = etstring.getBytes(CharSet.forName(GBK)).lenght;//
            // 编码根据自己的需求，注意u8中文占3个字节...
            return varlength;
        }
    };

    class OperationEquipmentAdapter extends BaseAdapter {
        private Context context;
        private List<DeviceInfo> devices = new ArrayList<DeviceInfo>();

        public OperationEquipmentAdapter(Context context, List<DeviceInfo> devices) {
            this.context = context;
            this.devices = devices;
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (null == convertView) {
                holder = new ViewHolder();
                convertView = View.inflate(context, R.layout.operation_equipment_list_item, null);
                holder.tv_operation_choose = (TextView) convertView.findViewById(R.id.tv_operation_choose);
                holder.tv_operation_equipment = (TextView) convertView.findViewById(R.id.tv_operation_equipment);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv_operation_choose.setTextColor(getResources().getColor(R.color.black));
            holder.tv_operation_equipment.setTextColor(getResources().getColor(R.color.black));

            DeviceInfo mInfo = devices.get(position);
            boolean isNull = ((mInfo.getWhere() == null || "".equals(mInfo.getWhere()) || "null".equals(mInfo.getWhere()))
                    && (mInfo.getType() == null || "".equals(mInfo.getType())));
            StringBuffer sb = new StringBuffer();
            sb.append(mInfo.getName());
            if (isNull) {
                holder.tv_operation_equipment.setText(sb.toString());
            } else {
                sb.append(" ");
                sb.append("( ");
                sb.append(((mInfo.getWhere() == null || "null".equals(mInfo.getWhere())) ? "" : mInfo.getWhere() + " "));
                sb.append((mInfo.getType()));
                sb.append(" )");
            }
            holder.tv_operation_equipment.setText(sb.toString());

//            if (devices.get(position).getType() != null && !devices.get(position).getType().equals("")) {
//                holder.tv_operation_equipment.setText(devices.get(position).getName() + ":" + devices.get(position).getType());
//            } else {
//                holder.tv_operation_equipment.setText(devices.get(position).getName());
//            }


            if (devices != null && devices.size() > 0 && devices.get(position).getCak() != null && devices.get(position).getCak().equals("control")) {
                List<Tips> tipList = tipMap.get(devices.get(position).getId());
                if (tipList != null) {
                    StringBuffer bs = new StringBuffer();
                    for (Tips t : tipList) {
                        bs.append(t.getE());
                        bs.append(",");
                    }
                    bs.deleteCharAt(bs.length() - 1);
                    holder.tv_operation_choose.setText(bs.toString());
                } else {
                    if (devices.get(position).getTipName() == null) {
                        final long uid = dcsp.getLong(Constant.LOGIN_APPID, 0);
                        final String code = dcsp.getString(Constant.LOGIN_CODE, "");
                        final long did = devices.get(position).getId();
                        showInProgress(getString(R.string.loading), false, false);
                        JavaThreadPool.getInstance().excute(new Runnable() {

                            @Override
                            public void run() {
                                DataCenterSharedPreferences dcsp = DataCenterSharedPreferences
                                        .getInstance(AddSceneActivity.this, Constant.CONFIG);
                                String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
                                JSONObject pJsonObject = new JSONObject();
                                pJsonObject.put("did", did);

                                final String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/d/dkeycomms", pJsonObject, AddSceneActivity.this);
                                defaultHandler.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        cancelInProgress();
                                        String o = null;
                                        try {
                                            o = result;
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        // Toast.makeText(AddSceneActivity.this,
                                        // o,
                                        // 1).show();
                                        String obj = null;
                                        String dec = null;
                                        xiaXing = JSON.parseArray(o, XiaXingInfo.class);
                                        if (xiaXing != null && !xiaXing.isEmpty()) {
                                            obj = xiaXing.get(0).getS();
                                            dec = xiaXing.get(0).getN();
                                        }

                                        Message msg = defaultHandler.obtainMessage(0);
                                        Bundle b = new Bundle();
                                        b.putString("obj", obj);
                                        b.putString("dec", dec);
                                        b.putInt("pos", position);
                                        msg.setData(b);
                                        defaultHandler.sendMessage(msg);
                                    }
                                });
                            }

                        });

                    }
                    holder.tv_operation_choose.setText(devices.get(position).getTipName());
                }


            } else {
                if (devices.get(position).getTipName() == null) {
                    devices.get(position).setTipName(getString(R.string.activity_scene_item_outside));
                    devices.get(position).setTip(3 + "");
                }
                if (devices.get(position).getTipName() != null) {
                    if (devices.get(position).getTipName()
                            .equals(getString(R.string.activity_scene_item_home))) {
                        devices.get(position).setTip(0 + "");
                    } else if (devices.get(position).getTipName()
                            .equals(getString(R.string.activity_scene_item_outside))) {
                        devices.get(position).setTip(3 + "");
                    } else if (devices.get(position).getTipName()
                            .equals(getString(R.string.devices_list_menu_dialog_jsbts))) {
                        devices.get(position).setTip(1 + "");
                    } else {
                        devices.get(position).setTip(2 + "");
                    }
                    holder.tv_operation_choose.setText(devices.get(position).getTipName());

                }
            }

            convertView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    deviceInfo = devices.get(position);

                    if (deviceInfo.getCak() != null && deviceInfo.getCak().equals("control")) { // 控制类型的分类

                        getData();

                    } else if (deviceInfo.getCak() != null && deviceInfo.getCak().equals("security")) {
                        showSingleDailog();
                    }

                }

            });
            return convertView;

        }

        class ViewHolder {
            TextView tv_operation_equipment, tv_operation_choose;
        }
    }

    public void frashDeviceList() {
//        devicesTips.addAll(devices);
//        //去重
//        for (int k = 0; k < devicesTips.size(); k++) {
//            for (int j = k; j < devicesTips.size(); j++) {
//                if (devicesTips.get(k).getId() == devicesTips.get(j).getId()) {
//                    List<Tips> tipsList = tipMap.get(devicesTips.get(k).getId());
//                    if (tipsList == null) {
//                        tipsList = new ArrayList<>();
//                    }
//                    Tips t = new Tips();
//                    t.setC(devicesTips.get(k).getTip());
//                    t.setE(devicesTips.get(k).getTipName());
//                    tipsList.add(t);
//                    tipMap.put(devicesTips.get(k).getId(), tipsList);
//                }
//            }
//        }
//        //log.e("wxbs", "devices.size" + devices.size());
    }

    class EquipmentAdapter extends BaseAdapter {
        private Context context;
        private List<DeviceInfo> devices = new ArrayList<DeviceInfo>();

        public EquipmentAdapter(Context context, List<DeviceInfo> devices) {
            this.context = context;
            this.devices = devices;
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (null == convertView) {
                holder = new ViewHolder();
                convertView = View.inflate(context, R.layout.operation_equipment_list_item, null);
                holder.tv_operation_choose = (TextView) convertView.findViewById(R.id.tv_operation_choose);
                holder.tv_operation_equipment = (TextView) convertView.findViewById(R.id.tv_operation_equipment);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv_operation_choose.setTextColor(getResources().getColor(R.color.black));
            holder.tv_operation_equipment.setTextColor(getResources().getColor(R.color.black));

            DeviceInfo mInfo = devices.get(position);
            boolean isNull = ((mInfo.getWhere() == null || "".equals(mInfo.getWhere()) || "null".equals(mInfo.getWhere()))
                    && (mInfo.getType() == null || "".equals(mInfo.getType())));
            StringBuffer sb = new StringBuffer();
            sb.append(mInfo.getName());
            if (isNull) {
                holder.tv_operation_equipment.setText(sb.toString());
            } else {
                sb.append(" ");
                sb.append("( ");
                sb.append(((mInfo.getWhere() == null || "null".equals(mInfo.getWhere())) ? "" : mInfo.getWhere() + " "));
                sb.append((mInfo.getType()));
                sb.append(" )");
            }
            holder.tv_operation_equipment.setText(sb.toString());
//            if (devices.get(position).getType() != null && !devices.get(position).getType().equals("")) {
//                holder.tv_operation_equipment.setText(devices.get(position).getName() + ":" + devices.get(position).getType());
//            } else {
//                holder.tv_operation_equipment.setText(devices.get(position).getName());
//            }
            final TextView t = holder.tv_operation_choose;
            if (devices != null && devices.size() > 0 && devices.get(position).getCak() != null && devices.get(position).getCak().equals("control")) {
                if (devices.get(position).getTipName() == null) {
                    final long uid = dcsp.getLong(Constant.LOGIN_APPID, 0);
                    final String code = dcsp.getString(Constant.LOGIN_CODE, "");
                    final long did = devices.get(position).getId();
                    showInProgress(getString(R.string.loading), false, false);
                    JavaThreadPool.getInstance().excute(new Runnable() {

                        @Override
                        public void run() {
                            DataCenterSharedPreferences dcsp = DataCenterSharedPreferences
                                    .getInstance(AddSceneActivity.this, Constant.CONFIG);
                            String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
                            JSONObject pJsonObject = new JSONObject();
                            pJsonObject.put("did", did);

//							final String result = HttpRequestUtils
//									.requestHttpServer(
//											 server
//													+ "/jdm/service/dkeycommands?v=" + URLEncoder.encode(SecurityUtil
//															.crypt(pJsonObject.toJSONString(), Constant.KEY_HTTP)),
//									AddSceneActivity.this, defaultHandler);
                            final String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/d/dkeycomms", pJsonObject, AddSceneActivity.this);

                            defaultHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    cancelInProgress();
                                    String o = null;
                                    try {
                                        o = result;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    // Toast.makeText(AddSceneActivity.this,
                                    // o,
                                    // 1).show();
                                    String obj = null;
                                    String dec = null;
                                    xiaXing = JSON.parseArray(o, XiaXingInfo.class);
                                    if (xiaXing != null && xiaXing.size() > 0) {
                                        obj = xiaXing.get(0).getS();
                                        dec = xiaXing.get(0).getN();
                                        Message msg = defaultHandler.obtainMessage(2);
                                        Bundle bs = new Bundle();
                                        bs.putString("obje", obj);
                                        bs.putString("dece", dec);
                                        bs.putInt("pose", position);
                                        msg.setData(bs);
                                        defaultHandler.sendMessage(msg);
                                    } else {
                                        t.setText(getString(R.string.activity_scene_get_tip));
                                    }

                                }
                            });
                        }


                    });

                }

            } else if (devices != null && devices.size() > 0 && devices.get(position).getCak() != null && devices.get(position).getCak().equals("security")) {
                if (devices.get(position).getTipName() == null) {
                    final long uid = dcsp.getLong(Constant.LOGIN_APPID, 0);
                    final String code = dcsp.getString(Constant.LOGIN_CODE, "");
                    final long did = devices.get(position).getId();
                    final String l = Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry();
                    showInProgress(getString(R.string.loading), false, false);

                    JavaThreadPool.getInstance().excute(new Runnable() {

                        @Override
                        public void run() {
                            DataCenterSharedPreferences dcsp = DataCenterSharedPreferences
                                    .getInstance(AddSceneActivity.this, Constant.CONFIG);
                            String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
                            JSONObject pJsonObject = new JSONObject();
//							pJsonObject.put("uid", uid);
//							pJsonObject.put("code", code);
                            pJsonObject.put("did", did);
//							pJsonObject.put("lang", l);
//							final String result = HttpRequestUtils
//									.requestHttpServer(
//											 server
//													+ "/jdm/service/dcommands?v=" + URLEncoder.encode(SecurityUtil
//															.crypt(pJsonObject.toJSONString(), Constant.KEY_HTTP)),
//									AddSceneActivity.this, defaultHandler);
                            final String result = HttpRequestUtils.requestoOkHttpPost( server
                                    + "/jdm/s3/d/dcomms", pJsonObject, AddSceneActivity.this);
                            String jsonStr = "";
                            try {
                                jsonStr = result;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            final String o = jsonStr;
                            if (o != null && o.length() > 4) {
                                defaultHandler.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        cancelInProgress();
                                        String objs = null;
                                        String decs = null;
                                        mTip = JSON.parseArray(o, Tips.class);
                                        Message msg = defaultHandler.obtainMessage(1);
                                        if (mTip != null && !mTip.isEmpty()) {
                                            objs = mTip.get(0).getC();
                                            decs = mTip.get(0).getE();
                                        }

                                        Bundle bl = new Bundle();
                                        bl.putString("objs", objs);
                                        bl.putString("decs", decs);
                                        bl.putInt("p", position);
                                        msg.setData(bl);
                                        defaultHandler.sendMessage(msg);

                                    }
                                });
                            } else {
                                cancelInProgress();
                            }
                        }
                    });

                }

            }

            convertView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    info = devices.get(position);
                    if (info.getCak() != null && info.getCak().equals("control")) { // 控制类型的分类

                        getData2();

                    } else if (info.getCak() != null && info.getCak().equals("security")) {

                        getTip();
                    }

                }

            });
            if (devices.get(position).getTipName() != null) {
                holder.tv_operation_choose.setText(devices.get(position).getTipName());

            }

            return convertView;

        }

        class ViewHolder {
            TextView tv_operation_equipment, tv_operation_choose;
        }
    }

    public void getTip() {
        final long did = info.getId();
        final String l = Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry();
        JavaThreadPool.getInstance().excute(new Runnable() {

            @Override
            public void run() {
                DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(AddSceneActivity.this,
                        Constant.CONFIG);
                String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("did", did);
                final String result = HttpRequestUtils.requestoOkHttpPost( server
                        + "/jdm/s3/d/dcomms", pJsonObject, AddSceneActivity.this);
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        String o = null;
                        try {
                            o = result;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        // Toast.makeText(AddSceneActivity.this, o,
                        // 1).show();
                        mTip = JSON.parseArray(o, Tips.class);
                        str = new ArrayList<String>();
                        if (mTip != null && mTip.size() > 0) {
                            for (int i = 0; i < mTip.size(); i++) {
                                str.add(mTip.get(i).getE());
                            }
                        }
                        if (str.size() > 0) {
                            showSingleDailog2(str);
                        } else {
                            Toast.makeText(AddSceneActivity.this, getString(R.string.device_not_getdata),
                                    Toast.LENGTH_LONG).show();
                        }

							/*
                             * tipOptions.setPicker(str);
							 * tipOptions.setCyclic(false); tipOptions.show();
							 */
                    }
                });
            }

        });

    }

    public void getData() {

        final long uid = dcsp.getLong(Constant.LOGIN_APPID, 0);
        final String code = dcsp.getString(Constant.LOGIN_CODE, "");
        final long did = deviceInfo.getId();
        JavaThreadPool.getInstance().excute(new Runnable() {

            @Override
            public void run() {
                DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(AddSceneActivity.this,
                        Constant.CONFIG);
                String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("did", did);
                final String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/d/dkeycomms", pJsonObject, AddSceneActivity.this);
                defaultHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        String o = null;
                        try {
                            o = result;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        // Toast.makeText(AddSceneActivity.this, o,
                        // 1).show();
                        xiaXing = JSON.parseArray(o, XiaXingInfo.class);
                        str = new ArrayList<String>();
                        str1 = new ArrayList<String>();
                        if (xiaXing != null && xiaXing.size() > 0) {


                            for (int i = 0; i < xiaXing.size(); i++) {
                                str.add(xiaXing.get(i).getN());
                                str1.add(xiaXing.get(i).getS());
                            }
                        }
                            /*
                             * pvOptions.setPicker(str);
							 * pvOptions.setCyclic(false); pvOptions.show();
							 */
                        if (str.size() > 0) {
                            showDialog5(str, str1);
                        } else {
                            Toast.makeText(AddSceneActivity.this, getString(R.string.device_not_getdata),
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                });


            }
        });

    }

    public void getData2() {

        final long uid = dcsp.getLong(Constant.LOGIN_APPID, 0);
        final String code = dcsp.getString(Constant.LOGIN_CODE, "");
        final long did = info.getId();
        JavaThreadPool.getInstance().excute(new Runnable() {

            @Override
            public void run() {
                DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(AddSceneActivity.this,
                        Constant.CONFIG);
                String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("did", did);

//				final String result = HttpRequestUtils
//						.requestHttpServer(
//								 server + "/jdm/service/dkeycommands?v="
//										+ URLEncoder.encode(
//												SecurityUtil.crypt(pJsonObject.toJSONString(), Constant.KEY_HTTP)),
//								AddSceneActivity.this, defaultHandler);
                final String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/d/dkeycomms", pJsonObject, AddSceneActivity.this);

                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        String o = null;
                        try {
                            o = result;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        // Toast.makeText(AddSceneActivity.this, o,
                        // 1).show();
                        xiaXing = JSON.parseArray(o, XiaXingInfo.class);
                        str = new ArrayList<String>();
                        if (xiaXing != null && xiaXing.size() > 0) {
                            for (int i = 0; i < xiaXing.size(); i++) {
                                str.add(xiaXing.get(i).getN());
                            }
                        }
                            /*
                             * pvOptions.setPicker(str);
							 * pvOptions.setCyclic(false); pvOptions.show();
							 */
                        if (str.size() > 0) {
                            showDialog6(str);
                        } else {
                            Toast.makeText(AddSceneActivity.this, getString(R.string.device_not_getdata),
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                });
            }


        });

    }

    /**
     * 加载所有设备列表
     */
    private void initDate() {

        JavaThreadPool.getInstance().excute(new loadAllDevicesInfo(1));
    }

    /**
     * 初始化被控制列表
     */
    private void initControlsView() {
        controls_layout.removeAllViews();
        controls_layout.setVisibility(View.GONE);

    }

    public void back(View v) {
        finish();
    }

    protected void onDestroy() {
        defaultHandler.removeCallbacksAndMessages(null);
        defaultHandler = null;
        super.onDestroy();
    }

    /**
     * 添加新场景
     *
     * @param v
     */
    public void addScene(View v) {
        if (result != null) {
            if (!TextUtils.isEmpty(et.getText())) {
                Modify();
            } else {
                Toast.makeText(this, getString(R.string.activity_scene_item_name), Toast.LENGTH_SHORT).show();
            }

        } else {
            if (!TextUtils.isEmpty(et.getText())) {
                submit();
            } else {
                Toast.makeText(this, getString(R.string.activity_scene_item_name), Toast.LENGTH_SHORT).show();
            }

        }
    }

    public void Modify() {

        long timer = 0;
        String timeStr = time.getText().toString();
        String[] subString = null;

        if (timeStr.length() > 3) {
            subString = timeStr.split(":");
            timer = Long.parseLong(subString[0]) * 60 + Long.parseLong(subString[1]);
        }
        final long timers = timer;
//        final String m = dcsp.getString(Constant.APP_MASTERID, "");
        //替换
        final String m = ZhujiListFragment.getMasterId() ;
        final String n = et.getText().toString();
        showInProgress(getString(R.string.loading), false, false);
        JavaThreadPool.getInstance().excute(new Runnable() {

            @Override
            public void run() {
                DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(AddSceneActivity.this,
                        Constant.CONFIG);
                String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("id", result.getId());
                pJsonObject.put("m", m);
                pJsonObject.put("n", n);
                pJsonObject.put("t", type);
                pJsonObject.put("tt", timers);
                pJsonObject.put("tc", cycleTime);
                JSONArray pA = new JSONArray();
//                for (DeviceInfo tip : devices) {
//
//                    if (tip.getTip() != null) {
//                        JSONObject o = new JSONObject();
//                        o.put("cd", tip.getId());
//                        o.put("cc", tip.getTip());
//                        if (tip.getCak().equals("control")) {
//                            o.put("ct", 1);
//                        } else {
//                            o.put("ct", 0);
//                        }
//                        pA.add(o);
//                    }
//
//                }
//                pJsonObject.put("cl", pA);
//
//                JSONArray pB = new JSONArray();
//                for (DeviceInfo tip : triggerDeviceInfos) {
//
//                    if (tip.getTip() != null) {
//                        JSONObject o = new JSONObject();
//                        o.put("tdid", tip.getId());
//                        o.put("tdc", tip.getTip());
//                        pB.add(o);
//                    }
//
//                }
//                pJsonObject.put("tl", pB);
                for (DeviceInfo tip : devices) {
                    List<Tips> tipList = tipMap.get(tip.getId());
                    if (tip.getCak().equals("security")) {
                        if (tip.getTip() != null) {
                            JSONObject o = new JSONObject();
                            o.put("cd", tip.getId());
                            o.put("cc", tip.getTip());
                            if (tip.getCak().equals("control")) {
                                o.put("ct", 1);
                            } else {
                                o.put("ct", 0);
                            }
                            pA.add(o);
                        }

                    } else {
                        for (Tips t : tipList) {
                            JSONObject o = new JSONObject();
                            o.put("cd", tip.getId());
                            o.put("cc", t.getC());
                            if (tip.getCak().equals("control")) {
                                o.put("ct", 1);
                            } else {
                                o.put("ct", 0);
                            }
                            pA.add(o);
                        }
                    }
                }
                pJsonObject.put("cl", pA);

                JSONArray pB = new JSONArray();
                for (DeviceInfo tip : triggerDeviceInfos) {
                    List<Tips> tipList = tipMap.get(tip.getId());

                    if (tip.getCak().equals("security")) {
                        if (tip.getTip() != null) {
                            JSONObject o = new JSONObject();
                            o.put("tdid", tip.getId());
                            o.put("tdc", tip.getTip());
                            pB.add(o);
                        }
                    } else {
                        for (Tips t : tipList) {
                            JSONObject o = new JSONObject();
                            o.put("tdid", tip.getId());
                            o.put("tdc", t.getC());
                            pB.add(o);
                        }
                    }
                }
                pJsonObject.put("tl", pB);
                //log.e("loglog", pJsonObject.toString());
                String result = HttpRequestUtils
                        .requestoOkHttpPost(
                                 server + "/jdm/s3/scenes/update", pJsonObject, AddSceneActivity.this);
//				final String result = HttpRequestUtils
//						.requestHttpServer(
//								 server + "/jdm/service/scenes/update?v="
//										+ URLEncoder.encode(
//												SecurityUtil.crypt(pJsonObject.toJSONString(), Constant.KEY_HTTP)),
//								AddSceneActivity.this, defaultHandler);
                // -1参数为空 -2校验失败 -3type为1时时间或周期为空 -4type为2时触发设备id或指令为空 -5未获取到数据
                // -6控制的设备id或指令为空 -7解析失败 -8名称为空 -9lang为空 -10masterid为空
                // -11类型只能为0,1,2中的一个 -12类型为空 -14被控制的设备必填
                if ("0".equals(result)) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(AddSceneActivity.this, getString(R.string.activity_editscene_modify_success),
                                    Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });
                } else if ("-1".equals(result)) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(AddSceneActivity.this, getString(R.string.register_tip_empty),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-2".equals(result)) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(AddSceneActivity.this, getString(R.string.device_check_failure),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-3".equals(result)) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(AddSceneActivity.this, getString(R.string.activity_editscene_type_1_empty),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-4".equals(result)) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(AddSceneActivity.this, getString(R.string.activity_editscene_type_2_empty),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-5".equals(result)) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(AddSceneActivity.this,
                                    getString(R.string.activity_editscene_type_control_erro), Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-6".equals(result)) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(AddSceneActivity.this,
                                    getString(R.string.activity_editscene_type_control_empty), Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
                } else if ("-7".equals(result)) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(AddSceneActivity.this, getString(R.string.activity_editscene_paser_erro),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-8".equals(result)) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(AddSceneActivity.this, getString(R.string.activity_editscene_name_empty),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-9".equals(result)) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(AddSceneActivity.this, getString(R.string.activity_editscene_lang_empty),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-10".equals(result)) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(AddSceneActivity.this, getString(R.string.activity_editscene_masterid_empty),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-11".equals(result)) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(AddSceneActivity.this, getString(R.string.activity_editscene_type_only),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-12".equals(result)) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(AddSceneActivity.this, getString(R.string.activity_editscene_type_empty),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-13".equals(result)) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(AddSceneActivity.this, getString(R.string.activity_editscene_isexist),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-14".equals(result)) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(AddSceneActivity.this,
                                    getString(R.string.activity_editscene_type_control_sure), Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-20".equals(result)) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(AddSceneActivity.this, getString(R.string.activity_editscene_not),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.addscene_trigger_layout:
                intent.setClass(getApplicationContext(), TriggerEqipmentActivity.class);
                intent.putExtra("deviceInfoses", (Serializable) triggerDeviceInfos);
                startActivityForResult(intent, TRIGGER);
            case R.id.addscene_btn:

                break;
            case R.id.addscene_time_layout:
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                // 隐藏软键盘
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                timePickerView.show();
                break;
            case R.id.addscene_cycle_layout:
                getCycleStrings(cycleTime);
//                cycleAlertView.show(i);
                break;
            case R.id.addscene_controls_layout:

			/*
			 * if (type == 2) { intent.setClass(this,
			 * ControlEquipmentActivity.class); intent.putExtra("deviceInfoses",
			 * (Serializable) devices); startActivityForResult(intent, CONTROL);
			 * } else {
			 */
                intent.setClass(this, AllEquipmentListActivity.class);
                intent.putExtra("deviceInfoses", (Serializable) devices);
                startActivityForResult(intent, CONTROL);
                // }

        }
    }

    ;

    public void getCycleStrings(String time) {
        char[] cycleStr = cycleTime.toCharArray();
        for (int indext = 0; indext < cycleStr.length; indext++) {
            JSONObject o = new JSONObject();
            o.put("name", times[indext]);
            o.put("o", Integer.parseInt(String.valueOf(cycleStr[indext])));
            i[indext] = o.toJSONString();
        }
    }

    private void submit() {
        long timer = 0;
        String timeStr = time.getText().toString();
        String[] subString = null;

        if (timeStr.length() > 3) {
            subString = timeStr.split(":");
            timer = Long.parseLong(subString[0]) * 60 + Long.parseLong(subString[1]);
        }
        final long timers = timer;
//        final String m = dcsp.getString(Constant.APP_MASTERID, "");
        //替换
        final String m = ZhujiListFragment.getMasterId() ;
        final String l = Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry();
        showInProgress(getString(R.string.loading));
        JavaThreadPool.getInstance().excute(new Runnable() {

                                                @Override
                                                public void run() {
                                                    DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(AddSceneActivity.this,
                                                            Constant.CONFIG);
                                                    String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
                                                    JSONObject pJsonObject = new JSONObject();
                                                    pJsonObject.put("n", et.getText().toString());
                                                    pJsonObject.put("lang", l);
                                                    pJsonObject.put("t", type);
                                                    pJsonObject.put("m", m);
                                                    if (type == 1) {
                                                        pJsonObject.put("tc", cycleTime);
                                                        pJsonObject.put("tt", timers);
                                                    }

                                                    JSONArray pA = new JSONArray();
                                                    for (DeviceInfo tip : devices) {
                                                        List<Tips> tipList = tipMap.get(tip.getId());
                                                        if (tipList != null) {
                                                            for (Tips t : tipList) {
                                                                JSONObject o = new JSONObject();
                                                                o.put("cd", tip.getId());
                                                                o.put("cc", t.getC());
                                                                if (tip.getCak().equals("control")) {
                                                                    o.put("ct", 1);
                                                                } else {
                                                                    o.put("ct", 0);
                                                                }
                                                                pA.add(o);
                                                            }

                                                        } else {
                                                            if (tip.getTip() != null) {
                                                                JSONObject o = new JSONObject();
                                                                o.put("cd", tip.getId());
                                                                o.put("cc", tip.getTip());
                                                                if (tip.getCak().equals("control")) {
                                                                    o.put("ct", 1);
                                                                } else {
                                                                    o.put("ct", 0);
                                                                }
                                                                pA.add(o);
                                                            }
                                                        }
                                                    }
                                                    pJsonObject.put("cl", pA);

                                                    JSONArray pB = new JSONArray();
                                                    for (DeviceInfo tip : triggerDeviceInfos) {
                                                        List<Tips> tipList = tipMap.get(tip.getId());

                                                        if (tipList != null) {
                                                            for (Tips t : tipList) {
                                                                JSONObject o = new JSONObject();
                                                                o.put("tdid", tip.getId());
                                                                o.put("tdc", t.getC());
                                                                pB.add(o);
                                                            }
                                                        } else {
                                                            if (tip.getTip() != null) {
                                                                JSONObject o = new JSONObject();
                                                                o.put("tdid", tip.getId());
                                                                o.put("tdc", tip.getTip());
                                                                pB.add(o);
                                                            }
                                                        }
                                                    }
                                                    pJsonObject.put("tl", pB);
                                                    String result = HttpRequestUtils
                                                            .requestoOkHttpPost(
                                                                     server + "/jdm/s3/scenes/add", pJsonObject, AddSceneActivity.this);

                                                    // -1参数为空 -2校验失败 -3type为1时时间或周期为空 -4type为2时触发设备id或指令为空 -5未获取到数据
                                                    // -6控制的设备id或指令为空 -7解析失败 -8名称为空 -9lang为空 -10masterid为空
                                                    // -11类型只能为0,1,2中的一个 -12类型为空 -14被控制的设备必填
                                                    if ("0".equals(result)) {
                                                        defaultHandler.post(new Runnable() {

                                                            @Override
                                                            public void run() {
                                                                cancelInProgress();

                                                                Toast.makeText(AddSceneActivity.this, getString(R.string.sence_add_success),
                                                                        Toast.LENGTH_LONG).show();
                                                                finish();
                                                            }
                                                        });
                                                    } else if ("-1".equals(result)) {
                                                        defaultHandler.post(new Runnable() {

                                                            @Override
                                                            public void run() {
                                                                cancelInProgress();
                                                                Toast.makeText(AddSceneActivity.this, getString(R.string.register_tip_empty),
                                                                        Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                                    } else if ("-2".equals(result)) {
                                                        defaultHandler.post(new Runnable() {

                                                            @Override
                                                            public void run() {
                                                                cancelInProgress();
                                                                Toast.makeText(AddSceneActivity.this, getString(R.string.device_check_failure),
                                                                        Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                                    } else if ("-3".equals(result)) {
                                                        defaultHandler.post(new Runnable() {

                                                            @Override
                                                            public void run() {
                                                                cancelInProgress();
                                                                Toast.makeText(AddSceneActivity.this, getString(R.string.activity_editscene_type_1_empty),
                                                                        Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                                    } else if ("-4".equals(result)) {
                                                        defaultHandler.post(new Runnable() {

                                                            @Override
                                                            public void run() {
                                                                cancelInProgress();
                                                                Toast.makeText(AddSceneActivity.this, getString(R.string.activity_editscene_type_2_empty),
                                                                        Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                                    } else if ("-5".equals(result)) {
                                                        defaultHandler.post(new Runnable() {

                                                            @Override
                                                            public void run() {
                                                                cancelInProgress();
                                                                Toast.makeText(AddSceneActivity.this,
                                                                        getString(R.string.activity_editscene_type_control_erro), Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                                    } else if ("-6".equals(result)) {
                                                        defaultHandler.post(new Runnable() {

                                                            @Override
                                                            public void run() {
                                                                cancelInProgress();
                                                                Toast.makeText(AddSceneActivity.this,
                                                                        getString(R.string.activity_editscene_type_control_empty), Toast.LENGTH_LONG)
                                                                        .show();
                                                            }
                                                        });
                                                    } else if ("-7".equals(result)) {
                                                        defaultHandler.post(new Runnable() {

                                                            @Override
                                                            public void run() {
                                                                cancelInProgress();
                                                                Toast.makeText(AddSceneActivity.this, getString(R.string.activity_editscene_paser_erro),
                                                                        Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                                    } else if ("-8".equals(result)) {
                                                        defaultHandler.post(new Runnable() {

                                                            @Override
                                                            public void run() {
                                                                cancelInProgress();
                                                                Toast.makeText(AddSceneActivity.this, getString(R.string.activity_editscene_name_empty),
                                                                        Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                                    } else if ("-9".equals(result)) {
                                                        defaultHandler.post(new Runnable() {

                                                            @Override
                                                            public void run() {
                                                                cancelInProgress();
                                                                Toast.makeText(AddSceneActivity.this, getString(R.string.activity_editscene_lang_empty),
                                                                        Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                                    } else if ("-10".equals(result)) {
                                                        defaultHandler.post(new Runnable() {

                                                            @Override
                                                            public void run() {
                                                                cancelInProgress();
                                                                Toast.makeText(AddSceneActivity.this, getString(R.string.activity_editscene_masterid_empty),
                                                                        Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                                    } else if ("-11".equals(result)) {
                                                        defaultHandler.post(new Runnable() {

                                                            @Override
                                                            public void run() {
                                                                cancelInProgress();
                                                                Toast.makeText(AddSceneActivity.this, getString(R.string.activity_editscene_type_only),
                                                                        Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                                    } else if ("-12".equals(result)) {
                                                        defaultHandler.post(new Runnable() {

                                                            @Override
                                                            public void run() {
                                                                cancelInProgress();
                                                                Toast.makeText(AddSceneActivity.this, getString(R.string.activity_editscene_type_empty),
                                                                        Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                                    } else if ("-13".equals(result)) {
                                                        defaultHandler.post(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                cancelInProgress();
                                                                Toast.makeText(AddSceneActivity.this, getString(R.string.activity_editscene_isexist),
                                                                        Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                                    } else if ("-14".equals(result)) {
                                                        defaultHandler.post(new Runnable() {

                                                            @Override
                                                            public void run() {
                                                                cancelInProgress();
                                                                Toast.makeText(AddSceneActivity.this,
                                                                        getString(R.string.activity_editscene_type_control_sure), Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                                    }
                                                }
                                            }

        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CONTROL) {
                devices = (List<DeviceInfo>) data.getSerializableExtra("devices");
                frashDeviceList();
                mOperationAdapter = new OperationEquipmentAdapter(this, devices);
                lv_operation_equipment.setAdapter(mOperationAdapter);
            } else if (requestCode == TRIGGER) {
                triggerDeviceInfos = (List<DeviceInfo>) data.getSerializableExtra("triggerDeviceInfos");
                mmAdapter = new EquipmentAdapter(this, triggerDeviceInfos);
                lv_operation_trigger.setAdapter(mmAdapter);
            }

        }
    }

    class CommandItem implements Serializable {
        private String command;
        private String explain;
        private String type;

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        public String getExplain() {
            return explain;
        }

        public void setExplain(String explain) {
            this.explain = explain;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

    }


    class loadAllDevicesInfo implements Runnable {
        private int what;

        public loadAllDevicesInfo() {
        }

        public loadAllDevicesInfo(int what) {
            this.what = what;
        }

        @Override
        public void run() {
            Cursor cursor = DatabaseOperator.getInstance(AddSceneActivity.this).getWritableDatabase()
                    .rawQuery("select * from DEVICE_STATUSINFO order by sort desc", new String[]{});
            List<DeviceInfo> deviceList = new ArrayList<DeviceInfo>();
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    deviceList.add(DatabaseOperator.getInstance(getApplicationContext()).buildDeviceInfo(cursor));
                }
                cursor.close();
            }
            Message m = defaultHandler.obtainMessage(this.what);
            m.obj = deviceList;
            defaultHandler.sendMessage(m);
        }
    }

    /**
     * 选择类型之后的回调
     */
    @Override
    public void onItemClick(int position) {
        if (type != position) {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), AddSceneActivity.class);
            intent.putExtra("type", position);
            startActivity(intent);
            finish();
        }
    }

    private String getTime(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(date);
    }

    public class SceneDevice {
        private String name;
        private String command;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

    }

}
