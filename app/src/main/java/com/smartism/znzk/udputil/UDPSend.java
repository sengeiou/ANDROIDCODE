package com.smartism.znzk.udputil;

import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/*
* 使用UDP进行发送数据
* author mz
* */
public final class UDPSend {

    public static final String LOG_DEBUG = "UDPSend";

    private static Executor sThreadPool  ;
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
       private AtomicInteger mAtomicInteger = new AtomicInteger(1);
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r,"UDPSend #"+mAtomicInteger.getAndIncrement());
        }
    };

    static {
        sThreadPool = Executors.newSingleThreadExecutor(sThreadFactory);//创建线程数为1的线程池，但是任务队列无限制 ,串行执行
    }

    private boolean mInitSuccess = false ;
    public UDPSend(){

    }

    //执行任务
    private   void executor(Runnable runnable){
        sThreadPool.execute(runnable);
    }

    //发送字符串数据 ,目标IP，目标端口号，发送数据，发送次数
    public  void sendString(final String destIp, final int destPort, final String data, final int sendTimes){
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                InetSocketAddress mInetSocketAddress = new InetSocketAddress(destIp,destPort);
                int times = sendTimes;
                DatagramSocket mDatagramSocket =null;
                if (mDatagramSocket == null) {
                        try {
                            mDatagramSocket = new DatagramSocket();
                            mInitSuccess = true;
                        } catch (IOException e) {
                            mInitSuccess = false;
                            Log.d(LOG_DEBUG, "ip:" + destIp + ",DatagramSocket打开失败" + e.getMessage());
                            e.printStackTrace();
                        }
                    }

                    if (mInitSuccess) {
                        try {
                            byte[] sendData = data.getBytes("utf-8");
                            while(times>0){
                                Log.d(LOG_DEBUG, "开始发送.#" + times);
                                mDatagramSocket.send(new DatagramPacket(sendData, sendData.length, mInetSocketAddress));
                                Log.d(LOG_DEBUG, "结束发送.#" + times);
                                Thread.sleep(1000);
                                times--;
                            }


                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            Log.d(LOG_DEBUG, "字符串解析出错");
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d(LOG_DEBUG, "发送异常" + e.getMessage());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            if (mDatagramSocket != null ) {
                                mDatagramSocket.close();
                                mDatagramSocket.disconnect();
                            }
                        }


                    } else {
                        Log.d(LOG_DEBUG, "添加失败,DatagramSocket初始化失败");
                    }
            }
        };
        //执行任务
        executor(runnable);
    }

}
