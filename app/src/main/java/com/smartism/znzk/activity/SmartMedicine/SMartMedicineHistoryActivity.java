package com.smartism.znzk.activity.SmartMedicine;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LanguageUtil;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.WeakRefHandler;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * Created by win7 on 2017/4/15.
 */

public class SMartMedicineHistoryActivity extends ActivityParentActivity {

    private ListView yx_list;
    private MyAdapter adapter;
    private int size = 20;
    List<JSONObject> listData;
    List<HistoryInfo> infos;

    private DeviceInfo deviceInfo;
    private int flag = -1;
    private boolean isChinses;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 3) {
                cancelInProgress();
                listData = new ArrayList<>();
                listData.addAll((Collection<? extends JSONObject>) msg.obj);
                if (mHandler.hasMessages(1)) {
                    mHandler.removeMessages(1);
                }

                if (listData != null && listData.size() > 0) {
                    Calendar c = Calendar.getInstance();
//                    List<JSONObject> list = new ArrayList<>();
                    for (int i = 0; i < listData.size(); i++) {
                        JSONObject object = new JSONObject();
                        HistoryInfo info = new HistoryInfo();
                        JSONObject object1 = listData.get(i);
                        info.setValue(object1.getString("deviceCommand"));
//                        long time = object1.getLong("deviceCommandTime");
//                        c.setTimeInMillis(time);
                        String parms1 = "yyyy:MM:dd:HH:mm";
                        Date date = object1.getDate("deviceCommandTime");
                        String hour = new SimpleDateFormat(parms1).format(date.getTime());
                        info.setDate(hour);
                        info.setDayOfWeek(getWeek(date));
//                        object.put("t_hour", hour);
//                        list.add(object);
                        infos.add(info);
                    }
                    adapter.notifyDataSetChanged();
                    if (totalSize <= infos.size()) {
                        yx_list.removeFooterView(footerView);
                    }
                }

            }
            return false;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);
    private int totalSize;
    private Button footerView_button;
    private View footerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_smart_history);
        initView();
    }

    private void initView() {
        isChinses = LanguageUtil.isZh(this);
        infos = new ArrayList<>();
        deviceInfo = (DeviceInfo) getIntent().getSerializableExtra("device");
        yx_list = (ListView) findViewById(R.id.yx_list);
        adapter = new MyAdapter(this, infos);
        footerView = LayoutInflater.from(SMartMedicineHistoryActivity.this).inflate(R.layout.list_foot_loadmore, null);
        yx_list.addFooterView(footerView);
        yx_list.setAdapter(adapter);
        footerView_button = (Button) footerView.findViewById(R.id.load_more);
        showInProgress(getString(R.string.ongoing), false, true);
        JavaThreadPool.getInstance().excute(new CommandLoad(0, size));
        footerView_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 加载更多按钮点击
                JavaThreadPool.getInstance().excute(new CommandLoad(infos.size(), 20));
            }
        });
    }

    public void back(View v) {
        finish();
    }


    class CommandLoad implements Runnable {
        private int start, size;

        public CommandLoad(int start, int size) {
            this.size = size;
            this.start = start;
        }

        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
//            object.put("id", 74235915769217024l);
            object.put("id", deviceInfo.getId());
            object.put("start", this.start);
            object.put("size", this.size);
            String result = HttpRequestUtils.requestoOkHttpPost(
                     server + "/jdm/s3/d/hm", object,
                    SMartMedicineHistoryActivity.this);
            if ("-3".equals(result)) {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(SMartMedicineHistoryActivity.this, getString(R.string.history_response_nodevice),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if (result.length() > 4) {

                List<JSONObject> commands = new ArrayList<JSONObject>();
                JSONObject resultJson = null;
                try {
                    resultJson = JSON.parseObject(result);
                } catch (Exception e) {
                    LogUtil.e(getApplicationContext(), TAG, "解密错误：：", e);
                    return;
                }

                JSONArray array = resultJson.getJSONArray("result");
                totalSize = resultJson.getIntValue("allCount");
                if (array != null && !array.isEmpty()) {
                    for (int j = 0; j < array.size(); j++) {
                        commands.add(array.getJSONObject(j));
                    }
                    Log.e("SmartHis", String.valueOf(commands.size()));
                }
                Message m = mHandler.obtainMessage(3);
                m.obj = commands;
                mHandler.sendMessage(m);
                // 请求成功了，需要刷新数据到页面，也需要清除此设备的历史未读记录
                ContentValues values = new ContentValues();
                values.put("nr", 0); // 未读消息数
                DatabaseOperator.getInstance(SMartMedicineHistoryActivity.this).getWritableDatabase().update(
                        "DEVICE_STATUSINFO", values, "id = ?", new String[]{String.valueOf(deviceInfo.getId())});
            }
        }
    }


    class MyAdapter extends BaseAdapter {
        private List<HistoryInfo> list;
        private LayoutInflater inflater;

        public MyAdapter(Context context, List<HistoryInfo> list) {
            this.list = list;
            this.inflater = LayoutInflater.from(context);

        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;
            MyHolder hodler;
            if (view == null) {
                hodler = new MyHolder();
                view = inflater.inflate(R.layout.activity_medic_history_list_item1, null, false);
                hodler.tv_month = (TextView) view.findViewById(R.id.tv_month);
                hodler.tv_day = (TextView) view.findViewById(R.id.tv_day);
                hodler.tv_xingqi = (TextView) view.findViewById(R.id.tv_xingqi);
                hodler.tv_time = (TextView) view.findViewById(R.id.tv_time);
                hodler.tv_oper = (TextView) view.findViewById(R.id.tv_oper);
                hodler.iv_circle_hover = (ImageView) view.findViewById(R.id.iv_circle_hover);
                hodler.iv_circle = (ImageView) view.findViewById(R.id.iv_circle);
                hodler.gray_line = (View) view.findViewById(R.id.gray_line);
                view.setTag(hodler);
            } else {
                hodler = (MyHolder) view.getTag();
            }
            HistoryInfo info = list.get(position);
            String[] array = info.getDate().split(":");
            if (position != 0) {
                String[] array1 = list.get(position - 1).getDate().split(":");
                if (!array1[2].equals(array[2])) {
                    hodler.tv_day.setVisibility(View.VISIBLE);
                    hodler.tv_month.setVisibility(View.VISIBLE);
                    hodler.tv_xingqi.setVisibility(View.VISIBLE);
                    hodler.iv_circle_hover.setVisibility(View.VISIBLE);
                    hodler.iv_circle.setVisibility(View.GONE);
                    hodler.tv_time.setTextColor(getResources().getColor(R.color.zhzj_default));
                    hodler.tv_oper.setTextColor(getResources().getColor(R.color.zhzj_default));
                } else {
                    hodler.iv_circle_hover.setVisibility(View.GONE);
                    hodler.iv_circle.setVisibility(View.VISIBLE);
                    hodler.tv_day.setVisibility(View.GONE);
                    hodler.tv_month.setVisibility(View.GONE);
                    hodler.tv_xingqi.setVisibility(View.GONE);
                    hodler.tv_time.setTextColor(getResources().getColor(R.color.black));
                    hodler.tv_oper.setTextColor(getResources().getColor(R.color.black));

                }
                if (position == list.size() - 1) {
                    hodler.gray_line.setVisibility(View.INVISIBLE);
                }
            } else {
                hodler.iv_circle.setVisibility(View.GONE);
                hodler.tv_time.setTextColor(getResources().getColor(R.color.zhzj_default));
                hodler.tv_oper.setTextColor(getResources().getColor(R.color.zhzj_default));
            }
            hodler.tv_xingqi.setText(info.getDayOfWeek());
            if (isChinses) {

                hodler.tv_month.setText(array[1] + getString(R.string.pickerview_month));
            } else {
                hodler.tv_month.setText(array[1]);
            }
            hodler.tv_day.setText(array[2]);
            hodler.tv_time.setText(array[3] + "：" + array[4]);
            hodler.tv_oper.setText(info.getValue());

            return view;
        }

        class MyHolder {
            public TextView tv_day, tv_month, tv_xingqi, tv_time, tv_oper;
            //            public RelativeLayout rl_day;
//            public LinearLayout ll_time;
            public ImageView iv_circle_hover, iv_circle;
            private View gray_line;
        }
    }

    public static String getWeek(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        String week = sdf.format(date);
        return week;
    }


    public class HistoryInfo implements Serializable {
        public String value;
        public String date;

        public String getDayOfWeek() {
            return dayOfWeek;
        }

        public void setDayOfWeek(String dayOfWeek) {
            this.dayOfWeek = dayOfWeek;
        }

        public String dayOfWeek;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }
    }
}
