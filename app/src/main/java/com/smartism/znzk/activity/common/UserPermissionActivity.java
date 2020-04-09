package com.smartism.znzk.activity.common;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.domain.PersInfo;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.ImageViewCheckable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Created by win7 on 2017/6/20.
 */

public class UserPermissionActivity extends ActivityParentActivity implements View.OnClickListener {
    private static final int GETPERMISSON = 5;
    private static final int SETPERMISSON = 6;


    private LinearLayout ll_add, ll_del;
    private ImageViewCheckable iv_add, iv_del;

    private long did, uid;
    private List<PersInfo> infos;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_devices_permission);
        mContext = this;
        did = getIntent().getLongExtra("did", 0);
        uid = getIntent().getLongExtra("uid", 0);
        initView();
        initData();
    }

    private long add_id, del_id;

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case GETPERMISSON:
                    if (msg.obj == null) {
                        iv_add.setChecked(true);
                        iv_del.setChecked(true);
                        return true;
                    }
                    infos.clear();
                    infos.addAll((Collection<? extends PersInfo>) msg.obj);
                    if (infos == null || infos.size() == 0)
                        return true;
                    for (PersInfo info : infos) {
                        if (PersInfo.UserPerssionKey.p_add.value().equals(info.getK())) {
                            add_id = info.getId();
                            iv_add.setChecked(info.getV().equals("1") ? true : false);
                        } else if (PersInfo.UserPerssionKey.p_del.value().equals(info.getK())) {
                            iv_del.setChecked(info.getV().equals("1") ? true : false);
                            del_id = info.getId();
                        }
                    }
                    if (add_id == 0 || del_id == 0)
                        finish();
                    break;
            }
            return false;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);

    private void initData() {
        showInProgress(getString(R.string.loading), false, true);
        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                String l = Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry();
                DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(
                        UserPermissionActivity.this, DataCenterSharedPreferences.Constant.CONFIG);
                String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("did", did);
                pJsonObject.put("uid", uid);
                final String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/d/u/permission", pJsonObject, UserPermissionActivity.this);
                List<PersInfo> infos = new ArrayList<PersInfo>();
                // 0删除成功 -1参数为空-2校验失败，-3无此设备 -4 无此权限-5无此用户
                if (result != null && result.startsWith("[")) {
                    cancelInProgress();
                    try {
                        infos = JSONObject.parseArray(result, PersInfo.class);
                        if (result.equals("[]")) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    iv_add.setChecked(true);
                                    iv_del.setChecked(true);
                                }
                            });
                            return;
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("UserPermissonInfo Json exception", "error");
                    }

                } else if ("-3".equals(result)) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(UserPermissionActivity.this,
                                    getString(R.string.device_set_tip_nodevice), Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });
                } else if ("-4".equals(result)) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(UserPermissionActivity.this,
                                    getString(R.string.reset_password_noaccounthave), Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });
                } else if ("-5".equals(result)) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(UserPermissionActivity.this,
                                    getString(R.string.login_request_no_user), Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });
                } else {
                    cancelInProgress();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(UserPermissionActivity.this,
                                    getString(R.string.net_error), Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });
                }

                Message message = Message.obtain();
                message.what = GETPERMISSON;
                message.obj = infos;
                mHandler.sendMessage(message);

            }
        });
    }

    private void initView() {
        ll_add = (LinearLayout) findViewById(R.id.ll_add);
        ll_del = (LinearLayout) findViewById(R.id.ll_del);
        iv_add = (ImageViewCheckable) findViewById(R.id.iv_add);
        iv_del = (ImageViewCheckable) findViewById(R.id.iv_del);
        ll_add.setOnClickListener(this);
        ll_del.setOnClickListener(this);
        iv_add.setChecked(true);
        iv_del.setChecked(true);
        infos = new ArrayList<>();
    }

    public void sure(View v) {
        boolean isAdd = iv_add.isChecked();
        boolean isDel = iv_del.isChecked();
        setPermisson(isAdd, isDel);
    }

    private void setPermisson(final boolean isAdd, final boolean isDel) {
        showInProgress(getString(R.string.loading), false, true);
        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                String l = Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry();
                DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(
                        UserPermissionActivity.this, DataCenterSharedPreferences.Constant.CONFIG);
                String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("did", did);
                pJsonObject.put("uid", uid);
                JSONArray array = new JSONArray();
                JSONObject o = new JSONObject();
                o.put("v", isAdd ? 1 : 0);
                o.put("k", "p_cd_add");
                o.put("n", "add_device");
                o.put("id", add_id);

                JSONObject o1 = new JSONObject();
                o1.put("v", isDel ? 1 : 0);
                o1.put("k", "p_cd_del");
                o1.put("n", "del_device");
                o1.put("id", del_id);
                array.add(o);
                array.add(o1);
                pJsonObject.put("ps", array);
                Log.d(TAG, "u/permission: " + pJsonObject.toJSONString());
                String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/d/u/upermission", pJsonObject, UserPermissionActivity.this);
                List<PersInfo> infos = new ArrayList<PersInfo>();
                // 0删除成功 -1参数为空-2校验失败，-3无此设备 -4 无此权限-5无此用户
                if ("0".equals(result)) {
                    cancelInProgress();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            iv_del.setChecked(isDel ? true : false);
                            iv_add.setChecked(isAdd ? true : false);
                            Toast.makeText(UserPermissionActivity.this,
                                    getString(R.string.success), Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });

                } else if ("-3".equals(result)) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(UserPermissionActivity.this,
                                    getString(R.string.device_set_tip_nodevice), Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-4".equals(result)) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(UserPermissionActivity.this,
                                    getString(R.string.device_not_permission), Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-5".equals(result)) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(UserPermissionActivity.this,
                                    getString(R.string.login_request_no_user), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(UserPermissionActivity.this,
                                    getString(R.string.net_error), Toast.LENGTH_LONG).show();
                            iv_add.setChecked(true);
                            iv_del.setChecked(true);
                        }
                    });
                }
            }
        });
    }

    public void back(View v) {
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_add:
                iv_add.toggle();
                break;
            case R.id.ll_del:
                iv_del.toggle();
                break;
        }
    }
}
