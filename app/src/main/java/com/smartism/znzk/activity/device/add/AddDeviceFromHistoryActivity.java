package com.smartism.znzk.activity.device.add;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.device.DeviceMainActivity;
import com.smartism.znzk.activity.device.ZhujiListFragment;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.DeviceInfo.ControlTypeMenu;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.WeakRefHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AddDeviceFromHistoryActivity extends ActivityParentActivity implements OnClickListener {
    private ZhujiInfo zhuji;
    private Button btn_add, btn_del;
    private ListView historyListView;
    private DeviceHistoryAdapter historyAdapter;
    private List<DeviceInfo> dInfos;

    // 显示图片的配置
    DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.loading)
            .showImageOnFail(R.drawable.sorrow).cacheInMemory(true).cacheOnDisc(true)
            .bitmapConfig(Bitmap.Config.RGB_565).imageScaleType(ImageScaleType.EXACTLY_STRETCHED)// 设置图片以如何的编码方式显示
            .resetViewBeforeLoading(true)// 设置图片在下载前是否重置，复位
            // .displayer(new RoundedBitmapDisplayer(20))//是否设置为圆角，弧度为多少
            .displayer(new FadeInBitmapDisplayer(100))// 是否图片加载好后渐入的动画时间
            .build();

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    cancelInProgress();
                    dInfos = new ArrayList<DeviceInfo>();
                    dInfos.addAll((Collection<? extends DeviceInfo>) msg.obj);
                    historyAdapter = new DeviceHistoryAdapter();
                    historyListView.setAdapter(historyAdapter);
                    break;
                case 2:
                    cancelInProgress();
                    if (dInfos != null) {
                        dInfos.clear();
                        historyAdapter.notifyDataSetChanged();
                    }
                    Toast.makeText(AddDeviceFromHistoryActivity.this, getString(R.string.net_error_nodata),
                            Toast.LENGTH_LONG).show();
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
        setContentView(R.layout.activity_add_device_from_history);
        historyListView = (ListView) findViewById(R.id.history_list);
        btn_add = (Button) findViewById(R.id.btn_add);
        btn_add.setOnClickListener(this);
        btn_add.setEnabled(false);
        btn_del = (Button) findViewById(R.id.btn_del);
        btn_del.setOnClickListener(this);
        btn_del.setEnabled(false);
        initData();
        initEvent();
    }

    private void initData() {
//        zhuji = DatabaseOperator.getInstance(AddDeviceFromHistoryActivity.this)
//                .queryDeviceZhuJiInfo(dcsp.getString(Constant.APP_MASTERID, ""));
        //替换
        zhuji = DatabaseOperator.getInstance(AddDeviceFromHistoryActivity.this).queryDeviceZhuJiInfo(ZhujiListFragment.getMasterId());
        showInProgress(getString(R.string.loading), false, false);
        JavaThreadPool.getInstance().excute(new DiviceHistoryLoad());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add:
                long id = 0;
                for (DeviceInfo d : dInfos) {
                    if (d.isFlag()) {
                        if (id != 0) {
                            Toast.makeText(AddDeviceFromHistoryActivity.this, getString(R.string.device_add_from_history_sone), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        id = d.getId();
                    }
                }
                if(id==0){
                    return ;
                }
                addOneDevice(id);
                break;
            case R.id.btn_del:
                JSONArray ids = new JSONArray();
                for (DeviceInfo d : dInfos) {
                    if (d.isFlag()) {
                        JSONObject o = new JSONObject();
                        o.put("id", d.getId());
                        ids.add(o);
                    }
                }
                if(ids.size()==0){
                    return ;
                }
                delSelectedDevice(ids);
                break;

            default:
                break;
        }
    }

    private void delSelectedDevice(final JSONArray ids) {
        showInProgress(getString(R.string.submiting), false, true);
        JavaThreadPool.getInstance().excute(new Runnable() {

            @Override
            public void run() {
                String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("ids", ids);
                String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/dh/del", pJsonObject, AddDeviceFromHistoryActivity.this);

                if ("0".equals(result)) {
                    defHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(AddDeviceFromHistoryActivity.this,
                                    getString(R.string.success), Toast.LENGTH_LONG).show();
                            initData();
                        }
                    });
                }

            }
        });

    }

    private void addOneDevice(final long id) {
        showInProgress(getString(R.string.submiting), false, true);
        JavaThreadPool.getInstance().excute(new Runnable() {

            @Override
            public void run() {
                String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("id", id);
                String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/dh/addto", pJsonObject, AddDeviceFromHistoryActivity.this);

                if ("0".equals(result)) {
                    defHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(AddDeviceFromHistoryActivity.this,
                                    getString(R.string.activity_add_zhuji_havezhu_addsuccess), Toast.LENGTH_LONG).show();
                            Intent in = new Intent();
                            in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            in.setClass(AddDeviceFromHistoryActivity.this, DeviceMainActivity.class);
                            startActivity(in);
                        }
                    });
                } else if ("-3".equals(result)) {
                    defHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(AddDeviceFromHistoryActivity.this, getString(R.string.add_submit_device_have),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }


    public void back(View v) {
        finish();
    }


    class DiviceHistoryLoad implements Runnable {
        @Override
        public void run() {
            String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("masterid", zhuji.getMasterid());
            String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/dh/list", object, AddDeviceFromHistoryActivity.this);
            // -1参数为空，-2设备不存在
            if ("0".equals(result)) {
                Message m = defHandler.obtainMessage(2);
                defHandler.sendMessage(m);
            } else if (result != null && result.length() > 5) {
                List<DeviceInfo> dList = new ArrayList<DeviceInfo>();
                JSONArray array = null;
                try {
                    array = JSON.parseArray(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (array != null && !array.isEmpty()) {
                    for (int j = 0; j < array.size(); j++) {
                        JSONObject jd = array.getJSONObject(j);
                        DeviceInfo d = new DeviceInfo();
                        d.setId(jd.getLongValue("id"));
                        d.setLogo(jd.getString("logo"));
                        d.setName(jd.getString("name"));
                        d.setWhere(jd.getString("where"));
                        d.setType(jd.getString("type"));
                        dList.add(d);
                    }
                }
                Message m = defHandler.obtainMessage(1);
                m.obj = dList;
                defHandler.sendMessage(m);
            }


        }
    }

    class DeviceHistoryAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return dInfos != null ? dInfos.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return dInfos != null ? dInfos.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DeviceInfoView spImg = new DeviceInfoView();

            if (convertView == null) {
                convertView = View.inflate(AddDeviceFromHistoryActivity.this, R.layout.zhzj_add_devices_history_list_item, null);
                spImg.checked = (ImageView) convertView.findViewById(R.id.checked);
                spImg.name = (TextView) convertView.findViewById(R.id.device_name);
                convertView.setTag(spImg);
            } else {
                spImg = (DeviceInfoView) convertView.getTag();
            }
            setShowInfo(spImg, position);
            return convertView;
        }

        /**
         * 设置设备logo图片和名称
         *
         * @param i
         */
        private void setShowInfo(DeviceInfoView viewCache, int i) {
            if (ControlTypeMenu.wenduji.value().equals(dInfos.get(i).getControlType())) {

                viewCache.name.setText(dInfos.get(i).getName() + "CH" + dInfos.get(i).getChValue());
            } else if (ControlTypeMenu.wenshiduji.value().equals(dInfos.get(i).getControlType())) {

                viewCache.name.setText(dInfos.get(i).getName() + "CH" + dInfos.get(i).getChValue());
            } else {
                String sign = "-";
                if (TextUtils.isEmpty(dInfos.get(i).getName()) || TextUtils.isEmpty(dInfos.get(i).getType()))
                    sign = "";
                viewCache.name.setText(dInfos.get(i).getName() + sign + dInfos.get(i).getType());
            }

            if (dInfos.get(i).isFlag()) {
                viewCache.checked.setBackgroundResource(R.drawable.zhzj_date_xuanzhong);
            } else {
                viewCache.checked.setBackgroundResource(R.drawable.zhzj_date_moren);
            }
        }

        class DeviceInfoView {
            ImageView checked;
            TextView name;
        }
    }

    public void initEvent() {
        historyListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (dInfos != null) {
                    for (DeviceInfo d : dInfos) { //全部取消
                        d.setFlag(false);
                    }
                    if (dInfos.get(position).isFlag()) {
                        dInfos.get(position).setFlag(false);
                    } else {
                        dInfos.get(position).setFlag(true);
                    }
                    boolean isSet = false;
                    for (DeviceInfo d : dInfos) {
                        if (d.isFlag()) {
                            btn_add.setEnabled(true);
                            btn_del.setEnabled(true);
                            isSet = true;
                            break;
                        }
                    }
                    if (!isSet) {
                        btn_add.setEnabled(false);
                        btn_del.setEnabled(false);
                    }
                }
                historyAdapter.notifyDataSetChanged();
            }
        });
    }
}
