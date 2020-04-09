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
import android.util.Log;
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

import com.alibaba.fastjson.JSONObject;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.WebChromeClient;
import com.just.agentweb.WebViewClient;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.device.DeviceMainActivity;
import com.smartism.znzk.activity.device.ZhujiListFragment;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.SecurityUtil;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.OnItemClickListener;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Created by win7 on 2016/11/5.
 */
@SuppressLint("SetJavaScriptEnabled")
public class ServiceNewFragment extends Fragment {

    public static final String TAG = ServiceNewFragment.class.getSimpleName();
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
        if (!hidden) {
            initWebViewData();
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
        String masterID = ZhujiListFragment.getMasterId() ;
        List<ZhujiInfo> zhujiLists = DatabaseOperator.getInstance(mContext).queryAllZhuJiInfos();
        boolean needReturn = false;
        if (zhujiInfo == null){
            if (StringUtils.isEmpty(masterID)){
                if (zhujiLists.size() == 1){
                    zhujiInfo = zhujiLists.get(0);
                }
            }else{
                if (!zhujiLists.isEmpty()) {
                    for (ZhujiInfo zj : zhujiLists) {
                        if (zj.getMasterid().equals(masterID)) {
                            zhujiInfo = zj;
                            break;
                        }
                    }
                }
            }
        }else{
            if (StringUtils.isEmpty(masterID)){
                if(zhujiLists.size() == 1){
                    if (zhujiLists.get(0).getId() != zhujiInfo.getId()){
                        zhujiInfo = zhujiLists.get(0);
                    }else{
                        return;
                    }
                }else{
                    zhujiInfo = null;
                }
            }else{
                ZhujiInfo tmpZhuji = new ZhujiInfo();
                for (ZhujiInfo zj: zhujiLists) {
                    if (zj.getMasterid().equals(masterID)){
                        tmpZhuji = zj;
                    }
                }
                if (tmpZhuji.getId() != 0 && tmpZhuji.getId() != zhujiInfo.getId()){
                    zhujiInfo = tmpZhuji;
                }else{
                    return;
                }
            }
        }

        pageUrl = "file:///android_asset/appserver.html";

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
            Log.e(TAG, " loadurl: " + url);
            //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
            if (url.contains("jdmnew://")) {
                Intent intent = new Intent();
                intent.setClass(mContext, CommonWebViewActivity.class);
                intent.putExtra("url", url.replace("jdmnew://", ""));
                startActivity(intent);
                return true;
            } else if (url.contains("jdmnohistory://")) {
                view.clearCache(true);
                view.loadUrl(url.replace("jdmnohistory://", ""));
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        view.clearHistory();
                    }
                }, 500);
                return true;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (request.getUrl().toString().contains("jdmnew://")) {
                Intent intent = new Intent();
                intent.setClass(mContext, CommonWebViewActivity.class);
                intent.putExtra("url", request.getUrl().toString().replace("jdmnew://", ""));
                startActivity(intent);
                return true;
            } else if (request.getUrl().toString().contains("jdmnohistory://")) {
                view.clearCache(true);
                view.loadUrl(request.getUrl().toString().replace("jdmnohistory://", ""));
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        view.clearHistory();
                    }
                }, 500);
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

    private View mErrorView;
    boolean mIsErrorPage;

//    protected void showErrorPage() {
//        RelativeLayout webParentView = (RelativeLayout) web_view1.getParent();
//        initErrorPage();//初始化自定义页面
//        while (webParentView.getChildCount() > 1) {
//            webParentView.removeViewAt(0);
//        }
//        @SuppressWarnings("deprecation")
//        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewPager.LayoutParams.FILL_PARENT, ViewPager.LayoutParams.FILL_PARENT);
//        webParentView.addView(mErrorView, 0, lp);
//        mIsErrorPage = true;
//    }

    /****
     * 把系统自身请求失败时的网页隐藏
     */
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
//                    web_view1.reload();
                }
            });
            mErrorView.setOnClickListener(null);
        }
    }

}
