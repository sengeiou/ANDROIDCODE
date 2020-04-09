package com.smartism.znzk.activity.common;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferService;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.hjq.toast.ToastUtils;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.awsClient.AWSClients;
import com.smartism.znzk.communication.connector.SyncClientAWSMQTTConnector;
import com.smartism.znzk.domain.TracerouteContainer;
import com.smartism.znzk.util.CollectionsUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LDNetPing;
import com.smartism.znzk.util.LDNetSocket;
import com.smartism.znzk.util.LDNetUtil;
import com.smartism.znzk.util.TracerouteUtil;
import com.smartism.znzk.view.MyRoundProcess;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 手机网络检测
 */
public class NetworkDiagnosisActivity extends ActivityParentActivity implements LDNetPing.LDNetPingListener, LDNetSocket.LDNetSocketListener {
    @BindView(R.id.round_process)
    MyRoundProcess roundProcess;

    @BindView(R.id.match_btn_start)
    Button btnStart;
    @BindView(R.id.match_btn_end)
    Button btnEnd;
    @BindView(R.id.layout_network_disgnosis_start)
    LinearLayout layoutPregressStart;
    @BindView(R.id.layout_network_disgnosis_process)
    LinearLayout layoutPregress;
    @BindView(R.id.layout_network_disgnosis_end)
    LinearLayout layoutPregressEnd;
    @BindView(R.id.txt_diagnosis_process)
    TextView textDiagnosisProcess;



    private InetAddress[] _remoteInet;
    private List<String> _remoteIpList;
    private boolean _isNetConnected;// 当前是否联网
    private boolean _isDomainParseOk;// 域名解析是否成功
    private boolean _isSocketConnected;// conected是否成功
    private LDNetSocket _netSocker;// 监控socket的连接时间
    private LDNetPing _netPinger; // 监控ping命令的执行时间

    private StringBuilder diagnosisResult = new StringBuilder("Start Diagnosis\n");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_disgnosis);
        ButterKnife.bind(this);
        getApplicationContext().startService(new Intent(getApplicationContext(), TransferService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void back(View v) {
        finish();
    }

    public void save(View v) {
        showInProgress();
        AWSClients.getInstance().saveStringToS3("network_diagnosis",diagnosisResult.toString(),"network_diagnosis",new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.
                    runOnUiThread(()->{
                        cancelInProgress();
                        ToastUtils.show("Submit success");
                        finish();
                    });
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int)percentDonef;

                Log.d(TAG, "ID:" + id + " bytesCurrent: " + bytesCurrent
                        + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                // Handle errors
                cancelInProgress();
                ToastUtils.show("Submit failed");
            }

        });
    }

    public void start(View v) {
        layoutPregressStart.setVisibility(View.GONE);
        btnStart.setVisibility(View.GONE);
        layoutPregress.setVisibility(View.VISIBLE);

        JavaThreadPool.getInstance().excute(() ->{
            recordCurrentAppVersion();
            recordLocalNetEnvironmentInfo();

            if (_isNetConnected) {
                // TCP三次握手时间测试
//                recordStepInfo("\n开始TCP连接测试...");
                _netSocker = LDNetSocket.getInstance();
                _netSocker._remoteInet = _remoteInet;
                _netSocker._remoteIpList = _remoteIpList;
                _netSocker.initListener(this);
                _netSocker.isCConn = false;// 设置是否启用C进行connected
//                _isSocketConnected = _netSocker.exec(SyncClientAWSMQTTConnector.IOT_ENDPOINT);
                _isSocketConnected = true;

                // 诊断ping信息, 同步过程

                runOnUiThread(() -> {
                    textDiagnosisProcess.setText("4. Ping ...");
                });
                if (_isNetConnected && _isDomainParseOk && _isSocketConnected) {// 联网&&DNS解析成功&&connect测试成功
                    recordStepInfo("\nStart ping...");
                    _netPinger = new LDNetPing(this, 4);
                    recordStepInfo("ping...MQTT Domain");
                    _netPinger.exec(SyncClientAWSMQTTConnector.IOT_ENDPOINT, false);
                }
            } else {
                recordStepInfo("\n\n当前手机未联网,请检查网络！");
            }
        });
    }

    /**
     * 输出关于应用、机器、网络诊断的基本信息
     */
    private void recordCurrentAppVersion() {
        runOnUiThread(() -> {
            textDiagnosisProcess.setText("1. App info ...");
        });
        // 输出应用版本信息和用户ID
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            diagnosisResult.append("AppCode:\t" + packageInfo.packageName);
            diagnosisResult.append("\n");
            diagnosisResult.append("AppName:\t" + packageInfo.applicationInfo.name);
            diagnosisResult.append("\n");
            diagnosisResult.append("AppVersion:\t" + packageInfo.versionName);
            diagnosisResult.append("\n");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // 输出机器信息
        diagnosisResult.append("Mobile type:\t" + android.os.Build.MANUFACTURER + ":"
                + android.os.Build.BRAND + ":" + android.os.Build.MODEL);
        diagnosisResult.append("\n");
        diagnosisResult.append("System version:\t" + android.os.Build.VERSION.RELEASE);
        diagnosisResult.append("\n");
        TelephonyManager _telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (_telManager != null) {
            diagnosisResult.append("Mobile operator:\t" + _telManager.getSimOperator() + " " +_telManager.getSimOperatorName());
            diagnosisResult.append("\n");
            diagnosisResult.append("ISOCountryCode:\t" + _telManager.getNetworkCountryIso());
            diagnosisResult.append("\n");
        }

        if (_telManager != null) {
            String tmp = _telManager.getNetworkOperator();
            if (tmp.length() >= 3) {
                diagnosisResult.append("MobileCountryCode:\t"+tmp.substring(0, 3));
                diagnosisResult.append("\n");
            }
            if (tmp.length() >= 5) {
                diagnosisResult.append("MobileNetworkCode:\t"+tmp.substring(3, 5));
                diagnosisResult.append("\n");
            }
        }
        runOnUiThread(() -> {
            roundProcess.setProgress(10);
        });
    }

    /**
     * 输出本地网络环境信息
     */
    private void recordLocalNetEnvironmentInfo() {
        runOnUiThread(() -> {
            textDiagnosisProcess.setText("2. Network info ...");
        });
        diagnosisResult.append("\n\n\n--------------------------------------------------------\n\n\n");
        // 网络状态
        if (LDNetUtil.isNetworkConnected(this)) {
            _isNetConnected = true;
            diagnosisResult.append("Connect to network:\tconnected");
            diagnosisResult.append("\n");
        } else {
            diagnosisResult.append("Connect to network:\tdisconnected");
            diagnosisResult.append("\n");
            _isNetConnected = false;
        }

        // 获取当前网络类型
        diagnosisResult.append("Net type:\t");
        String _netType = LDNetUtil.getNetWorkType(this);
        diagnosisResult.append(_netType);
        diagnosisResult.append("\n");
        String _localIp = "",_gateWay = "";
        if (_isNetConnected) {
            if (LDNetUtil.NETWORKTYPE_WIFI.equals(_netType)) { // wifi：获取本地ip和网关，其他类型：只获取ip
                _localIp = LDNetUtil.getLocalIpByWifi(this);
                _gateWay = LDNetUtil.pingGateWayInWifi(this);
            } else {
                _localIp = LDNetUtil.getLocalIpBy3G();
            }
            diagnosisResult.append("Local IP:\t" + _localIp);
            diagnosisResult.append("\n");
        } else {
            diagnosisResult.append("Local IP:\t127.0.0.1");
            diagnosisResult.append("\n");
        }
        if (_gateWay != null) {
            diagnosisResult.append("Local Gateway:\t" + _gateWay);
            diagnosisResult.append("\n");
        }

        // 获取本地DNS地址
//        if (_isNetConnected) {
//            _dns1 = LDNetUtil.getLocalDns("dns1");
//            _dns2 = LDNetUtil.getLocalDns("dns2");
//            recordStepInfo("本地DNS:\t" + this._dns1 + "," + this._dns2);
//        } else {
//            recordStepInfo("本地DNS:\t" + "0.0.0.0" + "," + "0.0.0.0");
//        }
//

        runOnUiThread(() -> {
            textDiagnosisProcess.setText("3. DNS analysis ...");
            roundProcess.setProgress(20);
        });
        // 获取远端域名的DNS解析地址
        if (_isNetConnected) {
            diagnosisResult.append("Domain:\t" + SyncClientAWSMQTTConnector.IOT_ENDPOINT);
            _isDomainParseOk = parseDomain(SyncClientAWSMQTTConnector.IOT_ENDPOINT);// 域名解析
        }
        runOnUiThread(() -> {
            roundProcess.setProgress(30);
        });
    }

    /**
     * 域名解析
     */
    private boolean parseDomain(String _dormain) {
        _remoteIpList = new ArrayList<String>();
        boolean flag = false;
        int len = 0;
        String ipString = "";
        Map<String, Object> map = LDNetUtil.getDomainIp(_dormain);
        String useTime = (String) map.get("useTime");
        _remoteInet = (InetAddress[]) map.get("remoteInet");
        String timeShow = null;
        if (Integer.parseInt(useTime) > 5000) {// 如果大于1000ms，则换用s来显示
            timeShow = " (" + Integer.parseInt(useTime) / 1000 + "s)";
        } else {
            timeShow = " (" + useTime + "ms)";
        }
        if (_remoteInet != null) {// 解析正确
            len = _remoteInet.length;
            for (int i = 0; i < len; i++) {
                _remoteIpList.add(_remoteInet[i].getHostAddress());
                ipString += _remoteInet[i].getHostAddress() + ",";
            }
            ipString = ipString.substring(0, ipString.length() - 1);
            recordStepInfo("DNS analysis result:\t" + ipString + timeShow);
            flag = true;
        } else {// 解析不到，判断第一次解析耗时，如果大于10s进行第二次解析
            if (Integer.parseInt(useTime) > 10000) {
                map = LDNetUtil.getDomainIp(_dormain);
                useTime = (String) map.get("useTime");
                _remoteInet = (InetAddress[]) map.get("remoteInet");
                if (Integer.parseInt(useTime) > 5000) {// 如果大于1000ms，则换用s来显示
                    timeShow = " (" + Integer.parseInt(useTime) / 1000 + "s)";
                } else {
                    timeShow = " (" + useTime + "ms)";
                }
                if (_remoteInet != null) {
                    len = _remoteInet.length;
                    for (int i = 0; i < len; i++) {
                        _remoteIpList.add(_remoteInet[i].getHostAddress());
                        ipString += _remoteInet[i].getHostAddress() + ",";
                    }
                    ipString = ipString.substring(0, ipString.length() - 1);
                    recordStepInfo("DNS analysis result:\t" + ipString + timeShow);
                    flag = true;
                } else {
                    recordStepInfo("DNS analysis result:\t" + "failed" + timeShow);
                }
            } else {
                recordStepInfo("DNS analysis result:\t" + "failed" + timeShow);
            }
        }
        return flag;
    }

    /**
     * 如果调用者实现了stepInfo接口，输出信息
     *
     * @param stepInfo
     */
    private void recordStepInfo(String stepInfo) {
        diagnosisResult.append(stepInfo + "\n");
    }

    /**
     * 获取运营商信息
     */
    private String requestOperatorInfo() {
        String res = null;
        String url = LDNetUtil.OPERATOR_URL;
        HttpURLConnection conn = null;
        URL Operator_url;
        try {
            Operator_url = new URL(url);
            conn = (HttpURLConnection) Operator_url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(1000 * 10);
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                res = LDNetUtil.getStringFromStream(conn.getInputStream());
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return res;
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return res;
    }

    private void traceRouteDomain(){
        runOnUiThread(() -> {
            textDiagnosisProcess.setText("5. Traceroute ...");
            roundProcess.setProgress(50);
        });
        // 开始诊断traceRoute
        recordStepInfo("\ntraceroute...");
        /**
         * 利用 ping 来实现 traceroute 功能
         */
//                if (!CollectionsUtils.isEmpty(_remoteIpList)) {
//                    for (String s:_remoteIpList) {
        TracerouteUtil util = new TracerouteUtil();
        util.tracerouteHost(SyncClientAWSMQTTConnector.IOT_ENDPOINT, (List<TracerouteContainer> traces) -> {
            if (!CollectionsUtils.isEmpty(traces)) {
                for (TracerouteContainer t : traces) {
                    if (t!=null) {
                        diagnosisResult.append(t.toString());
                        diagnosisResult.append("\r\n");
                    }
                }
                runOnUiThread(() -> {
                    btnEnd.setVisibility(View.VISIBLE);
                    layoutPregress.setVisibility(View.GONE);
                    layoutPregressEnd.setVisibility(View.VISIBLE);
                });
//                                diagnosisResult.append("****************************\r\n");
            }
        });
//                    }
//                }
    }
    @Override
    public void OnNetPingFinished(String log) {
        recordStepInfo(log);
        runOnUiThread(() -> {
            roundProcess.setProgress(40);
        });
        traceRouteDomain();
    }

    @Override
    public void OnNetSocketFinished(String log) {

    }

    @Override
    public void OnNetSocketUpdated(String log) {

    }

}