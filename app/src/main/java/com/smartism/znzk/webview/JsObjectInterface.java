package com.smartism.znzk.webview;

import android.webkit.JavascriptInterface;

public interface JsObjectInterface {
        /*
         * JS调用android的方法  显示toast
         * @JavascriptInterface仍然必不可少
         *
         * */
        @JavascriptInterface
        public void  showToast(String content);
        /*
         * JS调用android的方法  显示toast和finish当前页
         * @JavascriptInterface仍然必不可少
         *
         * */
        @JavascriptInterface
        public void  showToastAndFinish(String content);
        /*
         * JS调用android的方法  显示toast和finish当前页
         * @JavascriptInterface仍然必不可少
         *
         * */
        @JavascriptInterface
        public void  showAlertAndCloseSelfAndRefreshParent(String content);
        /*
         * JS调用android的方法  finish当前页
         * @JavascriptInterface仍然必不可少
         *
         * */
        @JavascriptInterface
        public void  closeMyself();
        /*
         * JS调用android的方法  finish当前页 提示并刷新父窗体
         * @JavascriptInterface仍然必不可少
         *
         * */
        @JavascriptInterface
        public void  closeSelfAndRefreshParent();
        /*
         * JS调用android的方法  点击超链接打开新的activity
         * @JavascriptInterface仍然必不可少
         *
         * */
        @JavascriptInterface
        public void  openNewViewWithGet(String url);
        /*
         * JS调用android的方法  点击超链接打开新的activity
         * @JavascriptInterface仍然必不可少
         *
         * */
        @JavascriptInterface
        public void  openNewAddViewWithGet(String url);

        /*
         * JS调用android的方法  支持的原生支付
         * @JavascriptInterface仍然必不可少
         *
         */
        @JavascriptInterface
        public String supportPayType();

        /**
         * 前往支付
         * @param result
         * @param type
         */
        @JavascriptInterface
        public void pay(String result, String type);

        /**
         * 刷新当前页并清空历史回退
         */
        @JavascriptInterface
        public void refreshAndClearHistory();
        /**
         * 页面跳转到url 并清空历史回退
         * @param url
         */
        @JavascriptInterface
        public void toUrlAndClearHistory(String url);

        /**
         * 为适配ios新的wkWebview，android和ios的接口统一
         */
        @JavascriptInterface
        public void postMessage(String params);
}
