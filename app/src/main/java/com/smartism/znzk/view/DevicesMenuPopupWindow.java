package com.smartism.znzk.view;


import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartism.znzk.R;
import com.smartism.znzk.activity.device.DeviceMainActivity;
import com.smartism.znzk.activity.device.GroupInfoActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.DeviceInfo.ControlTypeMenu;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;

/**
 * 设备列表设备菜单popupwindow
 *
 * @author 王建
 *         2015年9月30日
 */
public class DevicesMenuPopupWindow extends PopupWindow {

    private View mMenuView;
    private RelativeLayout btn_bind_camera,btn_checkversion, btn_accept_auto_strongshow, btn_accept_autoshow,
            btn_acceptnotshow, btn_notaccept, btn_deldevice, btn_setdevice, btn_setgsm, btn_setscall,btn_tantou_chufa_setting,hongcai_alarm_setting,hongcai_naozhong_setting;
    private TextView txt_setdevice, txt_deldevice, notshow_text;
    private boolean isDelPers = true;//设备删除权限


    public boolean isDelPers() {
        return isDelPers;
    }

    public void setDelPers(boolean delPers) {
        isDelPers = delPers;
    }

    public DevicesMenuPopupWindow(final Context context, OnClickListener itemsOnClick) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.activity_devices_list_item_menu, null);
        btn_tantou_chufa_setting = mMenuView.findViewById(R.id.tantou_chufa_setting);
        hongcai_alarm_setting = mMenuView.findViewById(R.id.hongcai_alarm_setting);
        hongcai_naozhong_setting = mMenuView.findViewById(R.id.hongcai_naozhong_setting);

        btn_checkversion = (RelativeLayout) mMenuView.findViewById(R.id.btn_checkversion);
        btn_accept_auto_strongshow = (RelativeLayout) mMenuView.findViewById(R.id.btn_accept_auto_strongshow);
        btn_accept_autoshow = (RelativeLayout) mMenuView.findViewById(R.id.btn_accept_autoshow);
        btn_acceptnotshow = (RelativeLayout) mMenuView.findViewById(R.id.btn_acceptnotshow);
        btn_deldevice = (RelativeLayout) mMenuView.findViewById(R.id.btn_deldevice);
        btn_notaccept = (RelativeLayout) mMenuView.findViewById(R.id.btn_notaccept);
        btn_setdevice = (RelativeLayout) mMenuView.findViewById(R.id.btn_setdevice);
        btn_setgsm = (RelativeLayout) mMenuView.findViewById(R.id.btn_setgsm);
        btn_setscall = (RelativeLayout) mMenuView.findViewById(R.id.btn_setscall);
        btn_bind_camera = mMenuView.findViewById(R.id.btn_bind_camera);
        txt_deldevice = (TextView) btn_deldevice.findViewById(R.id.txt_deldevice);
        txt_setdevice = (TextView) btn_setdevice.findViewById(R.id.txt_setdevice);
        notshow_text = (TextView) mMenuView.findViewById(R.id.notshow_text);

        btn_bind_camera.setOnClickListener(itemsOnClick);
        btn_checkversion.setOnClickListener(itemsOnClick);
        btn_accept_auto_strongshow.setOnClickListener(itemsOnClick);
        btn_accept_autoshow.setOnClickListener(itemsOnClick);
        btn_acceptnotshow.setOnClickListener(itemsOnClick);
        btn_deldevice.setOnClickListener(itemsOnClick);
        btn_notaccept.setOnClickListener(itemsOnClick);
        btn_setdevice.setOnClickListener(itemsOnClick);
        btn_setgsm.setOnClickListener(itemsOnClick);
        btn_setscall.setOnClickListener(itemsOnClick);
        btn_tantou_chufa_setting.setOnClickListener(itemsOnClick);
        hongcai_naozhong_setting.setOnClickListener(itemsOnClick);
        hongcai_alarm_setting.setOnClickListener(itemsOnClick);

        //设置SelectPicPopupWindow的View
        this.setContentView(mMenuView);
        //设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(LayoutParams.MATCH_PARENT);
        //设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        //设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        //设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.Devices_list_menu_Animation);
        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0x00000000);
        //设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
        //mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        mMenuView.setOnTouchListener(new OnTouchListener() {

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
        this.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
//                WindowManager.LayoutParams layoutParams = ((DeviceMainActivity) context).getWindow().getAttributes();//可能会出现类型转换异常
//                layoutParams.alpha = 1.0f;
                if(context instanceof DeviceMainActivity){
                    WindowManager.LayoutParams layoutParams = ((DeviceMainActivity) context).getWindow().getAttributes();
                    layoutParams.alpha = 1.0f;
                    ((DeviceMainActivity) context).getWindow().setAttributes(layoutParams);
                }else if(context instanceof GroupInfoActivity){
                    WindowManager.LayoutParams layoutParams = ((GroupInfoActivity) context).getWindow().getAttributes();
                     layoutParams.alpha = 1.0f;
                    ((GroupInfoActivity) context).getWindow().setAttributes(layoutParams);
                }
            //    ((DeviceMainActivity) context).getWindow().setAttributes(layoutParams);//可能会出现类型转换异常

            }
        });

    }


    public void updateDeviceMenu(Context context, DeviceInfo info, DataCenterSharedPreferences dcsp, ZhujiInfo zhujiInfo) {
         btn_tantou_chufa_setting.setVisibility(View.GONE);//探头主机设置,首先设置为不可见，被设置可见后，无法不可见
         hongcai_alarm_setting.setVisibility(View.GONE);
         hongcai_naozhong_setting.setVisibility(View.GONE);
        if(Actions.VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())&&zhujiInfo.isOnline()){
            if(zhujiInfo.getId()!=info.getId()){
                if(zhujiInfo.isAdmin()){
                    //设备的长按事件,探头设备显示探头主机设置
                    if(!info.getCa().equals(DeviceInfo.CaMenu.wenshiduji.value())){
                          btn_tantou_chufa_setting.setVisibility(View.VISIBLE);
                    }
                }
            }else{
                //主机的长按事件
                if(zhujiInfo.isAdmin()){
                    //显示闹钟设置和报警设置
                    hongcai_alarm_setting.setVisibility(View.VISIBLE);
                    hongcai_naozhong_setting.setVisibility(View.VISIBLE);
                }
            }

        }
        txt_setdevice.setText(context.getResources().getString(R.string.devices_list_menu_dialog_devicesetting));
        //默认和非1支持，1不支持
        if (zhujiInfo.getSetInfos().isEmpty() || !"1".equalsIgnoreCase(zhujiInfo.getSetInfos().get(ZhujiInfo.GNSetNameMenu.supportDelDevice.value()))) {
            if (MainApplication.app.getAppGlobalConfig().isChildAccountAllow()) {
                //支持app添加有线防区，那就可以删除,否则有线防区是不能删除的
                if (info.getNt() == 1){
                    if ("1".equalsIgnoreCase(zhujiInfo.getSetInfos().get(ZhujiInfo.GNSetNameMenu.supportCable.value()))){
                        txt_deldevice.setText(context.getResources().getString(R.string.devices_list_menu_dialog_devicedel));
                        btn_deldevice.setVisibility(View.VISIBLE);
                    }else{
                        btn_deldevice.setVisibility(View.GONE);
                    }
                }else{
                    txt_deldevice.setText(context.getResources().getString(R.string.devices_list_menu_dialog_devicedel));
                    btn_deldevice.setVisibility(View.VISIBLE);
                }
            } else {
                if (zhujiInfo.isAdmin()) {
                    btn_deldevice.setVisibility(View.VISIBLE);
                    txt_deldevice.setText(context.getResources().getString(R.string.devices_list_menu_dialog_devicedel));
                } else {
                    btn_deldevice.setVisibility(View.GONE);
                }
            }
//            if (MainApplication.app.getAppGlobalConfig().isShowDevicesPermisson()) {//是否显示用户设备授权，2020年01月07日屏蔽，想不明白这里干啥
//                if (!ControlTypeMenu.zhuji.value().equals(info.getControlType())
//                        && !zhujiInfo.isAdmin() && !isDelPers()) {
//                    btn_deldevice.setVisibility(View.GONE);
//                } else {
//                    btn_deldevice.setVisibility(View.VISIBLE);
//                }
//            }
        }else{
            btn_deldevice.setVisibility(View.GONE);
        }

        btn_setdevice.setVisibility(View.VISIBLE);

        if (ControlTypeMenu.zhuji.value().equals(info.getControlType()) && info.getGsm() == 1 && zhujiInfo.isAdmin()) {
            btn_setgsm.setVisibility(View.VISIBLE);
        } else {
            btn_setgsm.setVisibility(View.GONE);
        }
        if ((MainApplication.app.getAppGlobalConfig().isShowCallAlarm() || MainApplication.app.getAppGlobalConfig().isShowSmsAlarm())
                && ControlTypeMenu.zhuji.value().equals(info.getControlType()) && info.getGsm() == 0 && zhujiInfo.isAdmin()) {
            btn_setscall.setVisibility(View.VISIBLE);
        } else {
            btn_setscall.setVisibility(View.GONE);
        }
        if (info.getCak() != null && info.getCak().contains("control")) { //控制类型的分类
            btn_checkversion.setVisibility(View.GONE);
            btn_accept_auto_strongshow.setVisibility(View.GONE);
            btn_accept_autoshow.setVisibility(View.GONE);
            btn_acceptnotshow.setVisibility(View.GONE);
            btn_notaccept.setVisibility(View.GONE);
        } else if (info.getControlType() != null && ControlTypeMenu.zhuji.value().equals(info.getControlType())) {
            btn_checkversion.setVisibility(View.VISIBLE);
            //警告提示音
            btn_accept_auto_strongshow.setVisibility(View.GONE);
            //短信提示音
            btn_accept_autoshow.setVisibility(View.GONE);
            btn_acceptnotshow.setVisibility(View.GONE);
            btn_notaccept.setVisibility(View.GONE);
        } else if (info.getControlType() != null && ControlTypeMenu.group.value().equals(info.getControlType())) { //群组菜单
            txt_setdevice.setText(context.getResources().getString(R.string.devices_list_menu_dialog_groupsetting));
            txt_deldevice.setText(context.getResources().getString(R.string.devices_list_menu_dialog_group));
            btn_checkversion.setVisibility(View.GONE);
            btn_accept_auto_strongshow.setVisibility(View.GONE);
            btn_accept_autoshow.setVisibility(View.GONE);
            btn_acceptnotshow.setVisibility(View.GONE);
            btn_notaccept.setVisibility(View.GONE);
//			btn_setdevice.setVisibility(View.GONE);
        } else if (info.getCak() != null && (info.getCak().contains("security") || DeviceInfo.CaMenu.znyx.value().equals(info.getCa()))){
            btn_checkversion.setVisibility(View.GONE);
     /*      if (info.getAcceptMessage() <= 0) {
                btn_accept_auto_strongshow.setVisibility(View.GONE);
                btn_accept_autoshow.setVisibility(View.GONE);
                btn_acceptnotshow.setVisibility(View.GONE);
                btn_notaccept.setVisibility(View.GONE);
            } else {
                btn_accept_auto_strongshow.setVisibility(View.VISIBLE);
                btn_accept_autoshow.setVisibility(View.VISIBLE);
                btn_acceptnotshow.setVisibility(View.VISIBLE);
                btn_notaccept.setVisibility(View.VISIBLE);
            }*/

            btn_accept_auto_strongshow.setVisibility(View.VISIBLE);
            btn_accept_autoshow.setVisibility(View.VISIBLE);
            btn_acceptnotshow.setVisibility(View.VISIBLE);
            btn_notaccept.setVisibility(View.VISIBLE);


            btn_accept_auto_strongshow.findViewById(R.id.storongshow_img).setVisibility(View.GONE);
            btn_accept_autoshow.findViewById(R.id.autoshow_img).setVisibility(View.GONE);
            btn_acceptnotshow.findViewById(R.id.notshow_img).setVisibility(View.GONE);
            btn_notaccept.findViewById(R.id.notaccept_img).setVisibility(View.GONE);
            if (info.getAcceptMessage() == 3) {
                btn_accept_auto_strongshow.setEnabled(false);
                btn_accept_auto_strongshow.findViewById(R.id.storongshow_img).setVisibility(View.VISIBLE);
                btn_accept_autoshow.setEnabled(true);
                btn_acceptnotshow.setEnabled(true);
                btn_notaccept.setEnabled(true);
            } else if (info.getAcceptMessage() == 2) {
                btn_accept_auto_strongshow.setEnabled(true);
                btn_accept_autoshow.setEnabled(false);
                btn_accept_autoshow.findViewById(R.id.autoshow_img).setVisibility(View.VISIBLE);
                btn_acceptnotshow.setEnabled(true);
                btn_notaccept.setEnabled(true);
            } else if (info.getAcceptMessage() == 1) {
                btn_accept_auto_strongshow.setEnabled(true);
                btn_accept_autoshow.setEnabled(true);
                btn_acceptnotshow.setEnabled(false);
                btn_acceptnotshow.findViewById(R.id.notshow_img).setVisibility(View.VISIBLE);
                btn_notaccept.setEnabled(true);
            } else {
                btn_accept_auto_strongshow.setEnabled(true);
                btn_accept_autoshow.setEnabled(true);
                btn_acceptnotshow.setEnabled(true);
                btn_notaccept.setEnabled(false);
                btn_notaccept.findViewById(R.id.notaccept_img).setVisibility(View.VISIBLE);
            }
            if (DeviceInfo.CaMenu.znyx.value().equals(info.getCa())) {
                btn_notaccept.setVisibility(View.GONE);
            }
        } else if (info.getCak() != null && DeviceInfo.CaMenu.zhinengsuo.value().equals(info.getCa())) {
            //警告提示音
            btn_accept_auto_strongshow.setVisibility(View.GONE);
            //短信提示音
            btn_accept_autoshow.setVisibility(View.GONE);
        } else {
            btn_checkversion.setVisibility(View.GONE);
            btn_accept_auto_strongshow.setVisibility(View.GONE);
            btn_accept_autoshow.setVisibility(View.GONE);
            btn_acceptnotshow.setVisibility(View.GONE);
            btn_notaccept.setVisibility(View.GONE);
        }
        //24小时防区设备不能操作提示音
        if (info.isFa()) {
            //警告提示音
            btn_accept_auto_strongshow.setVisibility(View.GONE);
            //短信提示音
            btn_accept_autoshow.setVisibility(View.GONE);
            btn_acceptnotshow.setVisibility(View.GONE);
            btn_notaccept.setVisibility(View.GONE);
        }
        if (info.getCa().equals(DeviceInfo.CaMenu.ybq.value())) {
            btn_accept_autoshow.setVisibility(View.GONE);
            notshow_text.setText(context.getString(R.string.ybq_chart_name));
        } else {
            notshow_text.setText(context.getString(R.string.devices_list_menu_dialog_jsbts));
        }

        if (zhujiInfo != null && zhujiInfo.isEx() && !zhujiInfo.isAdmin()) {
            btn_deldevice.setVisibility(View.GONE);//非管理员不可删除设备
        }
        if (ControlTypeMenu.zhuji.value().equals(info.getControlType())) {
            btn_deldevice.setVisibility(View.VISIBLE);
            txt_setdevice.setText(context.getResources().getString(R.string.devices_list_menu_dialog_zhujisetting));
            txt_deldevice.setText(context.getResources().getString(R.string.devices_list_menu_dialog_zhuji));
        }
        //主机场景-3在家,0撤防,-1布防
        if(zhujiInfo.getScene().equals("-1")||zhujiInfo.getScene().equals("0")){
            btn_accept_autoshow.setVisibility(View.GONE);//在家模式
            btn_accept_auto_strongshow.setVisibility(View.GONE);//布防
            btn_acceptnotshow.setVisibility(View.GONE);//接收但不提醒
            btn_notaccept.setVisibility(View.GONE);//撤防
        }
        //宏才
        if(Actions.VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                ||Actions.VersionType.CHANNEL_ZHZJ.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            if(!zhujiInfo.isOnline()){
                //主机不在线时，隐藏设置设备
                if(!Actions.VersionType.CHANNEL_ZHZJ.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                    btn_setdevice.setVisibility(View.GONE);//设置设备
                }
                btn_setscall.setVisibility(View.GONE);//报警号码设置
                btn_checkversion.setVisibility(View.GONE);//更新固件
                btn_accept_autoshow.setVisibility(View.GONE);//在家模式
                btn_accept_auto_strongshow.setVisibility(View.GONE);//布防
                btn_acceptnotshow.setVisibility(View.GONE);//接收但不提醒
                btn_notaccept.setVisibility(View.GONE);//撤防
            }
        }else if(Actions.VersionType.CHANNEL_RUNLONG.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            if(zhujiInfo.getRolek().equals("lock_num_old")||zhujiInfo.getRolek().equals("lock_num_baby")||zhujiInfo.getRolek().equals("lock_num_guest")
                    ||zhujiInfo.getRolek().equals("lock_num_temp")){
                btn_setdevice.setVisibility(View.GONE);//设置设备
                btn_checkversion.setVisibility(View.GONE);//更新固件
            }
        }

        //志诚红外转发
        if(Actions.VersionType.CHANNEL_ZHICHENG.equals(MainApplication.app.getAppGlobalConfig().getVersion())&&info.getCa().equals(DeviceInfo.CaMenu.hongwaizhuanfaqi.value())){
            btn_deldevice.setVisibility(View.GONE);
        }else if(Actions.VersionType.CHANNEL_RUNLONG.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            if (!zhujiInfo.isAdmin()&&!zhujiInfo.getRolek().equals("lock_num_admin")&&!zhujiInfo.getRolek().equals("lock_num_partner")) {
                //不是管理员，不是爱人，隐藏删除设备
                btn_deldevice.setVisibility(View.GONE);
            }
        }

    }

    public void updateDeviceMenu(Context context, DeviceInfo info, DataCenterSharedPreferences dcsp, ZhujiInfo zhujiInfo, boolean flag) {
        goneAllItem();
        //隐藏宏才
        hongcai_alarm_setting.setVisibility(View.GONE);
        hongcai_naozhong_setting.setVisibility(View.GONE);
        btn_setdevice.setVisibility(View.VISIBLE);
        if(Actions.VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())&&zhujiInfo.isOnline()&&zhujiInfo.isAdmin()){
            hongcai_alarm_setting.setVisibility(View.VISIBLE);
            hongcai_naozhong_setting.setVisibility(View.VISIBLE);
        }

        if (info.getGsm() == 1 && zhujiInfo.isAdmin()) {
            btn_setgsm.setVisibility(View.VISIBLE);
        } else {
            btn_setgsm.setVisibility(View.GONE);
        }
        if ((MainApplication.app.getAppGlobalConfig().isShowCallAlarm() || MainApplication.app.getAppGlobalConfig().isShowSmsAlarm())
                && ControlTypeMenu.zhuji.value().equals(info.getControlType()) && info.getGsm() == 0 && zhujiInfo.isAdmin()) {
            btn_setscall.setVisibility(View.VISIBLE);
        } else {
            btn_setscall.setVisibility(View.GONE);
        }
        if (DeviceInfo.CakMenu.zhuji.value().equals(zhujiInfo.getCak())) {

            if(Actions.VersionType.CHANNEL_HZYCZN.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
                if(zhujiInfo.getCa()!=null&&zhujiInfo.getCa().equals(DeviceInfo.CaMenu.zhujijzm.value())&&zhujiInfo.isAdmin()){
                    btn_bind_camera.setVisibility(View.VISIBLE);//绑定摄像头设置可见
                }
            }

            //主机只有设置 、 固件更新、 删除
            txt_setdevice.setText(context.getResources().getString(R.string.devices_list_menu_dialog_zhujisetting));
            txt_deldevice.setText(context.getResources().getString(R.string.devices_list_menu_dialog_zhuji));
            if (zhujiInfo.isAdmin()) {
                txt_deldevice.setText(context.getResources().getString(R.string.devices_list_menu_dialog_zhuji));
            }
            btn_checkversion.setVisibility(View.VISIBLE);
        } else if (DeviceInfo.CaMenu.ipcamera.value().equals(zhujiInfo.getCa())) {
            //主机只有设置 、 删除
            txt_setdevice.setText(context.getResources().getString(R.string.devices_list_menu_dialog_devicesetting));
            txt_deldevice.setText(context.getResources().getString(R.string.devices_list_menu_dialog_devicedel));
//        } else if (DeviceInfo.CaMenu.cazuo.value().equals(zhujiInfo.getCa()) || DeviceInfo.CaMenu.djkzq.value().equals(zhujiInfo.getCa())) {
        } else {
            if((DeviceInfo.CaMenu.wifizns.value().equals(zhujiInfo.getCa())||DeviceInfo.CaMenu.nbyg.value().equals(zhujiInfo.getCa()))
                    &&zhujiInfo.isAdmin()){
                btn_bind_camera.setVisibility(View.VISIBLE);//绑定摄像头设置可见
            }
            txt_setdevice.setText(context.getResources().getString(R.string.devices_list_menu_dialog_devicesetting));
            txt_deldevice.setText(context.getResources().getString(R.string.devices_list_menu_dialog_devicedel));
            btn_checkversion.setVisibility(View.VISIBLE);
        }

        //宏才
        if(Actions.VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())&&zhujiInfo.getCa().equals(DeviceInfo.CaMenu.zhuji.value())){
            //主机列表长按主机
            if(!zhujiInfo.isOnline()){
                btn_setdevice.setVisibility(View.GONE);//设置设备
                btn_setscall.setVisibility(View.GONE);//报警号码设置
                btn_checkversion.setVisibility(View.GONE);//更新固件
            }
        }else if(Actions.VersionType.CHANNEL_RUNLONG.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            if(zhujiInfo.getRolek().equals("lock_num_old")||zhujiInfo.getRolek().equals("lock_num_baby")||zhujiInfo.getRolek().equals("lock_num_guest")
                    ||zhujiInfo.getRolek().equals("lock_num_temp")){
                btn_setdevice.setVisibility(View.GONE);//设置设备
                btn_checkversion.setVisibility(View.GONE);//更新固件
            }
        }
    }

    private void goneAllItem() {
        btn_bind_camera.setVisibility(View.GONE);//绑定摄像头
        btn_checkversion.setVisibility(View.GONE);//检查固件更新
        btn_accept_auto_strongshow.setVisibility(View.GONE);
        btn_accept_autoshow.setVisibility(View.GONE);
        btn_acceptnotshow.setVisibility(View.GONE);
        btn_notaccept.setVisibility(View.GONE);
        btn_setgsm.setVisibility(View.GONE);
        btn_setscall.setVisibility(View.GONE);
    }
}
