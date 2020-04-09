package com.smartism.znzk.activity.speech;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.MainActivity;
import com.smartism.znzk.activity.device.ZhujiListFragment;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.DeviceKeys;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.listener.speech.IatSettings;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.util.speech.ApkInstaller;
import com.smartism.znzk.util.speech.JsonParser;

import org.json.JSONException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class IatSpeechActivity extends ActivityParentActivity implements View.OnClickListener {
    private static String TAG = MainActivity.class.getSimpleName();
    // 语音听写对象
    private SpeechRecognizer mIat;
    // 语音听写UI
    private RecognizerDialog mIatDialog;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    private EditText mResultText;
    private Toast mToast;
    private SharedPreferences mSharedPreferences;
    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    // 语记安装助手类
    ApkInstaller mInstaller;

    int ret = 0;
    final public static int REQUEST_CODE_ASK_CALL_PHONE = 123;
    private List<DeviceInfo> deviceInfos;
    private DeviceInfo deviceInfo;

    private ZhujiInfo zhuji;

    //6.0以上动态获取权限
    public void onAUDIO() {
        if (Build.VERSION.SDK_INT >= 23) {
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_ASK_CALL_PHONE);
                return;
            }
        }
    }


    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 0) {
                //获取设备列表
                if (msg.obj != null) {
                    deviceInfos = (List<DeviceInfo>) msg.obj;
                }
            }
            if (msg.what == 1) {
                //获取键值
                List<CommandKey> keys = null;
                if (msg.obj != null) {
                    keys = (List<CommandKey>) msg.obj;
                    if (speechStr != null && !"".equals(speechStr)) {
                        for (CommandKey key : keys) {
                            if (speechStr.contains(key.getName())) {
                                SyncMessage message1 = new SyncMessage();
                                message1.setCommand(SyncMessage.CommandMenu.rq_control.value());
                                message1.setDeviceid(deviceInfo.getId());
                                // 操作
                                message1.setSyncBytes(new byte[]{(byte) msg.arg1});
                                SyncMessageContainer.getInstance().produceSendMessage(message1);
                            }
                        }
                    }
                    speechStr = "";
                }
            }
            if (msg.what == 2) {
                //超时
            }
            return false;
        }
    };
    private Handler handler = new WeakRefHandler(mCallback);
    String speechStr = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iat_speech);
        mEngineType = SpeechConstant.TYPE_CLOUD;
        findViewById(R.id.iat_recognize).setOnClickListener(IatSpeechActivity.this);
        onAUDIO();
        JavaThreadPool.getInstance().excute(new LoadAllDevicesInfo(0));
        mResultText = ((EditText) findViewById(R.id.iat_text));
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(IatSpeechActivity.this, mInitListener);
        // 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(IatSpeechActivity.this, mInitListener);
        //获取 语音配置的xml文件
        mSharedPreferences = getSharedPreferences(IatSettings.PREFER_NAME, Activity.MODE_PRIVATE);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        mResultText = ((EditText) findViewById(R.id.iat_text));
        mInstaller = new ApkInstaller(IatSpeechActivity.this);
    }

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 退出时释放连接
        mIat.cancel();
        mIat.destroy();
    }

    @Override
    protected void onResume() {
        // 开放统计 移动数据统计分析
//        FlowerCollector.onResume(MainActivity.this);
//        FlowerCollector.onPageStart(TAG);
        super.onResume();
    }

    @Override
    protected void onPause() {
        // 开放统计 移动数据统计分析
//        FlowerCollector.onPageEnd(TAG);
//        FlowerCollector.onPause(MainActivity.this);
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        isrunniong = false;
// 移动数据分析，收集开始听写事件
//        FlowerCollector.onEvent(MainActivity.this, "iat_recognize");

        mResultText.setText(null);// 清空显示内容
        mIatResults.clear();
        // 设置参数
        setParam();
        boolean isShowDialog = false;
//       boolean isShowDialog = mSharedPreferences.getBoolean(
//                getString(R.string.pref_key_iat_show), false);
        if (isShowDialog) {
            // 显示听写对话框(他的弹出框没有英文界面，可以自己弄对话框)
//            mIatDialog.setListener(mRecognizerDialogListener);
//            mIatDialog.show();
            showTip(getString(R.string.text_begin));
        } else {
            // 不显示听写对话框
            ret = mIat.startListening(mRecognizerListener);
            if (ret != ErrorCode.SUCCESS) {
                showTip("听写失败,错误码：" + ret);
            } else {
                showTip(getString(R.string.text_begin));
            }
        }
    }


    /**
     * 听写UI监听器
     */
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {
            printResult(results);
        }

        /**
         * 识别回调错误.
         */
        public void onError(SpeechError error) {
            showTip(error.getPlainDescription(true));
        }

    };

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }

    /**
     * 参数设置
     *
     * @param
     * @return
     */
    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

        String lag = mSharedPreferences.getString("iat_language_preference",
                "mandarin");
        if (lag.equals("en_us")) {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
        } else {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT, lag);
        }

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "1"));

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");
    }

    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            showTip("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
            showTip(error.getPlainDescription(true));
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            showTip("结束说话");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.d(TAG, results.getResultString());
            String resultStr = printResult(results);
            if (!isrunniong) {
                isrunniong = true;
                matchString(resultStr, deviceInfos);
            }
            if (isLast) {
                // TODO 最后的结果
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            showTip("当前正在说话，音量大小：" + volume);
            Log.d(TAG, "返回音频数据：" + data.length);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

    private String printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            org.json.JSONObject resultJson = new org.json.JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }

        mResultText.setText(resultBuffer.toString());
        mResultText.setSelection(mResultText.length());
        return resultBuffer.toString();
    }

    //        获取匹配到的设备列表
    public List<DeviceInfo> macthDevice(String speechStrs, List<DeviceInfo> deviceInfoList) {
        List<DeviceInfo> deviceInfos = new ArrayList<>();
        if (deviceInfoList == null) return deviceInfos;
        String keyStr = "";
        if (speechStrs != null && !"".equals(speechStrs)) {
            String str = "";
            char[] speechChar = speechStrs.toCharArray();
            for (int i = 0; i < speechChar.length; i++) {
                str = str + String.valueOf(speechChar[i]);
                List<DeviceInfo> list = new ArrayList<>();
                for (DeviceInfo deviceInfo : deviceInfoList) {
                    if (deviceInfo.getName().contains(keyStr)) {
                        list.add(deviceInfo);
                    }
                }
                if (!list.isEmpty()) {
                    keyStr = str;
                    speechStr = keyStr;
                    deviceInfos.clear();
                    deviceInfos.addAll(list);
                }
            }
        }

        return deviceInfos;
    }

    //    获取到能匹配到设备的字符的下标
    public int getSpeechIndex(String speechStr, List<DeviceInfo> deviceInfoList) {
        //把去掉头部和动词的字符串，用来匹配设备名称，先判断从那个地方开始能匹配到设备列表
        if (deviceInfoList == null) return -1;
        if (speechStr != null && !"".equals(speechStr)) {
            char[] speechChar = speechStr.toCharArray();
            for (int i = 0; i < speechChar.length; i++) {
                for (DeviceInfo deviceInfo : deviceInfoList) {
                    if (deviceInfo.getName().contains(String.valueOf(speechChar[i]))) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    public void matchString(String str,List<DeviceInfo> deviceInfos){
        if (str == null || "".equals(str)) {
            return;
        }
        String matchStr=str.replace("。","");
        String headStr="";
        String verbStr="";
        //获取到本地的库
        String[] strs = getValue();
        if (strs == null) {
            save("", "");
            strs = getValue();
        }
        // 获取到头部和动词 列表
        String[] heads = strs[0].split("\\|");
        String[] foods = strs[1].split("\\|");
        for (String hStr: heads){
            if (matchStr.startsWith(hStr)){
                headStr = hStr;
                matchStr = matchStr.substring(hStr.length(),matchStr.length());
                break;
            }
            if (matchStr.endsWith(hStr)){
                headStr = hStr;
                matchStr = matchStr.substring(0,matchStr.length()-hStr.length());
                break;
            }
        }
        for (String vStr: foods){
            if (matchStr.startsWith(vStr)){
                verbStr = vStr;
                matchStr = matchStr.substring(vStr.length(),matchStr.length());
                break;
            }
            if (matchStr.endsWith(vStr)){
                verbStr = vStr;
                matchStr = matchStr.substring(0,matchStr.length()-vStr.length());
                break;
            }
        }
        getTip(verbStr, matchStr, deviceInfos);
    }
    /**********
     * 获取到语音控制的指令
     *************/
    private boolean isrunniong = false;

    public void getTip(String verbStr, String str, List<DeviceInfo> deviceInfos) {
        Log.e("getTip", "" + str);
        String matchStr=str;
        if (matchStr == null || "".equals(matchStr)) {
            return;
        }
////        获取到本地的库
//        String[] strs = getValue();
//        if (strs == null) {
//            save("", "");
//            strs = getValue();
//        }
////        获取到头部和动词 列表
//        String[] heads = strs[0].split("\\|");
//        String[] foods = strs[1].split("\\|");
        Log.e("getTip", "" + verbStr);
//      匹配设备
        int index = getSpeechIndex(matchStr, deviceInfos);
        if (index == -1) {
            return;
        }
        matchStr = matchStr.substring(index);
        List<DeviceInfo> deviceInfoList = macthDevice(matchStr, deviceInfos);
        if (deviceInfoList == null || deviceInfoList.isEmpty()) {
            return;
        }
        Log.e("speechStr:", "" + deviceInfoList.size() + "deviceInfoList");
        for (DeviceInfo deviceInfo : deviceInfoList) {
            Log.e("deviceInfo:", deviceInfo.getId()+"---" + deviceInfo.getName());
        }
        Log.e("speechStr:", "" + speechStr);
//      获取设备的指令
        List<DeviceKeys> deviceKeyses = getDeviceKeys(deviceInfoList);
        if (deviceKeyses != null && !deviceKeyses.isEmpty()) {
            for (DeviceKeys dk : deviceKeyses) {
                Log.e("DeviceKeys-->", "" + dk.getKeyName());
            }
        }
        Log.e("lastKeys", deviceKeyses.size() + ">>>>>>>>");
        matchStr = matchStr.substring(speechStr.length(), matchStr.length());
        Log.e("lastKeys", "keyName" + matchStr);
        int keyIndex = getSpeechposition(matchStr, deviceKeyses);
        if (keyIndex == -1) {
            if (verbStr.equals("打开")){
                contrulDevice(deviceInfoList, false);
            }else {
                contrulDevice(deviceInfoList, true);
            }
            return;
        }
        Log.e("keyIndex", "keyIndex:" + keyIndex + "");
        matchStr = matchStr.substring(keyIndex, matchStr.length());
        List<DeviceKeys> lastKeys = macthDeviceKeys(matchStr, deviceKeyses);
        if (lastKeys == null || lastKeys.isEmpty()) {
            if (verbStr.equals("打开")){
                contrulDevice(deviceInfoList, false);
            }else {
                contrulDevice(deviceInfoList, true);
            }
            return;
        }
        Log.e("lastKeys", lastKeys.size() + "lastKeyssize");
        for (DeviceKeys dk : lastKeys) {
            Log.e("lastKeys","key"+dk.toString());
            // 操作
            SyncMessage message1 = new SyncMessage();
            message1.setCommand(SyncMessage.CommandMenu.rq_control.value());
            message1.setDeviceid(dk.getDeviceId());
            message1.setSyncBytes(new byte[]{(byte)dk.getKeySort()});
            SyncMessageContainer.getInstance().produceSendMessage(message1);
        }
    }

    //    获取到设备的指令/DeviceKeys::
    public List<DeviceKeys> getDeviceKeys(List<DeviceInfo> deviceInfoList) {
        List<DeviceKeys> deviceKeyses = Collections.synchronizedList(new ArrayList<DeviceKeys>());
        for (DeviceInfo deviceInfo : deviceInfoList) {
            List<DeviceKeys> keyses = DatabaseOperator.getInstance(IatSpeechActivity.this).findDeviceKeysByDeviceId(deviceInfo.getId());
            if (keyses != null && !keyses.isEmpty()) {
                deviceKeyses.addAll(keyses);
            } else {
                JavaThreadPool.getInstance().excute(new CommandKeyLoad(deviceInfo));
            }
        }
        return deviceKeyses;
    }
    public void  contrulDevice(List<DeviceInfo> deviceInfoList,boolean isOff) {
        List<DeviceKeys> deviceKeyses = Collections.synchronizedList(new ArrayList<DeviceKeys>());
        for (DeviceInfo deviceInfo : deviceInfoList) {
            List<DeviceKeys> keyses = DatabaseOperator.getInstance(IatSpeechActivity.this).findDeviceKeysByDeviceId(deviceInfo.getId());
            if (keyses != null && keyses.size()>2) {
            }else{
                SyncMessage message1 = new SyncMessage();
                message1.setCommand(SyncMessage.CommandMenu.rq_control.value());
                message1.setDeviceid(deviceInfo.getId());
                if (!isOff) { // 开关操作
                    // 这里的操作写死，需要在服务器的配置页面中配置此按钮对应的指令是什么
                    // 开操作
                    Log.e("aaa", "发送通知===开指令");
                    message1.setSyncBytes(new byte[]{0x01});
                } else {
                    // 关操作
                    Log.e("aaa", "发送通知===关指令");
                    message1.setSyncBytes(new byte[]{0x00});
                }
                SyncMessageContainer.getInstance().produceSendMessage(message1);
            }
        }
    }
    public List<DeviceKeys> macthDeviceKeys(String speechStrs, List<DeviceKeys> deviceKeyses) {
        Log.e("macthDeviceKeys", speechStrs);
        List<DeviceKeys> keyses = new ArrayList<>();
        if (deviceKeyses == null) return keyses;
        String keyStr = "";
        if (speechStrs != null && !"".equals(speechStrs)) {
            String str = "";
            char[] speechChar = speechStrs.toCharArray();
            for (int i = 0; i < speechChar.length; i++) {
                str = str + String.valueOf(speechChar[i]);
                List<DeviceKeys> list = new ArrayList<>();
                for (DeviceKeys deviceKeys : deviceKeyses) {
                    if (deviceKeys.getKeyName().contains(keyStr)) {
                        list.add(deviceKeys);
                    }
                }
                if (!list.isEmpty()) {
                    keyStr = str;
                    speechStr = keyStr;
                    keyses.clear();
                    keyses.addAll(list);
                }
            }
        }
        return keyses;
    }

    //    获取到能匹配到按钮的字符的下标
    public int getSpeechposition(String speechStr, List<DeviceKeys> deviceKeyList) {
        //把去掉头部和动词的字符串，用来匹配设备名称，先判断从那个地方开始能匹配到设备列表
        if (deviceKeyList == null) return -1;
        if (speechStr != null && !"".equals(speechStr)) {
            char[] speechChar = speechStr.toCharArray();
            for (int i = 0; i < speechChar.length; i++) {
                for (DeviceKeys deviceKeys : deviceKeyList) {
                    if (deviceKeys.getKeyName().contains(String.valueOf(speechChar[i]))) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }


    /*
     * 获取到语音控制的指令
     */
    public void getTips(String str, List<DeviceInfo> deviceInfos) {
        Log.e("wwwwwww_deviceInfos:", "" + deviceInfos.size());
        if (str == null || "".equals(str)) return;
        char[] spChar = str.toCharArray();

        int index = -1;
        //先获取到从那个字符开始有设备名字能匹配得上
        for (int i = 0; i < spChar.length; i++) {
            for (DeviceInfo deviceInfo : deviceInfos) {
                if (deviceInfo.getName().contains(String.valueOf(spChar[i]))) {
                    index = i;
                }
            }
        }
        if (index < 0) return;//没有匹配上
        String charStr = "";
        List<DeviceInfo> dlist = deviceInfos;
        for (int i = index; i < spChar.length; i++) {
            List<DeviceInfo> list = new ArrayList<>();
            String cs = charStr + String.valueOf(spChar[i]);
            for (DeviceInfo deviceInfo : dlist) {
                if (deviceInfo.getName().contains(cs)) {
                    //把匹配到的设备全部保存起来
                    list.add(deviceInfo);
                }
                if (i == spChar.length - 1) {
                    if (!list.isEmpty()) {
                        dlist.clear();
                        dlist.addAll(list);
                        charStr = cs;
                    } else {
                        //当cs匹配不到设备时跳出来
                        return;
                    }
                }
            }
        }
    }

    private DeviceInfo checkDevice(List<DeviceInfo> deviceInfos, String spStr) {
        for (DeviceInfo deviceInfo : deviceInfos) {
            String name = deviceInfo.getName();
            if (name.equals(spStr.substring(0, name.length()))) {
                return deviceInfo;
            }
        }
        return null;
    }

    private String checkStr(String spStr, String[] strs) {

        for (String mstr : strs) {
            if (spStr.length() < mstr.length()) return null;
            String headStr = spStr.substring(0, mstr.length());
            if (mstr.equals(headStr)) {
                return mstr;
            }
        }
        return null;
    }

    //    获取
    private String[] getValue() {
        String[] strs = new String[2];
        SharedPreferences sp = getSharedPreferences("speech_sp", Context.MODE_PRIVATE);
        strs[0] = sp.getString("speech_head", null);
        strs[1] = sp.getString("speech_body", null);
        if (strs[0] == null || strs[1] == null) return null;
        return strs;
    }

    //保存语音命令的文件
    private void save(String head, String body) {
        SharedPreferences sp = getSharedPreferences("speech_sp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("speech_head", "我要|我想|我需要");
        editor.putString("speech_body", "打开|启动|关闭|执行");
        editor.commit();
    }

    /**********
     * 结束
     * 获取到语音控制的指令
     *************/


    class LoadAllDevicesInfo implements Runnable {
        private int what;

        public LoadAllDevicesInfo(int what) {
            this.what = what;
        }

        @Override
        public void run() {
//            zhuji = DatabaseOperator.getInstance(IatSpeechActivity.this)
//                    .queryDeviceZhuJiInfo(dcsp.getString(DataCenterSharedPreferences.Constant.APP_MASTERID, ""));
            //替换
            zhuji = DatabaseOperator.getInstance(IatSpeechActivity.this).queryDeviceZhuJiInfo(ZhujiListFragment.getMasterId());
            if (zhuji == null) {
                return;
            }
            List<DeviceInfo> deviceList = new ArrayList<DeviceInfo>();
            if (zhuji != null) {
                int sortType = dcsp.getString(DataCenterSharedPreferences.Constant.SHOW_DLISTSORT, "zhineng").equals("zhineng") ? 0 : 1;
                String ordersql = "";
                Cursor cursor = DatabaseOperator.getInstance(IatSpeechActivity.this).getReadableDatabase().rawQuery(
                        "select * from DEVICE_STATUSINFO", null);
                if (cursor != null && cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        DeviceInfo deviceInfo = DatabaseOperator.getInstance(IatSpeechActivity.this)
                                .buildDeviceInfo(cursor);
                        deviceList.add(deviceInfo);
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            }
            for (DeviceInfo deviceInfo : deviceList) {
                if ("sst".equals(deviceInfo.getCa())) {
                    deviceList.remove(deviceInfo);
                    break;
                }
            }
            Message m = handler.obtainMessage(this.what);
            m.obj = deviceList;
            handler.sendMessage(m);
        }
    }

    class CommandKeyLoad implements Runnable {
        private DeviceInfo deviceInfo;

        public CommandKeyLoad(DeviceInfo deviceInfo) {
            this.deviceInfo = deviceInfo;
        }

        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("did", deviceInfo.getId());
            String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/d/dkeycomms", object, IatSpeechActivity.this);
            if (result != null && result.length() > 4) {

                List<CommandKey> list = new ArrayList<>();
                JSONArray array = JSON.parseArray(result);
                DatabaseOperator.getInstance(IatSpeechActivity.this).delDeviceKeysById(deviceInfo.getId());
                if (array != null && !array.isEmpty()) {
                    for (int j = 0; j < array.size(); j++) {
                        CommandKey key = new CommandKey();
                        key.setSort(array.getJSONObject(j).getIntValue("s"));
                        key.setName(array.getJSONObject(j).getString("n"));
                        key.setIoc(array.getJSONObject(j).getString("i"));
                        key.setWhere(array.getJSONObject(j).getIntValue("w"));
                        key.setId(array.getJSONObject(j).getLongValue("id"));

                        DeviceKeys dkey = new DeviceKeys();
                        dkey.setKeySort(array.getJSONObject(j).getIntValue("s"));
                        dkey.setKeyName(array.getJSONObject(j).getString("n"));
                        dkey.setKeyIco(array.getJSONObject(j).getString("i"));
                        dkey.setKeyWhere(array.getJSONObject(j).getIntValue("w"));
                        dkey.setDeviceId(array.getJSONObject(j).getLongValue("id"));
                        DatabaseOperator.getInstance(IatSpeechActivity.this).insertOrUpdateDeviceKeys(dkey, deviceInfo.getId());
                        list.add(key);
                    }
                }
            }

        }
    }

    private BroadcastReceiver defaultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, final Intent intent) {
            if (Actions.CONNECTION_FAILED_SENDFAILED.equals(intent.getAction())) { //服务器未连接
                handler.removeMessages(2);
                //返回指令操作失败
                Toast.makeText(IatSpeechActivity.this, getString(R.string.rq_control_sendfailed),
                        Toast.LENGTH_SHORT).show();
            } else if (intent.getAction().equals(Actions.ACCETP_ONEDEVICE_MESSAGE)) {
                //返回指令操作成功
                handler.removeMessages(2);
            }
        }
    };

    class CommandKey implements Serializable {
        private long id;
        private int sort;
        private String name;
        private String ioc;
        private int where;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public int getSort() {
            return sort;
        }

        public void setSort(int sort) {
            this.sort = sort;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIoc() {
            return ioc;
        }

        public void setIoc(String ioc) {
            this.ioc = ioc;
        }

        public int getWhere() {
            return where;
        }

        public void setWhere(int where) {
            this.where = where;
        }

        @Override
        public String toString() {
            return "CommandKey [id=" + id + ", sort=" + sort + ", name=" + name + ", ioc=" + ioc + ", where=" + where
                    + "]";
        }

    }
}
