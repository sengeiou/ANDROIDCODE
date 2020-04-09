package com.smartism.znzk.activity.yaokan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.domain.YKDeviceInfo;
import com.smartism.znzk.domain.yankan.DeviceTypeResult;
import com.smartism.znzk.util.Actions;

import java.util.ArrayList;
import java.util.List;


/**
 * 类型--(空调)
 */
public class YKGetDeviceTypeActivity extends ActivityParentActivity {

    //    private List<DeviceType> mList = new ArrayList<DeviceType>();
    private GridView gv;
    private DeviceTypeResult deviceTypeResult;
    private Long did;
    private List<YKDeviceInfo> ykDeviceInfos;


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yankan_device_display);
        Log.d("taskId:", "deviceType" + getTaskId() + "");
        initData();
        initView();
    }


    private void initData() {
        ykDeviceInfos = new ArrayList<>();
        deviceTypeResult = (DeviceTypeResult) getIntent().getSerializableExtra("deviceTypeResult");
        did = getIntent().getLongExtra("did", 0);
        //现在只打开空调功能
        for (int i = 0; i < deviceTypeResult.getRs().size(); i++) {
            YKDeviceInfo info = new YKDeviceInfo();
            if (deviceTypeResult.getRs().get(i).getTid() == 7) {
                info.setName(getString(R.string.hwzf_kt));
                info.setImageId(R.drawable.yaokan_ctrl_d_air);
                info.settId(deviceTypeResult.getRs().get(i).getTid());
                ykDeviceInfos.add(info);
            } else if (deviceTypeResult.getRs().get(i).getTid() == 6) {
                info.setName(getString(R.string.hwzf_fan_fan));
                info.setImageId(R.drawable.icon_yk_fan);
                info.settId(deviceTypeResult.getRs().get(i).getTid());
                ykDeviceInfos.add(info);
            } else if (deviceTypeResult.getRs().get(i).getTid() == 2) {
                info.setName(getString(R.string.hwzf_tv_tv));
                info.setImageId(R.drawable.icon_yk_tv);
                info.settId(deviceTypeResult.getRs().get(i).getTid());
                ykDeviceInfos.add(info);
            } else if (deviceTypeResult.getRs().get(i).getTid() == 1) {
                info.setName(getString(R.string.hwzf_tvbox_tvbox));
                info.setImageId(R.drawable.icon_yk_tvbox);
                info.settId(deviceTypeResult.getRs().get(i).getTid());
                ykDeviceInfos.add(info);
            }
        }

        IntentFilter filter = new IntentFilter(Actions.FINISH_YK_EXIT);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }

    private void initView() {
        gv = (GridView) findViewById(R.id.gv);
        gv.setAdapter(new DeviceTypeAdapter());
        gv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int typeId = ykDeviceInfos.get(position).gettId();
                //Log.e("aaa", typeId + "");
                Intent intent = new Intent();
                intent.putExtra("typeId", typeId);
                intent.putExtra("tname", ykDeviceInfos.get(position).getName());
                intent.putExtra("did", did);
                intent.setClass(getApplicationContext(), YKDisplayActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    class DeviceTypeAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return ykDeviceInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = View.inflate(YKGetDeviceTypeActivity.this, R.layout.listview_yankan_dev_item, null);
                holder = new ViewHolder();
                holder.tv = (TextView) convertView.findViewById(R.id.tv_title);
                holder.iv = (ImageView) convertView.findViewById(R.id.iv_icon);
                holder.relativeLayout = (RelativeLayout) convertView.findViewById(R.id.rl_device_item);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv.setSelected(true);
            holder.tv.setText(ykDeviceInfos.get(position).getName());
            holder.iv.setImageResource(ykDeviceInfos.get(position).getImageId());
            holder.relativeLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int typeId = ykDeviceInfos.get(position).gettId();
                    Intent intent = new Intent();
                    intent.putExtra("typeId", typeId);
                    intent.putExtra("tname", ykDeviceInfos.get(position).getName());
                    intent.putExtra("did", did);
                    intent.setClass(getApplicationContext(), YKDisplayActivity.class);
                    startActivity(intent);
                }
            });
//            holder.iv.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    int typeId = ykDeviceInfos.get(position).gettId();
//                    Intent intent = new Intent();
//                    intent.putExtra("typeId", typeId);
//                    intent.putExtra("tname", ykDeviceInfos.get(position).getName());
//                    intent.putExtra("did", did);
//                    intent.setClass(getApplicationContext(), YKDisplayActivity.class);
//                    startActivity(intent);
//                }
//            });
//            holder.tv.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    int typeId = ykDeviceInfos.get(position).gettId();
//                    Intent intent = new Intent();
//                    intent.putExtra("typeId", typeId);
//                    intent.putExtra("tname", ykDeviceInfos.get(position).getName());
//                    intent.putExtra("did", did);
//                    intent.setClass(getApplicationContext(), YKDisplayActivity.class);
//                    startActivity(intent);
//                }
//            });
            return convertView;
        }
    }

    class ViewHolder {
        TextView tv;
        ImageView iv;
        RelativeLayout relativeLayout;
    }
}
