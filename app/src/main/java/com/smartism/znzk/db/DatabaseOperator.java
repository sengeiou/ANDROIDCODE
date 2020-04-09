package com.smartism.znzk.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.smartism.znzk.R;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.domain.AppUserInfo;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.DeviceInfo.ControlTypeMenu;
import com.smartism.znzk.domain.DeviceKeys;
import com.smartism.znzk.domain.GroupInfo;
import com.smartism.znzk.domain.PersInfo;
import com.smartism.znzk.domain.WeightUserInfo;
import com.smartism.znzk.domain.ZhujiGroupInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.domain.camera.CameraInfo;
import com.smartism.znzk.util.CollectionsUtils;
import com.smartism.znzk.util.LogUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DatabaseOperator extends SQLiteOpenHelper {
    private static final String TAG = DatabaseOperator.class.getSimpleName();
    private static DatabaseOperator _instance;

    private static final String DATABASE_NAME = "Znwx_DB";
    private static final int DATABASE_VERSION = 38;

    private Context context;

    public DatabaseOperator(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        LogUtil.i(TAG, "--- DatabaseOperator instance finish ---");
    }

    public synchronized static DatabaseOperator getInstance(Context context) {
        if (_instance == null) {
            _instance = new DatabaseOperator(context.getApplicationContext());
        }
        return _instance;
    }

    public synchronized static DatabaseOperator getInstance() {
        return getInstance(MainApplication.app);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        LogUtil.i(TAG, "database " + DATABASE_NAME + " create......");
        String sql = readSQL();
        String[] str = sql.split(";");
        for (int i = 0; i < str.length; i++) {
            db.execSQL(str[i]);
        }
    }

    /**
     * DATABASE_VERSION 版本号不变，这个就不会触发。
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) { //重建数据库
            String sql = readSQL();
            String[] str = sql.split(";");
            for (int i = 0; i < str.length; i++) {
                db.execSQL(str[i]);
            }
        }
    }

    /**
     * 读取数据库资源
     *
     * @return 数据库字符串
     */
    protected String readSQL() {
        InputStream in = context.getResources().openRawResource(R.raw.datacenter);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder sql = new StringBuilder("");
        String temp = null;
        try {
            while ((temp = reader.readLine()) != null) {
                sql.append(temp);
            }
        } catch (IOException e) {
            Log.e(TAG, "readSQL error : " + e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        return sql.toString();
    }

    /**
     * 删除数据库表
     *
     * @param db        操作的数据库
     * @param tableName 数据表名
     */
    protected void dropTable(SQLiteDatabase db, String tableName) {
        db.execSQL("drop table if exists " + tableName); // 删除表
        LogUtil.i(TAG, "drop table if exists " + tableName);
    }


    /**
     * @param id
     * @return
     */
    public DeviceInfo queryDeviceInfo(long id) {
        DeviceInfo deviceInfo = null;
        Cursor cursor = _instance.getReadableDatabase().rawQuery("select * from DEVICE_STATUSINFO where id = ?",
                new String[]{String.valueOf(id)});
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                deviceInfo = buildDeviceInfo(cursor);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return deviceInfo;
    }

    /**
     * @param
     * @return
     */
    public AppUserInfo queryAppUserInfo() {
        AppUserInfo userInfo = null;
        Cursor cursor = _instance.getReadableDatabase().rawQuery("select * from USER_INFO",
                new String[]{});
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                userInfo = buildUserInfo(cursor);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return userInfo;
    }

    public void insertOrUpdateUserInfo(AppUserInfo userInfo){
        ContentValues values = new ContentValues();
        values.put("id", userInfo.getId());
        values.put("name", userInfo.getName());
        values.put("logo", userInfo.getLogo());
        values.put("account", userInfo.getAccount());
        values.put("email", userInfo.getEmail());
        values.put("mobile", userInfo.getMobile());
        values.put("code", userInfo.getCode());
        values.put("role", userInfo.getRole());
        Cursor cursor = DatabaseOperator.getInstance().getReadableDatabase().rawQuery(
                "select * from USER_INFO where account = ? ",
                new String[]{userInfo.getAccount()});
        if (cursor != null && cursor.getCount() > 0) {
            DatabaseOperator.getInstance().getWritableDatabase().update("USER_INFO",
                    values, "account = ?", new String[]{userInfo.getAccount()});

        } else {
            DatabaseOperator.getInstance().getWritableDatabase().insert("USER_INFO",
                    "account", values);
        }
        cursor.close();
    }

    public int queryDeviceInfoAllNotReadCount() {
        int total = 0;
        Cursor cursor = _instance.getReadableDatabase().rawQuery("select sum(nr) from DEVICE_STATUSINFO",
                new String[]{});
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                total = cursor.getInt(0);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return total;
    }

    public ZhujiInfo queryDeviceZhuJiInfo(long id) {
        ZhujiInfo deviceInfo = null;
        Cursor cursor = _instance.getReadableDatabase().rawQuery("select * from ZHUJI_STATUSINFO where id = ?",
                new String[]{String.valueOf(id)});
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                deviceInfo = buildZhujiInfo(cursor);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return deviceInfo;
    }

    public ZhujiInfo queryDeviceZhuJiInfo(String masterid) {
        ZhujiInfo deviceInfo = null;
        Cursor cursor = _instance.getReadableDatabase().rawQuery("select * from ZHUJI_STATUSINFO where masterid = ?",
                new String[]{String.valueOf(masterid)});
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                deviceInfo = buildZhujiInfo(cursor);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return deviceInfo;
    }

    public List<DeviceInfo> queryAllDeviceInfos(long zhujiId) {
        List<DeviceInfo> result = new ArrayList<DeviceInfo>();
        Cursor cursor = _instance.getReadableDatabase().rawQuery("select * from DEVICE_STATUSINFO where zj_id = ? ", new String[]{String.valueOf(zhujiId)});
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                result.add(buildDeviceInfo(cursor));
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return result;
    }
    public List<DeviceInfo> queryAllAdsInfoWithDeviceInfo(long zhujiId) {
        List<DeviceInfo> result = new ArrayList<DeviceInfo>();
        Cursor cursor = _instance.getReadableDatabase().rawQuery("select * from ZHUJI_ADSINFO where zj_id = ? ", new String[]{String.valueOf(zhujiId)});
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                result.add(buildAdsInfoWithDeviceInfo(cursor));
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return result;
    }

    public List<GroupInfo> queryAllGroups(long zhujiId) {
        List<GroupInfo> result = new ArrayList<GroupInfo>();
        Cursor cursor = _instance.getReadableDatabase().rawQuery("select * from GROUP_STATUSINFO where zj_id = ? ", new String[]{String.valueOf(zhujiId)});
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                result.add(buildGroupInfo(cursor));
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return result;
    }

    public List<ZhujiGroupInfo> queryAllZhujiGroups() {
        List<ZhujiGroupInfo> result = new ArrayList<ZhujiGroupInfo>();
        Cursor cursor = _instance.getReadableDatabase().rawQuery("select * from ZHUJI_GROUP_STATUSINFO", new String[]{});
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                result.add(buildZhujiGroupInfo(cursor));
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return result;
    }

    //查询组是否有设备DeviceInfo
    public boolean queryGroupsHasDevice(long groupid){
        Cursor cursor = _instance.getReadableDatabase().rawQuery("select * from GROUP_DEVICE_RELATIOIN where gid = ?",new String[]{String.valueOf(groupid)});
        if(cursor!=null&&cursor.getCount()>0){
            return true;
        }
        if(cursor!=null){
            cursor.close();
        }
        return false;
    }

    public List<DeviceInfo> queryAllDevicesByGroups(long groupid) {
        List<DeviceInfo> result = new ArrayList<DeviceInfo>();
        Cursor cursor = _instance.getReadableDatabase().rawQuery("SELECT * FROM DEVICE_STATUSINFO d LEFT JOIN GROUP_DEVICE_RELATIOIN r ON d.id = r.did WHERE r.gid = ? ", new String[]{String.valueOf(groupid)});
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                result.add(buildDeviceInfo(cursor));
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return result;
    }
    public List<ZhujiInfo> queryAllZhujisByGroups(long groupid) {
        List<ZhujiInfo> result = new ArrayList<ZhujiInfo>();
        Cursor cursor = _instance.getReadableDatabase().rawQuery("SELECT * FROM ZHUJI_STATUSINFO d LEFT JOIN ZHUJI_GROUP_DEVICE_RELATIOIN r ON d.id = r.did WHERE r.gid = ? ", new String[]{String.valueOf(groupid)});
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                ZhujiInfo zj = buildZhujiInfo(cursor);
                if (zj!=null) {
                    result.add(zj);
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return result;
    }

    public void insertOrUpdatePersInfo(PersInfo persInfo) {
        ContentValues cv = new ContentValues();
        cv.put("id", persInfo.getId());
        cv.put("zh_id", persInfo.getZj_id());
        cv.put("k", persInfo.getK());
        cv.put("v", persInfo.getV());
        Cursor cursor = getReadableDatabase().rawQuery("select * from PERS where zj_id = ?", new String[]{String.valueOf(cv.get("zj_id"))});
        if (cursor != null && cursor.getCount() > 0) {
            getWritableDatabase().update("PERS", cv, "zj_id = ?", new String[]{String.valueOf(cv.get("zj_id"))});
        } else {
            getWritableDatabase().insert("PERS", "zj_id", cv);
        }
        cursor.close();
    }

    public PersInfo queryPersInfo(long zj_id) {
        PersInfo persInfo = new PersInfo();
        Cursor cursor = _instance.getReadableDatabase().rawQuery("select * from FAMINY_MEMBER where zj_id = ?", new String[]{String.valueOf(zj_id)});

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                persInfo = buildPersInfo(cursor);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return persInfo;
    }

    public PersInfo buildPersInfo(Cursor cursor) {
        PersInfo persInfo = new PersInfo();
//        persInfo.setId(cursor.getLong(cursor.getColumnIndex("id")));
        persInfo.setZj_id(cursor.getLong(cursor.getColumnIndex("zj_id")));
        persInfo.setK(cursor.getString(cursor.getColumnIndex("k")));
        persInfo.setV(cursor.getString(cursor.getColumnIndex("v")));
        return persInfo;
    }

    public List<PersInfo> queryAllPersInfos(long zj_id) {
        List<PersInfo> persInfos = new ArrayList<PersInfo>();
        Cursor cursor = _instance.getReadableDatabase().rawQuery("select * from PERS where zj_id = ?",
                new String[]{String.valueOf(zj_id)});
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                persInfos.add(buildPersInfo(cursor));
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return persInfos;
    }


    public void insertOrUpdateFamilyMember(WeightUserInfo userInfo) {
        ContentValues values = new ContentValues();
        values.put("id", userInfo.getUserId());
        values.put("name", userInfo.getUserName());
        values.put("logo", userInfo.getUserLogo());
        values.put("sex", userInfo.getUserSex());
        values.put("height", userInfo.getUserHeight());
        values.put("birthday", userInfo.getUserBirthday());
        values.put("objectiveWeight", userInfo.getUserObjectiveWeight());
        values.put("odbp", userInfo.getOdbp());
        values.put("osbp", userInfo.getOsbp());
        values.put("skinFid", userInfo.getSkinFid());
        Cursor cursor = getReadableDatabase().rawQuery(
                "select * from FAMINY_MEMBER where id = ?",
                new String[]{String.valueOf(values.get("id"))});
        if (cursor != null && cursor.getCount() > 0) {
            getWritableDatabase().update("FAMINY_MEMBER", values,
                    "id = ?", new String[]{String.valueOf(values.get("id"))});
        } else {
            getWritableDatabase().insert("FAMINY_MEMBER", "id", values);
        }
        cursor.close();
    }


    public WeightUserInfo queryWeightUserInfo(long id) {
        WeightUserInfo userInfo = new WeightUserInfo();


        Cursor cursor = _instance.getReadableDatabase().rawQuery("select * from FAMINY_MEMBER where id = ?", new String[]{String.valueOf(id)});

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                userInfo = buildFamilyMember(cursor);
            }
        }
        if (cursor != null) {
            cursor.close();
        }

        return userInfo;
    }


    public void insertOrUpdateZhujiInfo(ZhujiInfo zhujiInfo) {
        ContentValues values = new ContentValues();
        values.put("id", zhujiInfo.getId());
        values.put("name", zhujiInfo.getName());
        values.put("dwhere", zhujiInfo.getWhere());
        values.put("logo", zhujiInfo.getLogo());
        values.put("dbn", zhujiInfo.getBrandName());
        values.put("online", zhujiInfo.isOnline() ? 1 : 0);
        values.put("cid", zhujiInfo.getCid()); // 厂商id
        values.put("masterid", zhujiInfo.getMasterid());// masterid
        values.put("admin", zhujiInfo.isAdmin() ? 1 : 0);// 是否是主机admin
        values.put("scene", zhujiInfo.getScene());//主机当前场景
        values.put("uc", zhujiInfo.getUc()); // 用户数
        values.put("updateStatus", zhujiInfo.getUpdateStatus()); // 0表示正常
        // 1显示固件更新
        values.put("gsm", zhujiInfo.getGsm());
        values.put("powerStatus", zhujiInfo.getPowerStatus());
        values.put("batteryStatus", zhujiInfo.getBatteryStatus());
        values.put("simStatus", zhujiInfo.getSimStatus());
        values.put("wanType", zhujiInfo.getWanType());
        values.put("statusCall", zhujiInfo.getStatusCall());
        values.put("statusSms", zhujiInfo.getStatusSMS());
        values.put("ca", zhujiInfo.getCa());
        values.put("cak", zhujiInfo.getCak());
        values.put("scenet", zhujiInfo.getScenet());
        values.put("dt", zhujiInfo.getDt());
        values.put("dtid", zhujiInfo.getDtid());
        values.put("bipc", zhujiInfo.getBipc());
        values.put("ipcid", zhujiInfo.getCameraInfo().getIpcid());
        values.put("cameraid", zhujiInfo.getCameraInfo().getId());
        values.put("cameran", zhujiInfo.getCameraInfo().getN());
        values.put("camerap", zhujiInfo.getCameraInfo().getOriginalP());
        values.put("camerac", zhujiInfo.getCameraInfo().getC());
        values.put("ac", zhujiInfo.getAc());
        values.put("ex", zhujiInfo.isEx() ? 1 : 0);
        values.put("la", zhujiInfo.isLa() ? 1 : 0);
        values.put("rolek",zhujiInfo.getRolek());
        Cursor cursor = getReadableDatabase().rawQuery(
                "select * from ZHUJI_STATUSINFO where id = ? ",
                new String[]{String.valueOf(values.get("id"))});
        if (cursor != null && cursor.getCount() > 0) {
            getWritableDatabase().update("ZHUJI_STATUSINFO", values,
                    "id = ?", new String[]{String.valueOf(values.get("id"))});
        } else {
            getWritableDatabase().insert("ZHUJI_STATUSINFO", "id",
                    values);
        }
        cursor.close();
    }
    public void insertOrUpdateDeviceInfo(DeviceInfo deviceInfo) {
        ContentValues values = new ContentValues();
        values.put("id", deviceInfo.getId());
        values.put("zj_id", deviceInfo.getZj_id());
        values.put("device_name", deviceInfo.getName());
        values.put("device_logo", deviceInfo.getLogo());
        values.put("device_type", deviceInfo.getType());
        values.put("device_tid", deviceInfo.getTypeid());
        values.put("device_where", deviceInfo.getWhere());
        values.put("device_controltype", deviceInfo.getControlType());
        values.put("appdownload", deviceInfo.getAppdownload());
        values.put("apppackage", deviceInfo.getApppackage());
        values.put("device_lastcommand", deviceInfo.getLastCommand());
        values.put("device_lasttime", deviceInfo.getLastUpdateTime());
        values.put("device_dtype", deviceInfo.getDtype());
        values.put("app_acceptmessage", deviceInfo.getAcceptMessage());
//        values.put("re_1", deviceInfo.get.getPowerStatus());
        values.put("sort", deviceInfo.getSort());
        values.put("status", deviceInfo.getStatus());
        values.put("nr", deviceInfo.getNr());
        values.put("ca", deviceInfo.getCa());
        values.put("cak", deviceInfo.getCak());
        values.put("dr", deviceInfo.getDr());
        values.put("lowb", deviceInfo.isLowb());
        values.put("ipc", deviceInfo.getIpc());
        values.put("device_slavedId", deviceInfo.getSlaveId());
        values.put("bipc", deviceInfo.getBipc());
        values.put("eid", deviceInfo.getEids());
        values.put("fa", deviceInfo.isFa());
        values.put("mc",deviceInfo.getMc());
        Cursor cursor = getReadableDatabase().rawQuery(
                "select * from DEVICE_STATUSINFO where id = ? ",
                new String[]{String.valueOf(values.get("id"))});
        if (cursor != null && cursor.getCount() > 0) {
            getWritableDatabase().update("DEVICE_STATUSINFO", values,
                    "id = ?", new String[]{String.valueOf(values.get("id"))});
        } else {
            getWritableDatabase().insert("DEVICE_STATUSINFO", "id",
                    values);
        }
        cursor.close();
    }
    public void insertOrUpdateDeviceCommand(long did,String ct,String value) {
        ContentValues values = new ContentValues();
        values.put("d_id", did);
        values.put("ct", ct);
        values.put("command", value);
        Cursor cursor = getReadableDatabase().rawQuery(
                "select * from DEVICE_COMMAND where d_id = ? and ct = ?",
                new String[]{String.valueOf(did),ct});
        if (cursor != null && cursor.getCount() > 0) {
            getWritableDatabase().update("DEVICE_COMMAND", values,
                    "d_id = ? and ct = ?", new String[]{String.valueOf(did),ct});
        } else {
            getWritableDatabase().insert("DEVICE_COMMAND", null,
                    values);
        }
        cursor.close();
    }


    public List<WeightUserInfo> queryAllFamilyInfos() {
        List<WeightUserInfo> userInfos = new ArrayList<>();
        Cursor cursor = _instance.getReadableDatabase().rawQuery("select * from FAMINY_MEMBER", new String[]{});
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                userInfos.add(buildFamilyMember(cursor));
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return userInfos;
    }

    public List<CameraInfo> queryAllCameras(ZhujiInfo zhuji) {
        List<CameraInfo> camera = new ArrayList<>();
        DeviceInfo deviceInfo = null;

        //StartSearchDevice(); 这个好像是搜索局域网内的在线摄像头。不需要
        if (zhuji != null) {
            List<DeviceInfo> dInfos = queryAllDeviceInfos(zhuji.getId());
            if (dInfos != null && !dInfos.isEmpty()) {
                for (DeviceInfo device : dInfos) {
                    if (DeviceInfo.CakMenu.surveillance.value().equals(device.getCak())) {
                        deviceInfo = device;
                        break;
                    }
                }
            }

            if (deviceInfo != null) {
                List<CameraInfo> list = (List<CameraInfo>) JSON.parseArray(deviceInfo.getIpc(), CameraInfo.class);
                camera.addAll(list);
            }
        }
        return camera;
    }

    public List<ZhujiInfo> queryAllZhuJiInfos() {
        List<ZhujiInfo> deviceInfos = new ArrayList<ZhujiInfo>();
        Cursor cursor = _instance.getReadableDatabase().rawQuery("select * from ZHUJI_STATUSINFO",
                new String[]{});
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                ZhujiInfo zj = buildZhujiInfo(cursor);
                if (zj!=null) {
                    deviceInfos.add(zj);
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return deviceInfos;
    }
    public List<ZhujiInfo> queryAllZhuJiInfos(String where) {
        List<ZhujiInfo> deviceInfos = new ArrayList<ZhujiInfo>();
        Cursor cursor = _instance.getReadableDatabase().rawQuery("select * from ZHUJI_STATUSINFO where name like ? or masterid like ? or dwhere like ?",
                new String[]{"%"+where+"%","%"+where+"%","%"+where+"%"});
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                ZhujiInfo zj = buildZhujiInfo(cursor);
                if (zj!=null) {
                    deviceInfos.add(zj);
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return deviceInfos;
    }
    public List<ZhujiInfo> queryAllZhuJiInfosNotInGroup() {
        List<ZhujiInfo> deviceInfos = new ArrayList<ZhujiInfo>();
        Cursor cursor = _instance.getReadableDatabase().rawQuery("select * from ZHUJI_STATUSINFO where id not in (select did from ZHUJI_GROUP_DEVICE_RELATIOIN)",
                new String[]{});
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                ZhujiInfo zj = buildZhujiInfo(cursor);
                if (zj!=null) {
                    deviceInfos.add(zj);
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return deviceInfos;
    }
    public int queryAllZhuJiCount() {
        int total = 0;
        Cursor cursor = _instance.getReadableDatabase().rawQuery("select count(1) from ZHUJI_STATUSINFO",
                new String[]{});
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                total = cursor.getInt(0);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return total;
    }

    public List<CommandInfo> queryAllCommands(long did) {
        List<CommandInfo> deviceInfos = new ArrayList<CommandInfo>();
        Cursor cursor = _instance.getReadableDatabase().rawQuery("select * from DEVICE_COMMAND where d_id = ?", new String[]{String.valueOf(did)});
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                deviceInfos.add(buildCommandInfo(cursor));
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return deviceInfos;
    }

    public boolean isAcceptNotificationCommands(long did,int index) {
        boolean isHave = false;
        Cursor cursor = _instance.getReadableDatabase().rawQuery("select * from USER_NOTIFICATION where d_id = ? and cindex = ?", new String[]{String.valueOf(did),String.valueOf(index)});
        if (cursor != null && cursor.getCount() > 0) {
            isHave = true;
        }
        if (cursor != null) {
            cursor.close();
        }
        return isHave;
    }

    public Map<String,String> queryZhujiSets(long did) {
        Map<String,String> sets = new HashMap<>();
        Cursor cursor = _instance.getReadableDatabase().rawQuery("select * from ZHUJI_SETINFO where zj_id = ?", new String[]{String.valueOf(did)});
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                sets.put(cursor.getString(cursor.getColumnIndex("k")),cursor.getString(cursor.getColumnIndex("v")));
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return sets;
    }

    /**
     * 查询最新的 也是最后的指令 ct为0表示指令信息
     * @param did
     * @return
     */
    public CommandInfo queryLastCommand(long did) {
        List<CommandInfo> deviceInfos = new ArrayList<CommandInfo>();
        Cursor cursor = _instance.getReadableDatabase().rawQuery("select * from DEVICE_COMMAND where d_id = ? and ct = '0'", new String[]{String.valueOf(did)});
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                deviceInfos.add(buildCommandInfo(cursor));
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return !CollectionsUtils.isEmpty(deviceInfos)?deviceInfos.get(0):null;
    }

    public List<CommandInfo> queryCommandsByCT(long did,String ct) {
        List<CommandInfo> deviceInfos = new ArrayList<CommandInfo>();
        Cursor cursor = _instance.getReadableDatabase().rawQuery("select * from DEVICE_COMMAND where d_id = ? and ct = ?", new String[]{String.valueOf(did),ct});
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                deviceInfos.add(buildCommandInfo(cursor));
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return deviceInfos;
    }

    /**
     * 获取所有未读消息数，不分主机
     *
     * @return
     */
    public int getTotalNotReadMessageCount() {
        int totalNr = 0;
        Cursor cursor = _instance.getWritableDatabase().rawQuery("select sum(nr) from DEVICE_STATUSINFO",
                new String[]{});
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                totalNr = cursor.getInt(0);
                break;
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return totalNr;
    }

    public boolean isInGroup(DeviceInfo dInfo) {
        int totalNr = 0;
        Cursor cursor = _instance.getWritableDatabase().rawQuery("select count(1) from GROUP_DEVICE_RELATIOIN gdr left join GROUP_STATUSINFO gs on gdr.gid = gs.id where gdr.did = ? and gs.zj_id = ?", new String[]{String.valueOf(dInfo.getId()), String.valueOf(dInfo.getZj_id())});
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                totalNr = cursor.getInt(0);
                break;
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        if (totalNr > 0) {
            return true;
        }
        return false;
    }

    public AppUserInfo buildUserInfo(Cursor cursor) {
        AppUserInfo userInfo = new AppUserInfo();
        userInfo.setId(cursor.getLong(cursor.getColumnIndex("id")));
        userInfo.setAccount(cursor.getString(cursor.getColumnIndex("account")));
        userInfo.setName(cursor.getString(cursor.getColumnIndex("name")));
        userInfo.setMobile(cursor.getString(cursor.getColumnIndex("mobile")));
        userInfo.setEmail(cursor.getString(cursor.getColumnIndex("email")));
        userInfo.setLogo(cursor.getString(cursor.getColumnIndex("logo")));
        userInfo.setRole(cursor.getString(cursor.getColumnIndex("role")));
        userInfo.setCode(cursor.getString(cursor.getColumnIndex("code")));
        return userInfo;
    }
    public DeviceInfo buildDeviceInfo(Cursor cursor) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setId(cursor.getLong(cursor.getColumnIndex("id")));
        deviceInfo.setZj_id(cursor.getLong(cursor.getColumnIndex("zj_id")));
        deviceInfo.setName(cursor.getString(cursor.getColumnIndex("device_name")));
        deviceInfo.setLastCommand(cursor.getString(cursor.getColumnIndex("device_lastcommand")));
        deviceInfo.setLastUpdateTime(cursor.getLong(cursor.getColumnIndex("device_lasttime")));
        deviceInfo.setLastCommandSpecial(cursor.getInt(cursor.getColumnIndex("special")));
        deviceInfo.setDtype(cursor.getString(cursor.getColumnIndex("device_dtype")));
        deviceInfo.setType(cursor.getString(cursor.getColumnIndex("device_type")));
        deviceInfo.setTypeid(cursor.getLong(cursor.getColumnIndex("device_tid")));
        deviceInfo.setWhere(cursor.getString(cursor.getColumnIndex("device_where")));
        deviceInfo.setControlType(cursor.getString(cursor.getColumnIndex("device_controltype")));
        deviceInfo.setAppdownload(cursor.getString(cursor.getColumnIndex("appdownload")));
        deviceInfo.setApppackage(cursor.getString(cursor.getColumnIndex("apppackage")));
        deviceInfo.setLogo(cursor.getString(cursor.getColumnIndex("device_logo")));
        deviceInfo.setAcceptMessage(cursor.getInt(cursor.getColumnIndex("app_acceptmessage")));
        if (ControlTypeMenu.wenduji.value().equals(deviceInfo.getControlType()) || ControlTypeMenu.wenshiduji.value().equals(deviceInfo.getControlType())) {
            deviceInfo.setChValue(cursor.getString(cursor.getColumnIndex("re_1")));
        }
        deviceInfo.setSort(cursor.getInt(cursor.getColumnIndex("sort")));
        deviceInfo.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
        deviceInfo.setNr(cursor.getInt(cursor.getColumnIndex("nr")));
        deviceInfo.setNt(cursor.getInt(cursor.getColumnIndex("nt")));
        deviceInfo.setCa(cursor.getString(cursor.getColumnIndex("ca")));
        deviceInfo.setCak(cursor.getString(cursor.getColumnIndex("cak")));
        deviceInfo.setDr(cursor.getInt(cursor.getColumnIndex("dr")));
        deviceInfo.setLowb(cursor.getInt(cursor.getColumnIndex("lowb")) != 0);
        deviceInfo.setIpc(cursor.getString(cursor.getColumnIndex("ipc")));
        deviceInfo.setSlaveId(cursor.getString(cursor.getColumnIndex("device_slavedId")));
        deviceInfo.setBipc(cursor.getString(cursor.getColumnIndex("bipc")));
        deviceInfo.setEids(cursor.getString(cursor.getColumnIndex("eid")));
        deviceInfo.setFa(cursor.getInt(cursor.getColumnIndex("fa")) != 0);
        deviceInfo.setMc(cursor.getString(cursor.getColumnIndex("mc")));
        deviceInfo.setdCommands(queryAllCommands(deviceInfo.getId()));
        return deviceInfo;
    }
    public DeviceInfo buildAdsInfoWithDeviceInfo(Cursor cursor) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setZj_id(cursor.getLong(cursor.getColumnIndex("zj_id")));
        deviceInfo.setName(cursor.getString(cursor.getColumnIndex("name")));
        deviceInfo.setLogo(cursor.getString(cursor.getColumnIndex("logo")));
        deviceInfo.setLastCommand(cursor.getString(cursor.getColumnIndex("remark")));
        deviceInfo.setCa(cursor.getString(cursor.getColumnIndex("ca")));
        deviceInfo.setControlType(ControlTypeMenu.adsinfo.value());//设置控制类型为广告
        deviceInfo.setAdsUt(cursor.getInt(cursor.getColumnIndex("ut")));
        deviceInfo.setAdsUrl(cursor.getString(cursor.getColumnIndex("url")));
        deviceInfo.setAcceptMessage(1);
        return deviceInfo;
    }

    public GroupInfo buildGroupInfo(Cursor cursor) {
        GroupInfo gInfo = new GroupInfo();
        gInfo
                .setId(cursor.getLong(cursor.getColumnIndex("id")));
        gInfo
                .setZj_id(cursor.getLong(cursor.getColumnIndex("zj_id")));
        gInfo.setName(cursor.getString(cursor
                .getColumnIndex("name")));
        gInfo.setLogo(cursor.getString(cursor
                .getColumnIndex("logo")));
        gInfo.setBipc(cursor.getString(cursor.getColumnIndex("bipc")));
        return gInfo;
    }
    public ZhujiGroupInfo buildZhujiGroupInfo(Cursor cursor) {
        ZhujiGroupInfo gInfo = new ZhujiGroupInfo();
        gInfo
                .setId(cursor.getLong(cursor.getColumnIndex("id")));
        gInfo.setName(cursor.getString(cursor
                .getColumnIndex("name")));
        gInfo.setLogo(cursor.getString(cursor
                .getColumnIndex("logo")));
        return gInfo;
    }

    public WeightUserInfo buildFamilyMember(Cursor cursor) {
        WeightUserInfo userInfo = new WeightUserInfo();
        userInfo.setUserId(cursor.getLong(cursor.getColumnIndex("id")));
        userInfo.setUserName(cursor.getString(cursor.getColumnIndex("name")));
        userInfo.setUserLogo(cursor.getString(cursor.getColumnIndex("logo")));
        userInfo.setUserHeight(cursor.getInt(cursor.getColumnIndex("height")));
        userInfo.setUserSex(cursor.getString(cursor.getColumnIndex("sex")));
        userInfo.setUserBirthday(cursor.getString(cursor.getColumnIndex("birthday")));
        userInfo.setUserObjectiveWeight(cursor.getString(cursor.getColumnIndex("objectiveWeight")));
        userInfo.setOdbp(cursor.getInt(cursor.getColumnIndex("odbp")));
        userInfo.setOsbp(cursor.getInt(cursor.getColumnIndex("osbp")));
        userInfo.setSkinFid(cursor.getLong(cursor.getColumnIndex("skinFid")));
        return userInfo;
    }


    public ZhujiInfo buildZhujiInfo(Cursor cursor) {
        ZhujiInfo deviceInfo = new ZhujiInfo();
        deviceInfo.setRolek(cursor.getString(cursor.getColumnIndexOrThrow("rolek")));
        deviceInfo
                .setId(cursor.getLong(cursor.getColumnIndex("id")));
        deviceInfo.setName(cursor.getString(cursor
                .getColumnIndex("name")));
        deviceInfo.setWhere(cursor.getString(cursor
                .getColumnIndex("dwhere")));
        deviceInfo.setLogo(cursor.getString(cursor
                .getColumnIndex("logo")));
        deviceInfo.setBrandName(cursor.getString(cursor
                .getColumnIndex("dbn")));
        deviceInfo.setOnline(cursor.getInt(cursor.getColumnIndex("online")) != 0);
        deviceInfo.setMasterid(cursor.getString(cursor.getColumnIndex("masterid")));
        deviceInfo.setCid(cursor.getLong(cursor.getColumnIndex("cid")));
        deviceInfo.setAdmin(cursor.getInt(cursor.getColumnIndex("admin")) != 0);
        deviceInfo.setScene(cursor.getString(cursor.getColumnIndex("scene")));
        deviceInfo.setUc(cursor.getInt(cursor.getColumnIndex("uc")));
        deviceInfo.setUpdateStatus(cursor.getInt(cursor.getColumnIndex("updateStatus")));
        deviceInfo.setGsm(cursor.getInt(cursor.getColumnIndex("gsm")));
        deviceInfo.setSimStatus(cursor.getInt(cursor.getColumnIndex("simStatus")));
        deviceInfo.setBatteryStatus(cursor.getInt(cursor.getColumnIndex("batteryStatus")));
        deviceInfo.setWanType(cursor.getInt(cursor.getColumnIndex("wanType")));
        deviceInfo.setPowerStatus(cursor.getInt(cursor.getColumnIndex("powerStatus")));
        deviceInfo.setStatusCall(cursor.getInt(cursor.getColumnIndex("statusCall")));
        deviceInfo.setStatusSMS(cursor.getInt(cursor.getColumnIndex("statusSms")));
        deviceInfo.setCa(cursor.getString(cursor.getColumnIndex("ca")));
        deviceInfo.setCak(cursor.getString(cursor.getColumnIndex("cak")));
        deviceInfo.setScenet(cursor.getString(cursor.getColumnIndex("scenet")));
        deviceInfo.setBipc(cursor.getLong(cursor.getColumnIndex("bipc")));
        deviceInfo.getCameraInfo().setC(cursor.getString(cursor.getColumnIndex("camerac")));
        deviceInfo.getCameraInfo().setId(cursor.getString(cursor.getColumnIndex("cameraid")));
        deviceInfo.getCameraInfo().setN(cursor.getString(cursor.getColumnIndex("cameran")));
        deviceInfo.getCameraInfo().setP(cursor.getString(cursor.getColumnIndex("camerap")));
        //添加的代码
        deviceInfo.getCameraInfo().setIpcid(cursor.getLong(cursor.getColumnIndex("ipcid")));
        deviceInfo.setIpcid(cursor.getLong(cursor.getColumnIndex("ipcid")));
        //结束
        deviceInfo.setDt(cursor.getString(cursor.getColumnIndex("dt")));
        deviceInfo.setDtid(cursor.getString(cursor.getColumnIndex("dtid")));
        deviceInfo.setAc(cursor.getInt(cursor.getColumnIndex("ac")));
        deviceInfo.setEx(cursor.getInt(cursor.getColumnIndex("ex")) != 0);
        deviceInfo.setLa(cursor.getInt(cursor.getColumnIndex("la")) != 0);


        if (deviceInfo.getId() <= 0){
            return null;
        }

        deviceInfo.setSetInfos(queryZhujiSets(deviceInfo.getId()));
        return deviceInfo;
    }

    public CommandInfo buildCommandInfo(Cursor cursor) {
        CommandInfo commandInfo = new CommandInfo();
        commandInfo.setmId(cursor.getLong(cursor.getColumnIndex("m_id")));
        commandInfo
                .setD_id(cursor.getLong(cursor.getColumnIndex("d_id")));
        commandInfo.setCommand(cursor.getString(cursor
                .getColumnIndex("command")));
        commandInfo.setCtime(cursor.getLong(cursor
                .getColumnIndex("ctime")));
        commandInfo.setCtype(cursor.getString(cursor
                .getColumnIndex("ct")));
        commandInfo.setgId(cursor.getLong(cursor.getColumnIndex("dcg")));
        commandInfo.setSpecial(cursor.getInt(cursor.getColumnIndex("special")));
        return commandInfo;
    }

    public List<DeviceKeys> findDeviceKeysByDeviceId(long did) {
        Cursor c = _instance.getReadableDatabase().rawQuery("select * from devices_key where d_id = ?", new String[]{String.valueOf(did)});
        List<DeviceKeys> deviceKeyses = null;
        if (c != null && c.getCount() > 0) {
            deviceKeyses = new ArrayList<>();
            while (c.moveToNext()) {
                DeviceKeys deviceKeys = new DeviceKeys();
                deviceKeys.setDeviceId(c.getLong(c.getColumnIndex(DeviceKeys.COLUMN_DEVICEKEYS_ID)));
                deviceKeys.setKeyName(c.getString(c.getColumnIndex(DeviceKeys.COLUMN_DEVICEKEYS_NAME)));
                deviceKeys.setKeyIco(c.getString(c.getColumnIndex(DeviceKeys.COLUMN_DEVICEKEYS_ICO)));
                deviceKeys.setKeyCommand(c.getString(c.getColumnIndex(DeviceKeys.COLUMN_DEVICEKEYS_COMMAND)));
                deviceKeys.setKeySort(c.getInt(c.getColumnIndex(DeviceKeys.COLUMN_DEVICEKEYS_SORT)));
                deviceKeys.setKeyWhere(c.getInt(c.getColumnIndex(DeviceKeys.COLUMN_DEVICEKEYS_WHERE)));
                deviceKeys.setKeySState(c.getInt(c.getColumnIndex(DeviceKeys.COLUMN_DEVICEKEYS_SSTATE)) != 0);
                deviceKeyses.add(deviceKeys);
            }
        }
        if (c != null) {
            c.close();
        }
        return deviceKeyses;
    }

    public void insertOrUpdateDeviceKeys(DeviceKeys deviceKeys, long did) {
        if (deviceKeys != null) {
            ContentValues values = new ContentValues();
            values.put(DeviceKeys.COLUMN_DEVICEKEYS_ID, did);
            values.put(DeviceKeys.COLUMN_DEVICEKEYS_NAME, deviceKeys.getKeyName());
            values.put(DeviceKeys.COLUMN_DEVICEKEYS_ICO, deviceKeys.getKeyIco());
            values.put(DeviceKeys.COLUMN_DEVICEKEYS_COMMAND, deviceKeys.getKeyCommand());
            values.put(DeviceKeys.COLUMN_DEVICEKEYS_SORT, deviceKeys.getKeySort());
            values.put(DeviceKeys.COLUMN_DEVICEKEYS_WHERE, deviceKeys.getKeyWhere());
            values.put(DeviceKeys.COLUMN_DEVICEKEYS_SSTATE, deviceKeys.isKeySState() ? 1 : 0);

            Cursor cursor = getReadableDatabase().rawQuery(
                    "select * from devices_key where d_id = ? and key_where = ?",
                    new String[]{String.valueOf(did), String.valueOf(deviceKeys.getKeyWhere())});
            if (cursor != null && cursor.getCount() > 0) {
                getWritableDatabase().update("devices_key", values,
                        "d_id = ? and key_where = ?", new String[]{String.valueOf(did), String.valueOf(deviceKeys.getKeyWhere())});
            } else {
                try {
                    getWritableDatabase().insert("devices_key", null, values);
                } catch (Exception e) {

                }
            }
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void delDeviceKeysById(long did) {
        if (did != 0) {
            _instance.getReadableDatabase().delete("devices_key", "d_id = ?", new String[]{String.valueOf(did)});
        }

    }

    public void clearAllDbData() {
        _instance.getWritableDatabase().delete("USER_INFO", null, null);
        _instance.getWritableDatabase().delete("USER_NOTIFICATION", null, null);
        _instance.getWritableDatabase().delete("ZHUJI_STATUSINFO", null, null);
        _instance.getWritableDatabase().delete("DEVICE_STATUSINFO", null, null);
        _instance.getWritableDatabase().delete("GROUP_STATUSINFO", null, null);
        _instance.getWritableDatabase().delete("GROUP_DEVICE_RELATIOIN", null, null);
        _instance.getWritableDatabase().delete("ZHUJI_GROUP_STATUSINFO", null, null);
        _instance.getWritableDatabase().delete("ZHUJI_GROUP_DEVICE_RELATIOIN", null, null);
        _instance.getWritableDatabase().delete("DEVICE_COMMAND", null, null);
    }
}