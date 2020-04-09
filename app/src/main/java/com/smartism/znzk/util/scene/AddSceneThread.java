package com.smartism.znzk.util.scene;

import android.os.Handler;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.device.ZhujiListFragment;
import com.smartism.znzk.adapter.recycleradapter.RecyclerItemBean;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.Tips;
import com.smartism.znzk.domain.scene.AddSceneInfo;
import com.smartism.znzk.domain.scene.DeviceTipsInfo;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;

import java.util.List;
import java.util.Locale;

/**
 * 抽出来做公共类，增加代码的重复量
 * Created by Administrator on 2017/6/5.
 */

public class AddSceneThread implements Runnable {
    private ActivityParentActivity context; //上下文
    private AddSceneInfo info; // 上传数据

    private String masterId;
    private String language;

    private Handler defaultHandler;

    public AddSceneThread(ActivityParentActivity context, AddSceneInfo info, Handler defaultHandler) {
        this.context = context;
        this.info = info;
        this.defaultHandler = defaultHandler;
//        masterId = context.dcsp.getString(DataCenterSharedPreferences.Constant.APP_MASTERID, "");
        //替换
        masterId = ZhujiListFragment.getMasterId() ;
        language = Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry();
    }

    @Override
    public void run() {
        {
            long timer = 0;
            if (info.timers != null && info.timers.length() > 3) {
                String[] subString = info.timers.split(":");
                timer = Long.parseLong(subString[0]) * 60 + Long.parseLong(subString[1]);
            }
            DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(context,
                    DataCenterSharedPreferences.Constant.CONFIG);
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject pJsonObject = new JSONObject();
            pJsonObject.put("n", info.name);
            pJsonObject.put("lang", language);
            pJsonObject.put("t", info.type);
            pJsonObject.put("m", masterId);

            if (info.type == 1) {
                //type 0:自定义场景  1:定时场景  2:联动场景
                pJsonObject.put("tc", info.cycleTime);
                pJsonObject.put("tt", timer);
            }
            //控制设备
            JSONArray pA = new JSONArray();
            for (RecyclerItemBean tip : info.devices) {
                DeviceInfo deviceInfo = ((DeviceTipsInfo) tip.getT()).getDeviceInfo();
                List<Tips> tipList = ((DeviceTipsInfo) tip.getT()).getTips();
                if (tipList != null && !tipList.isEmpty()) {
                    for (Tips t : tipList) {
                        JSONObject o = new JSONObject();
                        o.put("cd", deviceInfo.getId());
                        o.put("cc", t.getC());
                        if (deviceInfo.getCak().equals("control")) {
                            o.put("ct", 1);
                        } else {
                            o.put("ct", 0);
                        }
                        pA.add(o);
                    }
                } else {
                    if (deviceInfo.getTip() != null) {
                        JSONObject o = new JSONObject();
                        o.put("cd", deviceInfo.getId());
                        o.put("cc", deviceInfo.getTip());
                        if (deviceInfo.getCak().equals("control")) {
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
            for (RecyclerItemBean tip : info.triggerDeviceInfos) {
                DeviceInfo deviceInfo = ((DeviceTipsInfo) tip.getT()).getDeviceInfo();
                List<Tips> tipList = ((DeviceTipsInfo) tip.getT()).getTips();
                if (tipList != null && !tipList.isEmpty()) {
                    for (Tips t : tipList) {
                        JSONObject o = new JSONObject();
                        o.put("tdid", deviceInfo.getId());
                        o.put("tdc", t.getC());
                        pB.add(o);
                    }
                } else {
                    if (deviceInfo.getTip() != null) {
                        JSONObject o = new JSONObject();
                        o.put("tdid", deviceInfo.getId());
                        o.put("tdc", deviceInfo.getTip());
                        pB.add(o);
                    }
                }
            }
            pJsonObject.put("tl", pB);
            String result = HttpRequestUtils
                    .requestoOkHttpPost(server + "/jdm/s3/scenes/add", pJsonObject, context);

            // -1参数为空 -2校验失败 -3type为1时时间或周期为空 -4type为2时触发设备id或指令为空 -5未获取到数据
            // -6控制的设备id或指令为空 -7解析失败 -8名称为空 -9lang为空 -10masterid为空
            // -11类型只能为0,1,2中的一个 -12类型为空 -14被控制的设备必填
            if ("0".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        context.cancelInProgress();
                        Toast.makeText(context, context.getString(R.string.sence_add_success),
                                Toast.LENGTH_LONG).show();
                        context.finish();
                    }
                });
            } else if ("-1".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        context.cancelInProgress();
                        Toast.makeText(context, context.getString(R.string.register_tip_empty),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if ("-2".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        context.cancelInProgress();
                        Toast.makeText(context, context.getString(R.string.device_check_failure),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if ("-3".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        context.cancelInProgress();
                        Toast.makeText(context, context.getString(R.string.activity_editscene_type_1_empty),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if ("-4".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        context.cancelInProgress();
                        Toast.makeText(context, context.getString(R.string.activity_editscene_type_2_empty),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if ("-5".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        context.cancelInProgress();
                        Toast.makeText(context,
                                context.getString(R.string.activity_editscene_type_control_erro), Toast.LENGTH_LONG).show();
                    }
                });
            } else if ("-6".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        context.cancelInProgress();
                        Toast.makeText(context,
                                context.getString(R.string.activity_editscene_type_control_empty), Toast.LENGTH_LONG)
                                .show();
                    }
                });
            } else if ("-7".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        context.cancelInProgress();
                        Toast.makeText(context, context.getString(R.string.activity_editscene_paser_erro),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if ("-8".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        context.cancelInProgress();
                        Toast.makeText(context, context.getString(R.string.activity_editscene_name_empty),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if ("-9".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        context.cancelInProgress();
                        Toast.makeText(context, context.getString(R.string.activity_editscene_lang_empty),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if ("-10".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        context.cancelInProgress();
                        Toast.makeText(context, context.getString(R.string.activity_editscene_masterid_empty),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if ("-11".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        context.cancelInProgress();
                        Toast.makeText(context, context.getString(R.string.activity_editscene_type_only),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if ("-12".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        context.cancelInProgress();
                        Toast.makeText(context, context.getString(R.string.activity_editscene_type_empty),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if ("-13".equals(result)) {
                defaultHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        context.cancelInProgress();
                        Toast.makeText(context, context.getString(R.string.activity_editscene_isexist),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if ("-14".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        context.cancelInProgress();
                        Toast.makeText(context,
                                context.getString(R.string.activity_editscene_type_control_sure), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

    }
}
