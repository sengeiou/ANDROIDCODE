package com.smartism.znzk.activity.device;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lsemtmf.genersdk.tools.commen.ToastTools;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.DeviceInfo.ControlTypeMenu;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.*;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.view.ContainsEmojiEditText;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class DeviceSetActivity extends ActivityParentActivity {
    private EditText  where, set_num_edit;
    private ContainsEmojiEditText name;
    private TextView tvname;
    private LinearLayout lay_where, ll_security_device;
    private DeviceInfo operationDevice;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:  //初始化加载完成
                    JSONObject resultBack = (JSONObject) msg.obj;
                    if (resultBack.get("name") != null) {
                        name.setText(resultBack.getString("name"));
                    }
                    if (resultBack.get("where") != null) {
                        where.setText(resultBack.getString("where"));
                    }
                    if (resultBack.get("number") != null){
                        set_num_edit.setText(resultBack.getString("number"));
                    }
                    cancelInProgress();
                    break;
                case 10: // 修改完成
                    cancelInProgress();
                    sendBroadcast(new Intent(Actions.REFRESH_DEVICES_LIST));
                    Toast.makeText(DeviceSetActivity.this, getString(R.string.device_set_tip_success), Toast.LENGTH_LONG).show();
                    //请求刷新列表
                    SyncMessageContainer.getInstance()
                            .produceSendMessage(new SyncMessage(SyncMessage.CommandMenu.rq_refresh));
                    finish();
                    break;
            }
            return false;
        }
    };
    private Handler defaultHandler = new WeakRefHandler(mCallback);

    /* (non-Javadoc)
     * @see com.znwx.jiadianmao.activity.ActivityParentActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_set);
//        zhuji = DatabaseOperator.getInstance(DeviceSetActivity.this)
//                .queryDeviceZhuJiInfo(dcsp.getString(Constant.APP_MASTERID, ""));
        //替换
        zhuji = DatabaseOperator.getInstance(DeviceSetActivity.this).queryDeviceZhuJiInfo(ZhujiListFragment.getMasterId());
        name = (ContainsEmojiEditText) findViewById(R.id.set_name_edit);
        where = (EditText) findViewById(R.id.set_where_edit);
        lay_where = (LinearLayout) findViewById(R.id.layout_where);
        ll_security_device = (LinearLayout) findViewById(R.id.ll_security_device);

        set_num_edit = (EditText) findViewById(R.id.set_num_edit);

        tvname = (TextView) findViewById(R.id.set_name_tv);
        operationDevice = (DeviceInfo) getIntent().getSerializableExtra("device");

        if(DeviceInfo.CaMenu.nbyg.value().equals(operationDevice.getCa())){
            tvname.setText(getResources().getString(R.string.activity_nbyg_set_name));
        }else if (DeviceInfo.ControlTypeMenu.zhuji.value().equals(operationDevice.getControlType())) {
            tvname.setText(getResources().getString(R.string.activity_zhuji_set_name));
        }

        if (operationDevice == null) {
            Toast.makeText(DeviceSetActivity.this, getString(R.string.device_set_tip_nopro), Toast.LENGTH_LONG).show();
            finish();
        }
        if (zhuji != null && zhuji.isAdmin() &&  zhuji.getAc() > 0 && DeviceInfo.CakMenu.security.value().equals(operationDevice.getCak())) {
            ll_security_device.setVisibility(View.VISIBLE);
        }
        showInProgress(getString(R.string.loading), false, true);
        if (ControlTypeMenu.group.value().equals(operationDevice.getControlType())) {
            tvname.setText(getResources().getString(R.string.activity_group_set_name));
            lay_where.setVisibility(View.GONE);
            JavaThreadPool.getInstance().excute(new loadAllDevicesInfo());
        } else {
            lay_where.setVisibility(View.VISIBLE);
            JavaThreadPool.getInstance().excute(new InitDeviceInfoThread());
        }
    }

    public void back(View v) {
        finish();
    }

    public void subToUpdate(View v) {
        if(TextUtils.isEmpty(name.getText().toString())){
            ToastUtil.shortMessage(getString(R.string.zhuji_owner_msg_name));
            return ;
        }
        showInProgress(getString(R.string.device_set_tip_inupdate), false, true);
        if (ControlTypeMenu.group.value().equals(operationDevice.getControlType())) {
            JavaThreadPool.getInstance().excute(new UpdateGroupInfoThread());
        } else {
            JavaThreadPool.getInstance().excute(new UpdateInfoThread());
        }
    }

    protected void onDestroy() {
        defaultHandler.removeCallbacksAndMessages(null);
        defaultHandler = null;
        super.onDestroy();
    }

    ;

    class UpdateGroupInfoThread implements Runnable {
        @Override
        public void run() {
//            zhuji = DatabaseOperator.getInstance(DeviceSetActivity.this)
//                    .queryDeviceZhuJiInfo(dcsp.getString(Constant.APP_MASTERID, ""));
            //替换
            zhuji = DatabaseOperator.getInstance(DeviceSetActivity.this).queryDeviceZhuJiInfo(ZhujiListFragment.getMasterId());
            String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();

            object.put("id", operationDevice.getId());
            object.put("logo", operationDevice.getLogo());
            object.put("masterid", zhuji.getMasterid());
            if (!name.getText().toString().equals("")) {
                object.put("name", name.getText().toString());
            } else {
                object.put("name", operationDevice.getName());
            }

            JSONArray array = new JSONArray();
            for (DeviceInfo d : deviceList) {
                JSONObject o = new JSONObject();
                o.put("id", d.getId());
                array.add(o);
            }
            object.put("ids", array);
            if (!name.getText().toString().equals("")) {
                object.put("name", name.getText().toString());
            }
            if (!where.getText().toString().equals("")) {
                object.put("where", where.getText().toString());
            }
            String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/dg/update", object, DeviceSetActivity.this);

            if ("-3".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(DeviceSetActivity.this, getString(R.string.net_error_illegal_request), Toast.LENGTH_LONG).show();
                        LogUtil.e(DeviceSetActivity.this, TAG, "非法的http请求，id+code校验失败，触发端为android");
                    }
                });
            } else if ("0".equals(result)) {
                defaultHandler.sendEmptyMessage(10);
            }
            /*JSONObject object = new JSONObject();

			object.put("uid",dcsp.getLong(Constant.LOGIN_APPID, 0));
			object.put("code",dcsp.getString(Constant.LOGIN_CODE,""));
			object.put("id", operationDevice.getId());
			object.put("logo",operationDevice.getLogo());
			object.put("masterid",zhuji.getMasterid());
			if(!name.getText().toString().equals("")){
				object.put("name", name.getText().toString());
			}else{
				object.put("name",operationDevice.getName());
			}

			JSONArray array = new JSONArray();
				for (DeviceInfo d : deviceList) {
					JSONObject o = new JSONObject();
					o.put("id", d.getId());
					array.add(o);
				}
			object.put("ids",array);
			if(!name.getText().toString().equals("")){
				object.put("name", name.getText().toString());
			}
			if(!where.getText().toString().equals("")){
				object.put("where", where.getText().toString());
			}
			String result = null;
			try {
				result = HttpRequestUtils.requestHttpServer(server+"/jdm/service/dg/update?v="+URLEncoder.encode(SecurityUtil.crypt(object.toJSONString(), Constant.KEY_HTTP), "UTF-8"),DeviceSetActivity.this,defaultHandler);
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "不支持UTF-8格式");
			}
			if("-1".equals(result)){
				defaultHandler.post(new Runnable() {

					@Override
					public void run() {
						cancelInProgress();
						Toast.makeText(DeviceSetActivity.this, getString(R.string.net_error_programs), Toast.LENGTH_LONG).show();
					}
				});
			}else if("-2".equals(result)){
				defaultHandler.post(new Runnable() {

					@Override
					public void run() {
						cancelInProgress();
						Toast.makeText(DeviceSetActivity.this, getString(R.string.device_set_tip_nodevice), Toast.LENGTH_LONG).show();
					}
				});
			}else if("-3".equals(result)){
				defaultHandler.post(new Runnable() {

					@Override
					public void run() {
						cancelInProgress();
						Toast.makeText(DeviceSetActivity.this, getString(R.string.net_error_illegal_request), Toast.LENGTH_LONG).show();
						LogUtil.e(DeviceSetActivity.this,TAG,"非法的http请求，id+code校验失败，触发端为android");
					}
				});
			}else if("0".equals(result)){

				defaultHandler.sendEmptyMessage(10);
			}*/
        }

        private void updateDBDate(JSONObject resultBack) {
            if (ControlTypeMenu.zhuji.value().equals(operationDevice.getControlType())) {
                ContentValues values = new ContentValues();
                if (resultBack.get("deviceName") != null) {
                    values.put("name", resultBack.getString("deviceName"));
                } else {
                    values.put("name", "");
                }
                if (resultBack.get("deviceWhere") != null) {
                    values.put("dwhere", resultBack.getString("deviceWhere"));
                } else {
                    values.put("dwhere", "");
                }
                try {
                    DatabaseOperator.getInstance().getWritableDatabase().update("ZHUJI_STATUSINFO", values, "id = ?", new String[]{String.valueOf(resultBack.getLongValue("deviceId"))});
                } catch (Exception e) {
                    Log.e(TAG, "获取数据库失败");
                }
            } else {
                ContentValues values = new ContentValues();
                if (resultBack.get("deviceName") != null) {
                    values.put("device_name", resultBack.getString("deviceName"));
                } else {
                    values.put("device_name", "");
                }
                if (resultBack.get("deviceWhere") != null) {
                    values.put("device_where", resultBack.getString("deviceWhere"));
                } else {
                    values.put("device_where", "");
                }
                try {
                    DatabaseOperator.getInstance().getWritableDatabase().update("DEVICE_STATUSINFO", values, "id = ?", new String[]{String.valueOf(resultBack.getLongValue("deviceId"))});
                } catch (Exception e) {
                    Log.e(TAG, "获取数据库失败");
                }
            }
        }
    }

    ZhujiInfo zhuji;
    private int sortType = 0; // 主页面排序类型 0为智能类型， 1为排序类型
    List<DeviceInfo> deviceList = new ArrayList<DeviceInfo>();

    class loadAllDevicesInfo implements Runnable {
        private int what;

        public loadAllDevicesInfo() {
        }

        public loadAllDevicesInfo(int what) {
            this.what = what;
        }

        @Override
        public void run() {
//            zhuji = DatabaseOperator.getInstance(DeviceSetActivity.this)
//                    .queryDeviceZhuJiInfo(dcsp.getString(Constant.APP_MASTERID, ""));
            //替换
            zhuji = DatabaseOperator.getInstance(DeviceSetActivity.this).queryDeviceZhuJiInfo(ZhujiListFragment.getMasterId());
            if (zhuji == null) {
                return;
            }
            DeviceInfo shexiangtou = null;

            List<DeviceInfo> deviceList_close = new ArrayList<DeviceInfo>();
            if (zhuji != null) {
                sortType = dcsp.getString(Constant.SHOW_DLISTSORT, "zhineng").equals("zhineng") ? 0 : 1;
                String ordersql = "";
                if (sortType == 0) {
                    ordersql = "order by d.device_lasttime desc";
                } else {
                    ordersql = "order by d.sort desc";
                }
                int totalNr = 0;
                Cursor cursor = DatabaseOperator.getInstance(DeviceSetActivity.this).getReadableDatabase().rawQuery(
                        "SELECT d.* FROM DEVICE_STATUSINFO d LEFT JOIN GROUP_DEVICE_RELATIOIN r ON d.id = r.did WHERE r.gid = ? " + ordersql,
                        new String[]{String.valueOf(operationDevice.getId())});
                if (cursor != null && cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        DeviceInfo deviceInfo = DatabaseOperator.getInstance(DeviceSetActivity.this)
                                .buildDeviceInfo(cursor);
                        if ("shexiangtou".equals(deviceInfo.getControlType())) {
                            shexiangtou = deviceInfo;
                            if (shexiangtou.getIpc() != null) {
                                JSONArray array = JSONArray.parseArray(shexiangtou.getIpc());
                                if (array != null) {
                                    shexiangtou.setStatus(0); //显示指令
                                    shexiangtou.setLastCommand(array.size() + getString(R.string.deviceslist_camera_count));//显示摄像头个数
                                }
                            }
                            continue;
                        }
                        if (sortType == 0) {
                            if (deviceInfo.getAcceptMessage() == 0) {
                                deviceList_close.add(deviceInfo);
                            } else {
                                deviceList.add(deviceInfo);
                            }
                        } else {
                            deviceList.add(deviceInfo);
                        }
                    }
                    //摄像头必须放在遥控器定住的逻辑前面不然会出现崩溃
                    if (shexiangtou != null) {
                        deviceList.add(1, shexiangtou);
                    }
                    //使操作的遥控器定住
                    if (operationDevice != null
                            && operationDevice.getControlType().contains(ControlTypeMenu.xiaxing.value())) {
                        for (int k = 0; k < deviceList.size(); k++) {
                            if (operationDevice.getId() == deviceList.get(k).getId()
                                    && deviceList.size() >= operationDevice.getwIndex()
                                    && operationDevice.getwIndex() != -1) {
                                DeviceInfo opDevice = deviceList.get(k);
                                deviceList.remove(k);
                                deviceList.add(operationDevice.getwIndex(), opDevice);
                            }
                        }
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                deviceList.addAll(deviceList_close);
            }
            defaultHandler.post(new Runnable() {

                @Override
                public void run() {
                    cancelInProgress();
                }
            });
        }
    }

    class UpdateInfoThread implements Runnable {
        @Override
        public void run() {
            String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("id", operationDevice.getId());
            if (!name.getText().toString().equals("")) {
                object.put("name", name.getText().toString());
            }
            if (!where.getText().toString().equals("")) {
                object.put("where", where.getText().toString());
            }


            String secuirt = set_num_edit.getText().toString();
            object.put("number", secuirt);

            String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/d/uinfo", object, DeviceSetActivity.this);
//					String result = null;
//					try {
//						result = HttpRequestUtils.requestHttpServer(server+"/jdm/service/updateD?v="+URLEncoder.encode(SecurityUtil.crypt(object.toJSONString(), Constant.KEY_HTTP), "UTF-8"),DeviceSetActivity.this,defaultHandler);
//					} catch (UnsupportedEncodingException e) {
//						Log.e(TAG, "不支持UTF-8格式");
//					}
            if ("-3".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(DeviceSetActivity.this, getString(R.string.device_set_tip_nodevice), Toast.LENGTH_LONG).show();
                    }
                });
            } else if (!StringUtils.isEmpty(result)) {
                JSONObject resultBack = null;
                try {
                    resultBack = JSON.parseObject(result);
                } catch (Exception e) {
                    LogUtil.e(getApplicationContext(), TAG, "解密错误：：", e);
                }
                if (resultBack == null) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(DeviceSetActivity.this, getString(R.string.device_set_tip_responseerr), Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
                updateDBDate(resultBack);

                defaultHandler.sendEmptyMessage(10);
            }
        }

        private void updateDBDate(JSONObject resultBack) {
            if (ControlTypeMenu.zhuji.value().equals(operationDevice.getControlType())) {
                ContentValues values = new ContentValues();
                if (resultBack.get("deviceName") != null) {
                    values.put("name", resultBack.getString("deviceName"));
                } else {
                    values.put("name", "");
                }
                if (resultBack.get("deviceWhere") != null) {
                    values.put("dwhere", resultBack.getString("deviceWhere"));
                } else {
                    values.put("dwhere", "");
                }
                try {
                    DatabaseOperator.getInstance().getWritableDatabase().update("ZHUJI_STATUSINFO", values, "id = ?", new String[]{String.valueOf(resultBack.getLongValue("deviceId"))});
                } catch (Exception e) {
                    Log.e(TAG, "获取数据库失败");
                }
                if (StringUtils.isEmpty(values.getAsString("name"))){ //名字为空，需要发一下107刷新出默认的名字
                    SyncMessageContainer.getInstance()
                            .produceSendMessage(new SyncMessage(SyncMessage.CommandMenu.rq_refresh));
                }
            } else {
                ContentValues values = new ContentValues();
                if (resultBack.get("deviceName") != null) {
                    values.put("device_name", resultBack.getString("deviceName"));
                } else {
                    values.put("device_name", "");
                }
                if (resultBack.get("deviceWhere") != null) {
                    values.put("device_where", resultBack.getString("deviceWhere"));
                } else {
                    values.put("device_where", "");
                }
                try {
                    DatabaseOperator.getInstance().getWritableDatabase().update("DEVICE_STATUSINFO", values, "id = ?", new String[]{String.valueOf(resultBack.getLongValue("deviceId"))});
                } catch (Exception e) {
                    Log.e(TAG, "获取数据库失败");
                }
            }
        }
    }

    class InitDeviceInfoThread implements Runnable {
        @Override
        public void run() {
            String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("id", operationDevice.getId());
//			object.put("appid", dcsp.getLong(Constant.LOGIN_APPID, 0));
//			object.put("uid",dcsp.getLong(Constant.LOGIN_APPID, 0));
//			object.put("code",dcsp.getString(Constant.LOGIN_CODE,""));
            String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/d/info", object, DeviceSetActivity.this);
//			String result = null;
//			try {
//				result = HttpRequestUtils.requestHttpServer(server+"/jdm/service/initSet?v="+URLEncoder.encode(SecurityUtil.crypt(object.toJSONString(), Constant.KEY_HTTP), "UTF-8"),DeviceSetActivity.this,defaultHandler);
//			} catch (UnsupportedEncodingException e) {
//				Log.e(TAG, "不支持UTF-8格式");
//			}
            if ("-3".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(DeviceSetActivity.this, getString(R.string.device_set_tip_nodevice), Toast.LENGTH_LONG).show();
                    }
                });
            } else if (!StringUtils.isEmpty(result) && result.length() > 4) {
                JSONObject resultBack = null;
                try {
                    resultBack = JSON.parseObject(result);
                } catch (Exception e) {
                    LogUtil.e(getApplicationContext(), TAG, "解密错误：：", e);
                    return;
                }
                if (resultBack == null) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(DeviceSetActivity.this, getString(R.string.device_set_tip_responseerr), Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
                Message m = defaultHandler.obtainMessage(1);
                m.obj = resultBack;
                defaultHandler.sendMessage(m);
            }else{
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                    }
                });
            }
        }
    }
}
