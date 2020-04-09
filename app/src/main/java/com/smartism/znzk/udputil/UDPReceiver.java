package com.smartism.znzk.udputil;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/*
* UDP接收,接收结果会回调到主线程
* author mz
* */
public class UDPReceiver {

    public static final String DEBUG_LOG = "UDPReceiver";

    //串行执行
    private static Executor sThreadPool = Executors.newSingleThreadExecutor(new ThreadFactory() {
        AtomicInteger mCount = new AtomicInteger(1);
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r,"UDPReceiver #"+mCount.getAndIncrement());
        }
    });
    private static final int RECEIVE_RESULT_FLAG = 0x67;
    private static final int RECEIVE_TIMEOUT = 0X89 ; //接收超时

    private  Handler sHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case RECEIVE_RESULT_FLAG:
                    UDPReceiveResult udpReceiveResult = (UDPReceiveResult) msg.obj;
                    OnUDPReceiverListener listener = udpReceiveResult.mReceiver.mWeakReference.get();
                    if(listener!=null){
                        listener.receiveResult(udpReceiveResult.result,udpReceiveResult.address);
                    }
                    break ;
                case RECEIVE_TIMEOUT:
                    Log.d(DEBUG_LOG,"接收超时");
                    if(sDatagramChannel!=null){
                        try {
                            sDatagramChannel.close();//在主线程关闭会让主线程奔溃
                            sDatagramChannel=null;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    };

    private  DatagramChannel sDatagramChannel ;
    private WeakReference<OnUDPReceiverListener> mWeakReference ;


    public UDPReceiver(){
    }

    //接收的结果是字节数组
    public void receive(final int port, OnUDPReceiverListener<byte[]> onUDPReceiverListener){
        setListener(onUDPReceiverListener);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                DatagramChannel datagramChannel = null ;
                try {
                    ByteBuffer mByteBuffer = ByteBuffer.allocate(64);
                    mByteBuffer.clear();
                    datagramChannel = DatagramChannel.open();
                    datagramChannel.socket().bind(new InetSocketAddress(port));
                    Log.d(DEBUG_LOG,"开始接收...");
                    sDatagramChannel = datagramChannel ; //指向正在执行的通道
                    sHandler.sendEmptyMessageDelayed(RECEIVE_TIMEOUT,1000*1000);//20s秒超时
                    SocketAddress socketAddress = datagramChannel.receive(mByteBuffer);//阻塞当前线程
                    if (socketAddress != null) {
                        //读到了数据
                        int position = mByteBuffer.position();
                        mByteBuffer.flip();
                        byte[] temp = new byte[position];
                        for (int i = 0; i < position; i++) {
                            temp[i] = mByteBuffer.get();
                        }
                        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
                        UDPReceiveResult<byte[]> udpReceiveResult = new UDPReceiveResult<>();
                        udpReceiveResult.mReceiver = UDPReceiver.this;
                        udpReceiveResult.result = temp;
                        udpReceiveResult.address = inetSocketAddress;
                        Message message = sHandler.obtainMessage(RECEIVE_RESULT_FLAG);
                        message.obj = udpReceiveResult;
                        sHandler.sendMessage(message);
                        Log.d(DEBUG_LOG, "获取到数据:" + new String(temp));
//                            new UDPSend().sendString(inetSocketAddress.getAddress().getHostAddress(),inetSocketAddress.getPort(),"+MAC:OK",20);
                    }
                } catch (IOException e) {
                    Log.d(DEBUG_LOG,"接收异常."+e.getMessage());
                    e.printStackTrace();
                }finally {
                    try {
                        if(sDatagramChannel!=null){
                            sHandler.removeMessages(RECEIVE_TIMEOUT);
                            sDatagramChannel.close();
                            sDatagramChannel.disconnect();
                            sDatagramChannel = null ;
                            Log.d(DEBUG_LOG,"finally-接收关闭");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        sThreadPool.execute(runnable);
    }

    private void setListener(OnUDPReceiverListener listener){
        mWeakReference = new WeakReference<>(listener);
    }


    public interface OnUDPReceiverListener<T>{
        //对方的InetSocketAddress信息
        void receiveResult(T result, InetSocketAddress inetSocketAddress);
    }

    private static class UDPReceiveResult<Result>{
        Result result ;
        UDPReceiver mReceiver ;
        InetSocketAddress address ;//接收到对方的IP信息
    }
}
