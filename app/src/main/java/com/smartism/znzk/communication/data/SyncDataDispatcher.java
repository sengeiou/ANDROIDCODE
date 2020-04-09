package com.smartism.znzk.communication.data;

import android.app.ActivityManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.alert.AlertMessageActivity;
import com.smartism.znzk.activity.device.DeviceMainActivity;
import com.smartism.znzk.activity.device.ZhujiListFragment;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.communication.connector.SyncClientNettyConnector;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessage.CodeMenu;
import com.smartism.znzk.communication.protocol.SyncMessage.CommandMenu;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.MyNotificationInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.BadgerUtil;
import com.smartism.znzk.util.CompressionTools;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.ImageUtil;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.NotificationUtil;

import com.smartism.znzk.zhicheng.activities.ZCAlertMessageActivity;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SyncDataDispatcher {
    private static final String TAG = SyncDataDispatcher.class.getCanonicalName();
    private Context context;
    private static SyncDataDispatcher _instance;
    private DataCenterSharedPreferences dcsp;

    private SyncDataDispatcher(Context context) {
        this.context = context;
        dcsp = DataCenterSharedPreferences.getInstance(context, Constant.CONFIG);
    }

    public synchronized static SyncDataDispatcher getInstance(Context context) {
        if (_instance == null) {
            _instance = new SyncDataDispatcher(context);
        }
        return _instance;
    }

    protected void sendCustomBroadcast(Context context, String action, String objName, Object obj) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(objName, (Serializable) obj);
        context.sendBroadcast(intent);
    }

    protected void sendCustomBroadcast(Context context, String action, Map<String, Object> values) {
        Intent intent = new Intent();
        intent.setAction(action);
        if (values != null) {
            Iterator iterator = values.keySet().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                intent.putExtra(key, (Serializable) values.get(key));
            }
        }
        context.sendBroadcast(intent);
    }

    protected void sendCustomBroadcast(Context context, String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        context.sendBroadcast(intent);
    }

    /**
     * 调度方法 收到服务器发过来的消息
     *
     * @param msg
     */
    @SuppressWarnings("deprecation")
    public void dispatch(final SyncMessage msg) {
        if (msg == null)
            return;
        MainApplication.app.resendCount = 0;
        int command = msg.getCommand();
        if (command == CommandMenu.rp_login.value()) { // 登录成功 -
            SyncClientNettyConnector.getInstance().resetConnectionTryCount(); // 重置链接重连次数
            if (msg.getCode() == CodeMenu.rp_login_sessionout.value()) {
                sendCustomBroadcast(context, Actions.APP_KICKOFF_SESSIONFAILURE);
            } else if (msg.getCode() == CodeMenu.rp_login_outofday.value()) {
                sendCustomBroadcast(context, Actions.APP_KICKOFF_OUTOFDAY);
            } else if (msg.getCode() == CodeMenu.rp_login_errorappinfo.value()) {
                sendCustomBroadcast(context, Actions.APP_KICKOFF_SESSIONFAILURE);
            } else {
                Log.i(TAG, "dispatch: 发送登录成功的广播");
                context.sendBroadcast(new Intent(Actions.CONNECTION_SUCCESS)); // 连接成功
                try {//code更新，http和tcp都需要更新。
                    JSONObject jsonObject = JSONObject.parseObject(new String(msg.getSyncBytes(), "UTF-8"));
                    dcsp.putString(Constant.LOGIN_CODE, jsonObject.getString("code")).commit();
                } catch (UnsupportedEncodingException e) {
                    LogUtil.e(context, TAG, "不支持utf-8类型", e);
                } catch (Exception e) {
                    LogUtil.e(context, TAG, "解析tcp登录返回异常", e);
                }
                SyncMessageContainer.getInstance()
                        .produceSendMessage(new SyncMessage(CommandMenu.rq_refresh));
            }
        } else if (command == CommandMenu.rp_refresh.value() || command == CommandMenu.rp_szhuji.value()) { // 刷新并重新获取设备列表,搜索主机返回
            //获取到刷新列表结果，进行处理
            if (msg.getCode() == CodeMenu.zero.value()
                    || msg.getCode() == CodeMenu.rp_refresh_nocomdata.value()) {
                try {
                    List<ZhujiInfo> beforeZhujis = DatabaseOperator.getInstance().queryAllZhuJiInfos();//获取最新数据之前已存在的主机列表
                    JSONArray jsonArray = null;
                    if (msg.getCode() == CodeMenu.zero.value()) {
                        jsonArray = JSON
                                .parseArray(new String(CompressionTools.decompress(msg.getSyncBytes()), "UTF-8"));
                    } else {
                        jsonArray = JSON.parseArray(new String(msg.getSyncBytes(), "UTF-8"));
                    }
                    if (jsonArray != null && !jsonArray.isEmpty()) {
                        // DatabaseOperator.getInstance().getWritableDatabase().delete("DEVICE_STATUSINFO",
                        // null, null);
                        List<String> ids = new ArrayList<String>();
                        List<String> gids = new ArrayList<String>();
                        List<String> ZJids = new ArrayList<String>();
                        List<String> zjgids = new ArrayList<String>();
                        boolean haveShowZhuji = false;
                        int count = 0;//是否安防设备
                        //删除从机指令表(多指令)
                        DatabaseOperator.getInstance().getWritableDatabase().delete("DEVICE_COMMAND", "", null);
                        DatabaseOperator.getInstance().getWritableDatabase().delete("PERS", "", null);
                        for (int i = 0; i < jsonArray.size(); i++) {
                            // 主机
                            JSONObject zhuji = jsonArray.getJSONObject(i);
                            if (zhuji == null)
                                continue;
                            if("group".equals(zhuji.getString("ca"))){
                                ContentValues values = new ContentValues();
                                values.put("id", zhuji.get("id") != null ? zhuji.getLongValue("id") : 0);
                                zjgids.add(String.valueOf(zhuji.get("id") != null ? zhuji.getLongValue("id") : 0));
                                values.put("name", zhuji.get("gn") != null ? zhuji.getString("gn") : "群组名称");
                                values.put("logo", zhuji.get("gl") != null ? zhuji.getString("gl") : "");
                                Cursor cursor = DatabaseOperator.getInstance().getReadableDatabase().rawQuery(
                                        "select * from ZHUJI_GROUP_STATUSINFO where id = ? ",
                                        new String[]{String.valueOf(values.get("id"))});
                                if (cursor != null && cursor.getCount() > 0) {
                                    DatabaseOperator.getInstance().getWritableDatabase().update("ZHUJI_GROUP_STATUSINFO",
                                            values, "id = ?", new String[]{String.valueOf(values.get("id"))});

                                } else {
                                    DatabaseOperator.getInstance().getWritableDatabase().insert("ZHUJI_GROUP_STATUSINFO",
                                            "id", values);
                                }
                                cursor.close();

                                values = new ContentValues();
                                values.put("gid", zhuji.get("id") != null ? zhuji.getLongValue("id") : 0);
                                DatabaseOperator.getInstance().getWritableDatabase().delete("ZHUJI_GROUP_DEVICE_RELATIOIN", "gid = ?", new String[]{String.valueOf(values.get("gid"))});

                                JSONArray dgs = zhuji.getJSONArray("dids");
                                if (dgs == null)
                                    continue;
                                for (int k = 0; k < dgs.size(); k++) {
                                    JSONObject o = dgs.getJSONObject(k);
                                    if (o == null)
                                        continue;
                                    values.put("did", o.get("did") != null ? o.getLongValue("did") : 0);
                                    DatabaseOperator.getInstance().getWritableDatabase().insert("ZHUJI_GROUP_DEVICE_RELATIOIN",
                                            "id", values);
                                }
                                continue;
                            }
                            ZhujiInfo zhujiInfo = new ZhujiInfo();
                            zhujiInfo.setRolek(zhuji.getString("rolek"));
                            zhujiInfo.setId(zhuji.get("id") != null ? zhuji.getLongValue("id") : 0);
                            ZJids.add(String.valueOf(zhuji.get("id") != null ? zhuji.getLongValue("id") : 0));
                            zhujiInfo.setName(zhuji.get("dn") != null ? zhuji.getString("dn") : "主机");
                            zhujiInfo.setWhere(zhuji.get("dw") != null ? zhuji.getString("dw") : "");
                            zhujiInfo.setLogo(zhuji.get("dl") != null ? zhuji.getString("dl") : "");
                            zhujiInfo.setBrandName(zhuji.get("dbn") != null ? zhuji.getString("dbn") : "");
                            zhujiInfo.setGsm(zhuji.get("gsm") != null ? zhuji.getIntValue("gsm") : 0);
                            zhujiInfo.setOnline(zhuji.get("online") != null && zhuji.getIntValue("online") == 1);
                            zhujiInfo.setCid(zhuji.get("cid") != null ? zhuji.getLongValue("cid") : 0); // 厂商id
                            zhujiInfo.setMasterid(zhuji.get("dm") != null ? zhuji.getString("dm") : "");// masterid
                            zhujiInfo.setAdmin(zhuji.get("admin") != null && zhuji.getBooleanValue("admin"));// 是否是主机admin
                            zhujiInfo.setScene(zhuji.get("scene") != null ? zhuji.getString("scene") : "");// 主机当前场景
                            zhujiInfo.setUc(zhuji.get("uc") != null ? zhuji.getIntValue("uc") : 0); // 用户数
                            zhujiInfo.setUpdateStatus(zhuji.get("status") != null ? zhuji.getIntValue("status") : 0); // 0表示正常
                            zhujiInfo.setSimStatus(zhuji.get("ssim") != null ? zhuji.getIntValue("ssim") : -1); //-1状态未知
                            zhujiInfo.setPowerStatus(zhuji.get("sp") != null ? zhuji.getIntValue("sp") : -1);
                            zhujiInfo.setBatteryStatus(zhuji.get("sb") != null ? zhuji.getIntValue("sb") : -1);
                            zhujiInfo.setWanType(zhuji.get("sw") != null ? zhuji.getIntValue("sw") : -1);
                            zhujiInfo.setCa(zhuji.getString("ca"));
                            zhujiInfo.setCak(zhuji.getString("cak"));
                            zhujiInfo.setScenet(zhuji.getString("scenet"));
                            zhujiInfo.setBipc(zhuji.getLong("bipc"));
                            zhujiInfo.setDt(zhuji.getString("dt"));
                            zhujiInfo.setDtid(zhuji.getString("dtid"));
                            zhujiInfo.setAc(zhuji.getIntValue("ac"));
                            zhujiInfo.setEx(zhuji.get("ex") != null && zhuji.getBooleanValue("ex"));
                            zhujiInfo.setLa(zhuji.get("la") != null && zhuji.getBooleanValue("la"));

                            if (zhuji.containsKey("pers")) {
                                JSONArray pArray = JSON.parseArray(zhuji.getString("pers"));
                                if (pArray != null && pArray.size() > 0) {
                                    for (Object jsonObject : pArray) {
                                        JSONObject object = (JSONObject) jsonObject;
                                        ContentValues cv = new ContentValues();
                                        cv.put("zj_id", zhuji.get("id") != null ? zhuji.getLongValue("id") : 0);
                                        cv.put("k", object.getString("k") != null ? object.getString("k") : "");
                                        cv.put("v", object.getString("v") != null ? object.getString("v") : "");
                                        DatabaseOperator.getInstance().getWritableDatabase().insert("PERS", "", cv);
                                    }
                                }
                            }
                            if (zhuji.containsKey("ipc")) {
                                JSONArray carray = JSON.parseArray(zhuji.getString("ipc"));
                                if (carray != null && carray.size() > 0) {
                                    JSONObject object = carray.getJSONObject(0);
                                    zhujiInfo.getCameraInfo().setIpcid(object.getLong("ipcid"));
                                    zhujiInfo.getCameraInfo().setC(object.getString("c"));
                                    zhujiInfo.getCameraInfo().setN(object.getString("n"));
                                    zhujiInfo.getCameraInfo().setP(object.getString("p"));
                                    zhujiInfo.getCameraInfo().setId(object.getString("id"));

                                }
                            }
                            if (zhuji.containsKey("dcs")) {
                                JSONArray array = zhuji.getJSONArray("dcs");
                                if (array.size() == 0)
                                    continue;
                                for (Object object : array) {
                                    JSONObject o = (JSONObject) object;
                                    if (o.containsKey("dct") && o.getString("dct").equals("status_scall")) {
                                        zhujiInfo.setStatusCall(o.get("dc") != null ? o.getIntValue("dc") : 0);
                                    } else if (o.containsKey("dct") && o.getString("dct").equals("status_ssms")) {
                                        zhujiInfo.setStatusSMS(o.get("dc") != null ? o.getIntValue("dc") : 0);
                                    } else {
                                        ContentValues values = new ContentValues();
                                        values.put("m_id", o.get("mid") != null ? o.getLongValue("mid") : 0);
                                        values.put("d_id", zhujiInfo.getId());
                                        values.put("command", o.get("dc") != null ? o.getString("dc") : "0");
                                        values.put("ctime", o.get("dctime") != null ? o.getLongValue("dctime") : 0);
                                        values.put("ct", o.get("dct") != null ? o.getString("dct") : "0");
                                        DatabaseOperator.getInstance().getWritableDatabase().insert("DEVICE_COMMAND", "", values);
                                    }
                                }
                            }
//                            if (zhujiInfo.getMasterid().equals(dcsp.getString(Constant.APP_MASTERID, ""))) {
//                                haveShowZhuji = true;
//                            }
                            //替换
                            if(zhujiInfo.getMasterid().equals(ZhujiListFragment.getMasterId())){
                                haveShowZhuji = true ;
                            }
                            DatabaseOperator.getInstance().insertOrUpdateZhujiInfo(zhujiInfo);
                            // 从机
                            JSONArray congjis = zhuji.getJSONArray("dls");
                            if (congjis != null && !congjis.isEmpty()) {
                                for (int j = 0; j < congjis.size(); j++) {
                                    JSONObject object = congjis.getJSONObject(j);
                                    if (object == null)
                                        continue;
                                    ContentValues values = new ContentValues();
                                    values.put("id", object.get("id") != null ? object.getLongValue("id") : 0);
                                    ids.add(String.valueOf(object.get("id") != null ? object.getLongValue("id") : 0));
                                    values.put("zj_id", zhuji.get("id") != null ? zhuji.getLongValue("id") : 0);
                                    values.put("device_name", object.get("dn") != null ? object.getString("dn") : "设备名称");
                                    values.put("device_type", object.getString("dt"));
                                    values.put("device_tid", object.getLongValue("dtid"));
                                    values.put("device_where", object.getString("dw"));
                                    values.put("device_lastcommand", object.getString("dc"));
                                    values.put("device_lasttime", object.getLongValue("dctime"));
                                    //当有多个指令时会从dcs下来，需要更新lasttime，在显示的地方自有拼装显示格式
                                    if (object.get("dcs") != null) {
                                        JSONArray arrayC = object.getJSONArray("dcs");
                                        if (arrayC != null && arrayC.size() > 0) {
                                            ContentValues dcsValues = new ContentValues();
                                            for (int k = 0; k < arrayC.size(); k++) {
                                                dcsValues.put("d_id", object.get("id") != null ? object.getLongValue("id") : 0);
                                                dcsValues.put("command", arrayC.getJSONObject(k).get("dc") != null ? arrayC.getJSONObject(k).getString("dc") : "0");
                                                dcsValues.put("ctime", arrayC.getJSONObject(k).get("dctime") != null ? arrayC.getJSONObject(k).getLongValue("dctime") : 0);
                                                dcsValues.put("ct", arrayC.getJSONObject(k).get("dct") != null ? arrayC.getJSONObject(k).getString("dct") : "0");
                                                dcsValues.put("m_id", arrayC.getJSONObject(k).get("mid") != null ? arrayC.getJSONObject(k).getLongValue("mid") : 0);
                                                dcsValues.put("dcg", arrayC.getJSONObject(k).get("dcg") != null ? arrayC.getJSONObject(k).getLongValue("dcg") : 0);
                                                DatabaseOperator.getInstance().getWritableDatabase().insert("DEVICE_COMMAND", "", dcsValues);

                                                if (values.getAsLong("device_lasttime") < dcsValues.getAsLong("ctime")) {
                                                    values.put("device_lasttime", dcsValues.getAsLong("ctime"));
                                                }
                                            }
                                        }
                                    }
                                    values.put("app_acceptmessage", object.getIntValue("aam"));
                                    values.put("device_controltype", object.getString("dct"));
                                    values.put("appdownload", object.getString("dad"));
                                    values.put("apppackage", object.getString("dap"));
                                    values.put("device_logo",
                                            object.get("dl") != null ? object.getString("dl") : "default_device_logo.png");
                                    values.put("re_1", object.getString("re_1"));
                                    values.put("sort", object.getIntValue("sort"));
                                    values.put("status", object.getIntValue("status")); // 0表示显示指令
                                    // 1显示正常
                                    values.put("nr", object.getIntValue("nr")); // 未读消息数
                                    values.put("nt", object.getIntValue("nt")); // 是否是有线防区 1是的
                                    values.put("ca", object.getString("ca")); // 分类标识
                                    values.put("cak", object.getString("cak")); // 分类广义标识
                                    if (object.getString("cak").equals(DeviceInfo.CakMenu.security.value())) {
                                        count++;
                                    }

                                    values.put("dr", object.getIntValue("dr")); // 当前触发的第几键
                                    values.put("ipc", object.getString("ipc"));
                                    values.put("device_slavedId", object.getString("ds"));
                                    values.put("bipc", object.getString("bipc"));
                                    values.put("lowb", object.get("lowb") != null ? (object.getBooleanValue("lowb") ? 1 : 0) : 0);
                                    values.put("eid", object.get("eids") != null ? object.getString("eids") : "");
                                    values.put("fa", object.get("fa") != null ? (object.getBooleanValue("fa") ? 1 : 0) : 0);
                                    values.put("mc", object.getString("mc"));
                                    Cursor cursor = DatabaseOperator.getInstance().getReadableDatabase().rawQuery(
                                            "select * from DEVICE_STATUSINFO where id = ? ",
                                            new String[]{String.valueOf(values.get("id"))});
                                    if (cursor != null && cursor.getCount() > 0) {
                                        DatabaseOperator.getInstance().getWritableDatabase().update("DEVICE_STATUSINFO",
                                                values, "id = ?", new String[]{String.valueOf(values.get("id"))});

                                    } else {
                                        DatabaseOperator.getInstance().getWritableDatabase().insert("DEVICE_STATUSINFO",
                                                "id", values);
                                    }
                                    cursor.close();
                                }
                            }
                            // 从机
                            JSONArray ads = zhuji.getJSONArray("dads");
                            DatabaseOperator.getInstance().getWritableDatabase().delete("ZHUJI_ADSINFO", "zj_id = ?", new String[]{String.valueOf(zhuji.getLongValue("id"))});
                            if (ads != null && !ads.isEmpty()) {
                                for (int j = 0; j < ads.size(); j++) {
                                    JSONObject object = ads.getJSONObject(j);
                                    if (object == null)
                                        continue;
                                    ContentValues values = new ContentValues();
                                    values.put("zj_id", zhuji.get("id") != null ? zhuji.getLongValue("id") : 0);
                                    values.put("name", object.getString("name"));
                                    values.put("logo", object.getString("logo"));
                                    values.put("remark", object.getString("remark"));
                                    values.put("ca", object.getString("ca")); // 分类标识
                                    values.put("ut", object.getIntValue("ut")); //url 点击操作 0点击本app跳转到webview
                                    values.put("url", object.getString("url")); //url
                                    DatabaseOperator.getInstance().getWritableDatabase().insert("ZHUJI_ADSINFO",
                                                "id", values);
                                }
                            }
                            // 设备设置属性
                            JSONArray dsets = zhuji.getJSONArray("dset");
                            DatabaseOperator.getInstance().getWritableDatabase().delete("ZHUJI_SETINFO", "zj_id = ?", new String[]{String.valueOf(zhuji.getLongValue("id"))});
                            if (dsets != null && !dsets.isEmpty()) {
                                for (int j = 0; j < dsets.size(); j++) {
                                    JSONObject object = dsets.getJSONObject(j);
                                    if (object == null)
                                        continue;
                                    ContentValues values = new ContentValues();
                                    values.put("zj_id", zhuji.get("id") != null ? zhuji.getLongValue("id") : 0);
                                    values.put("k", object.getString("k"));
                                    values.put("v", object.getString("v"));
                                    DatabaseOperator.getInstance().getWritableDatabase().insert("ZHUJI_SETINFO",
                                            "id", values);
                                }
                            }
//                            if (congjis != null && congjis.size() > 0) {
//                                sendCustomBroadcast(context, Actions.ACCETP_MAIN_SHOW_SCENCE, "is_show", (count > 0) ? true : false);
//                            }
                            // 群组
                            JSONArray groups = zhuji.getJSONArray("dgs");
                            if (groups != null && !groups.isEmpty()) {
                                for (int j = 0; j < groups.size(); j++) {
                                    JSONObject object = groups.getJSONObject(j);
                                    if (object == null)
                                        continue;
                                    ContentValues values = new ContentValues();
                                    values.put("id", object.get("id") != null ? object.getLongValue("id") : 0);
                                    gids.add(String.valueOf(object.get("id") != null ? object.getLongValue("id") : 0));
                                    values.put("zj_id", zhuji.get("id") != null ? zhuji.getLongValue("id") : 0);
                                    values.put("name", object.get("gn") != null ? object.getString("gn") : "群组名称");
                                    values.put("logo", object.get("gl") != null ? object.getString("gl") : "");
                                    values.put("bipc", object.get("bipc") != null ? object.getString("bipc") : "");
                                    Cursor cursor = DatabaseOperator.getInstance().getReadableDatabase().rawQuery(
                                            "select * from GROUP_STATUSINFO where id = ? ",
                                            new String[]{String.valueOf(values.get("id"))});
                                    if (cursor != null && cursor.getCount() > 0) {
                                        DatabaseOperator.getInstance().getWritableDatabase().update("GROUP_STATUSINFO",
                                                values, "id = ?", new String[]{String.valueOf(values.get("id"))});

                                    } else {
                                        DatabaseOperator.getInstance().getWritableDatabase().insert("GROUP_STATUSINFO",
                                                "id", values);
                                    }
                                    cursor.close();

                                    values = new ContentValues();
                                    values.put("gid", object.get("id") != null ? object.getLongValue("id") : 0);
                                    DatabaseOperator.getInstance().getWritableDatabase().delete("GROUP_DEVICE_RELATIOIN", "gid = ?", new String[]{String.valueOf(values.get("gid"))});

                                    JSONArray dgs = object.getJSONArray("dids");
                                    if (dgs == null)
                                        continue;
                                    for (int k = 0; k < dgs.size(); k++) {
                                        JSONObject o = dgs.getJSONObject(k);
                                        if (o == null)
                                            continue;
                                        values.put("did", o.get("did") != null ? o.getLongValue("did") : 0);
                                        DatabaseOperator.getInstance().getWritableDatabase().insert("GROUP_DEVICE_RELATIOIN",
                                                "id", values);
                                    }
                                }
                            }
                        }


                        if (ids != null && ids.size() < 160) {//size 大于100会导致sql太长。后续再考虑怎么修改，不删除其实问题也不大
                            StringBuilder builder = new StringBuilder("id not in (");
                            for (int i = 0; i < ids.size(); i++) {
                                builder.append("?");
                                if (i < ids.size() - 1) {
                                    builder.append(",");
                                }
                            }
                            builder.append(")");
                            DatabaseOperator.getInstance().getWritableDatabase().delete("DEVICE_STATUSINFO",
                                    builder.toString(), ids.toArray(new String[ids.size()]));
                        }
                        if (gids != null) {
                            StringBuilder builder = new StringBuilder("id not in (");
                            for (int i = 0; i < gids.size(); i++) {
                                builder.append("?");
                                if (i < gids.size() - 1) {
                                    builder.append(",");
                                }
                            }
                            builder.append(")");
                            DatabaseOperator.getInstance().getWritableDatabase().delete("GROUP_STATUSINFO",
                                    builder.toString(), gids.toArray(new String[gids.size()]));
                            builder = new StringBuilder("gid not in (");
                            for (int i = 0; i < gids.size(); i++) {
                                builder.append("?");
                                if (i < gids.size() - 1) {
                                    builder.append(",");
                                }
                            }
                            builder.append(")");
                            DatabaseOperator.getInstance().getWritableDatabase().delete("GROUP_DEVICE_RELATIOIN",
                                    builder.toString(), gids.toArray(new String[gids.size()]));
                        }
                        if (zjgids != null) {
                            StringBuilder builder = new StringBuilder("id not in (");
                            for (int i = 0; i < zjgids.size(); i++) {
                                builder.append("?");
                                if (i < zjgids.size() - 1) {
                                    builder.append(",");
                                }
                            }
                            builder.append(")");
                            DatabaseOperator.getInstance().getWritableDatabase().delete("ZHUJI_GROUP_STATUSINFO",
                                    builder.toString(), zjgids.toArray(new String[zjgids.size()]));
                            builder = new StringBuilder("gid not in (");
                            for (int i = 0; i < zjgids.size(); i++) {
                                builder.append("?");
                                if (i < zjgids.size() - 1) {
                                    builder.append(",");
                                }
                            }
                            builder.append(")");
                            DatabaseOperator.getInstance().getWritableDatabase().delete("ZHUJI_GROUP_DEVICE_RELATIOIN",
                                    builder.toString(), zjgids.toArray(new String[zjgids.size()]));
                        }
                        if (ZJids != null) {
                            StringBuilder builder = new StringBuilder("id not in ("); //delete from ZHUJI_STATUSINFO where id not in ("130353806348255230") 这条sql在amitshekhar的web页面中执行，会出现很多为null的数据。可能也是导致列表有null数据的元凶。
                            for (int i = 0; i < ZJids.size(); i++) {
                                builder.append("?");
                                if (i < ZJids.size() - 1) {
                                    builder.append(",");
                                }
                            }
                            builder.append(")");
                            DatabaseOperator.getInstance().getWritableDatabase().delete("ZHUJI_STATUSINFO",
                                    builder.toString(), ZJids.toArray(new String[ZJids.size()]));
                        }
//                        if (!haveShowZhuji) {  //注释日期 2018年05月02日 王建 原因为有主机列表之后 这里应该是多余代码了 待验证
//                            if (!StringUtils.isEmpty(dcsp.getString(Constant.APP_MASTERID, ""))) {//app有显示过主机，刷新获取数据时显示没有主机，表示此主机被删除了
//                                showDeivceListView();
//                            }
//                            List<ZhujiInfo> zhujiInfos = DatabaseOperator.getInstance().queryAllZhuJiInfos();
//                            if (!zhujiInfos.isEmpty()) {
//                                dcsp.putString(Constant.APP_MASTERID, zhujiInfos.get(0).getMasterid()).commit();
//                            }
//                        }
                    }
                    BadgerUtil.updateBadger(context);
                    sendCustomBroadcast(context, Actions.REFRESH_DEVICES_LIST);
                    if (command == CommandMenu.rp_szhuji.value()) {
                        List<ZhujiInfo> afterZhujis = DatabaseOperator.getInstance().queryAllZhuJiInfos();//已获取最新主机列表数
                        List<ZhujiInfo> tempList = new ArrayList<>();

                        for (int i = 0; i < afterZhujis.size(); i++) {
                            ZhujiInfo info = afterZhujis.get(i);
                            inner:
                            for (int j = 0; j < beforeZhujis.size(); j++) {
                                if (info.getId() == beforeZhujis.get(j).getId()) {
                                    tempList.add(info);
                                    break inner;
                                }
                            }
                        }
                        afterZhujis.removeAll(tempList);
                        String id ="";
                        if (afterZhujis != null && afterZhujis.get(0) != null) {
                            id = afterZhujis.get(0).getMasterid();
                        }
//                        long id = zhuji.get("id") != null ? zhuji.getLongValue("id") : 0;
//                        dcsp.putString(Constant.APP_NEW_ADDMASTERID, id + "").commit();
//                        JSONArray congjis = zhuji.getJSONArray("dls");
                        Log.d("new_add_zhuji:", id + "");
                        Map<String, Object> map = new HashMap<>();
                        map.put("data", 0);
                        map.put("masterId", id);
//                        sendCustomBroadcast(context, Actions.SEARCH_ZHUJI_RESPONSE, "data", 0);
                        sendCustomBroadcast(context, Actions.SEARCH_ZHUJI_RESPONSE, map);
                    }
//                    else if (Actions.VersionType.CHANNEL_ZHILIDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {//添加主机是否删除设备
//                        if (dcsp.getBoolean(IS_REPEAT_LOGIN, false) && CollectionsUtils.isEmpty(beforeZhujis)) {//退出时会清空主机数据所以登录成功时IS_REPEAT_LOGIN为true
//                            return;
//                        }
//                        dcsp.putBoolean(IS_REPEAT_LOGIN, false).commit();
//                        List<ZhujiInfo> afterZhujis = DatabaseOperator.getInstance().queryAllZhuJiInfos();//已获取最新主机列表数
//                        List<ZhujiInfo> tempList = new ArrayList<>();
//                        if (!CollectionsUtils.isEmpty(afterZhujis) && !CollectionsUtils.isEmpty(beforeZhujis) && afterZhujis.size() == beforeZhujis.size())
//                            return;
//                        for (int i = 0; i < afterZhujis.size(); i++) {
//                            ZhujiInfo info = afterZhujis.get(i);
//                            inner:
//                            for (int j = 0; j < beforeZhujis.size(); j++) {
//                                if (info.getId() == beforeZhujis.get(j).getId()) {
//                                    tempList.add(info);
//                                    break inner;
//                                }
//                            }
//                        }
//                        afterZhujis.removeAll(tempList);
//                        long id = 0;
//                        if (afterZhujis != null && afterZhujis.get(0) != null && afterZhujis.get(0).isAdmin()) {
//                            id = afterZhujis.get(0).getId();
//
//                        }
//                        if (id == 0)
//                            return;
//                        Intent intent = new Intent(Actions.ADD_NEW_ZHUJI);
//                        intent.putExtra("masterId", String.valueOf(id));
//                        Log.d("new_add_zhuji:", id + "zld");
//                        context.sendBroadcast(intent);
//                    }
                } catch (Exception e) {
                    Log.e(TAG, "首次获取设备信息异常!!!!!", e);
                }
            } else if (command == CommandMenu.rp_szhuji.value()
                    && msg.getCode() == CodeMenu.rp_szhuji_needauthorization.value()) {// 有主机需要授权
                Intent intent = new Intent();
                intent.setAction(Actions.SEARCH_ZHUJI_RESPONSE);
                intent.putExtra("data", 1);
                try {
                    intent.putExtra("value", new String(msg.getSyncBytes(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "不支持UTF-8类型", e);
                }
                context.sendBroadcast(intent);
            } else { // 无数据或者无主机都要清空数据库刷新设备列表
                // if(command == SyncMessage.CommandMenu.rp_refresh.getValue()
                // && msg.getDeviceid() ==
                // SyncMessage.CodeMenu.rp_refresh_nodata.getValue()){//无数据
                if (command != CommandMenu.rp_szhuji.value()) {
                    try {
                        //判断当前数据库是不是无数据,是无数据则发送刷新广播让首页可能的刷新消失，有数据则走逻辑
                        List<ZhujiInfo> zhujiInfos = DatabaseOperator.getInstance().queryAllZhuJiInfos();
                        if (zhujiInfos != null && !zhujiInfos.isEmpty()) {
                            DatabaseOperator.getInstance().clearAllDbData();
                            //服务器无主机需要更新服务器地址
                            if (!LogUtil.isDebug) {
                                if (Actions.VersionType.CHANNEL_AIERFUDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                                        || Actions.VersionType.CHANNEL_UHOME.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {

                                    if (!"app.efud110.com:7778".equals(dcsp.getString(Constant.SYNC_DATA_SERVERS, "null"))) {
                                        new Handler(context.getMainLooper()).postDelayed(new Runnable() { //延迟两秒操作
                                            @Override
                                            public void run() {
                                                dcsp.putString(Constant.SYNC_DATA_SERVERS, "app.efud110.com:7778")
                                                        .putString(Constant.HTTP_DATA_SERVERS, "http://app.efud110.com:9999")
                                                        .commit();
                                                sendCustomBroadcast(context, Actions.APP_RECONNECTION);
                                            }
                                        }, 2000);
                                    }

                                } else if (Actions.VersionType.CHANNEL_WANGDUODUO.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {

                                    if (!"server.hengjukj.cn:7778".equals(dcsp.getString(Constant.SYNC_DATA_SERVERS, "null"))) {
                                        new Handler(context.getMainLooper()).postDelayed(new Runnable() { //延迟两秒操作
                                            @Override
                                            public void run() {
                                                dcsp.putString(Constant.SYNC_DATA_SERVERS, "server.hengjukj.cn:7778")
                                                        .putString(Constant.HTTP_DATA_SERVERS, "http://server.hengjukj.cn:9999")
                                                        .commit();
                                                sendCustomBroadcast(context, Actions.APP_RECONNECTION);
                                            }
                                        }, 2000);
                                    }

                                } else {

                                    if (!"jdm.smart-ism.com:7778".equals(dcsp.getString(Constant.SYNC_DATA_SERVERS, "null"))) {
                                        new Handler(context.getMainLooper()).postDelayed(new Runnable() { //延迟两秒操作
                                            @Override
                                            public void run() {
                                                dcsp.putString(Constant.SYNC_DATA_SERVERS, "jdm.smart-ism.com:7778")
                                                        .putString(Constant.HTTP_DATA_SERVERS, "https://jdm.smart-ism.com")
                                                        .commit();
                                                sendCustomBroadcast(context, Actions.APP_RECONNECTION);
                                            }
                                        }, 2000);
                                    }
                                }
                            }
                            showDeivceListView();
                        } else {
//                            sendCustomBroadcast(context, Actions.REFRESH_DEVICES_LIST);此处执行的话被其他终端删除主机则不会推送
                        }
                        sendCustomBroadcast(context, Actions.REFRESH_DEVICES_LIST);
                        //上面这个方法会是devicesListActivity触发OnResume方法,所以无需再发送刷新广播
//						context.sendStickyBroadcast(new Intent(Actions.REFRESH_DEVICES_LIST)); //必须使用粘性广播,不然DevicesListActivity收不到
                    } catch (Exception e) {
                        Log.e(TAG, "服务器返回无数据，清空本地数据库缓存");
                    }
                    BadgerUtil.updateBadger(context);
                }
            }
        } else if (command == CommandMenu.rp_pdByHand.value()) { // 手动配对返回
            Intent intent = new Intent(Actions.PEIDUI_ACTIONS);
            try {
                if (msg.getSyncBytes() != null && msg.getSyncBytes().length > 0) {
                    intent.putExtra("data_info", new String(msg.getSyncBytes(), "UTF-8"));
                }
                intent.putExtra("code", msg.getCode());
            } catch (Exception e) {
                Log.e(TAG, "不支持UTF-8,或者json解析错误");
            }
            context.sendBroadcast(intent);
        } else if (command == CommandMenu.rp_pdByHandE.value()) { // 退出手动配对返回
            // 不处理关系不大
            // sendCustomBroadcast(context, Actions.PEIDUI_ACTIONS, "code",
            // msg.getCode()==0?10001:msg.getCode()); //code为0有重叠
        } else if (command == CommandMenu.rp_pdByHand_onlyControl.value()) { // 仅控制类型确认
            if (msg.getCode() == CodeMenu.zero.value()) { // 配对完成
                // 重新获取设备列表
                SyncMessageContainer.getInstance()
                        .produceSendMessage(new SyncMessage(CommandMenu.rq_refresh));
            } else if (msg.getCode() == CodeMenu.rp_pdByHand_onlyControl_timeout.value()) { // 配对超时
                sendCustomBroadcast(context, Actions.PEIDUI_FAILED_TIMEOUT);
            } else if (msg.getCode() == CodeMenu.rp_pdByHand_onlyControl_sunottozj.value()) { // 退出配对模式失败
                sendCustomBroadcast(context, Actions.PEIDUI_MODEN_EXIT);
            }
        } else if (command == CommandMenu.rp_pdByAuto.value()) { // 自动配对返回
            Map<String, Object> map = new HashMap<>();
            map.put("code", msg.getCode());
            if (msg.getCode() == CodeMenu.rp_pdByAuto_addone.value()) {//添加一个上来了
                try {
                    map.put("msg", new String(msg.getSyncBytes(), "UTF-8"));//有需要两次回调的添加此参数
                } catch (UnsupportedEncodingException e) {
                    LogUtil.e(context, TAG, "此手机不支持UTF-8编码格式");
                }
            }
            sendCustomBroadcast(context, Actions.PEIDUI_ACTIONS, map);
        } else if (command == CommandMenu.rp_control.value() || command == CommandMenu.rp_controlRemind.value()) { // 控制返回
            Log.e("aaa", "返回了数据");
            if (msg.getCode() == CodeMenu.rp_control_commandnotexist.value()
                    || msg.getCode() == CodeMenu.rp_control_devicenotexist.value()
                    || msg.getCode() == CodeMenu.rp_control_deviceoffline.value()) {
                try {
                    sendCustomBroadcast(context, Actions.SHOW_SERVER_MESSAGE, "message",
                            new String(msg.getSyncBytes(), "UTF-8"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if (msg.getCode() == CodeMenu.rp_control_needconfirm.value()
                    || msg.getCode() == CodeMenu.rp_control_verifyerror.value()) {
                Intent intent = new Intent(Actions.CONTROL_BACK_MESSAGE);
                try {
                    if (msg.getSyncBytes() != null && msg.getSyncBytes().length > 0) {
                        intent.putExtra("data_info", new String(msg.getSyncBytes(), "UTF-8"));
                    }
                    intent.putExtra("code", msg.getCode());
                    intent.putExtra("did", msg.getDeviceid());
                } catch (Exception e) {
                    Log.e(TAG, "不支持UTF-8,或者json解析错误");
                }
                context.sendBroadcast(intent);
            } else {
                try {
                    JSONObject object = JSON.parseObject(new String(msg.getSyncBytes(), "UTF-8"));
                    ContentValues values = new ContentValues();
                    values.put("app_acceptmessage", object.get("a") != null ? object.getIntValue("a") : 0);
                    DatabaseOperator.getInstance().getWritableDatabase().update("DEVICE_STATUSINFO", values, "id = ?",
                            new String[]{String.valueOf(msg.getDeviceid())});
                    sendCustomBroadcast(context, Actions.ACCETP_ONEDEVICE_MESSAGE, "device_id",
                            String.valueOf(msg.getDeviceid()));
                } catch (Exception e) {
                    Log.e(TAG, "控制返回异常", e);
                }
            }
        } else if (command == CommandMenu.rp_keepalive.value()) { // 心跳包返回

        } else if (command == CommandMenu.rp_checkpudate.value()) { // 升级检查返回
            Intent intent = new Intent(Actions.ZHUJI_CHECKUPDATE);
            try {
                if (msg.getSyncBytes() != null && msg.getSyncBytes().length > 0) {
                    intent.putExtra("data_info", new String(msg.getSyncBytes(), "UTF-8"));
                }
                intent.putExtra("data", msg.getCode());
            } catch (Exception e) {
                Log.e(TAG, "不支持UTF-8,或者json解析错误");
            }
            context.sendBroadcast(intent);
        } else if (command == CommandMenu.rp_pudate.value()) { // 升级
            Intent intent = new Intent(Actions.ZHUJI_UPDATE);
            try {
                if (msg.getSyncBytes() != null && msg.getSyncBytes().length > 0) {
                    JSONObject jsonObject = JSON.parseObject(new String(msg.getSyncBytes(), "UTF-8"));
                    intent.putExtra("max", jsonObject.getIntValue("t"));
                    intent.putExtra("progress", jsonObject.getIntValue("p"));
                }
                intent.putExtra("data", msg.getCode());
            } catch (Exception e) {
                Log.e(TAG, "不支持UTF-8,或者json解析错误");
            }
            context.sendBroadcast(intent);
        } else if (command == CommandMenu.rp_pfactory.value()) { // 工厂模式生效
            sendCustomBroadcast(context, Actions.ZHUJI_FACTORY, "code", msg.getCode());
        } else if (command == CommandMenu.rp_developertc.value()) { // 开发者透传模式反馈
            if (msg.getCode() == 2) {
                try {
                    JSONObject jsonObject = JSON.parseObject(new String(msg.getSyncBytes(), "UTF-8"));
                    sendCustomBroadcast(context, Actions.DEVE_TC, "c", jsonObject.getString("c"));
                } catch (Exception e) {
                    Log.e(TAG, "不支持UTF-8,或者json解析错误");
                }
            } else {
                sendCustomBroadcast(context, Actions.DEVE_TC, "code", msg.getCode());
            }
        } else if (command == CommandMenu.rp_pStudy.value() || command == CommandMenu.rp_pStudyE.value()) { // 学习模式
            int c = 0, s = 0;
            if (msg.getSyncBytes() != null && msg.getSyncBytes().length > 0) {
                try {
                    JSONObject o = JSON.parseObject(new String(msg.getSyncBytes(), "UTF-8"));
                    c = o.get("c") != null ? o.getIntValue("c") : 0;
                    s = o.get("s") != null ? o.getIntValue("s") : 0;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            Intent intent = new Intent();
            intent.setAction(Actions.STUDY_ACTIONS);
            intent.putExtra("command", command);
            intent.putExtra("code", msg.getCode());
            intent.putExtra("c", c);
            intent.putExtra("s", s);
            context.sendBroadcast(intent);
        }else if (command == CommandMenu.rp_controlConfirm.value()) { // 控制确认返回

        }
        // else if(command == -1){ //消息返回,直接提示类型
        // try {
        // sendCustomBroadcast(context,
        // Actions.SHOW_SERVER_MESSAGE,"message",new
        // String(msg.getSyncBytes(),"UTF-8"));
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        // }
        // else if(command == 2){ //消息控制类返回
        // try {
        // JSONObject object = JSON.parseObject(new
        // String(msg.getSyncBytes(),"UTF-8"));
        // ContentValues values = new ContentValues();
        // values.put("app_acceptmessage",
        // object.get("a")!=null?object.getBoolean("a"):true);
        // DatabaseOperator.getInstance().getWritableDatabase().update("DEVICE_STATUSINFO",
        // values, "id = ?", new String[]{String.valueOf(msg.getDeviceid())});
        // sendCustomBroadcast(context,
        // Actions.ACCETP_ONEDEVICE_MESSAGE,"device_id",String.valueOf(msg.getDeviceid()));
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        // }else if(command == 3){ //消息控制类返回
        // try {
        // dcsp.putString(Constant.APP_MASTERID,
        // String.valueOf(msg.getDeviceid())).commit();
        // sendCustomBroadcast(context,
        // Actions.SHOW_SERVER_MESSAGE,"message",new
        // String(msg.getSyncBytes(),"UTF-8"));
        // sendCustomBroadcast(context, Actions.REFRESH_DEVICES_LIST);
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        // }
        // ac指令 server主动推送到app端的指令开始
        else if (command == CommandMenu.ac_newMessage.value()) { // 正常状态上报
            // 接收到
            MainApplication.app.setNeedRefreshMcenter(true);//收到了推送，有新消息，需要刷新
            try {
                boolean sforti = false;
                DeviceInfo dInfo = null;
                String message = "";
                JSONObject object = JSON.parseObject(new String(msg.getSyncBytes(), "UTF-8"));
                if (object != null) {
                    if (object.containsKey("index") && object.containsKey("al") && object.getIntValue("al") >= 2){
                        if (!DatabaseOperator.getInstance().isAcceptNotificationCommands(msg.getDeviceid(),object.getInteger("index"))){
                            ContentValues cv = new ContentValues();
                            cv.put("d_id", msg.getDeviceid());
                            cv.put("cindex", object.getIntValue("index"));
                            cv.put("command", object.getString("deviceCommand"));
                            cv.put("ctime", object.getLongValue("deviceCommandTime"));
                            DatabaseOperator.getInstance().getWritableDatabase().insert("USER_NOTIFICATION", "", cv);
                            DatabaseOperator.getInstance().getWritableDatabase().delete("USER_NOTIFICATION", "ctime < ?", new String[]{String.valueOf(object.getLongValue("deviceCommandTime")-600000)});//清空10分钟之前的记录

                            JSONObject back = new JSONObject();
                            back.put("index",object.getIntValue("index"));

                            SyncMessage messageBack = new SyncMessage();
                            messageBack.setCommand(SyncMessage.CommandMenu.su_newMessage.value());
                            messageBack.setDeviceid(msg.getDeviceid());
                            messageBack.setSyncBytes(back.toJSONString().getBytes("UTF-8"));
                            SyncMessageContainer.getInstance().produceSendMessage(messageBack);
                        }else{
                            return;
                        }
                    }
                    if (object.get("t") == null || object.getIntValue("t") == 1) { //设备状态和数据
                        sforti = object.getBooleanValue("sforti");
                        ContentValues values = new ContentValues();
                        if(Actions.VersionType.CHANNEL_RUNLONG.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
                            //润珑屏蔽deviceCommand,66,61,46
                            if(!object.getString("dt").equals("66")&&!object.getString("dt").equals("61")&&!object.getString("dt").equals("46")){
                                values.put("device_lastcommand", object.getString("deviceCommand"));
                            }
                        }else if(!object.getString("dt").equals("39")
                                &&!object.getString("dt").equals("126")&&!object.getString("dt").equals("138")){
                               values.put("device_lastcommand", object.getString("deviceCommand"));
                        }
                        message = values.getAsString("device_lastcommand");
                        values.put("device_lasttime", object.getLongValue("deviceCommandTime"));
                        values.put("status", 0); // 0表示显示指令 1显示正常
                        values.put("device_dtype", object.getString("dt") != null ? object.getString("dt") : "");//指令所属种类
                        values.put("dr", object.getIntValue("sort")); // 第几键的指令
                        if (object.get("notread") != null) {
                            values.put("nr", object.getIntValue("notread")); // 未读消息数,有数据时才更新
                        }
                        if ((!"95".equals(object.getString("dt"))
                                && !"alarm_dnc".equals(object.getString("dt"))) || 0 == object.getIntValue("dt")) { //需要更新到lastcommand里面,lastcommnd会在设备列表显示 本来是要屏蔽的，但是遥控器不同，又打开了，这里只判断dt为0时保存。
                            DatabaseOperator.getInstance().getWritableDatabase().update("DEVICE_STATUSINFO", values, "id = ?",
                                    new String[]{String.valueOf(msg.getDeviceid())});
                        }
                        dInfo = DatabaseOperator.getInstance().queryDeviceInfo(msg.getDeviceid());
                        if (object.get("al") != null) {
                            dInfo.setAcceptMessage(object.getIntValue("al"));
                        }
                        if ("tzc".equals(dInfo.getCa())) {
                            //广播收到了一个新体重数据消息
                            dInfo.setMid(object.getLongValue("mid"));
                            dInfo.setVid(object.getLongValue("vid"));
                            dInfo.setDtype(object.getString("dt"));
                            sendCustomBroadcast(context, Actions.ACCETP_ONEWEIGHT_MESSAGE, "device_id", dInfo);
                        }
//						else if (DeviceInfo.CaMenu.xueyaji.equals(dInfo.getCa())){
////							mid 成员ID
//							dInfo.setDtype(object.getIntValue("dt"));
//							dInfo.setVid(object.getLongValue("vid"));
//							dInfo.setMid(object.getLongValue("mid"));
//							sendCustomBroadcast(context,Actions.ACCETP_ONEXYJ_MESSAGE,"device_id",dInfo);
//						}
//						else if (dInfo.getCak().contains(DeviceInfo.CakMenu.health.value()) || dInfo.getCak().contains(DeviceInfo.CakMenu.detection.value())||
//								dInfo.getCa().equals(DeviceInfo.CaMenu.quwenqi.value())) {
                        //不增加限制条件，所有指令都需要放到DEVICE_COMMAND表中，但是只保留一个最新的ct=0的数据，也就是指令数据而非数据指令
                        values = new ContentValues();
                        values.put("d_id", dInfo.getId());
                        if (!"0".equals(object.getString("dt"))) {
                            if (!StringUtils.isEmpty(object.getString("dtv"))) {
                                values.put("command", object.getString("dtv"));
                            } else {
                                values.put("command", object.getString("deviceCommand"));
                            }
                        } else {
                            values.put("command", object.getString("deviceCommand"));
                        }
                        values.put("m_id", object.getLongValue("mid"));
                        values.put("dcg", object.getLongValue("dg"));

                        values.put("ctime", object.getLongValue("deviceCommandTime"));
                        values.put("ct", object.getString("dt"));
                        if (object.containsKey("sort") && object.getString("sort").equals("2") && dInfo.getCa().equals("qwq")) {
                            values.put("command", object.getLongValue("deviceCommandTime"));
                            dInfo.setDtype("49");
                            values.put("ct", "49");
                        }
                        values.put("special",object.getIntValue("special"));
                        Cursor cursor = DatabaseOperator.getInstance().getReadableDatabase().rawQuery(
                                "select * from DEVICE_COMMAND where d_id = ? and ct = ?",
                                new String[]{String.valueOf(dInfo.getId()), object.getString("dt")});
                        if (cursor != null && cursor.getCount() > 0) {
                            DatabaseOperator.getInstance().getWritableDatabase().update("DEVICE_COMMAND", values, "d_id = ? and ct = ?", new String[]{String.valueOf(dInfo.getId()), object.getString("dt")});
                        } else {
                            DatabaseOperator.getInstance().getWritableDatabase().insert("DEVICE_COMMAND", "id", values);
                        }
                        cursor.close();
//						}
                        int totalNr = BadgerUtil.updateBadger(context); //更新角标
                        // 获取默认提醒方式
                        if (DeviceInfo.CaMenu.zhujiControl.value().equals(dInfo.getCa()) && dInfo.getDr() == 4) { // 502遥控器按钮强力模式
                            showAlertMessageView(msg.getDeviceid(), null);
                        }
                        Map<String, Object> map = new HashMap<>();
                        map.put("device_id", String.valueOf(msg.getDeviceid()));
                        map.put("device_info", object.toJSONString());//有需要两次回调的添加此参数
                        //广播收到了一个新消息
//                        sendCustomBroadcast(context, Actions.ACCETP_ONEDEVICE_MESSAGE, "device_id",
//                                String.valueOf(msg.getDeviceid()));
                        sendCustomBroadcast(context, Actions.ACCETP_ONEDEVICE_MESSAGE, map);

                        if (dInfo.getCa().equals(DeviceInfo.CaMenu.menling.value())) {
                            showAlertMessageView(msg.getDeviceid(), null);
                        }
                        if (sforti) { //是否支持设防
                            int showDengji = dInfo.getAcceptMessage();
                            if (showDengji == 2) {
                                MyNotificationInfo info = new MyNotificationInfo();
                                if (dInfo!=null && !TextUtils.isEmpty(dInfo.getLogo())){
                                    info.setBigIcon(ImageUtil.resizeBitmap(ImageLoader.getInstance()
                                                    .loadImageSync( dcsp.getString(Constant.HTTP_DATA_SERVERS, "")
                                                            + "/devicelogo/" + dInfo.getLogo()),
                                            100, 100));
                                }else {
                                    info.setBigIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.login_logo));
                                }

                                info.setContext(context);
                                info.setNr(totalNr); //设置总数到通知栏去。防止MINI6.0以上不显示

                                boolean isNull = ((dInfo.getWhere() == null || "".equals(dInfo.getWhere()) || "null".equals(dInfo.getWhere()))
                                        && (dInfo.getType() == null || "".equals(dInfo.getType())));
                                StringBuffer sb = new StringBuffer();
                                sb.append(dInfo.getName());
                                if (isNull) {
                                    info.setContentTitle(sb.toString());
                                } else {
                                    sb.append(" ");
                                    sb.append("( ");
                                    sb.append(((dInfo.getWhere() == null || "null".equals(dInfo.getWhere())) ? "" : dInfo.getWhere() + " "));
                                    sb.append((dInfo.getType()));
                                    sb.append(" )");
                                    info.setContentTitle(sb.toString());
                                }
                                info.setContentText(message);
                                //门磁，发送通知
//                                NotificationUtil.showNotOngoingTips(context,info);
                                //自定义铃声的通知
//                                NotificationUtil.notifymsg(context);
                                NotificationUtil.setSmartMedicineSound(info,object.getIntValue("special"));   //判断是否是智能药箱吃药提醒
                                NotificationUtil.showDefineBellTip(context, info, msg.getDeviceid());


                            } else if (showDengji == 3) {
                                showAlertMessageView(msg.getDeviceid(), null);
                            }
                        }
                        //excuteZhujiControlBusiness(dInfo,totalNr,message,msg);//处理主机遥控器上报
                    } else if (object.getIntValue("t") == 0) { //主机状态和数据
                        ZhujiInfo zhuji = DatabaseOperator.getInstance(context).queryDeviceZhuJiInfo(msg.getDeviceid());
                        String dct = object.getString("dt") != null ? object.getString("dt") : "";
                        if (dct.equals("17") || dct.equals("18") || dct.equals("19") || dct.equals("20") || dct.equals("status_scall") || dct.equals("status_ssms")) {
                            ContentValues values = new ContentValues();
                            if (dct.equals("17")) {
                                values.put("simStatus",
                                        object.get("deviceCommand") != null ? object.getString("deviceCommand") : "");
                            } else if (dct.equals("18")) {
                                values.put("batteryStatus",
                                        object.get("deviceCommand") != null ? object.getString("deviceCommand") : "");
                            } else if (dct.equals("19")) {
                                values.put("wanType",
                                        object.get("deviceCommand") != null ? object.getString("deviceCommand") : "");
                            } else if (dct.equals("20")) {
                                values.put("powerStatus",
                                        object.get("deviceCommand") != null ? object.getString("deviceCommand") : "");
                            } else if (dct.equals("status_scall")) {
                                values.put("statusCall",
                                        object.get("deviceCommand") != null ? object.getString("deviceCommand") : "");
                            } else if (dct.equals("status_ssms")) {
                                values.put("statusSms",
                                        object.get("deviceCommand") != null ? object.getString("deviceCommand") : "");
                            }
                            Cursor cursor = DatabaseOperator.getInstance().getReadableDatabase().rawQuery("select * from ZHUJI_STATUSINFO where id = ?"
                                    , new String[]{String.valueOf(msg.getDeviceid())});
                            if (cursor != null && cursor.getCount() > 0) {
                                DatabaseOperator.getInstance().getWritableDatabase().update("ZHUJI_STATUSINFO", values, "id = ?",
                                        new String[]{String.valueOf(msg.getDeviceid())});
                            } else {
                                DatabaseOperator.getInstance().getWritableDatabase().insert("ZHUJI_STATUSINFO", "id", values);
                            }
                            cursor.close();

                        }
                        ContentValues values = new ContentValues();
                        values.put("d_id", msg.getDeviceid());
                        if (!"0".equals(dct)) {
                            if (!StringUtils.isEmpty(object.getString("dtv"))) {
                                values.put("command", object.getString("dtv"));
                            } else {
                                values.put("command", object.getString("deviceCommand"));
                            }
                        } else {
                            values.put("command", object.getString("deviceCommand"));
                        }
                        values.put("ctime", object.getLongValue("deviceCommandTime"));
                        values.put("ct", object.getString("dt"));
                        values.put("special",object.getIntValue("special"));
                        Cursor cursor = DatabaseOperator.getInstance().getReadableDatabase().rawQuery(
                                "select * from DEVICE_COMMAND where d_id = ? and ct = ?",
                                new String[]{String.valueOf(zhuji.getId()), object.getString("dt")});
                        if (cursor != null && cursor.getCount() > 0) {
                            DatabaseOperator.getInstance().getWritableDatabase().update("DEVICE_COMMAND", values, "d_id = ? and ct = ?", new String[]{String.valueOf(zhuji.getId()), object.getString("dt")});
                        } else {
                            DatabaseOperator.getInstance().getWritableDatabase().insert("DEVICE_COMMAND", "id", values);
                        }
                        cursor.close();

                        sforti = object.getBooleanValue("sforti");
                        message = object.getString("deviceCommand");
                        int showDengji = object.getIntValue("al");
//                            if (object.get("notread") != null) {
//                                values.put("nr", object.getIntValue("notread")); // 未读消息数,有数据时才更新
//                            }
                        if (sforti && zhuji != null) { //是否支持设防
                            int totalNr = BadgerUtil.updateBadger(context); //更新角标
                            if (showDengji == 2) {
                                MyNotificationInfo info = new MyNotificationInfo();
                                info.setBigIcon(ImageUtil.resizeBitmap(ImageLoader.getInstance()
                                                .loadImageSync( dcsp.getString(Constant.HTTP_DATA_SERVERS, "")
                                                        + "/devicelogo/" + zhuji.getLogo()),
                                        100, 100));
                                info.setContext(context);
                                info.setNr(totalNr); //设置总数到通知栏去。防止MINI6.0以上不显示

                                boolean isNull = ((zhuji.getWhere() == null || "".equals(zhuji.getWhere()) || "null".equals(zhuji.getWhere())));
                                StringBuffer sb = new StringBuffer();
                                sb.append(zhuji.getName());
                                if (isNull) {
                                    info.setContentTitle(sb.toString());
                                } else {
                                    sb.append(" ");
                                    sb.append(((zhuji.getWhere() == null || "null".equals(zhuji.getWhere())) ? "" : zhuji.getWhere() + " "));
                                    info.setContentTitle(sb.toString());
                                }
                                info.setContentText(message);
                                //门磁，发送通知
//                                  NotificationUtil.showNotOngoingTips(context,info);
                                //自定义铃声的通知
//                                  NotificationUtil.notifymsg(context);
                                NotificationUtil.showDefineBellTip(context, info, msg.getDeviceid());
                            } else if (showDengji == 3) {
                                showAlertMessageView(msg.getDeviceid(), null);//主机的报警有问题哦。一个参数不够后续再处理
                            }
                        }
                    }
                    Map<String, Object> map = new HashMap<>();
                    map.put("zhuji_id", String.valueOf(msg.getDeviceid()));
                    map.put("zhuji_info", object.toJSONString());
                    //广播收到了一个新消息
//                        sendCustomBroadcast(context, Actions.ACCETP_ONEDEVICE_MESSAGE, "zhuji_id",
//                                String.valueOf(msg.getDeviceid()));
                    sendCustomBroadcast(context, Actions.ACCETP_ONEDEVICE_MESSAGE, map);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (command == CommandMenu.ac_zhujiOnlineChange.value()) { // 5的包
            // 表示主机是否在线状态变更
            // deviceid
            // 表示主机ID
            // 数据内容为json格式字符创{'status':0}
            // 0表示未在线，1表示在线
            try {
                JSONObject object = JSON.parseObject(new String(msg.getSyncBytes(), "UTF-8"));
                ZhujiInfo zhujiInfo = DatabaseOperator.getInstance().queryDeviceZhuJiInfo(msg.getDeviceid());
                if (object.get("status") != null && object.getString("status").equals("1")) {
                    zhujiInfo.setOnline(true);
                } else {
                    zhujiInfo.setOnline(false);
                }
                DatabaseOperator.getInstance().insertOrUpdateZhujiInfo(zhujiInfo);
                sendCustomBroadcast(context, Actions.ACCETP_ONEDEVICE_MESSAGE, "device_id",
                        String.valueOf(msg.getDeviceid()));

//                int totalNr = DatabaseOperator.getInstance().getTotalNotReadMessageCount();
//
//                //判断是否开启主机在线离线状态提醒
////                if (dcsp.getBoolean(Constant.IS_SERVER_STATUS_NOTIFY, true)) {
//                    MyNotificationInfo info = new MyNotificationInfo();
//                    info.setBigIcon(ImageUtil.resizeBitmap(ImageLoader.getInstance().loadImageSync("http://"
//                                    + dcsp.getString(Constant.HTTP_DATA_SERVERS, "") + "/devicelogo/" + zhujiInfo.getLogo()), 100,
//                            100));
//                    info.setContext(context);
//                    info.setNr(totalNr + 1);
//                    info.setContentTitle(zhujiInfo.getWhere() + " " + zhujiInfo.getName());
//                    if (zhujiInfo.isOnline()) {
//                        info.setContentText(context.getString(R.string.zjonline));
//                    } else {
//                        info.setContentText(context.getString(R.string.zjoffline));
//                    }
//                    NotificationUtil.showNotOngoingTips(context, info);
////                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (command == CommandMenu.ac_changeIP.value()) { // 102的包 需要更换IP并重新登录
            new Handler(context.getMainLooper()).postDelayed(new Runnable() { //延迟两秒操作
                @Override
                public void run() {
                    try {
                        JSONObject object = JSON.parseObject(new String(msg.getSyncBytes(), "UTF-8"));
                        String ip = object.getString("ip");
                        String domain = object.getString("domain");
                        if (!StringUtils.isEmpty(domain)){
                            dcsp.putString(Constant.SYNC_DATA_SERVERS, domain + ":7778").putString(Constant.HTTP_DATA_SERVERS, "https://" + domain).commit();
                        }else if(!StringUtils.isEmpty(ip)){
                            dcsp.putString(Constant.SYNC_DATA_SERVERS, ip + ":7778").putString(Constant.HTTP_DATA_SERVERS, "http://" + ip + ":9999").commit();
                        }
                        sendCustomBroadcast(context, Actions.APP_RECONNECTION);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 2000);
        } else if (command == CommandMenu.ac_refresh.value()) { // 103的包
            // 表示设备有更新或者变化需要重新拉取数据，发送一个107指令包
            SyncMessageContainer.getInstance().produceSendMessage(new SyncMessage(CommandMenu.rq_refresh));
        } else if (command == CommandMenu.ac_kickoff.value()) { // 104的包
            // 表示被踢下线了
            sendCustomBroadcast(context, Actions.APP_KICKOFF,"kickofftype",msg.getCode());
        } else if (command == CommandMenu.ac_factory.value()) { // 105的包
            // 表示工厂模式上发包
            sendCustomBroadcast(context, Actions.ZHUJI_FACTORY, "code", msg.getCode());
        } else if (command == CommandMenu.ac_tipchange.value()) { // 106的包
            // 表示提醒方式变更
            try {
                JSONObject object = JSON.parseObject(new String(msg.getSyncBytes(), "UTF-8"));
                ContentValues values = new ContentValues();
                values.put("app_acceptmessage", object.get("s") != null ? object.getIntValue("s") : 0);
                DatabaseOperator.getInstance().getWritableDatabase().update("DEVICE_STATUSINFO", values, "id = ?",
                        new String[]{String.valueOf(msg.getDeviceid())});
                sendCustomBroadcast(context, Actions.ACCETP_ONEDEVICE_MESSAGE, "device_id",
                        String.valueOf(msg.getDeviceid()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (command == CommandMenu.ac_batterychange.value()) { // 107的包
            // 表示电量变更
            try {
                JSONObject object = JSON.parseObject(new String(msg.getSyncBytes(), "UTF-8"));
                ContentValues values = new ContentValues();
                values.put("lowb", object.get("s") != null ? object.getBooleanValue("s") ? 1 : 0 : 0);
                DatabaseOperator.getInstance().getWritableDatabase().update("DEVICE_STATUSINFO", values, "id = ?",
                        new String[]{String.valueOf(msg.getDeviceid())});
                sendCustomBroadcast(context, Actions.ACCETP_ONEDEVICE_MESSAGE, "device_id",
                        String.valueOf(msg.getDeviceid()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (command == CommandMenu.ac_alarm.value()) {
            String user = null;
            try {
                JSONObject o = JSON.parseObject(new String(msg.getSyncBytes(), "UTF-8"));
                user = o.getString("user");
            } catch (Exception ex) {
                Log.w(TAG, "dispatch: CommandMenu.ac_alarm error!!");
            }
            showAlertMessageView(msg.getDeviceid(), user);
        }
        // end
        else { // 单个设备信息提交
            Log.w(TAG, "未知回复包command=" + command);
        }
    }

    private void showAlertMessageView(long deviceId, String content) {
//        ActivityManager a = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//        List<RunningServiceInfo> sInfos = a.getRunningServices(Integer.MAX_VALUE);
//        boolean isRunning = false;
//        for (RunningServiceInfo rServiceInfo : sInfos) {
//            if (AudioTipsService.class.getCanonicalName().equals(rServiceInfo.service.getClassName())) {
//                isRunning = true;
//                break;
//            }
//        }
//        if (!isRunning) {
        Intent intent = new Intent();
        intent.putExtra("deviceid", deviceId);
        intent.putExtra("from", content);
        if(Actions.VersionType.CHANNEL_ZHICHENG.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            intent.setClass(context,ZCAlertMessageActivity.class);
        }else{
            intent.setClass(context, AlertMessageActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
//        }
    }

    private void showDeivceListView() {
        Log.e(TAG, "拉取到最新的数据,发现已经无数据了,重新打开设备列表并清空栈");
        int priority = 0;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                priority = appProcess.importance;
            }
        }
        if (priority == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) { //当前APP是在前端
            Intent in = new Intent();
            in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            in.setClass(DeviceMainActivity.mthis, DeviceMainActivity.class);
            DeviceMainActivity.mthis.startActivity(in);
        }
    }

    private void excuteZhujiControlBusiness(DeviceInfo dInfo, int totalNr, String message, SyncMessage msg) {
        if (DeviceInfo.CaMenu.zhujiControl.value().equals(dInfo.getCa())) {
            MyNotificationInfo info = new MyNotificationInfo();
            info.setBigIcon(ImageUtil.resizeBitmap(ImageLoader.getInstance()
                            .loadImageSync( dcsp.getString(Constant.HTTP_DATA_SERVERS, "")
                                    + "/devicelogo/" + dInfo.getLogo()),
                    100, 100));
            info.setContext(context);
            info.setNr(totalNr); //设置总数到通知栏去。防止MINI6.0以上不显示
            String whereAndType = dInfo.getWhere() + " " + dInfo.getType();
            info.setContentTitle(
                    dInfo.getName() + ((whereAndType.equals(" ") || whereAndType.equals("null")) ? "" : "(" + whereAndType + ")"));
            info.setContentText(message);
            //门磁，发送通知
            //NotificationUtil.showNotOngoingTips(context,info);
            //自定义铃声的通知
            NotificationUtil.showDefineBellTip(context, info, msg.getDeviceid());
        }
    }
}
