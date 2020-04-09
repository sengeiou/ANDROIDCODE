package com.smartism.znzk.util.update;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.FragmentParentActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.NetworkUtils;
import com.smartism.znzk.util.SecurityUtil;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.AlertView.Style;
import com.smartism.znzk.view.alertview.OnItemClickListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class UpdateAgent {
    private static UpdateListener updateListener;
    private static Handler f;
    private static boolean isNotice = true;//是否再次提醒

    public static boolean isNotice() {
        return isNotice;
    }

    public static void setNotice(boolean notice) {
        isNotice = notice;
    }

    public static UpdateListener getUpdateListener() {
        return updateListener;
    }

    public static void setUpdateListener(UpdateListener updateListener) {
        UpdateAgent.updateListener = updateListener;
    }


    public static void update(final ActivityParentActivity context) {
        f = new Handler(context.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (updateListener != null) {
                    if (msg.what == 99999) {
                        updateListener.onUpdateReturned(msg.arg1, (UpdateResponse) msg.obj);
                    } else {
                        updateListener.onUpdateReturned(msg.what, null);
                    }
                }
                if (msg.what == UpdateStatus.DOWNLOAD_PROGRESS) { //下载进度
//                    context.createNotification(context, true, msg.arg1, msg.arg2, 10086);
//                    context.showOrUpdateProgressBar(context.getString(R.string.Downloading), true, msg.arg1, msg.arg2);
                } else if (msg.what == UpdateStatus.DOWNLOAD_SUCCESS) { //下载完成
//                    context.cancelInProgressBar();
//                    context.cancleNotification(context, 10086);
                    if (msg.arg1 == 1) {
                        Util.install(context, Uri.fromFile((File) msg.obj));
                    } else if (msg.arg1 == 0) {
                        new AlertView(context.getString(R.string.tips), context.getString(R.string.Fileerror), context.getString(R.string.sure), null, null, context, Style.Alert, null).show();
                    } else {
                        //Toast.makeText(context, R.string.DownloadingFailed, Toast.LENGTH_SHORT).show();
                    }
                } else if (msg.what == 99998) {
                    showUpdateDialog(context, (UpdateResponse) msg.obj);
                }
            }
        };

        JavaThreadPool.getInstance().excute(new Runnable() {
            public void run() {
                if (!NetworkUtils.CheckNetwork(context.getApplicationContext())) {
                    f.sendEmptyMessage(UpdateStatus.NONET);
                    return;
                }
                // 发送http请求，请求更新
                DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(context,
                        DataCenterSharedPreferences.Constant.CONFIG);
                String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                try {
                    PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                    pJsonObject.put("v", packageInfo.versionName);
                } catch (NameNotFoundException e) {
                }
                pJsonObject.put("t", 1);
                pJsonObject.put("p", MainApplication.app.getAppGlobalConfig().getVersionPrefix());
                String path = server + "/jdm/app/update?v="
                        + URLEncoder.encode(SecurityUtil.crypt(pJsonObject.toJSONString(), Constant.KEY_HTTP));
                LogUtil.i(HttpRequestUtils.class.getName(), "开始发送http get请求：url为" + path);
                Log.i(HttpRequestUtils.class.getName(), "开始发送http get请求：url为" + path);
                StringBuilder sb = new StringBuilder();
                InputStream in = null;
                try {
                    URL url = new URL(path);
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

                        JSONObject object = JSONObject
                                .parseObject(SecurityUtil.decrypt(sb.toString(), Constant.KEY_HTTP));
                        UpdateAgent.setNotice(false);
                        MainApplication.app.setNotice(false);
                        if ("no".equals(object.getString("update"))) {
                            f.sendEmptyMessage(UpdateStatus.NO);
                        } else if ("yes".equals(object.getString("update"))) {
                            UpdateResponse response = new UpdateResponse();
                            response.setName(object.getString("name"));
                            response.setLog(object.getString("log"));
                            response.setMd5(object.getString("md5"));
                            response.setSize(object.getIntValue("size"));
                            response.setVersion(object.getString("version"));
                            boolean isUpdate = getPackageInfo(context, response.getVersion());
                            if (!isUpdate)
                                return;
                            if (updateListener != null) {
                                Message m = f.obtainMessage(99999);
                                m.arg1 = UpdateStatus.YES;
                                m.obj = response;
                                f.sendMessage(m);
                            } else {
                                Message m = f.obtainMessage(99998);
                                m.obj = response;
                                f.sendMessage(m);

                            }
                        }
                        return;
                    } else {
                        throw new Exception("请求失败，返回码为:" + connection.getResponseCode());
                    }
                } catch (IOException e) {
                    Log.e(HttpRequestUtils.class.getName(), "发送http - get 请求IO错误(网络不给力):", e);
                    f.sendEmptyMessage(UpdateStatus.TIMEOUT);
                } catch (Exception e) {
                    Log.e(HttpRequestUtils.class.getName(), "发送http - get 请求异常:", e);
                    f.sendEmptyMessage(UpdateStatus.ERROR);
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                f.sendEmptyMessage(UpdateStatus.TIMEOUT);
            }
        });
    }

    public static void update(final FragmentParentActivity context) {
        f = new Handler(context.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (updateListener != null) {
                    if (msg.what == 99999) {
                        updateListener.onUpdateReturned(msg.arg1, (UpdateResponse) msg.obj);
                    } else {
                        updateListener.onUpdateReturned(msg.what, null);
                    }
                }
                if (msg.what == UpdateStatus.DOWNLOAD_PROGRESS) { //下载进度
                    context.showOrUpdateProgressBar(context.getString(R.string.Downloading), true, msg.arg1, msg.arg2);
                } else if (msg.what == UpdateStatus.DOWNLOAD_SUCCESS) { //下载完成
                    context.cancelInProgressBar();
                    if (msg.arg1 == 1) {
                        Util.install(context, Uri.fromFile((File) msg.obj));
                    } else if (msg.arg1 == 0) {
                        new AlertView(context.getString(R.string.tips), context.getString(R.string.Fileerror), context.getString(R.string.sure), null, null, context, Style.Alert, null).show();
                    } else {
                        //Toast.makeText(context, R.string.DownloadingFailed, Toast.LENGTH_SHORT).show();
                    }
                } else if (msg.what == 99998) {
                    showUpdateDialog(context, (UpdateResponse) msg.obj);
                }
            }
        };

        JavaThreadPool.getInstance().excute(new Runnable() {
            public void run() {
                if (!NetworkUtils.CheckNetwork(context.getApplicationContext())) {
                    f.sendEmptyMessage(UpdateStatus.NONET);
                    return;
                }
                // 发送http请求，请求更新
                DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(context,
                        DataCenterSharedPreferences.Constant.CONFIG);
                String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                try {
                    PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                    pJsonObject.put("v", packageInfo.versionName);
                } catch (NameNotFoundException e) {
                }
                pJsonObject.put("t", 1);
                pJsonObject.put("p", MainApplication.app.getAppGlobalConfig().getVersionPrefix());
                String path = server + "/jdm/app/update?v="
                        + URLEncoder.encode(SecurityUtil.crypt(pJsonObject.toJSONString(), Constant.KEY_HTTP));
                LogUtil.i(HttpRequestUtils.class.getName(), "开始发送http get请求：url为" + path);
                StringBuilder sb = new StringBuilder();
                InputStream in = null;
                try {
                    URL url = new URL(path);
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

                        JSONObject object = JSONObject
                                .parseObject(SecurityUtil.decrypt(sb.toString(), Constant.KEY_HTTP));
                        if ("no".equals(object.getString("update"))) {
                            f.sendEmptyMessage(UpdateStatus.NO);
                        } else if ("yes".equals(object.getString("update"))) {
                            UpdateResponse response = new UpdateResponse();
                            response.setName(object.getString("name"));
                            response.setLog(object.getString("log"));
                            response.setMd5(object.getString("md5"));
                            response.setSize(object.getIntValue("size"));
                            response.setVersion(object.getString("version"));
                            boolean isUpdate = getPackageInfo(context, response.getVersion());
                            if (!isUpdate) {
                                f.sendEmptyMessage(UpdateStatus.NO);
                                return;
                            }
                            if (updateListener != null) {
                                if (isUpdate) {
                                    Message m = f.obtainMessage(99999);
                                    m.arg1 = UpdateStatus.YES;
                                    m.obj = response;
                                    f.sendMessage(m);
                                } else {
                                    f.sendEmptyMessage(UpdateStatus.NO);
                                }
                            } else {
                                Message m = f.obtainMessage(99998);
                                m.obj = response;
                                f.sendMessage(m);
                            }
                        }
                        return;
                    } else {
                        throw new Exception("请求失败，返回码为:" + connection.getResponseCode());
                    }
                } catch (IOException e) {
                    Log.e(HttpRequestUtils.class.getName(), "发送http - get 请求IO错误(网络不给力):", e);
                    f.sendEmptyMessage(UpdateStatus.TIMEOUT);
                } catch (Exception e) {
                    Log.e(HttpRequestUtils.class.getName(), "发送http - get 请求异常:", e);
                    f.sendEmptyMessage(UpdateStatus.ERROR);
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                f.sendEmptyMessage(UpdateStatus.TIMEOUT);
            }
        });
    }

    public static boolean getPackageInfo(Context context, String version) {
        boolean update = false;
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo packageInfo = manager.getPackageInfo(context.getPackageName(), 0);
            String[] remoteStrs = version.split("\\.");
            String[] currentStrs = packageInfo.versionName.split("\\.");
            if (remoteStrs.length >= 1 && currentStrs.length >= 1 && Integer.parseInt(remoteStrs[0]) > Integer.parseInt(currentStrs[0])){
                update = true;
            }
            if (remoteStrs.length >= 2 && currentStrs.length >= 2 && Integer.parseInt(remoteStrs[0]) == Integer.parseInt(currentStrs[0]) && Integer.parseInt(remoteStrs[1]) > Integer.parseInt(currentStrs[1])){
                update = true;
            }
            if (remoteStrs.length >= 3 && currentStrs.length >= 3 && Integer.parseInt(remoteStrs[0]) == Integer.parseInt(currentStrs[0]) && Integer.parseInt(remoteStrs[1]) == Integer.parseInt(currentStrs[1]) && Integer.parseInt(remoteStrs[2]) > Integer.parseInt(currentStrs[2])){
                update = true;
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        return update;
    }

    public static void showUpdateDialog(final ActivityParentActivity activity, final UpdateResponse updateInfo) {
        StringBuilder content = new StringBuilder();
        content.append(activity.getString(R.string.NewVersion));
        content.append("V");
        content.append(updateInfo.getVersion());
//		content.append("\r\n");
//		content.append(activity.getString(R.string.UpdateContent));
//		content.append("\r\n");
//		content.append(updateInfo.getLog());
        content.append("\r\n");
        content.append(activity.getString(R.string.TargetSize));
        content.append((updateInfo.getSize() / 1000000) + "M");

        new AlertView(activity.getString(R.string.UpdateTitle), content.toString(), activity.getString(R.string.NotNow),
                new String[]{activity.getString(R.string.UpdateNow)}, null, activity, AlertView.Style.Alert,
                new OnItemClickListener() {

                    @Override
                    public void onItemClick(Object o, int position) {
                        if (position != -1) {
                            // 开始更新
                            JavaThreadPool.getInstance().excute(new Runnable() {
                                public void run() {
                                    // 发送http请求，请求更新
                                    DataCenterSharedPreferences dcsp = activity.getDcsp();
                                    String server = dcsp
                                            .getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                                    JSONObject pJsonObject = new JSONObject();
                                    pJsonObject.put("t", 1);
                                    pJsonObject.put("p", MainApplication.app.getAppGlobalConfig().getVersionPrefix());
                                    String path = server + "/jdm/app/download?v=" + URLEncoder
                                            .encode(SecurityUtil.crypt(pJsonObject.toJSONString(), Constant.KEY_HTTP));
//                                    UpdateDownload dUtil = new UpdateDownload(path);

                                    new UpdateDownload(path, updateInfo.getName(), updateInfo.getMd5(), activity);
//                                    dUtil.down2sd("Download/", updateInfo.getName(), updateInfo.getMd5(), dUtil.new downhandler() {
//
//                                        @Override
//                                        public void setSize(File file, long size) {
//                                            Message msg = f.obtainMessage(UpdateStatus.DOWNLOAD_PROGRESS);
//                                            msg.arg1 = (int) size;
//                                            msg.arg2 = updateInfo.getSize();
//                                            msg.obj = file;
//                                            f.sendMessage(msg);
//                                        }
//
//                                        @Override
//                                        public void downSuccess(File file, int result) {
//                                            Message msg = f.obtainMessage(UpdateStatus.DOWNLOAD_SUCCESS);
//                                            msg.arg1 = result;
//                                            msg.obj = file;
//                                            f.sendMessage(msg);
//                                        }
//                                    });
                                }
                            });
                        }
                    }
                }).show();
    }

    public static void showUpdateDialog(final FragmentParentActivity activity, final UpdateResponse updateInfo) {
        StringBuilder content = new StringBuilder();
        content.append(activity.getString(R.string.NewVersion));
        content.append("V");
        content.append(updateInfo.getVersion());
//		content.append("\r\n");
//		content.append(activity.getString(R.string.UpdateContent));
//		content.append("\r\n");
//		content.append(updateInfo.getLog());
        content.append("\r\n");
        content.append(activity.getString(R.string.TargetSize));
        content.append((updateInfo.getSize() / 1000000) + "M");

        new AlertView(activity.getString(R.string.UpdateTitle), content.toString(), activity.getString(R.string.NotNow),
                new String[]{activity.getString(R.string.UpdateNow)}, null, activity, AlertView.Style.Alert,
                new OnItemClickListener() {

                    @Override
                    public void onItemClick(Object o, int position) {
                        if (position != -1) {
                            // 开始更新
                            JavaThreadPool.getInstance().excute(new Runnable() {
                                public void run() {
                                    // 发送http请求，请求更新
                                    DataCenterSharedPreferences dcsp = activity.getDcsp();
                                    String server = dcsp
                                            .getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                                    JSONObject pJsonObject = new JSONObject();
                                    pJsonObject.put("t", 1);
                                    pJsonObject.put("p", MainApplication.app.getAppGlobalConfig().getVersionPrefix());
                                    String path = server + "/jdm/app/download?v=" + URLEncoder
                                            .encode(SecurityUtil.crypt(pJsonObject.toJSONString(), Constant.KEY_HTTP));
                                    new UpdateDownload(path, updateInfo.getName(), updateInfo.getMd5(), activity);
//                                    UpdateDownload dUtil = new UpdateDownload(path);
//                                    dUtil.down2sd("Download/", updateInfo.getName(), updateInfo.getMd5(), dUtil.new downhandler() {
//
//                                        @Override
//                                        public void setSize(File file, long size) {
//                                            Message msg = f.obtainMessage(UpdateStatus.DOWNLOAD_PROGRESS);
//                                            msg.arg1 = (int) size;
//                                            msg.arg2 = updateInfo.getSize();
//                                            msg.obj = file;
//                                            f.sendMessage(msg);
//                                        }
//
//                                        @Override
//                                        public void downSuccess(File file, int result) {
//                                            Message msg = f.obtainMessage(UpdateStatus.DOWNLOAD_SUCCESS);
//                                            msg.arg1 = result;
//                                            msg.obj = file;
//                                            f.sendMessage(msg);
//                                        }
//                                    });
                                }
                            });
                        }
                    }
                }).show();
    }


}
