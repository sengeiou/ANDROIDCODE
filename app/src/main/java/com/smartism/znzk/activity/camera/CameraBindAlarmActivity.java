package com.smartism.znzk.activity.camera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.p2p.core.P2PHandler;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.domain.camera.CameraBindInfo;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.util.WeakRefHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * 摄像头联动报警
 */
public class CameraBindAlarmActivity extends ActivityParentActivity implements View.OnClickListener {
    private final int handler_timeout_operation = 10,handler_timeout_get = 11;
    public List<CameraBindInfo> camera = null;
    private int operationCameraIndex = -1;//当前操作的摄像头 在list中的位置
    private DeviceInfo deviceInfo;
    private ListView lv_camera;
    ImageView back;
    private BindingAdapter adapter;
    String cameraId = null;
    private boolean isRegFilter = false;

    private ZhujiInfo zhuji;

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case handler_timeout_get:// 获取超时
                    if (camera.size() > 0){
                        for (CameraBindInfo cInfo : camera) {
                            if (!cInfo.isInitSuccess()){
                                cInfo.setProgressing(false);
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                    break;
                case handler_timeout_operation:// 操作超时
                    if (operationCameraIndex != -1) {
                        Toast.makeText(CameraBindAlarmActivity.this, getString(R.string.time_out),
                                Toast.LENGTH_LONG).show();
                        camera.get(operationCameraIndex).setProgressing(false);
                        adapter.notifyDataSetChanged();
                    }
                    operationCameraIndex = -1;
                    break;
            }
            return false;
        }
    };
    private Handler defaultHandler = new WeakRefHandler(mCallback);

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (intent.getAction().equals(Constants.P2P.RET_SET_DEFENCE_AREA)) {//防区信息
                /**返回值：
                 0：学习成功
                 30：清除成功
                 32：此码已学
                 41：设备不支持
                 24：该通道已学
                 25：正在学码  应该是多次学习会返回，一般学习不返回
                 26：学习超时
                 37：无效的码值
                 * */
                int result = intent.getIntExtra("result", -1);
                if (operationCameraIndex >= 0) {
                    if (result == 0 && camera.get(operationCameraIndex).isChecked()) {
                        camera.get(operationCameraIndex).setProgressing(false);
                        camera.get(operationCameraIndex).setChecked(false);
                        operationCameraIndex = -1;
                        defaultHandler.removeMessages(handler_timeout_operation);
                    } else {
                        if ((result == 0 || result == 32) && !camera.get(operationCameraIndex).isChecked()) {//绑定成功,32码已经学过了
                            camera.get(operationCameraIndex).setChecked(true);
                            Toast.makeText(CameraBindAlarmActivity.this,getString(R.string.activity_beijingmy_bindsuccess),Toast.LENGTH_SHORT).show();
                            P2PHandler.getInstance().setDefenceAreaName(camera.get(operationCameraIndex).getId(), camera.get(operationCameraIndex).getContactPassword(), 0, 1, 0, zhuji.getMasterid(), MainApplication.GWELL_LOCALAREAIP);//修改防区名称
                            operationCameraIndex = -1;
                            defaultHandler.removeMessages(handler_timeout_operation);
                        } else if (result == 24) { //通道被占，删除重加
                            P2PHandler.getInstance().setDefenceAreaState(camera.get(operationCameraIndex).getId(), camera.get(operationCameraIndex).getContactPassword(), 1, 0, 1,MainApplication.GWELL_LOCALAREAIP);//防区通道固定0，防区固定1 ，type 1表示删除
                            P2PHandler.getInstance().setDefenceAreaState(camera.get(operationCameraIndex).getId(), camera.get(operationCameraIndex).getContactPassword(), 1, 0, 0,MainApplication.GWELL_LOCALAREAIP);//防区通道固定0，防区固定1 ，type 0表示学习
                            SyncMessage message1 = new SyncMessage();
                            message1.setCommand(SyncMessage.CommandMenu.rq_control.value());
                            message1.setDeviceid(zhuji.getId());
                            // 操作
                            message1.setSyncBytes(new byte[]{(byte) 100}); //主机固定100 发送报警码
                            SyncMessageContainer.getInstance().produceSendMessage(message1);
                        }else{
                            camera.get(operationCameraIndex).setProgressing(false);
                            operationCameraIndex = -1;
                            defaultHandler.removeMessages(handler_timeout_operation);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            } else if (intent.getAction().equals(Constants.P2P.RET_GET_DEFENCE_AREA_NAME)) {//获取防区名称
                byte[] data = (byte[]) intent.getSerializableExtra("data");
                if (data != null) {
                    try {
                        for (int j=0;j<camera.size();j++) {
                            if (camera.get(j).getId().equals(intent.getStringExtra("contactid"))) {
                                if (data.length > 24) {//大于24byte才有探头数据，只获取探头
                                    for (int i = 24; i < data.length; ) {
                                        byte[] td = new byte[19];//一个传感器数据的长度为19
                                        System.arraycopy(data, i, td, 0, 19);
                                        i = i + 19;
                                        if (td[0] == 1 && td[1] == 0) {//防区为1 通道为0
                                            byte[] bname = new byte[10];//固定的是主机序列号，10byte
                                            System.arraycopy(td, 3, bname, 0, 10);
                                            String mName = new String(bname);
                                            if (zhuji != null && zhuji.getMasterid().equals(mName)) {//存在，需要打开按钮
                                                camera.get(j).setChecked(true);
                                            } else {//按钮关闭
                                                camera.get(j).setChecked(false);
                                            }
                                        }
                                    }
                                } else if (data.length > 3 && data[2] > 40) {//当不支持报警绑定是返回的是41，正常返回的是1
                                    camera.get(j).setSupport433Alarm(false);
                                }
                                camera.get(j).setProgressing(false);
                                camera.get(j).setInitSuccess(true);

                            }
                        }
                    } catch (Exception ex) {
                        Log.e("jdm", "onReceive: + 解失败了");
                    }
                }
                adapter.notifyDataSetChanged();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_bind_alarm_list);
        zhuji = (ZhujiInfo) getIntent().getSerializableExtra("zhuji");
        initView();
        regFilter();
        initData();
    }


    private void initView() {
        lv_camera = (ListView) findViewById(R.id.lv_camera);
        back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(this);
    }


    private void initData() {
        if (camera == null) {
            camera = new ArrayList<>();
        }

        List<DeviceInfo> dInfos = DatabaseOperator.getInstance(CameraBindAlarmActivity.this).queryAllDeviceInfos(zhuji.getId());
        if (dInfos != null && !dInfos.isEmpty()) {
            for (DeviceInfo device : dInfos) {
                if (device.getCak().equals("surveillance")) {
                    this.deviceInfo = device;
                    break;

                }
            }
        }

        if (deviceInfo != null && deviceInfo.getCak().equals("surveillance")) {
            List<CameraBindInfo> list = JSON.parseArray(deviceInfo.getIpc(), CameraBindInfo.class);
            camera.addAll(list);
        }

        adapter = new BindingAdapter(camera, this);
        lv_camera.setAdapter(adapter);

        if (camera.size() > 0){
            for (CameraBindInfo cInfo : camera) {
                P2PHandler.getInstance().getDefenceAreaName(cInfo.getId(), cInfo.getContactPassword(),0,MainApplication.GWELL_LOCALAREAIP);//获取所有防区名称
            }
            defaultHandler.sendEmptyMessageDelayed(handler_timeout_get,5000);
        }
    }

    public void regFilter() {
        IntentFilter filter = new IntentFilter();

        filter.addAction(Constants.P2P.RET_SET_DEFENCE_AREA);
        filter.addAction(Constants.P2P.RET_GET_DEFENCE_AREA_NAME);

        mContext.registerReceiver(mReceiver, filter);
        isRegFilter = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isRegFilter) {
            mContext.unregisterReceiver(mReceiver);
            isRegFilter = false;
        }
        defaultHandler.removeMessages(handler_timeout_get);
        defaultHandler.removeMessages(handler_timeout_operation);
    }

    public class BindingAdapter extends BaseAdapter {
        private List<CameraBindInfo> CameraBindInfos;
        private Context context;

        public BindingAdapter(List<CameraBindInfo> CameraBindInfos, Context context) {
            this.CameraBindInfos = CameraBindInfos;
            this.context = context;
        }

        @Override
        public int getCount() {
            if (CameraBindInfos == null) return 0;
            return CameraBindInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return CameraBindInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHandler vh = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.activity_camera_alarm_list_item, null);
                vh = new ViewHandler();
                vh.layout_item_camera = (LinearLayout) convertView.findViewById(R.id.layout_item_camera);
                vh.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
                vh.img_alarm_bind_zj = (ImageView) convertView.findViewById(R.id.img_alarm_bind_zj);
                vh.progressBar_alarm_bindzj = (ProgressBar) convertView.findViewById(R.id.progressBar_alarm_bind_zj);
                convertView.setTag(vh);
            } else {
                vh = (ViewHandler) convertView.getTag();
            }
            CameraBindInfo cameraBindInfo = CameraBindInfos.get(position);
            vh.tv_name.setText(cameraBindInfo.getN());
            if (cameraBindInfo.isProgressing()){
                vh.progressBar_alarm_bindzj.setVisibility(ProgressBar.VISIBLE);
                vh.img_alarm_bind_zj.setVisibility(View.GONE);
            }else {
                if (cameraBindInfo.isInitSuccess()) {
                    vh.progressBar_alarm_bindzj.setVisibility(ProgressBar.GONE);
                    vh.img_alarm_bind_zj.setVisibility(View.VISIBLE);
                    if (cameraBindInfo.isChecked()) {
                        vh.img_alarm_bind_zj.setImageResource(R.drawable.zhzj_switch_on);
                    } else {
                        vh.img_alarm_bind_zj.setImageResource(R.drawable.zhzj_switch_off);
                    }
                }else{
                    vh.progressBar_alarm_bindzj.setVisibility(ProgressBar.GONE);
                    vh.img_alarm_bind_zj.setVisibility(View.VISIBLE);
                    vh.img_alarm_bind_zj.setImageResource(R.drawable.zhzj_abnormal);
                }
            }

            vh.layout_item_camera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (CameraBindInfos.get(position).isInitSuccess()) {
                        if (CameraBindInfos.get(position).isSupport433Alarm()) {
                            if (operationCameraIndex >= 0) {
                                Toast.makeText(mContext, getString(R.string.bind_zjalarm_notmore), Toast.LENGTH_SHORT).show();
                            } else {
                                operationCameraIndex = position;
                                CameraBindInfos.get(position).setProgressing(true);
                                adapter.notifyDataSetChanged();
                                defaultHandler.sendEmptyMessageDelayed(handler_timeout_operation, 5000);
                                if (!CameraBindInfos.get(position).isChecked()) {
                                    P2PHandler.getInstance().setDefenceAreaState(CameraBindInfos.get(position).getId(), CameraBindInfos.get(position).getContactPassword(), 1, 0, 0,MainApplication.GWELL_LOCALAREAIP);//防区通道固定0，防区固定1 ，type 0表示学习
                                    SyncMessage message1 = new SyncMessage();
                                    message1.setCommand(SyncMessage.CommandMenu.rq_control.value());
                                    message1.setDeviceid(zhuji.getId());
                                    // 操作
                                    message1.setSyncBytes(new byte[]{(byte) 100}); //主机固定100 发送报警码
                                    SyncMessageContainer.getInstance().produceSendMessage(message1);
                                } else {
                                    P2PHandler.getInstance().setDefenceAreaState(CameraBindInfos.get(position).getId(), CameraBindInfos.get(position).getContactPassword(), 1, 0, 1,MainApplication.GWELL_LOCALAREAIP);//防区通道固定0，防区固定1 ，type 1表示删除  删除第一次返回的是0
//                          P2PHandler.getInstance().setDefenceAreaState(idOrIp,contact.getContactPassword(),1,0,1);//防区通道固定0，防区固定1 ，type 1表示删除  删除第二次返回的是30
                                }
                            }
                        } else {
                            Toast.makeText(mContext, getString(R.string.bind_zjalarm), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });

            return convertView;
        }

        public class ViewHandler {
            public LinearLayout layout_item_camera;
            public TextView tv_name;
            public ImageView img_alarm_bind_zj;
            public ProgressBar progressBar_alarm_bindzj;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
        }
    }
}
