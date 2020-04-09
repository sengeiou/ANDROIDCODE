package com.smartism.znzk.activity.camera;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lsemtmf.genersdk.tools.commen.ToastTools;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.device.DeviceInfoActivity;
import com.smartism.znzk.adapter.camera.BindingAdapter;
import com.smartism.znzk.communication.data.SyncDataDispatcher;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.domain.camera.CameraInfo;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.global.FList;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.WeakRefHandler;

import java.util.ArrayList;
import java.util.List;

public class CameraListActivity extends ActivityParentActivity {

    public List<CameraInfo> camera = null;
    private DeviceInfo deviceInfo;
//    private ZhujiInfo zhuji;
    private ListView lv_camera;
    private Button binding;
    ImageView back;
    private BindingAdapter adapter;
    private DeviceInfo operationDevice;
    private CameraInfo currentChoiceCameraInfo;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case 10:// 获取数据成功
                    cancelInProgress();
                    defaultHandler.removeMessages(11);
                    if (cameraId!=null){
                        if ("0".equals(cameraId)){
                            Toast.makeText(CameraListActivity.this, getString(R.string.activity_bindipc_jiechu),
                                    Toast.LENGTH_LONG).show();

                        }else{
                            Toast.makeText(CameraListActivity.this, getString(R.string.activity_bindipc_success),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    cameraId = null;
                    finish();
                    break;
                case 11:// 超时
                    cancelInProgress();
                    cameraId = null;
                    Toast.makeText(CameraListActivity.this, "超时",
                            Toast.LENGTH_LONG).show();
                    break;
                case 12://勾选已经绑定的
                    cancelInProgress();
                    defaultHandler.removeMessages(11);
                    Contact contact = (Contact) msg.obj;
                    adapter.changeItemCheckBox(contact.contactId);
                    break;
                case 13:// 超时
                    cancelInProgress();
                    break;
            }
            return false;
        }
    };
    private Handler defaultHandler = new WeakRefHandler(mCallback);

    TextView no_camera_notice ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_list);
        operationDevice = (DeviceInfo) getIntent().getSerializableExtra("device");
        if(operationDevice.getCa().equals(DeviceInfo.CaMenu.wifizns.value())){
            initWifiSuoData();
        }else{
            initData();
        }
        initView();
        initValue();
        initEvent();
        if(camera.size()==0){
            //当前没有可以绑定的摄像头
            no_camera_notice = findViewById(R.id.no_camera_comment);
            no_camera_notice.setVisibility(View.VISIBLE);
        }
    }

    private void initWifiSuoData(){
        if(camera==null){
            camera = new ArrayList<>();
        }
        camera.clear();
        List<ZhujiInfo> infos = DatabaseOperator.getInstance(this).queryAllZhuJiInfos();
        for(ZhujiInfo zhujiInfo :infos){
            if(zhujiInfo.getCa().equals(DeviceInfo.CaMenu.ipcamera.value())){
                camera.add(zhujiInfo.getCameraInfo());
            }
        }
    }
    private void initValue() {
        adapter = new BindingAdapter(camera,this,lv_camera);
        lv_camera.setAdapter(adapter);
        //获取已经绑定的id
        defaultHandler.sendEmptyMessageDelayed(13, 10 * 1000);
        showInProgress(getString(R.string.loading), false, true);
        JavaThreadPool.getInstance().excute(new BindingCameraLoad(operationDevice.getBipc()));
    }


    private  void initView() {
        lv_camera = (ListView) findViewById(R.id.binding_camera_listview);
        binding = (Button) findViewById(R.id.binding_camera_sure);
        back = (ImageView) findViewById(R.id.binding_top_back);

    }

    String cameraId = null;
    private void initEvent() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(camera.size()==0){
                    ToastTools.short_Toast(mContext,getString(R.string.no_bind_camera));
                    return ;
                }
                cameraId = getCameraId();
                defaultHandler.sendEmptyMessageDelayed(11, 10 * 1000);
                showInProgress(getString(R.string.loading), false, true);
                JavaThreadPool.getInstance().excute(new CommandLoad(cameraId,
                        DeviceInfo.ControlTypeMenu.group.value().equals(operationDevice.getControlType())));
            }
        });
    }

    /**
     * 获取摄像头id
     * @return
     */
    public String getCameraId(){
        for (int indeex = 0;indeex<camera.size();indeex++){
            View view = lv_camera.getChildAt(indeex);
            BindingAdapter.ViewHandler vh = (BindingAdapter.ViewHandler) view.getTag();
            if (vh!=null) {
                if (vh.checkBox.isChecked()) {
                    currentChoiceCameraInfo = camera.get(indeex);
                    return camera.get(indeex).getId();
                }
            }
        }
        //没有选中时，设置为null
        currentChoiceCameraInfo = null ;
        return 0+"";
    }
    private void initData() {
        if (camera == null) {
            camera = new ArrayList<>();
        }

        camera.clear();
//        zhuji = DatabaseOperator.getInstance(getApplicationContext()).queryDeviceZhuJiInfo(operationDevice.getZj_id());
        //StartSearchDevice(); 这个好像是搜索局域网内的在线摄像头。不需要
        List<DeviceInfo> dInfos = DatabaseOperator.getInstance(CameraListActivity.this).queryAllDeviceInfos(operationDevice.getZj_id());
        if (dInfos != null && !dInfos.isEmpty()) {
            for (DeviceInfo device : dInfos) {
                if (device.getCa().equals(DeviceInfo.CaMenu.ipcamera.value())) {
                    this.deviceInfo = device;
                    break;

                }
            }
        }

        if (deviceInfo != null && deviceInfo.getCak().equals("surveillance")) {
            List<CameraInfo> list = (List<CameraInfo>) JSON.parseArray(deviceInfo.getIpc(), CameraInfo.class);
            camera.addAll(list);
        }

        //由于雄迈暂时添加到主机列表，判断一下主机列表是否有雄迈，有的话加进来
        List<ZhujiInfo> infos = DatabaseOperator.getInstance(this).queryAllZhuJiInfos();
        for(ZhujiInfo zhujiInfo :infos){
            if(zhujiInfo.getCa().equals(DeviceInfo.CaMenu.ipcamera.value())){
                zhujiInfo.getCameraInfo().setZjName(zhujiInfo.getName());
                camera.add(zhujiInfo.getCameraInfo());
            }
        }

        // FList.getInstance().list().clear();
        if (camera != null && camera.size() > 0) {
            FList.getInstance().list().clear();
            for (int i = 0; i < camera.size(); i++) {
                Contact c = new Contact();
                c.contactId = camera.get(i).getId();
                c.contactName = camera.get(i).getN();
                c.contactPassword = camera.get(i).getP();
                c.userPassword = camera.get(i).getOriginalP();
                if (camera.get(i).getC().equals("jiwei")) {
                    FList.getInstance().insert(c);
                    FList.getInstance().updateLocalDeviceFlag(c.contactId, Constants.DeviceFlag.ALREADY_SET_PASSWORD);
                    FList.getInstance().updateLocalDeviceWithLocalFriends();

                }
            }
        }
    }

    /**
     * 请求数据子线程,绑定摄像头
     */
    class CommandLoad implements Runnable {
        private String  id;
        private long uid;
        private String code;
        private boolean isGroup;

        public CommandLoad(String id,boolean isGroup) {
            this.id = id;
            this.isGroup = isGroup;
            uid = DataCenterSharedPreferences.getInstance(mContext, DataCenterSharedPreferences.Constant.CONFIG)
                    .getLong(DataCenterSharedPreferences.Constant.LOGIN_APPID, 0);
            code = DataCenterSharedPreferences
                    .getInstance(mContext, DataCenterSharedPreferences.Constant.CONFIG).getString(DataCenterSharedPreferences.Constant.LOGIN_CODE, "");
        }

        @Override
        public void run() {

            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("uid", uid);
            object.put("code", code);
            object.put("did", operationDevice.getId());
            Log.e("wxb", "object---->" + uid + ":" + code + ":" + operationDevice.getId());
            if (isGroup){
                object.put("gid", operationDevice.getId());
                object.put("did", 0);
            }else{
                object.put("gid", 0);
                object.put("did", operationDevice.getId());
            }
            object.put("iid",id);

            if(currentChoiceCameraInfo!=null){
                object.put("ipcid",currentChoiceCameraInfo.getIpcid());
                if(currentChoiceCameraInfo.getC().equals(CameraInfo.CEnum.xiongmai.value())){
                    object.put("ibrand",CameraInfo.CEnum.xiongmai.value());
                }else if(currentChoiceCameraInfo.getC().equals(CameraInfo.CEnum.jiwei.value())){
                    object.put("ibrand",CameraInfo.CEnum.jiwei.value());
                }
            }else{
                //为null时，表示用户想进行解绑
                object.put("ipcid","0");
                object.put("ibrand", "jiwei");
            }
//            String result = HttpRequestUtils.requestHttpServer(
//                     server + "/jdm/service/ipcs/bindIPC?v="
//                            + URLEncoder.encode(SecurityUtil.crypt(object.toJSONString(), DataCenterSharedPreferences.Constant.KEY_HTTP)),
//                    CameraListActivity.this, defaultHandler);
            String result=HttpRequestUtils.requestoOkHttpPost(  server + "/jdm/s3/ipcs/bindIPC",object,CameraListActivity.this);
            // -1参数为空，-2设备不存在
            if ("0".equals(result)) {
                defaultHandler.sendEmptyMessage(10);
            }
        }
    }

    /**
     * 请求数据子线程,查询绑定的摄像头
     */
    class BindingCameraLoad implements Runnable {
        private long uid;
        private String code;
        private String bIpc;

        public BindingCameraLoad(String bIpc) {
            this.bIpc = bIpc;
            uid = DataCenterSharedPreferences.getInstance(mContext, DataCenterSharedPreferences.Constant.CONFIG)
                    .getLong(DataCenterSharedPreferences.Constant.LOGIN_APPID, 0);
            code = DataCenterSharedPreferences
                    .getInstance(mContext, DataCenterSharedPreferences.Constant.CONFIG).getString(DataCenterSharedPreferences.Constant.LOGIN_CODE, "");


        }

        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("id", bIpc);
            Log.e("wxb", "object---->" + uid + ":" + code + ":" + bIpc);
//            String result = HttpRequestUtils.requestHttpServer(
//                     server + "/jdm/service/ipcs/getIPC?v="
//                            + URLEncoder.encode(SecurityUtil.crypt(object.toJSONString(), DataCenterSharedPreferences.Constant.KEY_HTTP)),
//                    CameraListActivity.this, defaultHandler);


            String result=HttpRequestUtils.requestoOkHttpPost(  server + "/jdm/s3/ipcs/getIPC",object,CameraListActivity.this);
            // -1参数为空，-2设备不存在
            if ("-3".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
//                        Toast.makeText(CameraListActivity.this, getString(R.string.history_response_nodevice),
//                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if (result!=null && result.length() > 4) {

                //List<JSONObject> commands = new ArrayList<JSONObject>();
                JSONObject resultJson = null;
                try {
                    resultJson = JSON.parseObject(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.e("wxb", "result---->" + resultJson);
                Contact c = new Contact();
                c.contactId = resultJson.getString("iid");
                c.contactName = resultJson.getString("iname");
                c.contactPassword = resultJson.getString("ipassword");
                Message message = new Message();
                message.what = 12;
                message.obj = c;
                defaultHandler.sendMessage(message);
//                   Log.e("wxb", "result---->" + resultJson);

            }
        }
    }
}
