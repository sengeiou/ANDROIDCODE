package com.smartism.znzk.activity.device;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
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
import com.nostra13.universalimageloader.core.ImageLoader;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.DeviceInfo.ControlTypeMenu;
import com.smartism.znzk.domain.HistoryCommandInfo;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.WeakRefHandler;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by win7 on 2017/9/8.
 */

public class THHistoryActivity extends ActivityParentActivity {
    public static final String TAG = THHistoryActivity.class.getSimpleName();
    private DeviceInfo deviceInfo;
    private View footerView;
    private Button footerView_button;
    private int totalSize = 0;
    private int mSize = 20;
    private List<HistoryCommandInfo> commandInfos;
    private CommandAdapter commandAdapter;
    private ListView command_list;
    private TextView tv_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wsd_history);
        deviceInfo = (DeviceInfo) getIntent().getSerializableExtra("device");
        initView();
        initData();
    }

    private void initData() {
        showInProgress(getString(R.string.loading), false, true);
        JavaThreadPool.getInstance().excute(new CommandLoad(0, mSize));
        commandInfos = new ArrayList<>();
        commandAdapter = new CommandAdapter(THHistoryActivity.this);
        command_list.addFooterView(footerView);
        command_list.setAdapter(commandAdapter);
        ImageView logo = (ImageView) findViewById(R.id.device_logo);
        TextView name = (TextView) findViewById(R.id.d_name);
        TextView where = (TextView) findViewById(R.id.d_where);
        TextView type = (TextView) findViewById(R.id.d_type);
        where.setText(deviceInfo.getWhere());
        type.setText(deviceInfo.getType());
        if (ControlTypeMenu.wenduji.value().equals(deviceInfo.getControlType())) {
            ImageLoader.getInstance().displayImage( dcsp.getString(Constant.HTTP_DATA_SERVERS, "")
                    + "/devicelogo/" + deviceInfo.getLogo(), logo, new ImageLoadingBar());
            name.setText(deviceInfo.getName() + "CH" + deviceInfo.getChValue());
        } else if (ControlTypeMenu.wenshiduji.value().equals(deviceInfo.getControlType())) {
            ImageLoader.getInstance().displayImage( dcsp.getString(Constant.HTTP_DATA_SERVERS, "")
                    + "/devicelogo/" + deviceInfo.getLogo(), logo, new ImageLoadingBar());
            name.setText(deviceInfo.getName() + "CH" + deviceInfo.getChValue());
        } else {
            // 设置图片
            ImageLoader.getInstance().displayImage(
                     dcsp.getString(Constant.HTTP_DATA_SERVERS, "") + "/devicelogo/" + deviceInfo.getLogo(),
                    logo, new ImageLoadingBar());
            name.setText(deviceInfo.getName());
        }

        footerView_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 加载更多按钮点击
                JavaThreadPool.getInstance().excute(new CommandLoad(commandInfos.size(), 20));
            }
        });
    }

    private ImageView iv_chart, iv_new_chart;

    private void initView() {
        iv_new_chart = (ImageView) findViewById(R.id.iv_new_chart);
        iv_new_chart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
//                intent.setClass(THHistoryActivity.this, WSDChartActivity.class);
                intent.putExtra("device", deviceInfo);
                startActivity(intent);
            }
        });
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_title.setText(TextUtils.isEmpty(deviceInfo.getName()) ? "" : deviceInfo.getName());
//        iv_chart = (ImageView) findViewById(R.id.iv_chart);
//        iv_chart.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setClass(THHistoryActivity.this, THLineChartActivity.class);
//                intent.putExtra("device", deviceInfo);
//                startActivity(intent);
//            }
//        });


        command_list = (ListView) findViewById(R.id.command_list);

        footerView = LayoutInflater.from(THHistoryActivity.this).inflate(R.layout.list_foot_loadmore, null);
        footerView_button = (Button) footerView.findViewById(R.id.load_more);
    }

    public void back(View v) {
        finish();
    }

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 10:
                    cancelInProgress();
//                    commandInfos.clear();
                    commandInfos.addAll((Collection<? extends HistoryCommandInfo>) msg.obj);
                    commandAdapter.notifyDataSetChanged();
                    if (totalSize == commandInfos.size()) {
                        command_list.removeFooterView(footerView);
                    }
                    break;
            }
            return false;
        }
    };
    private Handler defHandler = new WeakRefHandler(mCallback);

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
            object.put("id", deviceInfo.getId());
            object.put("start", this.start);
            object.put("size", this.size);
            object.put("mid", 0);

            JSONArray array1 = new JSONArray();
            JSONObject o = new JSONObject();
            o.put("dt", 3);
            if (DeviceInfo.CaMenu.wenshiduji.value().equals(deviceInfo.getCa())){
                JSONObject oo = new JSONObject();
                oo.put("dt", 4);
                array1.add(oo);
            }
            array1.add(o);
            object.put("dts", array1);
            object.put("c", 1);
            String result = HttpRequestUtils.requestoOkHttpPost(
                     server + "/jdm/s3/dhv/hvg", object,
                    THHistoryActivity.this);
            if ("-3".equals(result)) {
                defHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(THHistoryActivity.this, getString(R.string.history_response_nodevice),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if (result.length() > 4) {
                JSONObject resultJson = null;
                try {
                    resultJson = JSON.parseObject(result);
                    Log.d(TAG, "th： " + resultJson.toString());
                } catch (Exception e) {
                    LogUtil.e(getApplicationContext(), TAG, "解密错误：：", e);
                    return;
                }
                JSONArray array = resultJson.getJSONArray("result");
                totalSize = resultJson.getIntValue("total");

                List<HistoryCommandInfo> infos = new ArrayList<>();
                if (array != null && !array.isEmpty()) {
                    for (int i = 0; i < array.size(); i++) {
                        JSONObject jsonObject = array.getJSONObject(i);
                        HistoryCommandInfo commandInfo = new HistoryCommandInfo();
                        JSONArray data = jsonObject.getJSONArray("vs");
                        double t = 0;
                        double h = 0;
                        String type = "℃";
                        DecimalFormat decimalFormat = new DecimalFormat("0.0");
                        for (int j = 0; j < data.size(); j++) {

                            if (data.getJSONObject(j).getString("t").equals(CommandInfo.CommandTypeEnum.temperature.value())) {
                                t = data.getJSONObject(j).getDoubleValue("v");

                                Date date = data.getJSONObject(0).getDate("time");
                                String hour = new SimpleDateFormat("yyyy:MM:dd:HH:mm").format(date.getTime());
                                commandInfo.setDate(hour);
                                commandInfo.setDayOfWeek(getWeek(date));
                                if (dcsp.getString(Constant.SHOW_TEMPERATURE_UNIT, "ssd").equals("hsd")) {
                                    t = ((float) ((t * 1.8 + 32) * 10) / 10);
                                    type = "℉";
                                }
                            } else if (data.getJSONObject(j).getString("t").equals(CommandInfo.CommandTypeEnum.humidity.value())) {
                                h = data.getJSONObject(j).getDoubleValue("v");
                                commandInfo.setCommandShidu(decimalFormat.format(h) + "%");
                            }

                            commandInfo.setCommand(decimalFormat.format(t) + type);

                        }
                        infos.add(commandInfo);
                    }
                }

                Message m = defHandler.obtainMessage(10);
                m.obj = infos;
                defHandler.sendMessage(m);
            }

        }
    }

    public static String getWeek(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        String week = sdf.format(date);
        return week;
    }

    class CommandAdapter extends BaseAdapter {
        LayoutInflater layoutInflater;

        public CommandAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return commandInfos.size();
        }

        @Override
        public Object getItem(int arg0) {
            return commandInfos.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        /**
         * 返回一个view视图，填充gridview的item
         */
        @SuppressLint("NewApi")
        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {

            DeviceInfoView viewCache = new DeviceInfoView();
            if (view == null) {
//                view = layoutInflater.inflate(R.layout.activity_device_command_history_list_item, null);
                view = layoutInflater.inflate(R.layout.activity_zhzj_wsd_command_item, null);

                viewCache.tv_month = (TextView) view.findViewById(R.id.tv_month);
                viewCache.tv_day = (TextView) view.findViewById(R.id.tv_day);
                viewCache.tv_xingqi = (TextView) view.findViewById(R.id.tv_xingqi);
                viewCache.tv_time = (TextView) view.findViewById(R.id.tv_time);
                viewCache.tv_oper = (TextView) view.findViewById(R.id.tv_oper);
                viewCache.iv_circle_hover = (ImageView) view.findViewById(R.id.iv_circle_hover);
                viewCache.iv_circle = (ImageView) view.findViewById(R.id.iv_circle);
                viewCache.wendu_img = (ImageView) view.findViewById(R.id.wendu_img);
                viewCache.wendu_shidu_img = (ImageView) view.findViewById(R.id.wendu_shidu_img);
                viewCache.gray_line = (View) view.findViewById(R.id.gray_line);
//                viewCache.gray_line_top = (View) view.findViewById(R.id.gray_line_top);
                viewCache.tv_command = (TextView) view.findViewById(R.id.tv_command);
                viewCache.tv_commandshidu = (TextView) view.findViewById(R.id.tv_commandshidu);
                view.setTag(viewCache);

            } else {
                viewCache = (DeviceInfoView) view.getTag();
            }

            HistoryCommandInfo commandInfo = commandInfos.get(i);

            String[] array = commandInfo.getDate().split(":");
            if (i != 0) {
                String[] array1 = commandInfos.get(i - 1).getDate().split(":");

                if (!array1[2].equals(array[2])) {
                    viewCache.tv_day.setVisibility(View.VISIBLE);
                    viewCache.tv_month.setVisibility(View.VISIBLE);
                    viewCache.tv_xingqi.setVisibility(View.VISIBLE);
                    viewCache.iv_circle_hover.setVisibility(View.VISIBLE);
                    viewCache.iv_circle.setVisibility(View.GONE);
                    viewCache.tv_time.setTextColor(getResources().getColor(R.color.zhzj_default));
                    viewCache.tv_oper.setTextColor(getResources().getColor(R.color.zhzj_default));
                    viewCache.tv_command.setTextColor(getResources().getColor(R.color.zhzj_default));
                    viewCache.tv_commandshidu.setTextColor(getResources().getColor(R.color.zhzj_default));
                } else {
                    viewCache.iv_circle_hover.setVisibility(View.GONE);
                    viewCache.iv_circle.setVisibility(View.VISIBLE);
                    viewCache.tv_day.setVisibility(View.GONE);
                    viewCache.tv_month.setVisibility(View.GONE);
                    viewCache.tv_xingqi.setVisibility(View.GONE);
                    viewCache.tv_time.setTextColor(getResources().getColor(R.color.black));
                    viewCache.tv_oper.setTextColor(getResources().getColor(R.color.black));
                    viewCache.tv_command.setTextColor(getResources().getColor(R.color.black));
                    viewCache.tv_commandshidu.setTextColor(getResources().getColor(R.color.black));
                }
//                if (i == commandInfos.size() - 1) {
//                    viewCache.gray_line.setVisibility(View.INVISIBLE);
//                }
            } else {
                viewCache.tv_day.setVisibility(View.VISIBLE);
                viewCache.tv_month.setVisibility(View.VISIBLE);
                viewCache.tv_xingqi.setVisibility(View.VISIBLE);
                viewCache.iv_circle_hover.setVisibility(View.VISIBLE);
                viewCache.iv_circle.setVisibility(View.GONE);
                viewCache.tv_time.setTextColor(getResources().getColor(R.color.zhzj_default));
                viewCache.tv_oper.setTextColor(getResources().getColor(R.color.zhzj_default));
                viewCache.tv_command.setTextColor(getResources().getColor(R.color.zhzj_default));
                viewCache.tv_commandshidu.setTextColor(getResources().getColor(R.color.zhzj_default));
            }
            String[] array1;
            try {
                array1 = commandInfos.get(i + 1).getDate().split(":");
                if (!array1[2].equals(array[2])) {
                    viewCache.gray_line.setVisibility(View.GONE);
                } else {
                    viewCache.gray_line.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                viewCache.gray_line.setVisibility(View.GONE);
//                viewCache.gray_line_top.setVisibility(View.GONE);
            }

            viewCache.tv_xingqi.setText(commandInfo.getDayOfWeek());
            viewCache.tv_month.setText(array[1]);
            viewCache.tv_day.setText(array[2]);
            viewCache.tv_time.setText(array[3] + "：" + array[4]);
            DecimalFormat decimalFormat = new DecimalFormat("0.0");
            viewCache.tv_command.setText(commandInfo.getCommand() != null ? commandInfo.getCommand() : "");
            viewCache.tv_oper.setText(commandInfo.getOpreator() != null ? commandInfo.getOpreator() : "");

//            String command = commandInfo.getCommand();
//            if ("tzc".equals(deviceInfo.getCa())) {
//                try {
//                    String unitName = "";
//                    long commandUnit = Long.parseLong(command.substring(0, 4));
//                    double commandValue = Integer.parseInt(command.substring(4), 16) / 10.0;
//                    if (commandUnit == 2) {
//                        unitName = "KG";
//                    }
//                    viewCache.tv_command.setText(commandValue + unitName);
//                } catch (Exception e) {
//                    viewCache.tv_command.setText("error");
//                }
//            } else {
//                if (dcsp.getString(Constant.SHOW_TEMPERATURE_UNIT, "ssd").equals("ssd")) {
//                    viewCache.tv_command.setText(command);
//                } else if (dcsp.getString(Constant.SHOW_TEMPERATURE_UNIT, "ssd").equals("hsd")) {
//                    if (command.contains("℃")) {
//                        viewCache.tv_command.setText(decimalFormat.format(((double) Math
//                                .round((Float.parseFloat(command.substring(0, command.indexOf("℃"))) * 1.8 + 32) * 10)
//                                / 10)) + "℉" + command.substring(command.indexOf("℃") + 1));
//                    } else {
//                        viewCache.tv_command.setText(command);
//                    }
//                }
//            }
            if (!deviceInfo.getCak().equals("control")) {
                viewCache.tv_oper.setVisibility(View.GONE);
            }
            if (deviceInfo.getCa().equals(DeviceInfo.CaMenu.wenshiduji.value())) {
                viewCache.wendu_shidu_img.setVisibility(View.VISIBLE);
                viewCache.wendu_img.setVisibility(View.VISIBLE);
                viewCache.tv_commandshidu.setText(commandInfo.getCommandShidu() != null ? commandInfo.getCommandShidu() : "");
            } else if (deviceInfo.getCa().equals(DeviceInfo.CaMenu.wenduji.value())) {
                viewCache.wendu_shidu_img.setVisibility(View.GONE);
                viewCache.wendu_img.setVisibility(View.VISIBLE);
            } else {
                viewCache.wendu_shidu_img.setVisibility(View.GONE);
                viewCache.wendu_img.setVisibility(View.GONE);
            }
            return view;

        }

        class DeviceInfoView {
            TextView time, command, operator;
            public TextView tv_day, tv_month, tv_xingqi, tv_time, tv_oper, tv_command, tv_commandshidu;
            //            public RelativeLayout rl_day;
//            public LinearLayout ll_time;
            public ImageView iv_circle_hover, iv_circle, wendu_img, wendu_shidu_img;
            private View gray_line;
//            ,gray_line_top;
        }
    }


}
