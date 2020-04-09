package com.smartism.znzk.communication.service;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.widget.RadioGroup;
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
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.device.ZhujiListFragment;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.DeviceKeys;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.listener.speech.IatSettings;
import com.smartism.znzk.listener.speech.TtsSettings;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.util.speech.JsonParser;
import com.smartism.znzk.view.Speech.MFloatingView;
import com.smartism.znzk.view.alertview.AlertView;

import org.json.JSONException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

public class FloatService extends Service implements MFloatingView.OnFloatClickListent {
    protected DataCenterSharedPreferences dcsp = null;

    private List<DeviceInfo> deviceInfos;
    String speechStr = "";
    private DeviceInfo deviceInfo;
    private List<DeviceInfo> deviceInfoList;
    private List<DeviceKeys> lastKeyList;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private ZhujiInfo zhuji;


    private boolean isSendKey;
    String[] speeckStr = new String[]{
            "a",
            "a",
            "a",
            "a",
            "a",
    };
    int[] imgList = new int[] {
            R.drawable.dialog_progress_loading,
            R.drawable.dialog_progress_loading1,
            R.drawable.dialog_progress_loading2,
            R.drawable.dialog_progress_loading3
    };
    private Runnable timeRunnable = new Runnable() {
        @Override
        public void run() {
            Random random = new Random();
            int delayTeme = (int) (5+5*Math.random())*1000*60;
            int index =  random.nextInt(imgList.length);
            floatingView.setIconBackGround(imgList[index]);
            handler.postDelayed(this,8*1000);
        }
    };

    public void onCreate() {
        super.onCreate();
        /********************* 讯飞语音听写功能 开始 分割线 *********************/
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        mIat = SpeechRecognizer.createRecognizer(this, mInitListener);
        mIatDialog = new RecognizerDialog(this, mInitListener);
        mSharedPreferences = getSharedPreferences(IatSettings.PREFER_NAME, Activity.MODE_PRIVATE);
        setParam();
        /********************* 讯飞语音听合成功能 开始 分割线 *********************/

        mTts = SpeechSynthesizer.createSynthesizer(this, mTtsInitListener); // 初始化合成对象
        // 云端发音人名称列表
        mCloudVoicersEntries = getResources().getStringArray(R.array.voicer_cloud_entries);
        mCloudVoicersValue = getResources().getStringArray(R.array.voicer_cloud_values);

        mTSharedPreferences = getSharedPreferences(TtsSettings.PREFER_NAME, MODE_PRIVATE);
        setTParam();    // 讯飞语音听合成 设置参数
        /********************* 讯飞语音听合成功能 结束 分割线 *********************/
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        JavaThreadPool.getInstance().excute(new LoadAllDevicesInfo(0));
        dcsp = DataCenterSharedPreferences.getInstance(FloatService.this,
                DataCenterSharedPreferences.Constant.CONFIG);

        if (floatingView == null) {
            synchronized (this) {
                if (floatingView == null) {
                    floatingView = new MFloatingView(this);
                }
            }
        }

        if (!floatingView.isShowFloating()) {
            floatingView.showFloatingBtn();
        }
        a = (height - with) / with * with;
        s = with - floatingView.mLayoutParams.x / 50;
        handler.post(runnable);
        floatingView.setOnFloatClickListent(this);
        handler.postDelayed(timeRunnable,3*1000);
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
            if (msg.what == 3) {
                handler.removeCallbacks(timeRunnable);
                //发送开关指令
                if (deviceInfoList!=null&&deviceInfoList.size() > 0) {
                    startSpeek("正在发送...");
                    int flag = msg.arg1;
                    DeviceInfo info = (DeviceInfo) deviceInfoList.get(0);
                    List<DeviceKeys> keyses = DatabaseOperator.getInstance(FloatService.this).findDeviceKeysByDeviceId(info.getId());
                    if (keyses != null && keyses.size() <= 2) {
                        SyncMessage message1 = new SyncMessage();
                        message1.setCommand(SyncMessage.CommandMenu.rq_control.value());
                        message1.setDeviceid(info.getId());
                        if (flag == 1) { // 开关操作
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

                    deviceInfoList.remove(0);
                    Message message = new Message();
                    message.what = msg.what;
                    message.arg1 = msg.arg1;
                    handler.sendMessageDelayed(message,2*1000);
                }else {
                    handler.postDelayed(timeRunnable,3*1000);
                    isSendKey = false;
                    startSpeek("发送完成...");
                }
            }
            if (msg.what == 4) {
                handler.removeCallbacks(timeRunnable);
                //发送设备指令
                int flag = msg.what;
                DeviceInfo info = (DeviceInfo) msg.obj;
                if (lastKeyList!=null&&lastKeyList.size() > 0) {
                    startSpeek("正在发送...");
                    DeviceKeys key = lastKeyList.get(0);
                    SyncMessage message1 = new SyncMessage();
                    message1.setCommand(SyncMessage.CommandMenu.rq_control.value());
                    message1.setDeviceid(key.getDeviceId());
                    message1.setSyncBytes(new byte[]{(byte) key.getKeySort()});
                    SyncMessageContainer.getInstance().produceSendMessage(message1);
                    lastKeyList.remove(0);
                    Message message = new Message();
                    message.what = msg.what;
                    handler.sendMessageDelayed(message,2*1000);
                }else {
                    handler.postDelayed(timeRunnable,3*1000);
                    isSendKey = false;
                    startSpeek("发送完成...");
                }
            }
            return false;
        }
    };
    private Handler handler = new WeakRefHandler(mCallback);
    private int with = 250;
    private int height = 500;
    private int s = 0;
    private float a = 0;
    private int time = 10;
    private MFloatingView floatingView;
    private boolean isStart;

    public boolean isStart() {
        return isStart;
    }

    public void setStart(boolean start) {
        isStart = start;
    }


    /**
     * 悬浮框按钮
     */
    @Override
    public void onFloatClick() {
        if (!isSendKey){
            isSendKey = true;
            startSpeek("请开始说话！");
        }
    }

    private void startSpeek(String mesg){

        int code = mTts.startSpeaking(mesg, mTtsListener);
        if (code != ErrorCode.SUCCESS) {
            if (code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED) {

            } else {
                showTip("语音合成失败,错误码: " + code);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            floatingView.moveFloatingButton(2, 20);
            time = time - 1;
            if (time > 0) {
                handler.postDelayed(this, 20);
            }
        }
    };


/******************************  讯飞语音听写功能 开始 分割线 *************************************************/

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code);
                isSendKey = false;
            }
        }
    };

    /**
     * 听写UI监听器
     */
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {

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

        mIat.setParameter(SpeechConstant.PARAMS, null);   // 清空参数
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType); // 设置听写引擎
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");// 设置返回结果格式

        String lag = mSharedPreferences.getString("iat_language_preference", "mandarin");
        if (lag.equals("en_us")) {
            mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");// 设置语言
        } else {
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");// 设置语言
            mIat.setParameter(SpeechConstant.ACCENT, lag);// 设置语言区域
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
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
            String errorStr = error.getPlainDescription(true);
            if (errorStr.contains("20006")){
                new AlertView(null,
                        getString(R.string.capture_activity_opendiverfail), getString(R.string.capture_activity_setcamera_yes),
                        new String[]{getString(R.string.capture_activity_setcamera_no)}, null, FloatService.this,
                        AlertView.Style.Alert, new com.smartism.znzk.view.alertview.OnItemClickListener() {

                    @Override
                    public void onItemClick(Object o, int position) {
                        if (position == -1) {
                            Intent intent = new Intent(Settings.ACTION_SETTINGS);
                            startActivity(intent);
                        }
                    }
                }).show();
            }
            isSendKey = false;
            showTip(errorStr);
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
            if (isLast) {
                //获取到语音语句，开始匹配设备指令
                matchString(resultStr, deviceInfos);
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            showTip("当前正在说话，音量大小：" + volume);
            Log.d(TAG, "返回音频数据：" + data.length);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
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
        return resultBuffer.toString();
    }

    private static String TAG = FloatService.class.getSimpleName();
    // 语音听写对象
    private SpeechRecognizer mIat;
    // 语音听写UI
    private RecognizerDialog mIatDialog;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();

    private SharedPreferences mTSharedPreferences;

    int ret = 0;
    final public static int REQUEST_CODE_ASK_CALL_PHONE = 123;

    /******************************  讯飞语音听写功能 结束 分割线 *************************************************/


    /******************************
     * 讯飞语音合成功能 开始 分割线
     *************************************************/

    private SpeechSynthesizer mTts;// 语音合成对象
    private String voicer = "xiaoyan";// 默认发音人
    private String[] mCloudVoicersEntries;
    private String[] mCloudVoicersValue;
    private int mPercentForBuffering = 0; // 缓冲进度
    private int mPercentForPlaying = 0;// 播放进度
    private RadioGroup mRadioGroup;// 云端/本地单选按钮
    private String mEngineType = SpeechConstant.TYPE_CLOUD;// 引擎类型
    private Toast mToast;
    private SharedPreferences mSharedPreferences;


    /**
     * 参数设置
     *
     * @param
     * @return
     */
    private void setTParam() {
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            // 设置在线合成发音人
            mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
            //设置合成语速
            mTts.setParameter(SpeechConstant.SPEED, mTSharedPreferences.getString("speed_preference", "50"));
            //设置合成音调
            mTts.setParameter(SpeechConstant.PITCH, mTSharedPreferences.getString("pitch_preference", "50"));
            //设置合成音量
            mTts.setParameter(SpeechConstant.VOLUME, mTSharedPreferences.getString("volume_preference", "50"));
        } else {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
            // 设置本地合成发音人 voicer为空，默认通过语记界面指定发音人。
            mTts.setParameter(SpeechConstant.VOICE_NAME, "");
            /**
             * TODO 本地合成不设置语速、音调、音量，默认使用语记设置
             * 开发者如需自定义参数，请参考在线合成参数设置
             */
        }
        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, mTSharedPreferences.getString("stream_preference", "3"));
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/tts.wav");
    }

    /**
     * 初始化监听。
     */
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "InitListener init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败,错误码：" + code);
                isSendKey = false;
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };

    /**
     * 合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            showTip("开始播放");
        }

        @Override
        public void onSpeakPaused() {
            showTip("暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            showTip("继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
            // 合成进度
            mPercentForBuffering = percent;
            showTip(String.format(getString(R.string.tts_toast_format),
                    mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
            mPercentForPlaying = percent;
            showTip(String.format(getString(R.string.tts_toast_format),
                    mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                showTip("开始说话：");
                mIatResults.clear();
                ret = mIat.startListening(mRecognizerListener);
                if (ret != ErrorCode.SUCCESS) {
                    showTip("听写失败,错误码：" + ret);
                } else {
                    showTip(getString(R.string.text_begin));
                }
            } else if (error != null) {
                isSendKey = false;
                showTip(error.getPlainDescription(true));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }
    };

    /******************************
     * 讯飞语音合成功能 结束 分割线
     *************************************************/


    /******************************
     * 语音控制 开始
     *************************************************/

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
     * 获取到语音控制的指令
     *************/
    private boolean isrunniong = false;

    /**
     * 匹配设备指令
     *
     * @param verbStr
     * @param str
     * @param deviceInfos
     */
    public void getTip(String verbStr, String str, List<DeviceInfo> deviceInfos) {
        String matchStr = str;
        if (matchStr == null || "".equals(matchStr)) {
            isSendKey = false;
            return;
        }
        //      匹配设备
        int index = getSpeechIndex(matchStr, deviceInfos);
        if (index == -1) {
            isSendKey = false;
            return;
        }
        if (verbStr==null||"".equals(verbStr)){
            isSendKey = false;
            return;
        }
        matchStr = matchStr.substring(index);     List<DeviceInfo> mdeviceInfoList = macthDevice(matchStr, deviceInfos);
        if (mdeviceInfoList == null || mdeviceInfoList.isEmpty()) {
            isSendKey = false;
            return;
        }
        for (DeviceInfo deviceInfo : mdeviceInfoList) {
        }
        //      获取设备的指令
        List<DeviceKeys> deviceKeyses = getDeviceKeys(mdeviceInfoList);
        if (deviceKeyses != null && !deviceKeyses.isEmpty()) {
            for (DeviceKeys dk : deviceKeyses) {
            }
        }
        matchStr = matchStr.substring(speechStr.length(), matchStr.length());
        int keyIndex = getSpeechposition(matchStr, deviceKeyses);
        if (keyIndex == -1) {
            deviceInfoList = mdeviceInfoList;
            for (DeviceInfo deviceInfo: deviceInfoList){
            }
            Message message = new Message();
            message.what = 3;
            if (verbStr.equals("打开")) {
//                contrulDevice(deviceInfoList, false);
                message.arg1=1;
            } else {
                message.arg1=0;
            }
            handler.sendMessage(message);
            return;
        }
        Log.e("keyIndex", "keyIndex:" + keyIndex + "");
        matchStr = matchStr.substring(keyIndex, matchStr.length());
        List<DeviceKeys> lastKeys = macthDeviceKeys(matchStr, deviceKeyses);
        if (lastKeys == null || lastKeys.isEmpty()) {
            deviceInfoList = mdeviceInfoList;
            for (DeviceInfo deviceInfo: deviceInfoList){
            }
            Message message = new Message();
            message.what = 3;
            if (verbStr.equals("打开")) {
                message.arg1=1;
            } else {
                message.arg1=0;
            }
            handler.sendMessage(message);
            return;
        }

        Log.e("lastKeys", lastKeys.size() + "lastKeyssize");
        //发送指令
//        for (DeviceKeys dk : lastKeys) {
//            Log.e("lastKeys", "key" + dk.toString());
//            // 操作
//            SyncMessage message1 = new SyncMessage();
//            message1.setCommand(SyncMessage.CommandMenu.rq_control.value());
//            message1.setDeviceid(dk.getDeviceId());
//            message1.setSyncBytes(new byte[]{(byte) dk.getKeySort()});
//            SyncMessageContainer.getInstance().produceSendMessage(message1);
//        }
        lastKeyList = lastKeys;
        handler.sendEmptyMessage(4);
    }

    public List<DeviceKeys> macthDeviceKeys(String speechStrs, List<DeviceKeys> deviceKeyses) {
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

    public void contrulDevice(List<DeviceInfo> deviceInfoList, int index, boolean isOff) {
        if (deviceInfoList != null && index < deviceInfoList.size()) {

            List<DeviceKeys> deviceKeyses = Collections.synchronizedList(new ArrayList<DeviceKeys>());
            for (DeviceInfo deviceInfo : deviceInfoList) {

            }
        } else {

        }
    }

    //    获取到设备的指令/DeviceKeys::
    public List<DeviceKeys> getDeviceKeys(List<DeviceInfo> deviceInfoList) {
        List<DeviceKeys> deviceKeyses = Collections.synchronizedList(new ArrayList<DeviceKeys>());
        for (DeviceInfo deviceInfo : deviceInfoList) {
            List<DeviceKeys> keyses = DatabaseOperator.getInstance(FloatService.this).findDeviceKeysByDeviceId(deviceInfo.getId());
            if (keyses != null && !keyses.isEmpty()) {
                Log.e("deviceInfoList.size =", "" + keyses.size());
                deviceKeyses.addAll(keyses);
            } else {
                JavaThreadPool.getInstance().excute(new CommandKeyLoad(deviceInfo));
            }
        }
        return deviceKeyses;
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
            String result = "";
            HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/d/dkeycomms", object, dcsp);
//                            ( server + "/jdm/s3/d/dkeycomms", object, FloatService.this);
            if (result != null && result.length() > 4) {
                List<CommandKey> list = new ArrayList<>();
                JSONArray array = JSON.parseArray(result);
                DatabaseOperator.getInstance(FloatService.this).delDeviceKeysById(deviceInfo.getId());
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
                        DatabaseOperator.getInstance(FloatService.this).insertOrUpdateDeviceKeys(dkey, deviceInfo.getId());
                        list.add(key);
                    }
                }
            }

        }
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

    /**
     * 匹配语音转化后的文字
     *
     * @param str
     * @param deviceInfos
     */
    public void matchString(String str, List<DeviceInfo> deviceInfos) {
        if (str == null || "".equals(str)) {
            isSendKey = false;
            return;
        }
        String matchStr = str.replace("。", "");
        String headStr = "";
        String verbStr = "";
        //获取到本地的库
        String[] strs = getValue();
        if (strs == null) {
            save("", "");
            strs = getValue();
        }
        // 获取到头部和动词 列表
        String[] heads = strs[0].split("\\|");
        String[] foods = strs[1].split("\\|");
        for (String hStr : heads) {
            if (matchStr.startsWith(hStr)) {
                headStr = hStr;
                matchStr = matchStr.substring(hStr.length(), matchStr.length());
                Log.e("matchString", "headStr:" + headStr + "-" + "matchStr" + matchStr);
                break;
            }
            if (matchStr.endsWith(hStr)) {
                headStr = hStr;
                matchStr = matchStr.substring(0, matchStr.length() - hStr.length());
                Log.e("matchString", "headStr:" + headStr + "-" + "matchStr" + matchStr);
                break;
            }
        }
        for (String vStr : foods) {
            if (matchStr.startsWith(vStr)) {
                verbStr = vStr;
                matchStr = matchStr.substring(vStr.length(), matchStr.length());
                Log.e("matchString", "verbStr:" + verbStr + "-" + "matchStr" + matchStr);
                break;
            }
            if (matchStr.endsWith(vStr)) {
                verbStr = vStr;
                matchStr = matchStr.substring(0, matchStr.length() - vStr.length());
                Log.e("matchString", "verbStr:" + verbStr + "-" + "matchStr" + matchStr);
                break;
            }
        }
        //匹配设备指令
        getTip(verbStr, matchStr, deviceInfos);
    }

    /******************************
     * 语音控制 结束
     *************************************************/
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

    class LoadAllDevicesInfo implements Runnable {
        private int what;

        public LoadAllDevicesInfo(int what) {
            this.what = what;
        }

        @Override
        public void run() {
//            zhuji = DatabaseOperator.getInstance(FloatService.this)
//                    .queryDeviceZhuJiInfo(dcsp.getString(DataCenterSharedPreferences.Constant.APP_MASTERID, ""));
            //替换
            zhuji = DatabaseOperator.getInstance(FloatService.this).queryDeviceZhuJiInfo(ZhujiListFragment.getMasterId());
            if (zhuji == null) {
                return;
            }
            List<DeviceInfo> deviceList = new ArrayList<DeviceInfo>();
            if (zhuji != null) {
                int sortType = dcsp.getString(DataCenterSharedPreferences.Constant.SHOW_DLISTSORT, "zhineng").equals("zhineng") ? 0 : 1;
                String ordersql = "";
                Cursor cursor = DatabaseOperator.getInstance(FloatService.this).getReadableDatabase().rawQuery(
                        "select * from DEVICE_STATUSINFO", null);
                if (cursor != null && cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        DeviceInfo deviceInfo = DatabaseOperator.getInstance(FloatService.this)
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 退出时释放连接
        mIat.cancel();
        mIat.destroy();
        if (mTts.isSpeaking()) {
            mTts.stopSpeaking();
            // 退出时释放连接
            mTts.destroy();
        }
        Toast.makeText(this, "服务被杀死了...",Toast.LENGTH_SHORT).show();
    }

}