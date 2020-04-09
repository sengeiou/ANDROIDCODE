package com.smartism.znzk.activity.device;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.common.UserPermissionActivity;
import com.smartism.znzk.activity.device.share.ShareDevicesActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.DeviceUserInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.CircleImageView;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by win7 on 2017/8/26.
 */

public class PerminssonTransActivity extends ActivityParentActivity implements View.OnClickListener {

    private static final String TAG = PerminssonTransActivity.class.getSimpleName();
    private RecyclerView recyclerView;
    private List<DeviceUserInfo> userInfos;
    private ImageView iv_share;
    private Context mContext;
    private ZhujiInfo zhujiInfo;
    private DeviceInfo deviceInfo;

    private MyAdapter adapter;
    private TextView tv_no_user;
    private int itemPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trans_perssion);
        mContext = this;
        deviceInfo = (DeviceInfo) getIntent().getSerializableExtra("device");
        initView();
        initData();
    }

    private void initData() {
        popupWindow = new FootPopupWindow(this, this);
        userInfos = new ArrayList<>();
        adapter = new MyAdapter();
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(adapter);
    }

    private void initView() {
        relativeLayout = (RelativeLayout) findViewById(R.id.linearLayout);
        recyclerView = (RecyclerView) findViewById(R.id.recyle);
        tv_no_user = (TextView) findViewById(R.id.tv_no_user);
        iv_share = (ImageView) findViewById(R.id.iv_share);
        iv_share.setOnClickListener(this);
        if (deviceInfo.isFlag()) {
            iv_share.setVisibility(View.VISIBLE);
        }
    }

    public void back(View v) {
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                if (deviceInfo != null) {
                    zhujiInfo = DatabaseOperator.getInstance(mContext).queryDeviceZhuJiInfo(deviceInfo.getId());
                } else {
//                    String masterID = dcsp.getString(DataCenterSharedPreferences.Constant.APP_MASTERID, "");
                    //替换
                    String masterID = ZhujiListFragment.getMasterId();
                    zhujiInfo = DatabaseOperator.getInstance(mContext)
                            .queryDeviceZhuJiInfo(masterID);
                }
            }
        });
        getAllUser();
    }


    public final int TIME_OUT = 5;
    public final int LONG_TIME = 10 * 1000;

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 10:
                    if (defaultHandler.hasMessages(TIME_OUT)) {
                        defaultHandler.removeMessages(TIME_OUT);
                    }
                    cancelInProgress();
                    userInfos.clear();
                    userInfos.addAll((List<DeviceUserInfo>) msg.obj);
                    adapter.notifyDataSetChanged();
                    break;
                case TIME_OUT:
                    cancelInProgress();
                    Toast.makeText(getApplicationContext(), getString(R.string.timeout), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    private Handler defaultHandler = new WeakRefHandler(mCallback);

    private void getAllUser() {
        showInProgress(getString(R.string.loading), false, true);
        defaultHandler.sendEmptyMessageDelayed(TIME_OUT, LONG_TIME);
        JavaThreadPool.getInstance().excute(new Runnable() {

            @Override
            public void run() {
                DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(mContext,
                        Constant.CONFIG);
                String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("did", zhujiInfo != null ? zhujiInfo.getId() : 0);
                String result = "";
                result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/d/u/all", pJsonObject, PerminssonTransActivity.this);
                if ("-3".equals(result)) {
                    if (defaultHandler.hasMessages(TIME_OUT)) {
                        defaultHandler.removeMessages(TIME_OUT);
                    }
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(mContext, getString(R.string.device_set_tip_nodevice),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-5".equals(result)) {
                    if (defaultHandler.hasMessages(TIME_OUT)) {
                        defaultHandler.removeMessages(TIME_OUT);
                    }
                    defaultHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(mContext, getString(R.string.device_not_getdata),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-100".endsWith(result)){
                    if (defaultHandler.hasMessages(TIME_OUT)) {
                        defaultHandler.removeMessages(TIME_OUT);
                    }
                    defaultHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(mContext, getString(R.string.net_error),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }else if (!StringUtils.isEmpty(result)) {
                    Log.d(TAG,"userResult:"+result);
                    if (defaultHandler.hasMessages(TIME_OUT)) {
                        defaultHandler.removeMessages(TIME_OUT);
                    }
                    List<DeviceUserInfo> userInfos = JSON.parseArray(result, DeviceUserInfo.class);
//                    for (int i = 0; i < userInfos.size(); i++) {
//                        if (userInfos.get(i).getName() != null && userInfos.get(i).getName().equals(dcsp.getString(Constant.LOGIN_APPNAME, ""))) {
//                            userInfos.remove(userInfos.get(i));
//                        }
//                    }
                    if (userInfos.size() == 0) {
                        defaultHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                cancelInProgress();
                                tv_no_user.setVisibility(View.VISIBLE);
                                return;
                            }
                        });
                    }
                    Message msg = Message.obtain();
                    msg.what = 10;
                    msg.obj = userInfos;
                    defaultHandler.sendMessage(msg);
                } else {
                    if (defaultHandler.hasMessages(TIME_OUT)) {
                        defaultHandler.removeMessages(TIME_OUT);
                    }
                    defaultHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(mContext, getString(R.string.net_error),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        });
    }


    private FootPopupWindow popupWindow;
    private RelativeLayout relativeLayout;

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.iv_share:
                intent.putExtra("pattern", "status_forver");
                intent.putExtra("shareid", deviceInfo.getId());
                intent.setClass(getApplicationContext(), ShareDevicesActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_permission:
                popupWindow.dismiss();
                intent.setClass(this, UserPermissionActivity.class);
                intent.putExtra("did", zhujiInfo.getId());
                intent.putExtra("uid", userInfos.get(itemPosition).getId());
                startActivity(intent);
                break;
            case R.id.btn_deldevice:
                popupWindow.dismiss();
                showInProgress(getString(R.string.loading), false, true);
                JavaThreadPool.getInstance().excute(new Runnable() {
                    @Override
                    public void run() {
                        long did = zhujiInfo.getId();
                        long duid = userInfos.get(itemPosition).getId();
                        String l = Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry();
                        DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(
                                PerminssonTransActivity.this, Constant.CONFIG);
                        String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
                        JSONObject pJsonObject = new JSONObject();
                        pJsonObject.put("did", did);
                        pJsonObject.put("duid", duid);
                        String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/d/u/del", pJsonObject, PerminssonTransActivity.this);

                        // 0删除成功 -1参数为空-2校验失败，-3无此设备 -4 无此权限-5无此用户
                        if ("0".equals(result)) {
                            defaultHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    cancelInProgress();

                                    Toast.makeText(PerminssonTransActivity.this,
                                            getString(R.string.device_del_success), Toast.LENGTH_LONG).show();
                                    userInfos.remove(itemPosition);
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        } else if ("-3".equals(result)) {
                            defaultHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast.makeText(PerminssonTransActivity.this,
                                            getString(R.string.device_set_tip_nodevice), Toast.LENGTH_LONG).show();
                                }
                            });
                        } else if ("-4".equals(result)) {
                            defaultHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast.makeText(PerminssonTransActivity.this,
                                            getString(R.string.device_not_permission), Toast.LENGTH_LONG).show();
                                }
                            });
                        } else if ("-5".equals(result)) {
                            defaultHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast.makeText(PerminssonTransActivity.this,
                                            getString(R.string.login_request_no_user), Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                    }
                });
                break;
            case R.id.btn_setdevice:
                popupWindow.dismiss();
                showInProgress(getString(R.string.loading), false, true);
                JavaThreadPool.getInstance().excute(new Runnable() {

                    @Override
                    public void run() {
                        DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(mContext,
                                Constant.CONFIG);
                        String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
                        JSONObject pJsonObject = new JSONObject();
                        pJsonObject.put("did", zhujiInfo != null ? zhujiInfo.getId() : 0);
                        pJsonObject.put("duid", userInfos.get(itemPosition).getId());
                        String result = "";
                        result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/d/u/transfer", pJsonObject, PerminssonTransActivity.this);
                        if ("0".equals(result)) {
                            defaultHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast.makeText(mContext, getString(R.string.success),
                                            Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            });
                        } else if ("-3".equals(result)) {
                            defaultHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast.makeText(mContext, getString(R.string.device_set_tip_nodevice),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        } else if ("-4".equals(result)) {
                            defaultHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast.makeText(mContext, getString(R.string.device_not_permission),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        } else if ("-5".equals(result)) {
                            defaultHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast.makeText(mContext, getString(R.string.login_request_no_user),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        }else if("-6".equals(result)){
                            defaultHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast.makeText(mContext,getString(R.string.login_request_no_keys),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        }else if("-7".equals(result)){
                            defaultHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast.makeText(mContext,getString(R.string.login_request_temp_keys),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        }else {
                            defaultHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast.makeText(mContext, getString(R.string.net_error),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                    }
                });
                break;
        }
    }

    public class FootPopupWindow extends PopupWindow {

        private View mMenuView;
        private TextView btn_deldevice, btn_setdevice;
        private LinearLayout ll_permission;

        public FootPopupWindow(Context context, View.OnClickListener itemsOnClick) {
            super(context);
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mMenuView = inflater.inflate(R.layout.device_detail_user_item_menu, null);
            btn_deldevice = (TextView) mMenuView.findViewById(R.id.btn_deldevice);
            btn_setdevice = (TextView) mMenuView.findViewById(R.id.btn_setdevice);
            ll_permission = (LinearLayout) mMenuView.findViewById(R.id.ll_permission);
            btn_deldevice.setOnClickListener(itemsOnClick);
            btn_setdevice.setOnClickListener(itemsOnClick);
            ll_permission.setOnClickListener(itemsOnClick);
            //设置SelectPicPopupWindow的View
            this.setContentView(mMenuView);
            //设置SelectPicPopupWindow弹出窗体的宽
            this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            //设置SelectPicPopupWindow弹出窗体的高
            this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            //设置SelectPicPopupWindow弹出窗体可点击
            this.setFocusable(true);
            //设置SelectPicPopupWindow弹出窗体动画效果
            this.setAnimationStyle(R.style.Devices_list_menu_Animation);
            //实例化一个ColorDrawable颜色为半透明
            ColorDrawable dw = new ColorDrawable(0x00000000);
            //设置SelectPicPopupWindow弹出窗体的背景
            this.setBackgroundDrawable(dw);
            //mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
            mMenuView.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {

                    int height = mMenuView.findViewById(R.id.pop_layout).getTop();
                    int y = (int) event.getY();
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (y < height) {
                            dismiss();
                        }
                    }
                    return true;
                }
            });
            this.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    WindowManager.LayoutParams parms = getWindow().getAttributes();
                    parms.alpha = 1.0f;
                    getWindow().setAttributes(parms);

                }
            });

        }


        public void updateDeviceMenu(Context context) {
            if (MainApplication.app.getAppGlobalConfig().isShowDevicesPermisson() && DeviceInfo.CaMenu.zhuji.value().equals(zhujiInfo.getCa())) {
                ll_permission.setVisibility(View.VISIBLE);
            }
            btn_setdevice.setText(context.getResources().getString(R.string.devices_list_menu_dialog_admin_trans));
            btn_deldevice.setText(context.getResources().getString(R.string.zss_item_del));
        }

    }

    // logo图片的配置
    DisplayImageOptions options_userlogo = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.loading)
            .showImageOnFail(R.drawable.sorrow).cacheInMemory(true).cacheOnDisc(true)
            .bitmapConfig(Bitmap.Config.RGB_565).imageScaleType(ImageScaleType.EXACTLY_STRETCHED)// 设置图片以如何的编码方式显示
            .resetViewBeforeLoading(true)// 设置图片在下载前是否重置，复位
            .displayer(new RoundedBitmapDisplayer(40))// 是否设置为圆角，弧度为多少
            .displayer(new FadeInBitmapDisplayer(100))// 是否图片加载好后渐入的动画时间
            .build();

    public class MyAdapter extends RecyclerView.Adapter<MyHolder> {

        @Override
        public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_trans, null);
            MyHolder holder = new MyHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(MyHolder holder, int position) {
            DeviceUserInfo userInfo = userInfos.get(position);
            holder.tv_name.setText(userInfo.getName());
            if (userInfos.get(position).getAdmin() == 1) {
                holder.tv_admin.setVisibility(View.VISIBLE);
            } else {
                holder.tv_admin.setVisibility(View.GONE);
            }

            if (TextUtils.isEmpty(userInfo.getLogo())) {
                holder.iv_circle.setImageResource(R.drawable.h0);
            } else {
                ImageLoader.getInstance().displayImage(userInfo.getLogo(), holder.iv_circle,
                        options_userlogo);
            }
            if (!userInfo.getOnline()) {
                holder.tv_admin.setTextColor(getResources().getColor(R.color.graysloae));
                holder.tv_status.setText(getString(R.string.deviceslist_server_zhuji_offline));
                holder.tv_status.setTextColor(getResources().getColor(R.color.graysloae));
            } else {
                holder.tv_admin.setTextColor(getResources().getColor(R.color.zhzj_default));
                holder.tv_status.setTextColor(getResources().getColor(R.color.zhzj_default));
                holder.tv_status.setText(getString(R.string.deviceslist_server_zhuji_online));
            }
            setAdmin(holder.itemView, position);

        }

        private void setAdmin(View view, final int i) {

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!zhujiInfo.isAdmin() || userInfos.get(i).getAdmin() == 1)
                        return true;
                    itemPosition = i;
                    popupWindow.updateDeviceMenu(mContext);
                    popupWindow.showAtLocation(relativeLayout, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置
                    WindowManager.LayoutParams parms = getWindow().getAttributes();
                    parms.alpha = 0.7f;
                    getWindow().setAttributes(parms);
                    return false;
                }
            });


        }

        @Override
        public int getItemCount() {
            return userInfos.size();
        }
    }

    private class MyHolder extends RecyclerView.ViewHolder {
        TextView tv_name, tv_admin, tv_status;
        View view;
        CircleImageView iv_circle;

        public MyHolder(View itemView) {
            super(itemView);
            tv_name = (TextView) itemView.findViewById(R.id.tv_name);
            tv_admin = (TextView) itemView.findViewById(R.id.tv_admin);
            tv_status = (TextView) itemView.findViewById(R.id.tv_status);
            iv_circle = (CircleImageView) itemView.findViewById(R.id.iv_circle);
            view = itemView;
        }

    }
}
