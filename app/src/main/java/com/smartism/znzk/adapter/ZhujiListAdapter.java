package com.smartism.znzk.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.domain.camera.CameraInfo;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.view.BadgeView;

import java.util.List;

/**
 * Created by Administrator on 2017/3/27.
 */

public class ZhujiListAdapter extends BaseAdapter {
    private List<ZhujiInfos> list;
    private Context context;
    private LayoutInflater layoutInflater;
    private ZhujiListAdapterOnclick zhujiListAdapterOnclick;
    public DataCenterSharedPreferences dcsp = null;
    public Animation imgloading_animation;

    public void setZhujiListAdapterOnclick(ZhujiListAdapterOnclick zhujiListAdapterOnclick) {
        this.zhujiListAdapterOnclick = zhujiListAdapterOnclick;
    }

    public ZhujiListAdapter(List<ZhujiInfos> list, Context context) {
        this.list = list;
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        dcsp = DataCenterSharedPreferences.getInstance(context,
                DataCenterSharedPreferences.Constant.CONFIG);
        imgloading_animation = AnimationUtils.loadAnimation(context, R.anim.loading_revolve);
        imgloading_animation.setInterpolator(new LinearInterpolator());
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHandler handler = null;
        final ZhujiInfos zhuji = list.get(position);
        final int devices = zhuji.getDevices();
//        if (convertView == null) {
        convertView = layoutInflater.inflate(R.layout.item_zhiji_list, null, false);
        handler = new ViewHandler(convertView);
        final View view = convertView;
        handler.scene_arming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zhujiListAdapterOnclick.OnItemImgClickListener(position, view, R.id.scene_arming);
            }

        });
        handler.scene_disarming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zhujiListAdapterOnclick.OnItemImgClickListener(position, view, R.id.scene_disarming);
            }
        });
        handler.scene_home.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                zhujiListAdapterOnclick.OnItemImgClickListener(position, view, R.id.scene_home);
            }

        });
//        if (zhuji.getZhujiInfo().get)
//            convertView.setTag(handler);
//        }else {
//            handler = (ViewHandler) convertView.getTag();
//        }
        handler.setValue(zhuji);
//        handler.setModen(zhuji.getZhujiInfo());
        return convertView;
    }

    // 0默认手动，1、自动，2、联动 默认场景无此字段
    public static final int SecuritySceneType_Normal = 0;
    public static final int SecuritySceneType_Time = 1;
    public static final int SecuritySceneType_Trigger = 2;
    public static final String Scene_Default_DesArming = "0";
    public static final String Scene_Default_Arming = "-1";
    public static final String Scene_Default_No = "-2";
    public static final String Scene_Default_Home = "-3";

    public class ViewHandler {
        private TextView zjName, zjType, zhuji_users, zhuji_devices, zhuji_statu;
        ImageView device_logo, device_low, device_power, scene_arming, scene_disarming, scene_home, offline_imag, scene_imag;
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

            badgeView = new BadgeView(context, device_logo);
            badgeView.setBadgeMargin(0, 0);
            badgeView.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
            badgeView.setTextSize(10);
        }

        public void setValue(ZhujiInfos zhujis) {
            ZhujiInfo object = zhujis.getZhujiInfo();
            zjName.setText(String.valueOf(object.getName()));
            zhuji_users.setText(String.valueOf(object.getUc()) + " " + context.getString(R.string.deviceslist_server_totalonlineapps));
            zhuji_statu.setText((object.getWhere() == null ? "" : (object.getWhere() + " ")));
            if (DeviceInfo.CaMenu.zhuji.value().equals(object.getCa()) && object.getNr() != 0) {
                badgeView.setText(object.getNr() + "");
                badgeView.show();
            } else {
                badgeView.setVisibility(View.GONE);
            }
            if (DeviceInfo.CakMenu.zhuji.value().equals(object.getCak())) {
                //只有主机才显示设备数、场景
                zhuji_devices.setVisibility(View.VISIBLE);
                zhuji_devices.setText(zhujis.getDevices() + " " + context.getString(R.string.deviceslist_server_totaldevices));
//                device_logo.setImageResource(R.drawable.zhzj_host_zaixian);
                ImageLoader.getInstance().displayImage(dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "")
                        + "/devicelogo/" + object.getLogo(), device_logo, options, new MImageLoadingBar());
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
                }
            } else if (DeviceInfo.CaMenu.ipcamera.value().equals(object.getCa())) {
                // 设置图片
                ImageLoader.getInstance().displayImage(dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "")
                        + "/devicelogo/" + object.getLogo(), device_logo, options, new MImageLoadingBar());
            } else if (DeviceInfo.CaMenu.djkzq.value().equals(object.getCa())) {
                // 设置图片
                ImageLoader.getInstance().displayImage( dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "")
                        + "/devicelogo/" + object.getLogo(), device_logo, options, new MImageLoadingBar());
            }


            zjType.setBackgroundColor(Color.TRANSPARENT);
            if (object.getBatteryStatus() == 1) { //低电
                device_low.setVisibility(View.VISIBLE);
            } else {
                device_low.setVisibility(View.GONE);
            }
            if (object.getPowerStatus() == 0) { //市电
                device_power.setImageResource(R.drawable.ic_power_normal);
            } else { //电池
                device_power.setImageResource(R.drawable.ic_power_battery);
            }

            if (DeviceInfo.CaMenu.ipcamera.value().equals(object.getCa()) && object.getCameraInfo()!=null && CameraInfo.CEnum.jiwei.value().equals(object.getCameraInfo().getC())) {
                stopAnim(offline_imag);
                zjType.setText(zhujis.isFlag() ? context.getString(R.string.deviceslist_server_zhuji_online) : context.getString(R.string.deviceslist_server_zhuji_offline));
                if (zhujis.isFlag()) {
                    offline_imag.setBackgroundResource(R.drawable.online_animlist);
                } else {
                    offline_imag.setBackgroundResource(R.drawable.offline_animlist);
                }
            } else {
                zjType.setText(object.isOnline() ? context.getString(R.string.deviceslist_server_zhuji_online) : context.getString(R.string.deviceslist_server_zhuji_offline));
                if (object.isOnline()) {
                    offline_imag.setBackgroundResource(R.drawable.online_animlist);
                } else {
                    offline_imag.setBackgroundResource(R.drawable.offline_animlist);
                }
            }
            startAnim(offline_imag);

        }

        private void stopAnim(ImageView img) {
            AnimationDrawable anim = (AnimationDrawable) img.getBackground();
            if (anim.isRunning()) {
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
            } else if (!zhuji.getScene().equals("")) {
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
        public void OnItemImgClickListener(int position, View view, int viewId);
    }
}
