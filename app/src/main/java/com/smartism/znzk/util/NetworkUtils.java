package com.smartism.znzk.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.format.Formatter;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by bear on 2016/8/27.
 */
public class NetworkUtils {

    private static final String TAG = "Network";

    private static final int STATUS_NO_NETWORK = 0;

    private static final int STATUS_WIFI = 1;

    private static final int STATUS_MODILE = 2;

    //判断移动网络是否可用
    public static boolean isMobileConnected(Context context) {
        if (context != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mMobileNetworkInfo = connectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mMobileNetworkInfo != null) {
                return mMobileNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    //判断wifi是否可用
    public static boolean isWifiConnect(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWifiNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if ((mWifiNetworkInfo != null)) {
                return mWifiNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    //当前网络是否可用
    public static boolean isNetworkConnect(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager
                    .getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }


    //判断当前网络是否为Wi-Fi
    public static boolean whetherNetWorkIsWifi(Context context) {
        if (context != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo.State wifi = connectivityManager.getNetworkInfo(
                    ConnectivityManager.TYPE_WIFI).getState();
            if (wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING) {
                Log.i(TAG, "THE CURRENT NETWORK IS WIFI !");
                return true;
            }
        }
        return false;
    }

    //判断当前网络是否为Wi-Fi
    public static boolean whetherNetWorkIsMobile(Context context) {
        if (context != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo.State gprs = connectivityManager.getNetworkInfo(
                    ConnectivityManager.TYPE_MOBILE).getState();
            if (gprs == NetworkInfo.State.CONNECTED || gprs == NetworkInfo.State.CONNECTING) {
                Log.i(TAG, "THE CURRENT NETWORK IS MOBILE !");
                return true;

            }
        }
        return false;
    }

    //获取当前网络的状态：0为no network，1为wifi,2为mobile，
    public static int getCurrentNetWorkStatus(Context context) {
        int status = STATUS_NO_NETWORK;
        if (context != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = connectivityManager
                    .getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                NetworkInfo.State gprs = connectivityManager.getNetworkInfo(
                        ConnectivityManager.TYPE_MOBILE).getState();
                NetworkInfo.State wifi = connectivityManager.getNetworkInfo(
                        ConnectivityManager.TYPE_WIFI).getState();
                if (gprs == NetworkInfo.State.CONNECTED || gprs == NetworkInfo.State.CONNECTING) {
                    Log.i(TAG, "THE CURRENT NETWORK IS MOBILE !");
                    status = STATUS_MODILE;
                }
                if (wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING) {
                    Log.i(TAG, "THE CURRENT NETWORK IS WIFI !");
                    status = STATUS_WIFI;
                }
            } else {
                status = STATUS_NO_NETWORK;
            }
        }
        return status;
    }


    //获取当前SSID
//    public static String getCurentWifiSSID(Context context) {
//        String ssid = "";
//        if (context != null) {
//            WifiManager mWifiManage = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//            WifiInfo mWifiInfo = mWifiManage.getConnectionInfo();
//            ssid = mWifiInfo.getSSID();
//
//            if (ssid.substring(0, 1).equals("\"")
//                    && ssid.substring(ssid.length() - 1).equals("\"")) {
//                ssid = ssid.substring(1, ssid.length() - 1);
//            }
//        }
//        return ssid;
//    }

    /**
     * 获取SSID
     * @param context 上下文
     * @return  WIFI 的SSID
     */
    public static String getCurentWifiSSID(Context context) {
        String ssid="<unknown ssid>";

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O||Build.VERSION.SDK_INT>Build.VERSION_CODES.O_MR1) {

            WifiManager mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            assert mWifiManager != null;
            WifiInfo info = mWifiManager.getConnectionInfo();

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                return info.getSSID();
            } else {
                return info.getSSID().replace("\"", "");
            }
        } else if (Build.VERSION.SDK_INT==Build.VERSION_CODES.O_MR1){
            ConnectivityManager connManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            assert connManager != null;
            NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
            if (networkInfo.isConnected()) {
                if (networkInfo.getExtraInfo()!=null){
                    return networkInfo.getExtraInfo().replace("\"","");
                }
            }
        }
        return ssid;
    }

    //获取当前wifi的IP
    public static String getCurentWifiIp(Context context) {
        String ipAddress = "";
        if (context != null) {
            WifiManager mWifiManage = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo mWifiInfo = mWifiManage.getConnectionInfo();
            int ip = mWifiInfo.getIpAddress();
            ipAddress = Formatter.formatIpAddress(ip);
        }
        return ipAddress;
    }
    public static int getCurentWifiIpToInt(Context context) {
        if (context != null) {
            WifiManager mWifiManage = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo mWifiInfo = mWifiManage.getConnectionInfo();
            return mWifiInfo.getIpAddress();
        }
        return 0;
    }


    //判断是否有外网连接（普通办法不能判断外网的网络是否连接，例如局域网不能连上外网）
    public static boolean isPing() {
        boolean isping = false;

        try {
            String ip = "www.baidu.com";
            Process p = Runtime.getRuntime().exec("ping -c 3 -w 100 "+ip);// ping网址3次
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            String content = "";
            while((content = in.readLine()) != null){
                stringBuffer.append(content);
            }
            int status = p.waitFor();
            if(status == 0){
                isping = true;
            }else{
                isping = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return isping;
    }
    //判断是否有外网连接（普通办法不能判断外网的网络是否连接，例如局域网不能连上外网）
    public static boolean isPing(String ip) {
        boolean isping = false;

        try {
            Process p = Runtime.getRuntime().exec("ping -c 3 -w 100 "+ip);// ping网址3次
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            String content = "";
            while((content = in.readLine()) != null){
                stringBuffer.append(content);
            }
            int status = p.waitFor();
            if(status == 0){
                isping = true;
            }else{
                isping = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return isping;
    }

    /**
     * 解析域名获取IP数组
     * @param host
     * @return
     */
    public static String[] parseHostGetIPAddress(String host) {
        String[] ipAddressArr = null;
        try {
            InetAddress[] inetAddressArr = InetAddress.getAllByName(host);
            if (inetAddressArr != null && inetAddressArr.length > 0) {
                ipAddressArr = new String[inetAddressArr.length];
                for (int i = 0; i < inetAddressArr.length; i++) {
                    ipAddressArr[i] = inetAddressArr[i].getHostAddress();
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
        return ipAddressArr;
    }


    /**
     * 检查网络
     *
     * @return
     */
    public static boolean CheckNetwork(Context c) {
        ConnectivityManager connectivityManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return false;
        }

//		NetworkInfo[] netWorks = connectivityManager.getAllNetworkInfo();
        NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
//		if (netWorks != null && netWorks.length > 0) {
//			for (NetworkInfo net : netWorks) {
//				if (net.getState() == NetworkInfo.State.CONNECTED) {
//					return true;
//				}
//			}
//		}
        if (wifiInfo != null && wifiInfo.isConnectedOrConnecting()) {
            return true;
        }
        if (mobileNetworkInfo != null && mobileNetworkInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    /**
     * 检查网络 是否为wifi环境
     *
     * @return
     */
    public static boolean CheckNetworkIsWifi(Context c) {
        ConnectivityManager connectivityManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return false;
        }

        NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

}
