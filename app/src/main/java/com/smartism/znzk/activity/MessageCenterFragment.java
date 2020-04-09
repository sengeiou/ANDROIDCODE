package com.smartism.znzk.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.just.agentweb.AgentWeb;
import com.just.agentweb.WebChromeClient;
import com.just.agentweb.WebViewClient;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.SmartMedicine.SmartMedicineMainActivity;
import com.smartism.znzk.activity.camera.ApMonitorActivity;
import com.smartism.znzk.activity.device.BeiJingMaoYanActivity;
import com.smartism.znzk.activity.device.BeijingSuoActivity;
import com.smartism.znzk.activity.device.DeviceInfoActivity;
import com.smartism.znzk.activity.device.DeviceMainActivity;
import com.smartism.znzk.activity.device.HoshooIPCActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.domain.camera.CameraInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.SecurityUtil;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.OnItemClickListener;
import com.smartism.znzk.xiongmai.activities.XiongMaiDisplayCameraActivity;

import org.apache.commons.lang.StringUtils;

/**
 * Created by win7 on 2016/11/5.
 */
@SuppressLint("SetJavaScriptEnabled")
public class MessageCenterFragment extends Fragment {

    public static final String TAG = MessageCenterFragment.class.getSimpleName();
    /***mProgressBar 进度条参数**/
    public ProgressBar mProgressBar;
    public boolean isAnimStart = false;
    public int currentProgress;
    /**
     * end
     ***/
    private ZhujiInfo zhujiInfo;
    private DeviceMainActivity mContext;
    private AgentWeb mAgentWeb;
    private LinearLayout mAgentWebLayout;
    private boolean resumed = false;
    private String pageUrl; //当前webview正在请求的url，在webviewclient中用此url进行判断友好提示错误页

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = (DeviceMainActivity) getActivity();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service, container, false);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && MainApplication.app.isNeedRefreshMcenter()) {
            initWebViewData();
            MainApplication.app.setNeedRefreshMcenter(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!this.isHidden() && resumed) {
//            web_view1.reload(); //跳转回来会触发，下面的tab页签选择变动时会触发onHiddenChanged，不会触发此方法
        }
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        resumed = true;
    }

    public void initView(View view) {
        mAgentWebLayout = (LinearLayout) view.findViewById(R.id.web_view_layout);
        initWebViewData();
    }

    private void initWebViewData() {
        String server = mContext.getDcsp().getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
        long uid = mContext.getDcsp().getLong(DataCenterSharedPreferences.Constant.LOGIN_APPID, 0);
        String code = mContext.getDcsp().getString(DataCenterSharedPreferences.Constant.LOGIN_CODE, "");

        String n = Util.randomString(12);
        String s = SecurityUtil.createSign(null, MainApplication.app.getAppGlobalConfig().getAppid(), MainApplication.app.getAppGlobalConfig().getAppSecret(), code, n);

        pageUrl = server + "/jdm/page/mcenter/index?v=&uid="+uid+"&n="+n+"&s="+s+"&appid="+ MainApplication.app.getAppGlobalConfig().getAppid();

        if (mAgentWeb == null) {
            mAgentWeb = AgentWeb.with(this)
                    .setAgentWebParent(mAgentWebLayout, new LinearLayout.LayoutParams(-1, -1))
                    .useDefaultIndicator()
                    .interceptUnkownUrl()
                    .setWebChromeClient(mWebChromeClient)
                    .setWebViewClient(mWebViewClient)
                    .createAgentWeb()
                    .ready()
                    .go(pageUrl);
        }else{
            mAgentWeb.getUrlLoader().loadUrl(pageUrl);
        }
    }

    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
            if (url.contains("jdmnew://")) {
                try {
                    startDevicePage(url.replace("jdmnew://", ""));
                }catch (Exception ex){
                    LogUtil.e(mContext,TAG,"消息中心点击出现异常：",ex);
                }
                return true;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
            if (request.getUrl().toString().contains("jdmnew://")) {
                try {
                    startDevicePage(request.getUrl().toString().replace("jdmnew://", ""));
                }catch (Exception ex){
                    LogUtil.e(mContext,TAG,"消息中心点击出现异常：",ex);
                }
                return true;
            }
            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setAlpha(1.0f);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            new AlertView(getString(R.string.tips), getString(R.string.notification_error_ssl_cert_invalid), getString(R.string.cancel),
                    new String[]{getString(R.string.sure)}, null, mContext, AlertView.Style.Alert, new OnItemClickListener() {

                @Override
                public void onItemClick(Object o, int position) {
                    if (position != -1) {
                        handler.proceed();
                    }else {
                        handler.cancel();
                    }
                }
            }).show();
        }
    };
    private WebChromeClient mWebChromeClient = new WebChromeClient() {

    };


    /**
     * progressBar递增动画
     */
    private void startProgressAnimation(int newProgress) {
        ObjectAnimator animator = ObjectAnimator.ofInt(mProgressBar, "progress", currentProgress, newProgress);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }

    /**
     * progressBar消失动画
     */
    private void startDismissAnimation(final int progress) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(mProgressBar, "alpha", 1.0f, 0.0f);
        anim.setDuration(1500);  // 动画时长
        anim.setInterpolator(new DecelerateInterpolator());     // 减速
        // 关键, 添加动画进度监听器
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fraction = valueAnimator.getAnimatedFraction();      // 0.0f ~ 1.0f
                int offset = 100 - progress;
                mProgressBar.setProgress((int) (progress + offset * fraction));
            }
        });

        anim.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                // 动画结束
                mProgressBar.setProgress(0);
                mProgressBar.setVisibility(View.GONE);
                isAnimStart = false;
            }
        });
        anim.start();
    }

    public class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView webView, int newProgress) {
            super.onProgressChanged(webView, newProgress);
            currentProgress = mProgressBar.getProgress();
            if (newProgress >= 100 && !isAnimStart) {
                // 防止调用多次动画
                isAnimStart = true;
                mProgressBar.setProgress(newProgress);
                // 开启属性动画让进度条平滑消失
                startDismissAnimation(mProgressBar.getProgress());
            } else {
                // 开启属性动画让进度条平滑递增
                startProgressAnimation(newProgress);
            }
        }
    }


    private View mErrorView;
    boolean mIsErrorPage;

//    protected void showErrorPage() {
//        LinearLayout webParentView = (LinearLayout) web_view1.getParent();
//        initErrorPage();//初始化自定义页面
//        while (webParentView.getChildCount() > 1) {
//            webParentView.removeViewAt(0);
//        }
//        @SuppressWarnings("deprecation")
//        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewPager.LayoutParams.FILL_PARENT, ViewPager.LayoutParams.FILL_PARENT);
//        webParentView.addView(mErrorView, 0, lp);
//        mIsErrorPage = true;
//    }
//
//    /****
//     * 把系统自身请求失败时的网页隐藏
//     */
//    protected void hideErrorPage() {
//        LinearLayout webParentView = (LinearLayout) web_view1.getParent();
//        mIsErrorPage = false;
//        while (webParentView.getChildCount() > 1) {
//            webParentView.removeViewAt(0);
//        }
//    }

    /***
     * 显示加载失败时自定义的网页
     */
    protected void initErrorPage() {
        if (mErrorView == null) {
            mErrorView = View.inflate(mContext, R.layout.activity_error, null);
            RelativeLayout layout = (RelativeLayout) mErrorView.findViewById(R.id.online_error_btn_retry);
            layout.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mAgentWeb.getUrlLoader().reload();
                }
            });
            mErrorView.setOnClickListener(null);
        }
    }

    public Contact buildContact(CameraInfo info) {
        Contact contact = null;
        if (info != null) {
            contact = new Contact();
            contact.contactId = info.getId();
            contact.contactName = info.getN();
            contact.contactPassword = info.getP();
            contact.userPassword = info.getOriginalP();
        }
        return contact;
    }

    /**
     * 打开设备信息和主机列表和设备列表的打开方法类似
     */
    private void startDevicePage(String param) throws Exception{
        if (StringUtils.isEmpty(param)){
            return;
        }
        String[] p = param.split("/");
        if (p.length >= 2){
            if (DeviceInfo.CakMenu.zhuji.value().equals(p[0])) {
                ZhujiInfo zj = DatabaseOperator.getInstance(mContext).queryDeviceZhuJiInfo(Long.parseLong(p[1]));
                if (zj!=null){
                    Intent intent = new Intent();
                    intent.setClass(mContext, DeviceInfoActivity.class);
                    intent.putExtra("device", Util.getZhujiDevice(zj));
                    startActivity(intent);
                }
            } else if (DeviceInfo.CaMenu.ipcamera.value().equals(p[0])) {
                ZhujiInfo zj = DatabaseOperator.getInstance(mContext).queryDeviceZhuJiInfo(Long.parseLong(p[1]));
                if (zj!=null){
                    Intent intent = new Intent();
                    if (CameraInfo.CEnum.jiwei.value().equals(zj.getCameraInfo().getC())) {
                        intent.setClass(mContext, ApMonitorActivity.class);
                    }else if(CameraInfo.CEnum.hoshoo.value().equals(zj.getCameraInfo().getC())){
                        intent.setClass(mContext, HoshooIPCActivity.class);
                    }else if(CameraInfo.CEnum.xiongmai.value().equals(zj.getCameraInfo().getC())){
                        intent.setClass(mContext, XiongMaiDisplayCameraActivity.class);
                    }
                    intent.putExtra("contact", buildContact(zj.getCameraInfo()));
                    intent.putExtra("deviceInfo", Util.getZhujiDevice(zj));
                    startActivity(intent);
                }
            } else if (DeviceInfo.CaMenu.maoyan.value().equals(p[0])) {
                DeviceInfo device = DatabaseOperator.getInstance(mContext).queryDeviceInfo(Long.parseLong(p[1]));
                if (device!=null){
                    Intent deviceIntent = new Intent();
                    deviceIntent.setClass(mContext.getApplicationContext(), BeiJingMaoYanActivity.class);
                    deviceIntent.putExtra("device", device);
                    startActivity(deviceIntent);
                }
            } else if (DeviceInfo.CaMenu.znyx.value().equals(p[0])) {
                DeviceInfo device = DatabaseOperator.getInstance(mContext).queryDeviceInfo(Long.parseLong(p[1]));
                if (device!=null){
                    Intent deviceIntent = new Intent();
                    deviceIntent.setClass(mContext.getApplicationContext(), SmartMedicineMainActivity.class);
                    deviceIntent.putExtra("device", device);
                    startActivity(deviceIntent);
                }
            } else if (Actions.VersionType.CHANNEL_RUNLONG.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                        && DeviceInfo.CaMenu.zhinengsuo.value().equals(p[0])) {
                DeviceInfo device = DatabaseOperator.getInstance(mContext).queryDeviceInfo(Long.parseLong(p[1]));
                if (device!=null){
                    Intent deviceIntent = new Intent();
                    deviceIntent.setClass(mContext.getApplicationContext(), BeijingSuoActivity.class);
                    deviceIntent.putExtra("device", device);
                    startActivity(deviceIntent);
                }
            } else if ((Actions.VersionType.CHANNEL_AIERFUDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                        || Actions.VersionType.CHANNEL_ZNZK.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                        || Actions.VersionType.CHANNEL_UHOME.equals(MainApplication.app.getAppGlobalConfig().getVersion()))
                        && DeviceInfo.CaMenu.zhinengsuo.value().equals(p[0])) {
                DeviceInfo device = DatabaseOperator.getInstance(mContext).queryDeviceInfo(Long.parseLong(p[1]));
                if (device!=null){
                    Intent deviceIntent = new Intent();
                    deviceIntent.setClass(mContext.getApplicationContext(), ZSSuoNewActivity.class);
                    deviceIntent.putExtra("device", device);
                    startActivity(deviceIntent);
                }
            }
//            else if ((!Actions.VersionType.CHANNEL_AIERFUDE.equals(MainApplication.app.getAppGlobalConfig().getVersion()) && !Actions.VersionType.CHANNEL_ZHILIDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())) &&
//                        (DeviceInfo.CaMenu.yangan.value().equals(p[0]) || DeviceInfo.CaMenu.cazuo.value().equals(p[0]) || DeviceInfo.CaMenu.menci.value().equals(p[0]))) {
//                DeviceInfo device = DatabaseOperator.getInstance(mContext).queryDeviceInfo(Long.parseLong(p[1]));
//                if (device!=null){
//                    //门磁、烟感、排插
//                    Intent deviceIntent = new Intent();
//                    deviceIntent.setClass(mContext.getApplicationContext(), SecurityInfoActivity.class);
//                    deviceIntent.putExtra("device", device);
//                    mContext.showInProgress(getString(R.string.loading), false, true);
////                    if (!StringUtils.isEmpty(device.getBipc()) && !"0".equals(device.getBipc())) {
////                        JavaThreadPool.getInstance().excute(new DeviceMainFragment.BindingCameraLoad(device.getBipc()));
////                    } else {
//                        mContext.cancelInProgress();
//                        startActivity(deviceIntent);
////                    }
//                }
//
//            }
            else { // 其他
                DeviceInfo device = DatabaseOperator.getInstance(mContext).queryDeviceInfo(Long.parseLong(p[1]));
                if (device!=null) {
                    Intent deviceIntent = new Intent();
                    deviceIntent.setClass(mContext.getApplicationContext(), DeviceInfoActivity.class);

                    deviceIntent.putExtra("device", device);
                    mContext.showInProgress(getString(R.string.loading), false, true);
//                    if (!StringUtils.isEmpty(device.getBipc()) && !"0".equals(device.getBipc())) {
//                        JavaThreadPool.getInstance().excute(new DeviceMainFragment.BindingCameraLoad(device.getBipc()));
//                    } else {
                        mContext.cancelInProgress();
                        startActivity(deviceIntent);
//                    }
                }
            }
        }
    }

}
