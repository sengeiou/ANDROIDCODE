package com.smartism.znzk.xiongmai.activities;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.SystemClock;

import com.lib.FunSDK;
import com.smartism.znzk.xiongmai.lib.funsdk.support.models.FunDevice;


/*
* 实现与雄迈摄像头语言对讲
* */
public class XiongMaiAudioRecordThread extends Thread {
    public boolean isAudioRecoed = false ; //是否语音对讲标志
    private AudioRecord mAudioRecord ;//录制音频对象
    private int bufferSizeInBytes =0 ; //设置音频录制时缓冲区大小，必须大于一帧音频的大小，否则音频数据会溢出，跑出异常，而且读取得及时，否则也会因为音频缓冲区溢出
    private int sampleRateInHz = 8000 ; //音频采样率，默认为8000，表示每一秒采样的次数，频率越高，音质越好
    private static XiongMaiAudioRecordThread mThread  = null ;
    private FunDevice mFunDevice;

    XiongMaiAudioRecordThread(FunDevice device){
        mFunDevice = device;
    }
    public void setSampleRateInHz(int sampleRateInHz){
        this.sampleRateInHz = sampleRateInHz ;
    }

    //结束录制
    public static  void startRecord(FunDevice funDevice){
        mThread = new XiongMaiAudioRecordThread(funDevice);
        mThread.isAudioRecoed = true ; //设置录制标志位
        mThread.start();//开启线程录制
    }

    //开始录制
    public static void stopRecord(){
        if(mThread==null){
            return ;
        }
        mThread.isAudioRecoed = false ; //关闭
    }

    //进行一些初始化操作
    @Override
    public synchronized void start() {
        if(mAudioRecord==null){
           bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);//计算最低缓冲区大小，不能小于这个值，否则会溢出.
           mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,8000, AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,bufferSizeInBytes);
        }
        super.start();
    }

    //实现录制音频操作的线程执行体
    @Override
    public synchronized void run() {
        super.run();
        mThread.mAudioRecord.startRecording();//开始录制
        while(isAudioRecoed){
            byte[] dataByte = new byte[bufferSizeInBytes];//取出音频数据的字节数组
            mAudioRecord.read(dataByte,0,dataByte.length);//读取数据,这里取处的数据是PCM格式，可以认为是原始的音频数据，不能被播放器播放
            FunSDK.DevSendTalkData(mFunDevice.getDevSn(),dataByte,dataByte.length);//发送语音数据给摄像头
            SystemClock.sleep(5);//休眠5毫秒
        }
        mThread.mAudioRecord.stop();
        mThread.mAudioRecord.release();//释放资源
        mThread = null ;
    }
}
