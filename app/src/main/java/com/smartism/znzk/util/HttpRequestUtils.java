package com.smartism.znzk.util;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.ActivityParentMonitorActivity;
import com.smartism.znzk.activity.FragmentParentActivity;
import com.smartism.znzk.application.MainApplication;
import com.zhy.http.okhttp.OkHttpUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.Response;

public class HttpRequestUtils {
    public static final String TAG = MainApplication.TAG;

    /**
     * 和下面一个方法一样，记得修改要同时修改
     *
     * @param urlstring
     * @param activity
     * @param defaultHandler
     * @return
     */
    public static String requestHttpServer(String urlstring, final ActivityParentActivity activity, Handler defaultHandler) {
        if (urlstring == null) {
//            Log.e(HttpRequestUtils.class.getName(), "开始发送http get请求：url为空，取消请求");
            return null;
        }
        if (!NetworkUtils.CheckNetwork(activity.getApplicationContext())) {
            defaultHandler.post(new Runnable() {

                @Override
                public void run() {
                    activity.cancelInProgress();
                    Toast.makeText(activity, activity.getString(R.string.net_error_nonet), Toast.LENGTH_LONG).show();
                }
            });
            return null;
        }
        LogUtil.i(HttpRequestUtils.class.getName(), "开始发送http get请求：url为" + urlstring);
        StringBuilder sb = new StringBuilder();
        InputStream in = null;
        try {
            URL url = new URL(urlstring);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10 * 1000);
            connection.setReadTimeout(20 * 1000);
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                in = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String len = null;
                while ((len = reader.readLine()) != null) {
                    sb.append(len);
                }
                reader.close();
            } else {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        activity.cancelInProgress();
                        Toast.makeText(activity, activity.getString(R.string.net_error_requestfailed), Toast.LENGTH_LONG).show();
                    }
                });
            }
        } catch (MalformedURLException e) {
            defaultHandler.post(new Runnable() {

                @Override
                public void run() {
                    activity.cancelInProgress();
                    Toast.makeText(activity, activity.getString(R.string.net_error_urlerror), Toast.LENGTH_LONG).show();
                }
            });
        } catch (IOException e) {
//            Log.e(HttpRequestUtils.class.getName(), "发送http - get 请求IO错误(网络不给力):", e);
            defaultHandler.post(new Runnable() {

                @Override
                public void run() {
                    activity.cancelInProgress();
                    Toast.makeText(activity, activity.getString(R.string.net_error_ioerror), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
//            /Log.e(HttpRequestUtils.class.getName(), "发送http - get 请求异常:", e);
            defaultHandler.post(new Runnable() {

                @Override
                public void run() {
                    activity.cancelInProgress();
                    Toast.makeText(activity, activity.getString(R.string.net_error_exception), Toast.LENGTH_LONG).show();
                }
            });
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        /***通用的返回值异常处理**/
        if (sb.toString() != null && "-100".equals(sb.toString())) {
            defaultHandler.post(new Runnable() {

                @Override
                public void run() {
                    activity.cancelInProgress();
                    Toast.makeText(activity, activity.getString(R.string.net_error_servererror), Toast.LENGTH_SHORT).show();
                }
            });
            return null;
        }
        return sb.toString();
    }

    /**
     * 和上面一个方法一样，记得修改要同时修改
     *
     * @param urlstring
     * @param activity
     * @param defaultHandler
     * @return
     */
    public static String requestHttpServer(String urlstring, final ActivityParentMonitorActivity activity, Handler defaultHandler) {
        if (urlstring == null) {
            Log.e(HttpRequestUtils.class.getName(), "开始发送http get请求：url为空，取消请求");
            return null;
        }
        if (!NetworkUtils.CheckNetwork(activity.getApplicationContext())) {
            defaultHandler.post(new Runnable() {

                @Override
                public void run() {
                    activity.cancelInProgress();
                    Toast.makeText(activity, activity.getString(R.string.net_error_nonet), Toast.LENGTH_LONG).show();
                }
            });
            return null;
        }
        LogUtil.i(HttpRequestUtils.class.getName(), "开始发送http get请求：url为" + urlstring);
        StringBuilder sb = new StringBuilder();
        InputStream in = null;
        try {
            URL url = new URL(urlstring);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10 * 1000);
            connection.setReadTimeout(10 * 1000);
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                in = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String len = null;
                while ((len = reader.readLine()) != null) {
                    sb.append(len);
                }
                reader.close();
            } else {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        activity.cancelInProgress();
                        Toast.makeText(activity, activity.getString(R.string.net_error_requestfailed), Toast.LENGTH_LONG).show();
                    }
                });
            }
        } catch (MalformedURLException e) {
            defaultHandler.post(new Runnable() {

                @Override
                public void run() {
                    activity.cancelInProgress();
                    Toast.makeText(activity, activity.getString(R.string.net_error_urlerror), Toast.LENGTH_LONG).show();
                }
            });
        } catch (IOException e) {
            Log.e(HttpRequestUtils.class.getName(), "发送http - get 请求IO错误(网络不给力):", e);
            defaultHandler.post(new Runnable() {

                @Override
                public void run() {
                    activity.cancelInProgress();
                    Toast.makeText(activity, activity.getString(R.string.net_error_ioerror), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Log.e(HttpRequestUtils.class.getName(), "发送http - get 请求异常:", e);
            defaultHandler.post(new Runnable() {

                @Override
                public void run() {
                    activity.cancelInProgress();
                    Toast.makeText(activity, activity.getString(R.string.net_error_exception), Toast.LENGTH_LONG).show();
                }
            });
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        /***通用的返回值异常处理**/
        if (sb.toString() != null && "-100".equals(sb.toString())) {
            defaultHandler.post(new Runnable() {

                @Override
                public void run() {
                    activity.cancelInProgress();
                    Toast.makeText(activity, activity.getString(R.string.net_error_servererror), Toast.LENGTH_SHORT).show();
                }
            });
            return null;
        }
        return sb.toString();
    }

    /**
     * 净量用上面一个方法，此方法为老的废弃方法
     *
     * @param url
     * @return
     * @throws Exception
     */
    public static String requestHttpServer(URL url) throws Exception {
        if (url == null) {
            Log.e(HttpRequestUtils.class.getName(), "开始发送http get请求：url为空，取消请求");
            return null;
        }
        LogUtil.i(HttpRequestUtils.class.getName(), "开始发送http get请求：url为" + url.toString());
        StringBuilder sb = new StringBuilder();
        InputStream in = null;
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10 * 1000);
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                in = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String len = null;
                while ((len = reader.readLine()) != null) {
                    sb.append(len);
                }
                reader.close();
            }
        } catch (IOException e) {
            Log.e(HttpRequestUtils.class.getName(), "发送http - get 请求IO错误(网络不给力):", e);
            throw e;
        } catch (Exception e) {
            Log.e(HttpRequestUtils.class.getName(), "发送http - get 请求异常:", e);
            throw e;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    /**
     * okhttp request 框架的请求
     *
     * @param url      http url
     * @param value    json格式的参数
     * @param activity 调用的activity
     * @return
     * @throws IOException
     */
    public static String requestoOkHttpPost(String url, JSONObject value, final ActivityParentActivity activity) {
        return requestoOkHttpPost(url, value, true, activity);
    }

    /**
     * okhttp request 框架的请求
     *
     * @param url         http url
     * @param value       json格式的参数
     * @param suportCrypt 是否支持加密 true支持 false不支持
     * @param activity    调用的activity
     * @return
     * @throws IOException
     */
    public static String requestoOkHttpPost(String url, JSONObject value, boolean suportCrypt, final ActivityParentActivity activity) {
        String result = "";
        if (!NetworkUtils.CheckNetwork(activity.getApplicationContext())) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {

                @Override
                public void run() {
                    activity.cancelInProgress();
                    Toast.makeText(activity, activity.getString(R.string.net_error_nonet), Toast.LENGTH_LONG).show();
                }
            });
            return result;
        }
        if (url == null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {

                @Override
                public void run() {
                    activity.cancelInProgress();
                    Toast.makeText(activity, activity.getString(R.string.net_error_programs), Toast.LENGTH_LONG).show();
                }
            });
            return result;
        }
        long uid = activity.getDcsp().getLong(DataCenterSharedPreferences.Constant.LOGIN_APPID, 0);
        String code = activity.getDcsp().getString(DataCenterSharedPreferences.Constant.LOGIN_CODE, "");
        if (url.contains("s3/u/login") || url.contains("s3/u/gzhlogin") || url.contains("s3/sms/sendcode") || url.contains("/jdm/serverChange")) {//登录请求不需要uid验证，登出和踢下线dcsp里面的数据偶尔会不清除
            uid = 0;
            code = "";
        }
        String v = "";
        if (value != null) {
            try {
                if (suportCrypt) {
                    v = SecurityUtil.cryptToHexString(value.toJSONString(), MainApplication.app.getAppGlobalConfig().getAppSecret());
                } else {
                    v = value.toJSONString();
                }
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        activity.cancelInProgress();
                        Toast.makeText(activity, activity.getString(R.string.net_error_requestfailed), Toast.LENGTH_LONG).show();
                    }
                });
                return result;
            }
        }
        String n = Util.randomString(12);
        String s = SecurityUtil.createSign(v, MainApplication.app.getAppGlobalConfig().getAppid(), MainApplication.app.getAppGlobalConfig().getAppSecret(), code, n);
        try {
            Response response = OkHttpUtils.post().url(url)
                    .addParams("uid", String.valueOf(uid))
                    .addParams("appid", MainApplication.app.getAppGlobalConfig().getAppid())
                    .addParams("v", v)
                    .addParams("n", n)
                    .addParams("s", s)
                    .build().execute();

            if (response != null && response.isSuccessful()) {
                result = response.body().string();
                if (result != null && "-1".equals(result)) {
                    new Handler(activity.getMainLooper()).post(new Runnable() {

                        @Override
                        public void run() {
                            activity.cancelInProgress();
                            Toast.makeText(activity, activity.getString(R.string.net_error_programs), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if ("-2".equals(result)) {
                    LogUtil.e(activity.getApplicationContext(), "HTTPREQUEST", "出现校验失败情况：n：" + n + "，v：" + v + ",uid:" + uid + ",code:" + code + ",s:" + s);
                    new Handler(activity.getMainLooper()).post(new Runnable() {

                        @Override
                        public void run() {
                            activity.cancelInProgress();
                            Intent intent = new Intent();
                            intent.setAction(Actions.APP_RECONNECTION); //重新连接服务器
                            activity.sendBroadcast(intent);
                            Toast.makeText(activity, activity.getString(R.string.net_error_illegal_request), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if ("-100".equals(result)) {
                    new Handler(activity.getMainLooper()).post(new Runnable() {

                        @Override
                        public void run() {
                            activity.cancelInProgress();
                            Toast.makeText(activity, activity.getString(R.string.net_error_servererror), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if ("-101".equals(result)) {
                    new Handler(activity.getMainLooper()).post(new Runnable() {

                        @Override
                        public void run() {
                            activity.cancelInProgress();
                            Toast.makeText(activity, activity.getString(R.string.net_error_loginoutofday), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (result == null || "".equals(result)) {
                    new Handler(activity.getMainLooper())
                            .post(new Runnable() {

                                @Override
                                public void run() {
                                    activity.cancelInProgress();
                                    Toast.makeText(activity, activity.getString(R.string.net_error_requestfailed), Toast.LENGTH_LONG).show();
                                }
                            });
                }

                return result;
            } else {
                new Handler(activity.getMainLooper())
                        .post(new Runnable() {

                            @Override
                            public void run() {
                                activity.cancelInProgress();
                                Toast.makeText(activity, activity.getString(R.string.net_error_requestfailed), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        } catch (IOException e) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {

                @Override
                public void run() {
                    activity.cancelInProgress();
                    Toast.makeText(activity, activity.getString(R.string.net_error_ioerror), Toast.LENGTH_LONG).show();
                }
            });
        }
        return result;
    }

    /**
     * okhttp request 框架的请求
     *
     * @param url      http url
     * @return
     * @throws IOException
     */
    public static String requestoOkHttpGet(String url) {
        String result = "";
        try {
            Response response = OkHttpUtils.get().url(url).build().execute();
            if (response != null && response.isSuccessful()) {
                result = response.body().string();
                Log.d(TAG, "sendHttprequest: success,back body：" + result);
            }
        } catch (IOException e) {
            Log.e(TAG,"请求http get error ：",e);
        }
        return result;
    }

    /**
     * okhttp request 框架的请求
     *
     * @param url      http url
     * @param value    json格式的参数
     * @param activity 调用的activity
     * @return
     * @throws IOException
     */
    public static String requestoOkHttpPost(String url, JSONObject value, final FragmentParentActivity activity) {
        return requestoOkHttpPost(url, value, true, activity);
    }

    /**
     * @param url      http url
     * @param value    json格式的参数
     * @param activity 调用的activity
     * @return
     * @throws IOException
     */
    public static String requestoOkHttpPost(String url, JSONObject value, boolean suportCrypt, final FragmentParentActivity activity) {
        String result = "";
        if (!NetworkUtils.CheckNetwork(activity.getApplicationContext())) {
            new Handler(Looper.getMainLooper()).post(() ->{
                activity.cancelInProgress();
                ToastUtil.longMessage(activity.getString(R.string.net_error_nonet));
            });
            return result;
        }
        if (url == null) {
            new Handler(Looper.getMainLooper()).post(() ->{
                activity.cancelInProgress();
                ToastUtil.longMessage(activity.getString(R.string.net_error_programs));
            });
            return result;
        }
        long uid = activity.getDcsp().getLong(DataCenterSharedPreferences.Constant.LOGIN_APPID, 0);
        String code = activity.getDcsp().getString(DataCenterSharedPreferences.Constant.LOGIN_CODE, "");
        if (url.contains("s3/u/login") || url.contains("s3/u/gzhlogin") || url.contains("s3/sms/sendcode")) {//登录请求不需要uid验证，登出和踢下线dcsp里面的数据偶尔会不清除
            uid = 0;
            code = "";
        }
        String v = "";
        if (value != null) {
            try {
                if (suportCrypt) {
                    v = SecurityUtil.cryptToHexString(value.toJSONString(), MainApplication.app.getAppGlobalConfig().getAppSecret());
                } else {
                    v = value.toJSONString();
                }
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() ->{
                        activity.cancelInProgress();
                    ToastUtil.longMessage(activity.getString(R.string.net_error_requestfailed));
                });
                return result;
            }
        }
        String n = Util.randomString(12);
        String s = SecurityUtil.createSign(v, MainApplication.app.getAppGlobalConfig().getAppid(), MainApplication.app.getAppGlobalConfig().getAppSecret(), code, n);
        try {
            Response response = OkHttpUtils.post().url(url)
                    .addParams("uid", String.valueOf(uid))
                    .addParams("appid", MainApplication.app.getAppGlobalConfig().getAppid())
                    .addParams("v", v)
                    .addParams("n", n)
                    .addParams("s", s)
                    .build().execute();
            Log.d(TAG, "sendHttprequest: url:"+ url + "v:" + v + " n:" + n + " s:" + s + " appid:" + MainApplication.app.getAppGlobalConfig().getAppid() + " uid:" + uid);
            if (response != null && response.isSuccessful()) {
                result = response.body().string();
                Log.d(TAG, "sendHttprequest: success,back body："+result);
                if (result != null && "-1".equals(result)) {
                    new Handler(activity.getMainLooper()).post(() -> {
                            activity.cancelInProgress();
                            ToastUtil.longMessage(activity.getString(R.string.net_error_programs));
                    });
                } else if ("-2".equals(result)) {
                    LogUtil.e(activity.getApplicationContext(), "HTTPREQUEST", "出现校验失败情况：n：" + n + "，v：" + v + ",uid:" + uid + ",code:" + code + ",s:" + s);
                    new Handler(activity.getMainLooper()).post(() ->{
                        activity.cancelInProgress();
                        Intent intent = new Intent();
                        intent.setAction(Actions.APP_RECONNECTION); //重新连接服务器
                        activity.sendBroadcast(intent);
                        ToastUtil.longMessage(activity.getString(R.string.net_error_illegal_request));
                    });
                } else if ("-100".equals(result)) {
                    new Handler(activity.getMainLooper()).post(() ->{
                        activity.cancelInProgress();
                        ToastUtil.longMessage(activity.getString(R.string.net_error_servererror));
                    });
                } else if ("-101".equals(result)) {
                    new Handler(activity.getMainLooper()).post(() ->{
                        activity.cancelInProgress();
                        ToastUtil.longMessage(activity.getString(R.string.net_error_loginoutofday));
                    });
                } else if ("-103".equals(result)) {
                    new Handler(activity.getMainLooper()).post(() ->{
                        activity.cancelInProgress();
                        ToastUtil.longMessage(activity.getString(R.string.net_error_sendtimeout));
                    });
                } else if (result == null || "".equals(result)) {
                    new Handler(activity.getMainLooper())
                            .post(() ->{
                                activity.cancelInProgress();
                                ToastUtil.longMessage(activity.getString(R.string.net_error_requestfailed));
                            });
                }

                return result;
            } else {
                new Handler(activity.getMainLooper())
                        .post(() ->{
                            activity.cancelInProgress();
                            ToastUtil.longMessage(activity.getString(R.string.net_error_requestfailed));
                        });
            }
        } catch (IOException e) {
            new Handler(Looper.getMainLooper()).post(() ->{
                    activity.cancelInProgress();
                ToastUtil.longMessage(activity.getString(R.string.net_error_ioerror));
            });
        }
        return result;
    }

    /**
     * 和上面一样 就最后一个参数不一样
     *
     * @param url      http url
     * @param value    json格式的参数
     * @param activity 调用的activity
     * @return
     * @throws IOException
     */
    public static String requestoOkHttpPost(String url, JSONObject value, final ActivityParentMonitorActivity activity) {
        String result = "";
        if (!NetworkUtils.CheckNetwork(activity.getApplicationContext())) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {

                @Override
                public void run() {
                    activity.cancelInProgress();
                    Toast.makeText(activity, activity.getString(R.string.net_error_nonet), Toast.LENGTH_LONG).show();
                }
            });
            return result;
        }
        if (url == null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {

                @Override
                public void run() {
                    activity.cancelInProgress();
                    Toast.makeText(activity, activity.getString(R.string.net_error_programs), Toast.LENGTH_LONG).show();
                }
            });
            return result;
        }
        long uid = activity.getDcsp().getLong(DataCenterSharedPreferences.Constant.LOGIN_APPID, 0);
        String code = activity.getDcsp().getString(DataCenterSharedPreferences.Constant.LOGIN_CODE, "");
        String v = "";
        if (value != null) {
            try {
                v = SecurityUtil.cryptToHexString(value.toJSONString(), MainApplication.app.getAppGlobalConfig().getAppSecret());
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {

                    @Override
                    public void run() {
                        activity.cancelInProgress();
                        Toast.makeText(activity, activity.getString(R.string.net_error_requestfailed), Toast.LENGTH_LONG).show();
                    }
                });
                return result;
            }
        }
        String n = Util.randomString(12);
        String s = SecurityUtil.createSign(v, MainApplication.app.getAppGlobalConfig().getAppid(), MainApplication.app.getAppGlobalConfig().getAppSecret(), code, n);
        try {
            Response response = OkHttpUtils.post().url(url)
                    .addParams("uid", String.valueOf(uid))
                    .addParams("appid", MainApplication.app.getAppGlobalConfig().getAppid())
                    .addParams("v", v)
                    .addParams("n", n)
                    .addParams("s", s)
                    .build().execute();

            if (response != null && response.isSuccessful()) {

                result = response.body().string();

                if (result != null && "-1".equals(result)) {
                    new Handler(activity.getMainLooper()).post(new Runnable() {

                        @Override
                        public void run() {
                            activity.cancelInProgress();
                            Toast.makeText(activity, activity.getString(R.string.net_error_programs), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if ("-2".equals(result)) {
                    LogUtil.e(activity.getApplicationContext(), "HTTPREQUEST", "出现校验失败情况：n：" + n + "，v：" + v + ",uid:" + uid + ",code:" + code + ",s:" + s);
                    new Handler(activity.getMainLooper()).post(new Runnable() {

                        @Override
                        public void run() {
                            activity.cancelInProgress();
                            Intent intent = new Intent();
                            intent.setAction(Actions.APP_RECONNECTION); //重新连接服务器
                            activity.sendBroadcast(intent);
                            Toast.makeText(activity, activity.getString(R.string.net_error_illegal_request), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if ("-100".equals(result)) {
                    new Handler(activity.getMainLooper()).post(new Runnable() {

                        @Override
                        public void run() {
                            activity.cancelInProgress();
                            Toast.makeText(activity, activity.getString(R.string.net_error_servererror), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if ("-101".equals(result)) {
                    new Handler(activity.getMainLooper()).post(new Runnable() {

                        @Override
                        public void run() {
                            activity.cancelInProgress();
                            Toast.makeText(activity, activity.getString(R.string.net_error_loginoutofday), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (result == null || "".equals(result)) {
                    new Handler(activity.getMainLooper())
                            .post(new Runnable() {

                                @Override
                                public void run() {
                                    activity.cancelInProgress();
                                    Toast.makeText(activity, activity.getString(R.string.net_error_requestfailed), Toast.LENGTH_LONG).show();
                                }
                            });
                }

                return result;
            } else {
                new Handler(activity.getMainLooper())
                        .post(new Runnable() {

                            @Override
                            public void run() {
                                activity.cancelInProgress();
                                Toast.makeText(activity, activity.getString(R.string.net_error_requestfailed), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        } catch (IOException e) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {

                @Override
                public void run() {
                    activity.cancelInProgress();
                    Toast.makeText(activity, activity.getString(R.string.net_error_ioerror), Toast.LENGTH_LONG).show();
                }
            });
        }
        return result;
    }

    /**
     * @param url   http url
     * @param value json格式的参数
     * @return
     * @throws IOException
     */
    public static String requestoOkHttpPost(String url, JSONObject value,DataCenterSharedPreferences dcsp) {
        String result = "";
        if (url == null) {
            return result;
        }
        long uid = dcsp.getLong(DataCenterSharedPreferences.Constant.LOGIN_APPID, 0);
        String code = dcsp.getString(DataCenterSharedPreferences.Constant.LOGIN_CODE, "");
        String v = "";
        if (value != null) {
            try {
                v = SecurityUtil.cryptToHexString(value.toJSONString(), MainApplication.app.getAppGlobalConfig().getAppSecret());
            } catch (Exception e) {
                Log.e("HTTP", "requestoOkHttpPost: ", e);
                return result;
            }
        }
        String n = Util.randomString(12);
        String s = SecurityUtil.createSign(v, MainApplication.app.getAppGlobalConfig().getAppid(), MainApplication.app.getAppGlobalConfig().getAppSecret(), code, n);
        try {
            Response response = OkHttpUtils.post().url(url)
                    .addParams("uid", String.valueOf(uid))
                    .addParams("appid", MainApplication.app.getAppGlobalConfig().getAppid())
                    .addParams("v", v)
                    .addParams("n", n)
                    .addParams("s", s)
                    .build().execute();

            if (response != null && response.isSuccessful()) {

                result = response.body().string();

                if (result != null && "-1".equals(result)) {
                    Log.e("HTTP", "requestoOkHttpPost: -1");
                } else if ("-2".equals(result)) {
                    Log.e("HTTP", "requestoOkHttpPost: -2");
                } else if ("-100".equals(result)) {
                    Log.e("HTTP", "requestoOkHttpPost: -100");
                } else if ("-101".equals(result)) {
                    Log.e("HTTP", "requestoOkHttpPost: -101");
                }

                return result;
            } else {
                Log.e("HTTP", "requestoOkHttpPost: not 200");
            }
        } catch (IOException e) {
            Log.e("HTTP", "requestoOkHttpPost: ", e);
        }
        return result;
    }
}