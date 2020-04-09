package com.smartism.znzk.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.device.ZhujiListFragment.ZhujiInfos;
import com.smartism.znzk.activity.scene.SceneActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiGroupInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.domain.camera.CameraInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.CollectionsUtils;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.view.BadgeView;

import java.util.List;

/**
 * Created by Administrator on 2017/3/27.
 */

public class ZhujiListExpandableAdapter extends BaseExpandableListAdapter {
    private List<ZhujiGroupInfo> groupInfoList;
    private List<List<ZhujiInfos>> zhujiInfoList;
    private Context context;
    private LayoutInflater layoutInflater;
    private ZhujiListAdapterOnclick zhujiListAdapterOnclick;
    public DataCenterSharedPreferences dcsp = null;
    public Animation imgloading_animation;

    public void setZhujiListAdapterOnclick(ZhujiListAdapterOnclick zhujiListAdapterOnclick) {
        this.zhujiListAdapterOnclick = zhujiListAdapterOnclick;
    }

    public ZhujiListExpandableAdapter(List<ZhujiGroupInfo> groupInfoList,List<List<ZhujiInfos>> zhujiInfoList, Context context) {
        this.groupInfoList = groupInfoList;
        this.zhujiInfoList = zhujiInfoList;
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        dcsp = DataCenterSharedPreferences.getInstance(context,
                DataCenterSharedPreferences.Constant.CONFIG);
        imgloading_animation = AnimationUtils.loadAnimation(context, R.anim.loading_revolve);
        imgloading_animation.setInterpolator(new LinearInterpolator());
    }

    // 0默认手动，1、自动，2、联动 默认场景无此字段
    public static final int SecuritySceneType_Normal = 0;
    public static final int SecuritySceneType_Time = 1;
    public static final int SecuritySceneType_Trigger = 2;
    public static final String Scene_Default_DesArming = "0";
    public static final String Scene_Default_Arming = "-1";
    public static final String Scene_Default_No = "-2";
    public static final String Scene_Default_Home = "-3";

    @Override
    public int getGroupCount() {
        return groupInfoList!=null ? groupInfoList.size() : 0;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return zhujiInfoList!=null?zhujiInfoList.get(groupPosition).size():0;
    }

    public int getAllChildrenCount() {
        int a = 0;
        if (!CollectionsUtils.isEmpty(zhujiInfoList)){
            for (List<ZhujiInfos> zj: zhujiInfoList){
                a += zj.size();
            }
        }
        return a;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupInfoList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return zhujiInfoList.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return getCombinedChildId(groupPosition,childPosition);
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupView viewCache = null;
        if (convertView == null) {
            viewCache = new GroupView();
            convertView = layoutInflater.inflate(R.layout.item_zhiji_group_list, null, false);
            viewCache.group_playout = (RelativeLayout) convertView.findViewById(R.id.item_group_list_pl);
            viewCache.group_name = (TextView) convertView.findViewById(R.id.group_name);
            viewCache.group_imag = (ImageView) convertView.findViewById(R.id.group_imag);
            convertView.setTag(viewCache);
        } else {
            viewCache = (GroupView) convertView.getTag();
        }
        viewCache.group_name.setText(groupInfoList.get(groupPosition).getName());
        if (isExpanded){//展开
            viewCache.group_imag.setImageResource(R.drawable.down);
        }else{
            viewCache.group_imag.setImageResource(R.drawable.right);
        }
        if (groupPosition == getGroupCount() - 1 && groupInfoList.get(groupPosition).getId() == -1){//最后一项，隐藏掉
            viewCache.group_playout.setVisibility(View.GONE);
        }else{
            viewCache.group_playout.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition,final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHandler handler = null;
        final ZhujiInfos zhuji = zhujiInfoList.get(groupPosition).get(childPosition);
        final int devices = zhuji.getDevices();
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_zhiji_list, null, false);
            handler = new ViewHandler(convertView);
            convertView.setTag(handler);
        }else{
            handler = (ViewHandler) convertView.getTag();
        }
        final View view = convertView;
        handler.scene_arming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zhujiListAdapterOnclick.OnItemImgClickListener(groupPosition,childPosition, view, R.id.scene_arming);
            }

        });
        handler.scene_disarming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zhujiListAdapterOnclick.OnItemImgClickListener(groupPosition,childPosition, view, R.id.scene_disarming);
            }
        });
        handler.scene_home.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                zhujiListAdapterOnclick.OnItemImgClickListener(groupPosition,childPosition, view, R.id.scene_home);
            }

        });
        handler.dlist_imag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zhujiListAdapterOnclick.OnDlistImgClickListener(groupPosition,childPosition);
            }
        });
//        if (zhuji.getZhujiInfo().get)
//            convertView.setTag(handler);
//        }else {
//            handler = (ViewHandler) convertView.getTag();
//        }
        handler.setValue(zhuji);
        handler.setModen(zhuji.getZhujiInfo());
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;//返回false 表示不可点击。可由位置决定逻辑
    }

    /**
     * 视图内部类
     *
     * @author Administrator
     */
    class GroupView {
        RelativeLayout group_playout;
        TextView group_name;
        ImageView group_imag;
    }

    class ViewHandler {
        private TextView zjName, zjType, zhuji_users, zhuji_devices, zhuji_statu;
        ImageView device_logo, device_low, device_power, scene_arming, scene_disarming, scene_home, offline_imag, scene_imag,dlist_imag;
        BadgeView badgeView;

        public ViewHandler(View view) {
            zjName = (TextView) view.findViewById(R.id.zhuji_name);
            zjType = (TextView) view.findViewById(R.id.zhuji_type);
            zhuji_statu = (TextView) view.findViewById(R.id.zhuji_statu);
            zhuji_users = (TextView) view.findViewById(R.id.zhuji_users);
            zhuji_devices = (TextView) view.findViewById(R.id.zhuji_devices);
            device_logo = (ImageView) view.findViewById(R.id.device_logo);
            device_low = (ImageView) view.findViewById(R.id.device_low);
            device_power = (ImageView) view.findViewById(R.id.device_power);
            scene_arming = (ImageView) view.findViewById(R.id.scene_arming);
            scene_disarming = (ImageView) view.findViewById(R.id.scene_disarming);
            scene_home = (ImageView) view.findViewById(R.id.scene_home);
            offline_imag = (ImageView) view.findViewById(R.id.offline_imag);
            scene_imag = (ImageView) view.findViewById(R.id.scene_imag);
            dlist_imag = (ImageView) view.findViewById(R.id.dlist_imag);

            badgeView = new BadgeView(context, device_logo);
            badgeView.setBadgeMargin(0, 0);
            badgeView.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
            badgeView.setTextSize(10);
        }

        public void setValue(ZhujiInfos zhujis) {
            dlist_imag.setVisibility(View.GONE);
            ZhujiInfo object = zhujis.getZhujiInfo();
            zjName.setText(String.valueOf(object.getName()));
//            zhuji_users.setText(String.valueOf(object.getUc()) + " " + context.getString(R.string.deviceslist_server_totalonlineapps));
            zhuji_statu.setText((object.getWhere() == null ? "" : (object.getWhere() + " ")));
            if (DeviceInfo.CakMenu.zhuji.value().equals(object.getCak()) && object.getNr() != 0) {
                badgeView.setText(object.getNr() + "");
                badgeView.show();
            } else {
                badgeView.setVisibility(View.GONE);
            }
            if (DeviceInfo.CakMenu.zhuji.value().equals(object.getCak())) {
                if (DeviceInfo.CaMenu.zhujijzm.value().equals(object.getCa())||DeviceInfo.CaMenu.rqzj.value().equals(object.getCa())){//卷闸门主机,燃气主机
                    dlist_imag.setVisibility(View.VISIBLE);
                }else{
                    dlist_imag.setVisibility(View.GONE);
                }
                //只有主机才显示设备数、场景
                zhuji_devices.setVisibility(View.VISIBLE);
//                zhuji_devices.setText(zhujis.getDevices() + " " + context.getString(R.string.deviceslist_server_totaldevices));
                device_logo.setImageResource(object.getLogoResource());
//                ImageLoader.getInstance().displayImage(dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "")
//                        + "/devicelogo/" + object.getLogo(), device_logo, options, new MImageLoadingBar());
                if (object.getScene() != null && !"".equals(object.getScene())) {
                    scene_imag.setVisibility(View.VISIBLE);
                    if (Scene_Default_DesArming.equals(object.getScene())) {
                        scene_imag.setImageResource(R.drawable.zhzj_sl_chefang);
                    } else if (Scene_Default_Arming.equals(object.getScene())) {
                        scene_imag.setImageResource(R.drawable.zhzj_sl_shefang);
                    } else if (Scene_Default_Home.equals(object.getScene())) {
                        scene_imag.setImageResource(R.drawable.zhzj_sl_zaijia);
                    } else if (Scene_Default_No.equals(object.getScene())) {
                        scene_imag.setVisibility(View.GONE);
                    } else if ("0".equals(object.getScenet())) {
                        scene_imag.setImageResource(R.drawable.zhzj_sl_zdy);
                    } else if ("1".equals(object.getScenet())) {
                        scene_imag.setImageResource(R.drawable.zhzj_sl_dingshi);
                    } else if ("2".equals(object.getScenet())) {
                        scene_imag.setImageResource(R.drawable.zhzj_sl_liandong);
                    }
                }else{
                    scene_imag.setVisibility(View.GONE);
                }
            } else{
                zhuji_devices.setVisibility(View.GONE);
                scene_imag.setVisibility(View.GONE);
                // 设置图片
                ImageLoader.getInstance().displayImage(dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "")
                        + "/devicelogo/" + object.getLogo(), device_logo, options, new MImageLoadingBar());
            }

            zjType.setBackgroundColor(Color.TRANSPARENT);

            boolean isHaveBattery = false;
            if (!CollectionsUtils.isEmpty(zhujis.getZhujiInfo().getdCommands())){
                for (CommandInfo c : zhujis.getZhujiInfo().getdCommands()) {
                    if (CommandInfo.CommandTypeEnum.battery.value().equals(c.getCtype())){
                        isHaveBattery = true;
                        device_low.setVisibility(View.VISIBLE);
                        /**
                         * < 20%     : 0格；
                         20% - 35%  : 1格；
                         35% - 50%  : 2格；
                         50% - 70%  : 3格；
                         70% - 100% : 4格；
                         */
                        if (Integer.parseInt(c.getCommand()) < 20){
                            device_low.setImageBitmap(BitmapFactory.decodeResource(context.getResources(),R.drawable.dianliang0));
                        }else if(Integer.parseInt(c.getCommand()) >= 20 && Integer.parseInt(c.getCommand()) < 35){
                            device_low.setImageBitmap(BitmapFactory.decodeResource(context.getResources(),R.drawable.dianliang1));
                        }else if(Integer.parseInt(c.getCommand()) >= 35 && Integer.parseInt(c.getCommand()) < 50){
                            device_low.setImageBitmap(BitmapFactory.decodeResource(context.getResources(),R.drawable.dianliang2));
                        }else if(Integer.parseInt(c.getCommand()) >= 50 && Integer.parseInt(c.getCommand()) < 70){
                            device_low.setImageBitmap(BitmapFactory.decodeResource(context.getResources(),R.drawable.dianliang3));
                        }else{
                            device_low.setImageBitmap(BitmapFactory.decodeResource(context.getResources(),R.drawable.dianliang4));
                        }
                    }
                }
            }
            if (!isHaveBattery) {
                if (object.getBatteryStatus() == 1) { //低电
                    device_low.setVisibility(View.VISIBLE);
                    device_low.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.low));
                } else {
                    device_low.setVisibility(View.GONE);
                }
            }
            if (object.getPowerStatus() == 0) { //市电
                device_power.setImageResource(R.drawable.ic_power_normal);
            } else { //电池
                device_power.setImageResource(R.drawable.ic_power_battery);
            }
           //先前代码 if (DeviceInfo.CaMenu.ipcamera.value().equals(object.getCa()) && object.getCameraInfo()!=null && CameraInfo.CEnum.jiwei.value().equals(object.getCameraInfo().getC())) {
            if(DeviceInfo.CaMenu.ipcamera.value().equals(object.getCa())){
                offline_imag.setVisibility(View.INVISIBLE);
                stopAnim(offline_imag);
                zjType.setText(zhujis.isFlag() ? context.getString(R.string.deviceslist_server_zhuji_online) : context.getString(R.string.deviceslist_server_zhuji_offline));
                if (zhujis.isFlag()) {
                    offline_imag.setBackgroundResource(R.drawable.online_animlist);
                } else {
                    offline_imag.setBackgroundResource(R.drawable.offline_animlist);
                }
                startAnim(offline_imag);
            }else if(DeviceInfo.CaMenu.nbyg.value().equals(object.getCa())
                    ||DeviceInfo.CaMenu.nbrqbjq.value().equals(object.getCa())){
                zjType.setText(object.isOnline() ? context.getString(R.string.deviceslist_server_zhuji_online) : context.getString(R.string.deviceslist_server_zhuji_offline));
                    //nb烟感
                  nbYgSignalHandler(object,offline_imag);
            }else {
                offline_imag.setImageDrawable(null);
                offline_imag.setVisibility(View.GONE);
                zjType.setText(object.isOnline() ? context.getString(R.string.deviceslist_server_zhuji_online) : context.getString(R.string.deviceslist_server_zhuji_offline));
                if (object.isOnline()) {
                    offline_imag.setBackgroundResource(R.drawable.online_animlist);
                } else {
                    offline_imag.setBackgroundResource(R.drawable.offline_animlist);
                }
                startAnim(offline_imag);
            }


        }

        //nb烟感信号图标
        private void nbYgSignalHandler(ZhujiInfo zhujiInfo,ImageView displayView){
            offline_imag.setBackgroundDrawable(null);
            if(!zhujiInfo.isOnline()){
                //不在线处理
                offline_imag.setImageResource(R.drawable.nb_no_signal_pic);
            }else{
                List<CommandInfo> commandInfos = zhujiInfo.getdCommands() ;
                displayView.setImageResource(R.drawable.nb_four_signal_pic);//默认显示
                for(CommandInfo commandInfo : commandInfos){
                    if("164".equals(commandInfo.getCtype())){
                        if(!TextUtils.isDigitsOnly(commandInfo.getCommand())){
                            return ;
                        }
                        int signalValue = Integer.parseInt(commandInfo.getCommand());
//                    signalValue = 26;  //nb离线时，用于测试用的
                        if(signalValue<=0){
                            displayView.setImageResource(R.drawable.nb_no_signal_pic);
                        }else if(1<=signalValue&&signalValue<=7){
                            //显示一格信号
                            displayView.setImageResource(R.drawable.nb_one_signal_pic);
                        }else if(8<=signalValue&&signalValue<=15){
                            displayView.setImageResource(R.drawable.nb_two_signal_pic);
                        }else if(16<=signalValue&&signalValue<=23){
                            displayView.setImageResource(R.drawable.nb_three_signal_pic);
                        }else if(signalValue>=24){
                            displayView.setImageResource(R.drawable.nb_four_signal_pic);
                        }
                    }
                }
            }

        }

        private void stopAnim(ImageView img) {
            AnimationDrawable anim = (AnimationDrawable) img.getBackground();
            if (anim!= null && anim.isRunning()) {
                anim.stop();
            }
        }

        private void startAnim(ImageView img) {
            final AnimationDrawable anim = (AnimationDrawable) img.getBackground();
            img.post(new Runnable() {
                @Override
                public void run() {
                    anim.start();
                }
            });
        }

        private void setModen(ZhujiInfo zhuji) {
            if (DataCenterSharedPreferences.Constant.SCENE_NOW_SF.equals(zhuji.getScene())) {
                zjType.setVisibility(View.VISIBLE);
                zjType.setText(context.getString(R.string.activity_scene_item_outside_moden));
                scene_arming.setImageResource(R.drawable.scene_item_arming_pressed);
                scene_disarming.setImageResource(R.drawable.scene_item_disarming_normal);
                scene_home.setImageResource(R.drawable.scene_item_home_normal);
            } else if (DataCenterSharedPreferences.Constant.SCENE_NOW_CF.equals(zhuji.getScene())) {
                zjType.setText(context.getString(R.string.activity_scene_item_home_moden));
                scene_arming.setImageResource(R.drawable.scene_item_arming_normal);
                scene_disarming.setImageResource(R.drawable.scene_item_disarming_pressed);
                scene_home.setImageResource(R.drawable.scene_item_home_normal);
            } else if (DataCenterSharedPreferences.Constant.SCENE_NOW_HOME.equals(zhuji.getScene())) {
                zjType.setText(context.getString(R.string.activity_scene_item_inhome_moden));
                scene_arming.setImageResource(R.drawable.scene_item_arming_normal);
                scene_disarming.setImageResource(R.drawable.scene_item_disarming_normal);
                scene_home.setImageResource(R.drawable.scene_item_home_pressed);
            } else if (!"".equals(zhuji.getScene())) {
                zjType.setText(zhuji.getScene());
                SceneActivity.test = zhuji.getScene();
                zjType.setTextColor(Color.BLACK);
                scene_arming.setImageResource(R.drawable.scene_item_arming_normal);
                scene_disarming.setImageResource(R.drawable.scene_item_disarming_normal);
                scene_home.setImageResource(R.drawable.scene_item_home_normal);
            } else {
                zjType.setBackgroundColor(Color.TRANSPARENT);
                zjType.setVisibility(View.GONE);
                scene_arming.setImageResource(R.drawable.scene_item_arming_normal);
                scene_disarming.setImageResource(R.drawable.scene_item_disarming_normal);
                scene_home.setImageResource(R.drawable.scene_item_home_normal);
            }
        }
    }

    // 显示图片的配置
    DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.loading)
            .showImageOnFail(R.drawable.sorrow).cacheInMemory(true).cacheOnDisc(true)
            .bitmapConfig(Bitmap.Config.RGB_565).imageScaleType(ImageScaleType.EXACTLY_STRETCHED)// 设置图片以如何的编码方式显示
            .resetViewBeforeLoading(true)// 设置图片在下载前是否重置，复位
            // .displayer(new RoundedBitmapDisplayer(20))//是否设置为圆角，弧度为多少
            .displayer(new FadeInBitmapDisplayer(100))// 是否图片加载好后渐入的动画时间
            .build();

    public class MImageLoadingBar implements ImageLoadingListener {

        @Override
        public void onLoadingCancelled(String arg0, View arg1) {
            if (arg1 != null)
                arg1.clearAnimation();
        }

        @Override
        public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
            if (arg1 != null)
                arg1.clearAnimation();
        }

        @Override
        public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
            if (arg1 != null)
                arg1.clearAnimation();
        }

        @Override
        public void onLoadingStarted(String arg0, View arg1) {
            if (arg1 != null)
                arg1.startAnimation(imgloading_animation);
        }
    }

    public interface ZhujiListAdapterOnclick {
        public void OnItemImgClickListener(int groupPosition,int position, View view, int viewId);
        public void OnDlistImgClickListener(int groupPosition,int position);
    }
}
