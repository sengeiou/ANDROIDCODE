package com.smartism.znzk.activity.common;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.domain.TracerouteContainer;
import com.smartism.znzk.util.CollectionsUtils;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DateUtil;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.NetworkUtils;
import com.smartism.znzk.util.TracerouteUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 这是一个app环境的检测工具类。后续可以持续完善。
 *
 * 当前功能：1、输出当前APP的网络环境，语言，地区，时间。
 * 2、是否支持UTF-8
 * 3、获取google或者ping 8.8.8.8 成功则联网
 * 4、开始获取jdm.smart-ism.com服务器的页面或者图片，看是否可以获取到。
 * 5、单独获取各个地区的服务器看是否可以连上。
 * 6、检测7777端口的联通情况
 *
 */
public class EnvironmentCheckToolActivity extends ActivityParentActivity implements View.OnClickListener {

    private TextView logText;
    Channel channel7777 ,channel7776 ,channel7781 ,channel7782 ,channel7783 ,channel7784 ,channel7785 ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 启动activity时不自动弹出软键盘
        setContentView(R.layout.activity_zhzj_envitool);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        logText = (TextView) findViewById(R.id.log_text);
    }

    public void back(View v){
        finish();
    }
    public void toCheck(View v){
        logText.setText(logText.getText().toString() + "Server Address :");
        logText.setText(logText.getText().toString() + dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS,""));
        logText.setText(logText.getText().toString() + "\r\n");
        //开始进行检查。
        int status = NetworkUtils.getCurrentNetWorkStatus(getApplicationContext());
        logText.setText(logText.getText().toString() + "NetWork type:");
        switch (status){
            case 0:
                logText.setText(logText.getText().toString() + "no network");
                break;
            case 1:
                logText.setText(logText.getText().toString() + "WIFI");
                break;
            case 2:
                logText.setText(logText.getText().toString() + "mobile");
                break;
        }
        logText.setText(logText.getText().toString() + "\r\n");

        logText.setText(logText.getText().toString() + "Time :");
        logText.setText(logText.getText().toString() + DateUtil.formatDate(new Date()));
        logText.setText(logText.getText().toString() + "\r\n");

        logText.setText(logText.getText().toString() + "Language :");
        logText.setText(logText.getText().toString() + Locale.getDefault().toString());
        logText.setText(logText.getText().toString() + "\r\n");

        boolean isSupportUtf8 = true;
        try{
            "123".getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            isSupportUtf8 = false;
        }
        logText.setText(logText.getText().toString() + "Is support UTF-8:");
        logText.setText(logText.getText().toString() + String.valueOf(isSupportUtf8));
        logText.setText(logText.getText().toString() + "\r\n");


        logText.setText(logText.getText().toString() + "NetWork connect status:");
        logText.setText(logText.getText().toString() + String.valueOf(NetworkUtils.isNetworkConnect(getApplicationContext())));
        logText.setText(logText.getText().toString() + "\r\n");


        JavaThreadPool.getInstance().excute(new Runnable() {
                                                @Override
                                                public void run() {
                                                    final String[] ips = NetworkUtils.parseHostGetIPAddress("jdm.smart-ism.com");
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            logText.setText(logText.getText().toString() + "JDM's IP:");
                                                            if (ips!=null){
                                                                for (int j=0;j<ips.length;j++){
                                                                    logText.setText(logText.getText().toString() + ips[j]);
                                                                    logText.setText(logText.getText().toString() + "\r\n");
                                                                }
                                                            }
                                                        }
                                                    });
                                                }
                                            });

        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                TracerouteUtil util = new TracerouteUtil();
                util.tracerouteHost("jdm.smart-ism.com",(List<TracerouteContainer> traces) ->{
                    if (!CollectionsUtils.isEmpty(traces)){
                        StringBuilder tInfo = new StringBuilder();
                        for (TracerouteContainer t :  traces){
                            tInfo.append(t.toString());
                            tInfo.append("\r\n");
                        }
                        runOnUiThread(() ->{
                            StringBuilder builder = new StringBuilder(logText.getText().toString());
                            builder.append("network output:");
                            builder.append(tInfo.toString());
                            logText.setText(builder.toString());
                        });
                    }
                });
            }
        });

        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                final boolean googleResult = NetworkUtils.isPing("8.8.8.8");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logText.setText(logText.getText().toString() + "DNS status:");
                        logText.setText(logText.getText().toString() + String.valueOf(googleResult));
                        logText.setText(logText.getText().toString() + "\r\n");
                    }
                });
            }
        });
        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                final boolean googleResult = NetworkUtils.isPing("www.baidu.com");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logText.setText(logText.getText().toString() + "Access to Baidu:");
                        logText.setText(logText.getText().toString() + String.valueOf(googleResult));
                        logText.setText(logText.getText().toString() + "\r\n");
                    }
                });
            }
        });
        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                final boolean googleResult = NetworkUtils.isPing("www.google.com");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logText.setText(logText.getText().toString() + "Access to Google:");
                        logText.setText(logText.getText().toString() + String.valueOf(googleResult));
                        logText.setText(logText.getText().toString() + "\r\n");
                    }
                });
            }
        });
        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                URL url = null;
                try {
                    url = new URL("http://jdm.smart-ism.com:9999/jdm/login");
                    HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                    urlc.setConnectTimeout(30000);
                    urlc.connect();
                    if (urlc.getResponseCode() == 200) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                logText.setText(logText.getText().toString() + "Access to JDM:");
                                logText.setText(logText.getText().toString() + "true");
                                logText.setText(logText.getText().toString() + "\r\n");
                            }
                        });
                    }
                } catch (MalformedURLException e) {
                } catch (IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logText.setText(logText.getText().toString() + "Access to JDM:");
                            logText.setText(logText.getText().toString() + "io exception");
                            logText.setText(logText.getText().toString() + "\r\n");
                        }
                    });
                }
            }
        });

        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                URL url = null;
                try {
                    url = new URL("http://jdmsh.smart-ism.com:9999/jdm/login");
                    HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                    urlc.setConnectTimeout(30000);
                    urlc.connect();
                    if (urlc.getResponseCode() == 200) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                logText.setText(logText.getText().toString() + "Access to JDMSH:");
                                logText.setText(logText.getText().toString() + "true");
                                logText.setText(logText.getText().toString() + "\r\n");
                            }
                        });
                    }
                } catch (MalformedURLException e) {
                } catch (IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logText.setText(logText.getText().toString() + "Access to JDMSH:");
                            logText.setText(logText.getText().toString() + "io exception");
                            logText.setText(logText.getText().toString() + "\r\n");
                        }
                    });
                }
            }
        });

        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                URL url = null;
                try {
                    url = new URL("http://jdmxjp.smart-ism.com:9999/jdm/login");
                    HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                    urlc.setConnectTimeout(30000);
                    urlc.connect();
                    if (urlc.getResponseCode() == 200) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                logText.setText(logText.getText().toString() + "Access to JDMXJP:");
                                logText.setText(logText.getText().toString() + "true");
                                logText.setText(logText.getText().toString() + "\r\n");
                            }
                        });
                    }
                } catch (MalformedURLException e) {
                } catch (IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logText.setText(logText.getText().toString() + "Access to JDMXJP:");
                            logText.setText(logText.getText().toString() + "io exception");
                            logText.setText(logText.getText().toString() + "\r\n");
                        }
                    });
                }
            }
        });


        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                URL url = null;
                try {
                    url = new URL("http://jdm.smart-ism.com:9999/jdm/login");
                    HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                    urlc.setConnectTimeout(30000);
                    urlc.connect();
                    if (urlc.getResponseCode() == 200) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                logText.setText(logText.getText().toString() + "Access to JDMAW:");
                                logText.setText(logText.getText().toString() + "true");
                                logText.setText(logText.getText().toString() + "\r\n");
                            }
                        });
                    }
                } catch (MalformedURLException e) {
                } catch (IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logText.setText(logText.getText().toString() + "Access to JDMAW:");
                            logText.setText(logText.getText().toString() + "io exception");
                            logText.setText(logText.getText().toString() + "\r\n");
                        }
                    });
                }
            }
        });


        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                URL url = null;
                try {
                    url = new URL("http://jdmflkf.smart-ism.com:9999/jdm/login");
                    HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                    urlc.setConnectTimeout(30000);
                    urlc.connect();
                    if (urlc.getResponseCode() == 200) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                logText.setText(logText.getText().toString() + "Access to JDMFLKF:");
                                logText.setText(logText.getText().toString() + "true");
                                logText.setText(logText.getText().toString() + "\r\n");
                            }
                        });
                    }
                } catch (MalformedURLException e) {
                } catch (IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logText.setText(logText.getText().toString() + "Access to JDMFLKF:");
                            logText.setText(logText.getText().toString() + "io exception");
                            logText.setText(logText.getText().toString() + "\r\n");
                        }
                    });
                }
            }
        });


       JavaThreadPool.getInstance().excute(new Runnable() {
           @Override
           public void run() {
//                   String host = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                   String[] ips = NetworkUtils.parseHostGetIPAddress("jdm.smart-ism.com");
                   StringBuilder sb = new StringBuilder();
                   if(ips!=null){
                       for (int i = 0; i < ips.length; i++) {
                           sb.append(ips[i]);
                       }
                   }
                  channel7777 =  checkTcp(sb.toString(), 7777);
                  channel7776 = checkTcp(sb.toString(),7776);
                  channel7781 = checkTcp(sb.toString(),7781);
                  channel7782 = checkTcp(sb.toString(),7782);
                  channel7783 = checkTcp(sb.toString(),7783);
                  channel7784 = checkTcp(sb.toString(),7784);
                  channel7785 = checkTcp(sb.toString(),7785);

           }
       });
    }


    private Channel checkTcp(final String ip, final int port){
        Channel clientChannel=null ;
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000) //5s超时
                .group(new NioEventLoopGroup())
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        ChannelPipeline pipeline = nioSocketChannel.pipeline() ;
                        pipeline.addLast(new ChannelInboundHandlerAdapter(){

                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                ctx.fireChannelActive();
                                ctx.close();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        logText.setText(logText.getText().toString() + "Access to "+ ip +":"+ port+":");
                                        logText.setText(logText.getText().toString() + "true");
                                        logText.setText(logText.getText().toString() + "\r\n");
                                    }
                                });
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                super.exceptionCaught(ctx,cause);
                                ctx.close();
                            }

                        });
                    }
                });

        try {
            ChannelFuture future  = bootstrap.connect(ip,port).addListener(new ChannelFutureListener() {

                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if(channelFuture.isSuccess()){
                        //成功结束
                        return ;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logText.setText(logText.getText().toString() + "Access to "+ ip + ":" + port+":");
                            logText.setText(logText.getText().toString() + "false");
                            logText.setText(logText.getText().toString() + "\r\n");
                        }
                    });
                }
            }).sync();
            clientChannel = future.channel();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }catch (Exception e) {
        }
        return clientChannel;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            if (channel7776 != null) {
                channel7776.close();
            }
            if (channel7777 != null) {
                channel7777.close();
            }
            if (channel7781 != null) {
                channel7781.close();
            }
            if (channel7782 != null) {
                channel7782.close();
            }
            if (channel7783 != null) {
                channel7783.close();
            }
            if (channel7784 != null) {
                channel7784.close();
            }
            if (channel7785 != null) {
                channel7785.close();
            }
        }catch(Exception e){
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {

    }
}
