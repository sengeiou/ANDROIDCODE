package com.smartism.znzk.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.common.AboutActivity;
import com.smartism.znzk.activity.common.CLDTimeSetActivity;
import com.smartism.znzk.activity.common.DeviceSortStyleActivity;
import com.smartism.znzk.activity.common.NetworkDiagnosisActivity;
import com.smartism.znzk.activity.common.XZSWAboutActivity;
import com.smartism.znzk.activity.device.DeviceMainActivity;
import com.smartism.znzk.activity.user.GenstureInitActivity;
import com.smartism.znzk.activity.user.GenstureSettingActivity;
import com.smartism.znzk.activity.user.UserInfoActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.CollectionsUtils;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.view.CircleImageView;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.OnItemClickListener;


import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * Created by win7 on 2016/11/5.
 */
@SuppressLint("SetJavaScriptEnabled")
public class MineFragment extends Fragment implements View.OnClickListener {

    private DeviceMainActivity mContext;
    public CircleImageView userinfo_logo;
    public TextView tv_cld_time,tv_cld_pwd,tv_a;
    private TextView tv_userinfo_name, tv_gensture_status, tip_sort,tv_about;
    private LinearLayout ll_cld_time,ll_cld_pwd, ll_setting_gensture, ll_about, ll_sort, ll_update,ll_privacy,ll_networdDisgnosis;
    private ImageView iv_temp;
    private RelativeLayout rl_user;
    private InputMethodManager imm;

    public int cldTime = 0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = (DeviceMainActivity) context;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mContext.dcsp.getString(Constant.SHOW_DLISTSORT, "zhineng").equals("zhineng")) {
            tip_sort.setText(getString(R.string.setting_activity_zhineng));
        } else {
            tip_sort.setText(getString(R.string.setting_activity_addsort));
        }
        tv_userinfo_name.setText(mContext.dcsp.getString(DataCenterSharedPreferences.Constant.LOGIN_APPNAME, ""));
        String patternString = mContext.dcsp.getString(Constant.CODE_GENSTURE, "");
        if (TextUtils.isEmpty(patternString)  /*patternString == null*/) {
            tv_gensture_status.setText(R.string.gesture_unInit);
        } else {
            mContext.runOnUiThread(new Runnable() {
                public void run() {
                    if (mContext.dcsp.getBoolean(Constant.IS_APP_GENSTURE, false)) {
                        tv_gensture_status.setText(R.string.gensture_open);
                    } else {
                        tv_gensture_status.setText(R.string.gensture_off);
                    }
                }
            });
        }
    }

    public void initView(View view) {
        imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        rl_user = (RelativeLayout) view.findViewById(R.id.rl_user);
        userinfo_logo = (CircleImageView) view.findViewById(R.id.userinfo_logo);
        tv_userinfo_name = (TextView) view.findViewById(R.id.tv_userinfo_name);
        tv_about = (TextView) view.findViewById(R.id.tv_about);
        tv_cld_time = (TextView) view.findViewById(R.id.tv_cld_time);
        tv_cld_pwd = (TextView) view.findViewById(R.id.tv_cld_pwd);
        tv_gensture_status = (TextView) view.findViewById(R.id.tv_gensture_status);
        tip_sort = (TextView) view.findViewById(R.id.tip_sort);
        tv_a  = view.findViewById(R.id.tv_a);
        tv_a.setText(Html.fromHtml(getResources().getString(R.string.activity_privacy_policy)));


        ll_setting_gensture = (LinearLayout) view.findViewById(R.id.ll_setting_gensture);
        ll_cld_time = (LinearLayout) view.findViewById(R.id.ll_cld_time);
        ll_cld_pwd = (LinearLayout) view.findViewById(R.id.ll_cld_pwd);
        ll_about = (LinearLayout) view.findViewById(R.id.ll_about);
        ll_sort = (LinearLayout) view.findViewById(R.id.ll_sort);
        ll_update = (LinearLayout) view.findViewById(R.id.ll_update);
        ll_privacy = view.findViewById(R.id.ll_privacy);
        ll_networdDisgnosis = view.findViewById(R.id.ll_network_disgnosis);

        iv_temp = (ImageView) view.findViewById(R.id.iv_temp);

        if (MainApplication.app.getAppGlobalConfig().isSupportGestures()){
            ll_setting_gensture.setVisibility(View.VISIBLE);
        }
        ll_privacy.setOnClickListener(this);
        ll_cld_time.setOnClickListener(this);
        ll_cld_pwd.setOnClickListener(this);
        rl_user.setOnClickListener(this);
        ll_about.setOnClickListener(this);
        ll_sort.setOnClickListener(this);
        ll_setting_gensture.setOnClickListener(this);
        iv_temp.setOnClickListener(this);
        ll_update.setOnClickListener(this);
        ll_networdDisgnosis.setOnClickListener(this);

        ll_update.setVisibility(MainApplication.app.getAppGlobalConfig().isAutomaticUpdates() ? View.VISIBLE : View.GONE);
        tv_about.setText(R.string.about_s);

        view.findViewById(R.id.ll_sort).setVisibility(View.GONE);
    }

    private void initData() {
        hashMap = new HashMap<>();
        hashMap.put(0, 0);
        hashMap.put(15, 30);
        hashMap.put(30, 60);
        hashMap.put(45, 90);
        hashMap.put(60, 120);
        hashMap.put(75, 150);
        hashMap.put(5, 0);
        hashMap.put(90, 180);
        hashMap.put(105, 210);
        hashMap.put(120, 240);
        hashMap.put(135, 270);
        hashMap.put(150, 300);
        if (mContext.dcsp.getString(Constant.SHOW_TEMPERATURE_UNIT, "ssd").equals("ssd")) {
            iv_temp.setImageResource(R.drawable.zhzj_sz_c);

        } else if (mContext.dcsp.getString(Constant.SHOW_TEMPERATURE_UNIT, "ssd").equals("hsd")) {
            iv_temp.setImageResource(R.drawable.zhzj_sz_f);
        }

        if (mContext.dcsp.getString(Constant.SHOW_DLISTSORT, "zhineng").equals("zhineng")) {
            tip_sort.setText(getString(R.string.setting_activity_zhineng));
        } else {
            tip_sort.setText(getString(R.string.setting_activity_addsort));
        }

        if (!"".equals(mContext.dcsp.getString(Constant.LOGIN_LOGO, ""))) {
            ImageLoader.getInstance().displayImage(mContext.dcsp.getString(Constant.LOGIN_LOGO, ""), userinfo_logo, options_userlogo);
        } else {
            userinfo_logo.setImageResource(R.drawable.h0);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 10000 && resultCode == 10) {
            String time = data.getStringExtra("time");
            cldTime = Integer.parseInt(time);
            tv_cld_time.setText((cldTime == 5) ? "测试" : hashMap.get(cldTime) + getString(R.string.cld_time_squre));
        }
    }

    private HashMap<Integer, Integer> hashMap;

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.ll_privacy:
                Intent disintent = new Intent();
                disintent.setClass(getContext(), DisclaimerActivity.class);
                startActivity(disintent);
                break ;
            case R.id.ll_update:
                mContext.checkUpdate();
                break;
            case R.id.rl_user:
//                intent.setClass(mContext, UserInfoActivity.class);
//                startActivity(intent);
                break;
            case R.id.ll_setting_gensture:
                String patternString = mContext.dcsp.getString(Constant.CODE_GENSTURE, "");
                if (TextUtils.isEmpty(patternString)) {
                    startActivity(new Intent(mContext, GenstureInitActivity.class));
                } else {
                    startActivity(new Intent(mContext, GenstureSettingActivity.class));
                }
                break;
            case R.id.ll_cld_time:
                if (!(mContext.getMainShowFragment() instanceof DeviceMainFragment)) {
                    Toast.makeText(mContext, "当前不可设置", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!mContext.getZhuji().isAdmin()) {
                    Toast.makeText(mContext, "主账户才能设置", Toast.LENGTH_SHORT).show();
                    return;
                }
                intent.setClass(mContext, CLDTimeSetActivity.class);
                intent.putExtra("zhuji", mContext.getZhuji());
                intent.putExtra("time", cldTime);
                startActivityForResult(intent, 10000);
                break;
            case R.id.ll_cld_pwd:
                if (!(mContext.getMainShowFragment() instanceof DeviceMainFragment)) {
                    Toast.makeText(mContext, "当前不可设置", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!mContext.getZhuji().isAdmin()) {
                    Toast.makeText(mContext, "主账户才能设置", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
            case R.id.ll_sort:
                intent = new Intent();
                intent.setClass(mContext, DeviceSortStyleActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_about:
                intent = new Intent();
                if (Actions.VersionType.CHANNEL_QYJUNHONG.equals(MainApplication.app.getAppGlobalConfig().getVersion()) ||
                        Actions.VersionType.CHANNEL_ANBABAOQUAN.equals(MainApplication.app.getAppGlobalConfig().getVersion())||
                        Actions.VersionType.CHANNEL_RUNLONG.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                    intent.setClass(mContext, XZSWAboutActivity.class);
                } else if(Actions.VersionType.CHANNEL_JAOLH.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
                    intent.setClass(mContext,CommonWebViewActivity.class);
                    String url = getString(R.string.smart_url_home);
                    intent.putExtra("url",url);
                    intent.putExtra("title","");
                }else {
                    intent.setClass(mContext, AboutActivity.class);
                }
//                intent.setClass(mContext,BbsWebViewActivity.class);
//                long uid =  DataCenterSharedPreferences.getInstance(mContext, DataCenterSharedPreferences.Constant.CONFIG).getLong(DataCenterSharedPreferences.Constant.LOGIN_APPID, 0);
//                String code = DataCenterSharedPreferences.getInstance(mContext, DataCenterSharedPreferences.Constant.CONFIG).getString(DataCenterSharedPreferences.Constant.LOGIN_CODE, "");
//                String n = Util.randomString(12);
//                String s = SecurityUtil.createSign("",MainApplication.app.getAppGlobalConfig().getAppid(),MainApplication.app.getAppGlobalConfig().getAppSecret(),code,n);
//                intent.putExtra("url","http://dev.smart-ism.com/shop/store/messageindex?uid="+uid+"&s="+s+"&n="+n+"&appid="+MainApplication.app.getAppGlobalConfig().getAppid());
                startActivity(intent);

                break;
            case R.id.iv_temp:
                if (mContext.dcsp.getString(Constant.SHOW_TEMPERATURE_UNIT, "ssd").equals("ssd")) {
                    iv_temp.setImageResource(R.drawable.zhzj_sz_f);
                    mContext.dcsp.putString(Constant.SHOW_TEMPERATURE_UNIT, "hsd").commit();
                } else {
                    mContext.dcsp.putString(Constant.SHOW_TEMPERATURE_UNIT, "ssd").commit();
                    iv_temp.setImageResource(R.drawable.zhzj_sz_c);
                }
                intent = new Intent();
                intent.setAction(Actions.REFRESH_DEVICES_LIST); // 发送一个广播刷新页面
                mContext.sendBroadcast(intent);
                break;
            case R.id.ll_network_disgnosis:
                intent.setClass(mContext, NetworkDiagnosisActivity.class);
                startActivity(intent);
                break;
        }

    }

    // logo图片的配置
    DisplayImageOptions options_userlogo = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.loading)
            .showImageOnFail(R.drawable.h0).cacheInMemory(true).cacheOnDisc(true)
            .bitmapConfig(Bitmap.Config.RGB_565).imageScaleType(ImageScaleType.EXACTLY_STRETCHED)// 设置图片以如何的编码方式显示
            .resetViewBeforeLoading(true)// 设置图片在下载前是否重置，复位
            .displayer(new RoundedBitmapDisplayer(40))// 是否设置为圆角，弧度为多少
            .displayer(new FadeInBitmapDisplayer(100))// 是否图片加载好后渐入的动画时间
            .build();
}
