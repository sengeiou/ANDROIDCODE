package com.smartism.znzk.activity.yaokan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.domain.yankan.YKTvInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.GetAndDecodeMapString;
import com.smartism.znzk.util.LanguageUtil;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by win7 on 2017/3/1.
 */

public class YKTVMainActivity extends ActivityParentActivity implements View.OnClickListener {

    private static final int SEND_CODE = 1;
    private Context mContext;
    private long did;
    private String ctrlId;

    private List<YKTvInfo> ykTvInfos;
    private HashMap<String, YKTvInfo> ykMap;
    private HashMap<String, YKTvInfo> ykOtherMap;
    private YKTvInfo ykTvInfo;
    private TextView tv_tname, tv_type;
    private GridView gridNumber;
    private GridView gridOtherKeys;
    private List<String> numberTitles;
    private List<String> otherKeyTitles;
    private GridNumberAdapter numberAdapter;
    private GridOtherKeyAdapter otherKeyAdapter;

    private Button back_btn, exit_btn;
    private LinearLayout ll_zhuye, ll_power, ll_choose;
    private ImageView iv_menu, iv_back, iv_ok, iv_ok_left, iv_ok_right, iv_ok_up, iv_ok_down;
    private ImageView pindao_plus_btn, pindao_minus_btn, jingyin_btn, vol_plus_btn, vol_minus_btn;

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SEND_CODE:
                    if (progressIsShowing()) {
                        cancelInProgress();
                        Toast.makeText(mContext, getString(R.string.timeout), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
            return false;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);

    private String type;
    private String brand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yktv_main);
        mContext = this;
        initView();
        initData();
        initCodeData();
    }


    private void initView() {
        tv_tname = (TextView) findViewById(R.id.tv_tname);
        tv_tname.setText(getString(R.string.hwzf_tv_tv));
        back_btn = (Button) findViewById(R.id.btn_back);
        back_btn.setOnClickListener(this);

        ll_power = (LinearLayout) findViewById(R.id.ll_power);
        ll_choose = (LinearLayout) findViewById(R.id.ll_choose);
        ll_zhuye = (LinearLayout) findViewById(R.id.ll_zhuye);

        pindao_plus_btn = (ImageView) findViewById(R.id.pindao_plus_btn);
        pindao_minus_btn = (ImageView) findViewById(R.id.pindao_minus_btn);
        jingyin_btn = (ImageView) findViewById(R.id.jingyin_btn);
        exit_btn = (Button) findViewById(R.id.exit_btn);
        vol_plus_btn = (ImageView) findViewById(R.id.vol_plus_btn);
        vol_minus_btn = (ImageView) findViewById(R.id.vol_minus_btn);

        iv_menu = (ImageView) findViewById(R.id.iv_menu);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        iv_ok = (ImageView) findViewById(R.id.iv_ok);
        iv_ok_left = (ImageView) findViewById(R.id.iv_ok_left);
        iv_ok_right = (ImageView) findViewById(R.id.iv_ok_right);
        iv_ok_up = (ImageView) findViewById(R.id.iv_ok_up);
        iv_ok_down = (ImageView) findViewById(R.id.iv_ok_down);

        tv_type = (TextView) findViewById(R.id.tv_type);
        ll_power.setOnClickListener(this);
        ll_choose.setOnClickListener(this);
        ll_zhuye.setOnClickListener(this);

        pindao_plus_btn.setOnClickListener(this);
        pindao_minus_btn.setOnClickListener(this);
        jingyin_btn.setOnClickListener(this);
        exit_btn.setOnClickListener(this);
        vol_plus_btn.setOnClickListener(this);
        vol_minus_btn.setOnClickListener(this);

        iv_menu.setOnClickListener(this);
        iv_back.setOnClickListener(this);
        iv_ok.setOnClickListener(this);
        iv_ok_left.setOnClickListener(this);
        iv_ok_right.setOnClickListener(this);
        iv_ok_up.setOnClickListener(this);
        iv_ok_down.setOnClickListener(this);


        gridNumber = (GridView) findViewById(R.id.grid_number);
        gridOtherKeys = (GridView) findViewById(R.id.grid_other_key);

        gridNumber.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        gridOtherKeys.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(Actions.ACCETP_ONEDEVICE_MESSAGE);
        registerReceiver(receiver, filter);
    }


    private void initData() {
        ykTvInfos = new ArrayList<>();
        ykMap = new HashMap<>();
        ykOtherMap = new HashMap<>();
        numberTitles = new ArrayList<>();
        otherKeyTitles = new ArrayList<>();

        did = getIntent().getLongExtra("did", -1);
        ctrlId = getIntent().getStringExtra("ctrlId");
        type = getIntent().getStringExtra("type");
        brand = getIntent().getStringExtra("brand");
        tv_type.setText(brand + " " + type);
        // 获取本地存储此遥控器的码库
        String mapString = Util.readYKCodeFromFile(brand+type);
//        ykTvInfos = new GetAndDecodeMapString().getYkTvList(mapString);
        ykMap = new GetAndDecodeMapString().getYkTvMap(mapString);
        ykOtherMap.putAll(ykMap);
//        Log.e("yktv:", ykTvInfos.toString());
        for (int i = 1; i <= 9; i++) {
            numberTitles.add(String.valueOf(i));
        }
        numberTitles.add("-/--");
        numberTitles.add("0");
        numberTitles.add(getString(R.string.hwzf_tv_info));
        numberAdapter = new GridNumberAdapter(this, numberTitles);
        gridNumber.setAdapter(numberAdapter);
        setListViewHeightBasedOnChildren(gridNumber);
        numberAdapter.notifyDataSetChanged();
    }

    private void initCodeData() {
        List<String> commonKeys = new ArrayList<>();
        commonKeys.add("power");
        commonKeys.add("signal");
        commonKeys.add("boot");
        commonKeys.add("ch+");
        commonKeys.add("ch-");
        commonKeys.add("mute");
        commonKeys.add("exit");
        commonKeys.add("vol+");
        commonKeys.add("vol-");
        commonKeys.add("menu");
        commonKeys.add("back");
        commonKeys.add("ok");
        commonKeys.add("left");
        commonKeys.add("right");
        commonKeys.add("up");
        commonKeys.add("down");
        commonKeys.add("info");
        commonKeys.addAll(numberTitles);
        for (String a : commonKeys) {
            if (ykOtherMap.containsKey(a)) {
                ykOtherMap.remove(a);
            }
        }
        for (String oterkey : ykOtherMap.keySet()) {
            ykTvInfos.add(ykOtherMap.get(oterkey));
        }

        otherKeyAdapter = new GridOtherKeyAdapter(this, ykTvInfos);

        gridOtherKeys.setAdapter(otherKeyAdapter);
        gridOtherKeys.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e("aaa", "vvv");
            }
        });
        setListViewHeightBasedOnChildren(gridOtherKeys);
        otherKeyAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_power:
                bindView("power");
                break;
            case R.id.ll_choose:
                bindView("signal");
                break;
            case R.id.ll_zhuye:
                bindView("boot");
                break;
            case R.id.pindao_plus_btn:
                bindView("ch+");
                break;
            case R.id.pindao_minus_btn:
                bindView("ch-");
                break;
            case R.id.jingyin_btn:
                bindView("mute");
                break;
            case R.id.exit_btn:
                bindView("exit");
                break;
            case R.id.vol_plus_btn:
                bindView("vol+");
                break;
            case R.id.vol_minus_btn:
                bindView("vol-");
                break;
            case R.id.iv_menu:
                bindView("menu");
                break;
            case R.id.iv_back:
                bindView("back");
                break;
            case R.id.iv_ok:
                bindView("ok");
                break;
            case R.id.iv_ok_left:
                bindView("left");
                break;
            case R.id.iv_ok_right:
                bindView("right");
                break;
            case R.id.iv_ok_up:
                bindView("up");
                break;
            case R.id.iv_ok_down:
                bindView("down");
                break;
            case R.id.btn_back:
                Intent intent = new Intent();
                intent.putExtra("result", false);
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }


    private void bindView(String id) {
        if (ykMap.containsKey(id)) {
            ykTvInfo = ykMap.get(id);
            sendCommand(ykTvInfo.getKey(), ykTvInfo.getCode());
        } else {
            Toast.makeText(this, getString(R.string.hwzf_mode_not_supply), Toast.LENGTH_SHORT).show();
        }
    }

    public void sendCommand(String key, String code) {
        showInProgress(getString(R.string.loading), false, true);
        JSONObject object = new JSONObject();
        object.put("name", key);
        object.put("code", code);
        SyncMessage message1 = new SyncMessage();
        message1.setCommand(SyncMessage.CommandMenu.rq_control.value());
        message1.setDeviceid(did);// 红外转发器的ID
        try {
            message1.setSyncBytes(object.toJSONString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        SyncMessageContainer.getInstance().produceSendMessage(message1);
        mHandler.sendEmptyMessageDelayed(SEND_CODE, 8 * 1000);
    }


    public static void setListViewHeightBasedOnChildren(GridView listView) {
        // 获取listview的adapter
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        // 固定列宽，有多少列
        int col = 3;// listView.getNumColumns();
        int totalHeight = 0;
        // i每次加4，相当于listAdapter.getCount()小于等于4时 循环一次，计算一次item的高度，
        // listAdapter.getCount()小于等于8时计算两次高度相加
        for (int i = 0; i < listAdapter.getCount(); i += col) {
            // 获取listview的每一个item
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            // 获取item的高度和
            totalHeight += listItem.getMeasuredHeight() + listView.getVerticalSpacing();
        }

        // 获取listview的布局参数
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        // 设置高度
        params.height = totalHeight;
        // 设置margin
        ((ViewGroup.MarginLayoutParams) params).setMargins(10, 10, 10, 10);
        // 设置参数
        listView.setLayoutParams(params);
    }

    private void adjustTvTextSize(Button tv, int maxWidth, String text) {
        int avaiWidth = maxWidth - tv.getPaddingLeft() - tv.getPaddingRight() - 10;

        if (avaiWidth <= 0) {
            return;
        }

        TextPaint textPaintClone = new TextPaint(tv.getPaint());
        // note that Paint text size works in px not sp
        float trySize = textPaintClone.getTextSize();

        while (textPaintClone.measureText(text) > avaiWidth) {
            trySize--;
            textPaintClone.setTextSize(trySize);
        }

        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, trySize);
    }

    class GridNumberAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private List<String> titles;

        public GridNumberAdapter(Context context, List<String> titles) {
            this.titles = titles;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return titles.size();
        }

        @Override
        public Object getItem(int position) {
            return titles.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewNumerHolder holder;
            if (convertView == null) {
                holder = new ViewNumerHolder();
//                convertView = inflater.inflate(R.layout.item_number_tv, null);
                convertView = View.inflate(mContext, R.layout.item_number_tv, null);
                holder.number_Btn = (Button) convertView.findViewById(R.id.tv_number_btn);
                convertView.setTag(holder);
            } else {
                holder = (ViewNumerHolder) convertView.getTag();
            }
            holder.number_Btn.setText(titles.get(position));
            final int pos = position;
            holder.number_Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String key = numberTitles.get(pos);
                    if (ykMap.containsKey(key)) {
                        ykTvInfo = ykMap.get(key);
                        sendCommand(key, ykTvInfo.getCode());
                    } else {
                        Toast.makeText(YKTVMainActivity.this, getString(R.string.hwzf_mode_not_supply), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            return convertView;
        }

        class ViewNumerHolder {
            public Button number_Btn;
        }
    }

    class GridOtherKeyAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private List<YKTvInfo> ykOtherInfos;

        public GridOtherKeyAdapter(Context context, List<YKTvInfo> titles) {
            this.ykOtherInfos = titles;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return ykOtherInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return ykOtherInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewOtherKeyHolder holder;
            if (convertView == null) {
                holder = new ViewOtherKeyHolder();
                convertView = inflater.inflate(R.layout.item_otherkey_tv, null);
                holder.otherkey_Btn = (Button) convertView.findViewById(R.id.btn_otherkey);
                holder.otherkey_Btn.setSelected(true);
                holder.otherkey_Btn.setFocusableInTouchMode(false);
//                holder.otherkey_Btn.setFocusable(false);
                convertView.setTag(holder);
            } else {
                holder = (ViewOtherKeyHolder) convertView.getTag();
            }
//            adjustTvTextSize(holder.otherkey_Btn,80,ykOtherInfos.get(position).getKeyName());
            holder.otherkey_Btn.setText(ykOtherInfos.get(position).getKeyName());
            if (!LanguageUtil.isZh(YKTVMainActivity.this)) {
                holder.otherkey_Btn.setText(ykOtherInfos.get(position).getKey());
            }
            final int pos = position;
            holder.otherkey_Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendCommand(ykTvInfos.get(pos).getKey(), ykTvInfos.get(pos).getCode());
                }
            });
            return convertView;
        }

        class ViewOtherKeyHolder {
            public Button otherkey_Btn;
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String deviceId = (String) intent.getSerializableExtra("device_id");
            if (intent.getAction().equals(Actions.ACCETP_ONEDEVICE_MESSAGE)) {
                if (deviceId != null && deviceId.equals(String.valueOf(did))) {
                    if (mHandler.hasMessages(SEND_CODE)) {
                        mHandler.removeMessages(SEND_CODE);
                    }
                    if (progressIsShowing()) {
                        cancelInProgress();
                        Toast.makeText(YKTVMainActivity.this, getString(R.string.rq_control_sendsuccess), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };


}
