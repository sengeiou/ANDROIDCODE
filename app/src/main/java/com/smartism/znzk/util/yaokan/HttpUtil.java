package com.smartism.znzk.util.yaokan;

import android.net.http.AndroidHttpClient;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class HttpUtil {

    public HttpUtil(String appId, String deviceId) {
        this.appId = appId;
        this.deviceId = deviceId;
    }

    private String userAgent = "(Liunx; u; Android ; en-us;Media)";


    private String appId;
    private String deviceId;

    private String key = "demo.fortest1234";

    public String registerDevId() {
        String total = "";
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        //String deviceId = "FF00000001";
        //String deviceId = "FF000F4262";
        //String deviceId = "MyDev";
        //String deviceId = "MyDev1";
        //String deviceId = "MyDev2";
        String deviceId = "MyDev3";
        nameValuePairs.add(new BasicNameValuePair("f", deviceId));
        nameValuePairs.add(new BasicNameValuePair("c", "r"));
        //正式ID
        String appid = "14689138661283";
        nameValuePairs.add(new BasicNameValuePair("appid", appid));
        String time = SystemClock.uptimeMillis() + "";
        total = total + deviceId + time;
        String auth = Encrypt.encryptSpecial(total);
        AndroidHttpClient httpClient = AndroidHttpClient.newInstance(userAgent);
        try {
            //测试url
            //String url = "http://city.sun-cam.com.cn/open/m.php";
            //正式url
            String url = "http://api.yaokongyun.cn/open/m.php";
            HttpPost request = new HttpPost(url);
            request.addHeader("accept-encoding", "gzip,deflate");
            request.addHeader("client", time + "_" + auth);
            HttpEntity httpEntity = new UrlEncodedFormEntity(nameValuePairs, "UTF-8");
            request.setEntity(httpEntity);
            HttpResponse response = httpClient.execute(request);
            StatusLine statusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                byte[] srcData = EntityUtils.toByteArray(entity);
                byte[] nData = null;
                if (!Utility.isEmpty(entity.getContentEncoding())
                        && entity.getContentEncoding().getValue().contains("gzip")) {
                    nData = unzip(srcData);
                } else {
                    nData = srcData;
                }
                //Log.e("aaa", "正式APPID申请设备ID结果   ==" + nData.length);
                String iv = "testfor.demo4213";
                Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
                SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
                IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
                cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
                byte[] original = cipher.doFinal(nData);
                String originalString = new String(original, "UTF-8");
                originalString = originalString.trim(); // 源文
                Log.e("aaa", originalString);
                return originalString;
            }
            return "";
        } catch (Exception e) {
            Log.e("aaa", "postMethod: e is " + e);
            e.printStackTrace();
            return "";
        } finally {
            if (httpClient != null) {
                httpClient.close();
            }
        }
    }


    public String postMethod(String url, List<String> list) {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        String total = "";
        if (!Utility.isEmpty(list)) {
            for (String str : list) {
                if (!Utility.isEmpty(str)) {
                    String[] kv = str.split("=");
                    if (kv.length == 2) {
                        //避免空指针异常
                        //Log.e("aaa", kv[0]+"   ~~~   "+kv[1]);
                        nameValuePairs.add(new BasicNameValuePair(kv[0], kv[1]));
                        total = total + kv[1];
                    }
                }
            }
        }
        nameValuePairs.add(new BasicNameValuePair("f", deviceId));
        nameValuePairs.add(new BasicNameValuePair("appid", appId));

        String time = SystemClock.uptimeMillis() + "";
        total = total + deviceId + time;
        String auth = Encrypt.encryptSpecial(total);
        AndroidHttpClient httpClient = AndroidHttpClient.newInstance(userAgent);
        try {
            HttpPost request = new HttpPost(url);
            request.addHeader("accept-encoding", "gzip,deflate");
            request.addHeader("client", time + "_" + auth);
            String lan = Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry();
            request.addHeader("Accept-Language", lan);
            HttpEntity httpEntity = new UrlEncodedFormEntity(nameValuePairs, "UTF-8");
            request.setEntity(httpEntity);
            HttpResponse response = httpClient.execute(request);
            StatusLine statusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                byte[] srcData = EntityUtils.toByteArray(entity);
                byte[] nData = null;
                if (!Utility.isEmpty(entity.getContentEncoding())
                        && entity.getContentEncoding().getValue().contains("gzip")) {
                    nData = unzip(srcData); // 解压
                } else {
                    nData = srcData;
                }
                String iv = "testfor.demo4213";
                Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
                SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
                IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
                cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
                byte[] original = cipher.doFinal(nData);
                String originalString = new String(original, "UTF-8");
                originalString = originalString.trim(); // 源文
                return originalString;
            }
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        } finally {
            if (httpClient != null) {
                httpClient.close();
            }
        }
    }


    public byte[] unzip(byte[] srcData) throws IOException {
        InputStream inputStream = new GZIPInputStream(new ByteArrayInputStream(
                srcData));
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        byte[] temp = new byte[1024];
        int len = 0;
        while ((len = inputStream.read(temp, 0, 1024)) != -1) {
            arrayOutputStream.write(temp, 0, len);
        }
        arrayOutputStream.close();
        inputStream.close();
        return arrayOutputStream.toByteArray();
    }

    public String encrypt(String sSrc, String sKey) {
        if (sSrc == null || sKey == null) {
            return null;
        }
        try {
            sKey = new String(Base64.encode(key.getBytes("UTF-8"), Base64.DEFAULT));
            byte[] raw = Base64.decode(sKey.getBytes("UTF-8"), Base64.DEFAULT);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] encrypted = cipher.doFinal(sSrc.getBytes("UTF-8"));
            return URLEncoder.encode(Base64.encodeToString(encrypted, Base64.DEFAULT), "UTF-8");
        } catch (Exception e) {
        }
        return sSrc;
    }
}
