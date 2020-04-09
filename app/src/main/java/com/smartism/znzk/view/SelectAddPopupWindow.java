package com.smartism.znzk.view;


import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;

import com.smartism.znzk.R;
import com.smartism.znzk.activity.device.DeviceInfoActivity;
import com.smartism.znzk.adapter.ExperAdapter;
import com.smartism.znzk.adapter.ZhujiAdapter;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.CollectionsUtils;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.Util;

import java.util.ArrayList;
import java.util.List;


/**
 * 显示新增菜单的poputwindow
 *
 * @author 王建
 *         2015年9月30日
 */
public class SelectAddPopupWindow extends PopupWindow {

    private Context context;
    private ZhujiAdapter adapter;
    private ExperAdapter mAadapter;
    private List<ZhujiInfo> zhujiInfos;
    private ListView listViwe;
    private View mMenuView;
    private View pop_addzhuji, pop_addgroup, pop_addy, pop_showss, pop_showss_temp, pop_addyx, pop_addt, pop_adddevice, pop_addfromfactory, pop_devetc, pop_camera, pop_vcamera, pop_exper;

    public SelectAddPopupWindow(Activity context, OnClickListener itemsOnClick) {
        super(context);
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.activity_devices_list_rightmenu, null);
        pop_addzhuji = mMenuView.findViewById(R.id.pop_addzhuji);
        pop_adddevice = mMenuView.findViewById(R.id.pop_adddevice);
        pop_addgroup = mMenuView.findViewById(R.id.pop_adddgroup);
        pop_showss = mMenuView.findViewById(R.id.pop_showss);
        pop_showss_temp = mMenuView.findViewById(R.id.pop_showss_temp);
        pop_addfromfactory = mMenuView.findViewById(R.id.pop_addfromfactory);
        pop_devetc = mMenuView.findViewById(R.id.pop_devetc);
        pop_addy = mMenuView.findViewById(R.id.pop_addy);
        pop_addyx = mMenuView.findViewById(R.id.pop_addyx);
        pop_addt = mMenuView.findViewById(R.id.pop_addt);
        pop_camera = mMenuView.findViewById(R.id.pop_camera);
        pop_vcamera = mMenuView.findViewById(R.id.pop_vcamera);
        pop_exper = mMenuView.findViewById(R.id.pop_exper);

        pop_addzhuji.setOnClickListener(itemsOnClick);
        pop_adddevice.setOnClickListener(itemsOnClick);
        pop_addgroup.setOnClickListener(itemsOnClick);
        pop_showss.setOnClickListener(itemsOnClick);
        pop_showss_temp.setOnClickListener(itemsOnClick);
        pop_addfromfactory.setOnClickListener(itemsOnClick);
        pop_devetc.setOnClickListener(itemsOnClick);
        pop_addy.setOnClickListener(itemsOnClick);
        pop_addyx.setOnClickListener(itemsOnClick);
        pop_addt.setOnClickListener(itemsOnClick);
        pop_camera.setOnClickListener(itemsOnClick);
        pop_vcamera.setOnClickListener(itemsOnClick);
        pop_exper.setOnClickListener(itemsOnClick);
        int h = context.getWindowManager().getDefaultDisplay().getHeight();
        int w = context.getWindowManager().getDefaultDisplay().getWidth();
        //设置按钮监听
        //设置SelectPicPopupWindow的View
        this.setContentView(mMenuView);
        //设置SelectPicPopupWindow弹出窗体的宽
//        this.setWidth(w / 2 + 50);
        this.setWidth(LayoutParams.MATCH_PARENT);
        //设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        //设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        //设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.Right_menu_animation);
        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0000000000);
        //设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
        //mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        mMenuView.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                int height = mMenuView.findViewById(R.id.pop_layout2).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });

    }

    public SelectAddPopupWindow(final Activity context, OnClickListener itemsOnClick, int index) {
        super(context);
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.activity_group_edit_rightmenug, null);
//        RadioButton group_add = (RadioButton) mMenuView.findViewById(R.id.group_add);
//        RadioButton group_dele = (RadioButton) mMenuView.findViewById(R.id.group_dele);
        TextView group_add = (TextView) mMenuView.findViewById(R.id.group_add);
        TextView group_dele = (TextView) mMenuView.findViewById(R.id.group_dele);

        if (index == 1) {
            group_add.setText(context.getResources().getString(R.string.activity_group_rightmenu_add));
            group_dele.setText(context.getResources().getString(R.string.activity_group_rightmenu_del));
        }
        group_add.setOnClickListener(itemsOnClick);
        group_dele.setOnClickListener(itemsOnClick);

        int h = context.getWindowManager().getDefaultDisplay().getHeight();
        int w = context.getWindowManager().getDefaultDisplay().getWidth();

        //设置按钮监听
        //设置SelectPicPopupWindow的View
        this.setContentView(mMenuView);
        //设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(w * 1 / 2);
        //设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        //设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        //设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.Right_menu_animation);
        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0000000000);
        //设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
        //mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        mMenuView.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                int height = mMenuView.findViewById(R.id.pop_layout2).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });
        this.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = context.getWindow().getAttributes();
                params.alpha = 1f;
                context.getWindow().setAttributes(params);
            }
        });

    }


    public SelectAddPopupWindow(final Activity context, OnClickListener itemsOnClick, String device_type) {
        super(context);
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.activity_xyj_menu, null);
        mMenuView = inflater.inflate(R.layout.activity_weight_menu, null);
//        RadioButton group_add = (RadioButton) mMenuView.findViewById(R.id.group_add);
//        RadioButton group_dele = (RadioButton) mMenuView.findViewById(R.id.group_dele);
        TextView tv_zn = (TextView) mMenuView.findViewById(R.id.tv_zn);
        TextView tv_sd = (TextView) mMenuView.findViewById(R.id.tv_sd);
        TextView tv_weight_unit = (TextView) mMenuView.findViewById(R.id.tv_weight_unit);

        if (device_type.equals(DeviceInfo.CaMenu.xueyaji.value())) {
            tv_zn.setText(context.getResources().getString(R.string.xyj_menu_zn));
            tv_sd.setText(context.getResources().getString(R.string.xyj_menu_sd));
            tv_weight_unit.setVisibility(View.GONE);
        }  if (device_type.equals(DeviceInfo.CaMenu.tizhongceng.value())) {
            tv_zn.setText(context.getResources().getString(R.string.xyj_menu_zn));
            tv_sd.setText(context.getResources().getString(R.string.xyj_menu_sd));
            tv_weight_unit.setText(context.getResources().getString(R.string.xyj_menu_unit));
            tv_weight_unit.setVisibility(View.VISIBLE);
        }
        tv_zn.setOnClickListener(itemsOnClick);
        tv_sd.setOnClickListener(itemsOnClick);
        tv_weight_unit.setOnClickListener(itemsOnClick);

        int h = context.getWindowManager().getDefaultDisplay().getHeight();
        int w = context.getWindowManager().getDefaultDisplay().getWidth();

        //设置按钮监听
        //设置SelectPicPopupWindow的View
        this.setContentView(mMenuView);
        //设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(w * 1 / 2);
        //设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        //设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        //设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.Right_menu_animation);
        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0000000000);
        //设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
        //mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        mMenuView.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                int height = mMenuView.findViewById(R.id.pop_layout2).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });
        this.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = context.getWindow().getAttributes();
                params.alpha = 1f;
                context.getWindow().setAttributes(params);
            }
        });

    }


    //体验主机列表
    public SelectAddPopupWindow(final Activity context, double a, AdapterView.OnItemClickListener itemsOnClick) {
        super(context);
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.activity_zhujis_exper, null);
        zhujiInfos = new ArrayList<>();
        mAadapter = new ExperAdapter(zhujiInfos, context);
        listViwe = (ListView) mMenuView.findViewById(R.id.rightmenu_zhuji_listview);
        listViwe.setAdapter(mAadapter);
        listViwe.setOnItemClickListener(itemsOnClick);
        int h = context.getWindowManager().getDefaultDisplay().getHeight();
        int w = context.getWindowManager().getDefaultDisplay().getWidth();
        //设置按钮监听
        //设置SelectPicPopupWindow的View
        this.setContentView(mMenuView);
        //设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(w);
        //设置SelectPicPopupWindow弹出窗体的高
        this.setHeight((h * 2) / 5);
        //设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        //设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.Right_menu_animation);
        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0000000000);
        //设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
        //mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        mMenuView.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                int height = mMenuView.findViewById(R.id.pop_zhuji_layout).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });

        this.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = context.getWindow().getAttributes();
                params.alpha = 1f;
                context.getWindow().setAttributes(params);
            }
        });

    }

    public SelectAddPopupWindow(final Activity context, OnClickListener itemsOnClick, int i, boolean flag, DeviceInfo deviceInfo) {
        super(context);
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.activity_device_commandhistory_rightmenu, null);
        RadioButton edit = (RadioButton) mMenuView.findViewById(R.id.pop_edit);
        RadioButton shock = (RadioButton) mMenuView.findViewById(R.id.pop_shock);
        RadioButton voiced = (RadioButton) mMenuView.findViewById(R.id.pop_voiced);
        RadioButton silent = (RadioButton) mMenuView.findViewById(R.id.pop_silent);
        RadioButton history = (RadioButton) mMenuView.findViewById(R.id.pop_history);
        RadioButton voice_and_shock = (RadioButton) mMenuView.findViewById(R.id.pop_voice_and_shock);
        if (flag) history.setVisibility(View.VISIBLE);
        if (deviceInfo != null) {
            if (DeviceInfo.CaMenu.zhinengsuo.value().equals(deviceInfo.getCa())) {
                edit.setVisibility(View.GONE);
                shock.setVisibility(View.GONE);
                voiced.setVisibility(View.GONE);
                silent.setVisibility(View.GONE);
                voice_and_shock.setVisibility(View.GONE);
            }
        }

        edit.setOnClickListener(itemsOnClick);
        shock.setOnClickListener(itemsOnClick);
        voiced.setOnClickListener(itemsOnClick);
        silent.setOnClickListener(itemsOnClick);
        history.setOnClickListener(itemsOnClick);
        voice_and_shock.setOnClickListener(itemsOnClick);

        int h = context.getWindowManager().getDefaultDisplay().getHeight();
        int w = context.getWindowManager().getDefaultDisplay().getWidth();
        if (DeviceInfoActivity.key == 1) {
            shock.setChecked(true);
        } else if (DeviceInfoActivity.key == 2) {
            voiced.setChecked(true);
        } else if (DeviceInfoActivity.key == 3) {
            voice_and_shock.setChecked(true);
        } else {
            silent.setChecked(true);
        }
        //设置按钮监听
        //设置SelectPicPopupWindow的View
        this.setContentView(mMenuView);
        //设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(w * 2 / 3);
        //设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        //设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        //设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.Right_menu_animation);
        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0000000000);
        //设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
        //mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        mMenuView.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                int height = mMenuView.findViewById(R.id.pop_layout2).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });

    }

    public SelectAddPopupWindow(Activity context, AdapterView.OnItemClickListener itemsOnClick, ZhujiInfo zhuji) {
        super(context);
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.activity_zhuji_list_rightmenu, null);
        zhujiInfos = new ArrayList<>();
        adapter = new ZhujiAdapter(zhujiInfos, context, zhuji);
        listViwe = (ListView) mMenuView.findViewById(R.id.rightmenu_zhuji_listview);
        listViwe.setAdapter(adapter);
        listViwe.setOnItemClickListener(itemsOnClick);
        int h = context.getWindowManager().getDefaultDisplay().getHeight();
        int w = context.getWindowManager().getDefaultDisplay().getWidth();
        //设置按钮监听
        //设置SelectPicPopupWindow的View
        this.setContentView(mMenuView);
        //设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(w / 2 + 50);
        //设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        //设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        //设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.Right_menu_animation);
        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0000000000);
        //设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
        //mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        mMenuView.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                int height = mMenuView.findViewById(R.id.pop_zhuji_layout).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });

    }

//    public void updateMenu(DataCenterSharedPreferences dcsp, ZhujiInfo zhuji, int flag) {
//        if (Actions.VersionType.CHANNEL_WOAIJIA.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
//            if (flag == 1) {
//                pop_addzhuji.setVisibility(View.VISIBLE);
//            } else {
//                updateMenu(dcsp, zhuji);
//                pop_addzhuji.setVisibility(View.GONE);
//            }
//        } else {
//            updateMenu(dcsp, zhuji);
//        }
//    }
//
//    public void updateMenu(DataCenterSharedPreferences dcsp, ZhujiInfo zhuji) {
//        if (zhuji != null && zhuji.isAdmin()) {
//            pop_showss.setVisibility(View.VISIBLE);
//        } else {
//            pop_showss.setVisibility(View.GONE);
//        }
//        goneAllMenu();
//
//        if (zhuji != null) { //摄像头全部启用。有需要关闭的版本在下面关闭
//            if (MainApplication.app.getAppGlobalConfig().isShowAddGroup()) {
//                pop_addgroup.setVisibility(View.VISIBLE);
//            }
//            if (MainApplication.app.getAppGlobalConfig().isShowAddDevice()) {
//                //默认和非1支持，1不支持
//                if (zhuji.getSetInfos().isEmpty() || !"1".equalsIgnoreCase(zhuji.getSetInfos().get(ZhujiInfo.GNSetNameMenu.supportAddDevice.value()))) {
//                    pop_adddevice.setVisibility(View.VISIBLE);
//                }
//            }
//            if (Constant.ROLE_USERADMIN.equals(dcsp.getString(Constant.LOGIN_ROLE, Constant.ROLE_NORMAL))
//                    || Constant.ROLE_SUPERADMIN.equals(dcsp.getString(Constant.LOGIN_ROLE, Constant.ROLE_NORMAL))) {
//                pop_addfromfactory.setVisibility(View.VISIBLE);
//                pop_devetc.setVisibility(View.VISIBLE);
//            }
//            if (MainApplication.app.getAppGlobalConfig().isShowAddCamera())
//                pop_camera.setVisibility(View.VISIBLE);//摄像头
//            if (MainApplication.app.getAppGlobalConfig().isShowMostZhuji())
//                pop_addzhuji.setVisibility(View.VISIBLE);//多主机
//            if (MainApplication.app.getAppGlobalConfig().isShowTemporarySharing())
//                pop_showss_temp.setVisibility(View.VISIBLE); //开启临时分享
//
//            if (dcsp.getBoolean(Constant.IS_SUPORT_STU, false)) {
//                if (MainApplication.app.getAppGlobalConfig().isShowVirtual())
//                    pop_addyx.setVisibility(View.VISIBLE);//创建虚拟遥控器
//                if (MainApplication.app.getAppGlobalConfig().isShowStudyFrequency())
//                    pop_addy.setVisibility(View.VISIBLE);//学习遥控器
//                if (MainApplication.app.getAppGlobalConfig().isShowStudyProbe())
//                    pop_addt.setVisibility(View.VISIBLE);//学习探头
//            }
//            if (LogUtil.isDebug) {//测试环境支持多主机
////				pop_addzhuji.setVisibility(View.VISIBLE);
//            }
//            if (Actions.VersionType.CHANNEL_ZNZK.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
//                pop_addy.setVisibility(View.VISIBLE);
//                pop_camera.setVisibility(View.VISIBLE);
//            }
//        } else {
//            if (!Util.isHaveDevices(context)) {
//                if (MainApplication.app.getAppGlobalConfig().isShowAddDevice())
//                    pop_adddevice.setVisibility(View.VISIBLE);
//            } else {
//                if (MainApplication.app.getAppGlobalConfig().isShowAddDevice())
//                    pop_adddevice.setVisibility(View.GONE);
////					pop_camera.setVisibility(View.VISIBLE);
//            }
//        }
//    }

    //体验主机列表
    public void updateMenu(List<ZhujiInfo> list) {

        if (mAadapter == null) return;
        if (zhujiInfos != null) zhujiInfos.clear();
        if (list == null) {
            mAadapter.notifyDataSetChanged();
            return;
        }
        for (ZhujiInfo zj : list) {
            zhujiInfos.add(zj);
        }
        mAadapter.notifyDataSetChanged();
    }

    public void updateMenu() {
        goneAllMenu();

    }

    private void goneAllMenu() {
        pop_addzhuji.setVisibility(View.GONE);
        pop_addy.setVisibility(View.GONE);
        pop_addyx.setVisibility(View.GONE);
        pop_addt.setVisibility(View.GONE);
        pop_adddevice.setVisibility(View.GONE);
        pop_addgroup.setVisibility(View.GONE);
        pop_addfromfactory.setVisibility(View.GONE);
        pop_devetc.setVisibility(View.GONE);
        pop_camera.setVisibility(View.GONE);
        pop_showss_temp.setVisibility(View.GONE);
    }

    public void updateMenu(ZhujiInfo zhuji, List<ZhujiInfo> list) {
        if (adapter == null) return;
        adapter.setZhuji(zhuji);
        if (zhujiInfos != null) zhujiInfos.clear();
        if (list == null) {
            adapter.notifyDataSetChanged();
            return;
        }
        for (ZhujiInfo zj : list) {
            zhujiInfos.add(zj);
        }
    }

}
