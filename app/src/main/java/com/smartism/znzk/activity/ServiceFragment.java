package com.smartism.znzk.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.just.agentweb.AgentWeb;
import com.just.agentweb.WebChromeClient;
import com.just.agentweb.WebViewClient;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.device.DeviceMainActivity;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.SecurityUtil;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.OnItemClickListener;

/**
 * Created by win7 on 2016/11/5.
 */
@SuppressLint("SetJavaScriptEnabled")
public class ServiceFragment extends Fragment {
    private final String JS_OBJECT_NAME = "android"; //js调用原生方法的object名称

    /***mProgressBar 进度条参数**/
    public ProgressBar mProgressBar;
    public boolean isAnimStart = false;
    public int currentProgress;
    /**
     * end
     ***/

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
        mContext.bbsMenuWindow.update(2);
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            mContext.bbsMenuWindow.update(2);//显示show是调用
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!this.isHidden() && resumed) {
            //web_view1.reload(); //跳转回来会触发，下面的tab页签选择变动时会触发onHiddenChanged，不会触发此方法
            mContext.bbsMenuWindow.update(2);
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
        long uid = mContext.dcsp.getLong(DataCenterSharedPreferences.Constant.LOGIN_APPID, 0);
        String code = mContext.dcsp.getString(DataCenterSharedPreferences.Constant.LOGIN_CODE, "");
        String server = mContext.dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
//        server = "192.168.2.15:9999";
        String v = "";
        String n = Util.randomString(12);
        String s = SecurityUtil.createSign(v, uid, code, n);
        if (/*DataCenterSharedPreferences.Constant.ROLE_ASADMIN.equals(mContext.dcsp.getString(DataCenterSharedPreferences.Constant.LOGIN_ROLE, DataCenterSharedPreferences.Constant.ROLE_NORMAL))
                || */DataCenterSharedPreferences.Constant.ROLE_ASSERVICE.equals(mContext.dcsp.getString(DataCenterSharedPreferences.Constant.LOGIN_ROLE, DataCenterSharedPreferences.Constant.ROLE_NORMAL))) {
            String param = "uid=" + uid + "&n=" + n + "&s=" + s;
            pageUrl = server + "/interaction/sjob/index?" + param;
        } else {
            String param = "uid=" + uid + "&n=" + n + "&s=" + s;//工单
            pageUrl = server + "/interaction/wjob/index?" + param;
//          web_view.postUrl("http://192.168.2.6:9999/interaction/wjob/index",param.getBytes());
        }

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

        mAgentWeb.getJsInterfaceHolder().addJavaObject(JS_OBJECT_NAME,new JSObject(mContext));
    }


    private WebViewClient mWebViewClient = new WebViewClient() {

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

    public class JSObject {
        /*
         * 绑定的object对象
         * */
        private Context context;

        public JSObject(Context context) {
            this.context = context;
        }

        /*
         * JS调用android的方法  显示toast
         * @JavascriptInterface仍然必不可少
         *
         * */
        @JavascriptInterface
        public void showToast(String content) {
            Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
        }

        /*
         * JS调用android的方法  点击超链接打开新的activity
         * @JavascriptInterface仍然必不可少
         *
         * */
        @JavascriptInterface
        public void openNewViewWithGet(String url) {
            Intent intent = new Intent();
            intent.setClass(context.getApplicationContext(), CommonWebViewActivity.class);
            intent.putExtra("url", url);
            startActivity(intent);
        }
    }

}
