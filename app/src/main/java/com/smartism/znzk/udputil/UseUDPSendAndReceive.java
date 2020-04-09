package com.smartism.znzk.udputil;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.zhicheng.activities.MZBaseActivity;
import com.smartism.znzk.zhicheng.iviews.IBaseView;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
//AP配网,使用UDP发送和接收
/*
* author mz
* */
public class UseUDPSendAndReceive extends AsyncTask<String,Void,String> {

    private static final String TAG = UseUDPSendAndReceive.class.getSimpleName() ;
    WeakReference<OnUseUDPSendAndReceiveListener> mListenerWeakReference; //结果回调
    DatagramChannel mDatagramChannel ; //可以收发的数据通道
    boolean isInit = true ; //UDP是否初始化成功
    private  int myPort = 8674 ; //自己绑定的端口号，可以自己想一个
    private String destIp  ; //对方的ip
    private int destPort ;  //对方的端口号
    private boolean isSuccessReceive  =false ;  //标志位，接收成功设置为true,退出发送数据
    private String mProgressText ="" ; //进度条显示文字
    private boolean isCancel = false ; //取消任务执行

    public UseUDPSendAndReceive(OnUseUDPSendAndReceiveListener listener, String destIp, int destPort){
        mListenerWeakReference =  new WeakReference<>(listener);
        this.destIp = destIp ;
        this.destPort = destPort ;
        try {
            mDatagramChannel = DatagramChannel.open();
            //要接收，需要先进行绑定端口号
            mDatagramChannel.socket().bind(new InetSocketAddress(myPort));//绑定一个端口号
        } catch (IOException e) {
            isInit = false ;
            e.printStackTrace();
        }
    }

    public void setProgressText(String text){
        this.mProgressText = text;
    }


    @Override
    protected void onPreExecute() {
        if(!MZBaseActivity.isActive((Activity) mListenerWeakReference.get())){
            return ;
        }
        mListenerWeakReference.get().showProgress(mProgressText);
    }
    @Override
    protected void onPostExecute(String s){
        if(!MZBaseActivity.isActive((Activity) mListenerWeakReference.get())){
            return ;
        }
        //接收到结果,取消任务
        cancelTask();
        mListenerWeakReference.get().hideProgress();
        mListenerWeakReference.get().receiveResult(s);
    }

    @Override
    protected String doInBackground(String... strings) {
        if(!isInit){
            return null ;
        }
        final String sendData = strings[0];
        //开启一个线程，发送数据
        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                int i = 10 ;//每隔2秒发送一次，一共十次
                ByteBuffer sendBuffer = ByteBuffer.allocate(64);
                try {

                    while(i>0){
                        if(isSuccessReceive||isCancel){
                            //超时或者接收到结果，取消执行
                            return ;
                        }
                        sendBuffer.clear();
                        sendBuffer.put(sendData.getBytes("utf-8"));
                        sendBuffer.flip();
                        //目标IP地址为255.255.255.255是受限的广播地址可能会出现Socket异常
                        mDatagramChannel.send(sendBuffer,new InetSocketAddress(InetAddress.getByName(destIp),destPort));
                        Thread.sleep(2000);
                        i--;
                        Log.d(TAG,"第"+(10-i)+"次");
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "发送线程发生异常" + e.getMessage());
                }

            }
        });
        ByteBuffer receiveBuffer = ByteBuffer.allocate(64);//设置过小，会溢出
        try {
            while(true){
                if(isCancel){
                    return null;
                }
                Log.d(TAG,"接收中...");
                receiveBuffer.clear() ;
                 SocketAddress socketAddress = mDatagramChannel.receive(receiveBuffer);//阻塞当前线程,一直
                if(socketAddress!=null){
                    int position = receiveBuffer.position();
                    receiveBuffer.flip();//将position归0，limit==position，准备读取数据,这一步很重要
                    byte[] tempData = new byte[position];
                    for(int i=0;i<position;i++){
                        tempData[i] = receiveBuffer.get();
                    }
                    isSuccessReceive = true ; //成功接收,设置标志位
                    return new String(tempData,"utf-8");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG,"接收线程发生异常"+e.getMessage());
            return null ;
        }finally {
            disconnect();
        }

    }

    //通过源码发现，当任务被取消后，转而调用该方法，而不是onPostxxxx方法 ,主线程调用,所以在这里隐藏进度条
    @Override
    protected void onCancelled(String s) {
        super.onCancelled(s);
        //任务被取消后，在这里进行进度条隐藏
        if(mListenerWeakReference.get()!=null){
            mListenerWeakReference.get().hideProgress();//隐藏进度条
        }
    }

    public void disconnect(){
        if(mDatagramChannel!=null){
            try {
                mDatagramChannel.disconnect();
                mDatagramChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void cancelTask(){
        isCancel = true ;
    }



    public interface OnUseUDPSendAndReceiveListener extends IBaseView {
        void receiveResult(String result);
    }
}
