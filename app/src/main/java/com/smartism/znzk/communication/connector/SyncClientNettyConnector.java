package com.smartism.znzk.communication.connector;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.communication.codec.AppDelimiterBasedFrameDecoder;
import com.smartism.znzk.communication.codec.MessageNettyDecoder;
import com.smartism.znzk.communication.codec.MessageNettyEncoder;
import com.smartism.znzk.communication.handler.ProcessNettyInHandler;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.service.CoreService;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.DateUtil;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.SecurityUtil;
import com.ta.utdid2.device.UTDevice;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

public class SyncClientNettyConnector {
    private static final String TAG = "SyncClientSupport";
    private static volatile SyncClientNettyConnector _instance;
    private int restartConn = 0;//重连次数,此字段需要在收到登录返回 之后重置。
    private int connection_timeout = 10000; //连接超时时间
    private int sequenceId = 0;
    private Context context = null;
    private ChannelHandlerContext ctx;
    private InetSocketAddress address;
    DataCenterSharedPreferences dcsp = null;

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.net.preferIPv6Addresses", "false");
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    /**
     * 重置重连次数
     */
    public void resetConnectionTryCount(){
        restartConn = 0;
    }

    public static SyncClientNettyConnector getInstance() {
        if (_instance == null) {
            synchronized (SyncClientNettyConnector.class) {
                if (_instance == null) {
                    _instance = new SyncClientNettyConnector();
                }
            }
        }
        return _instance;
    }

    public synchronized void connect(Context context) {
//        if (isConnected()) {
            disconnect();
//            ctx = null;
//        }

        this.context = context;

        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
//                if (NetWorkUtil.CheckNetwork(SyncClientNettyConnector.getInstance().context)) {
                    LogUtil.i(TAG, "init ...connection properties........");
                    dcsp = DataCenterSharedPreferences.getInstance(getInstance().context,
                            Constant.CONFIG);
                    String syncDataServers = dcsp.getString(Constant.SYNC_DATA_SERVERS, "");

                    LogUtil.i(TAG, "syncDataServers : " + syncDataServers);

                    String[] strArray = syncDataServers.split(":");

                    Log.v(TAG, "strArray[0] = " + strArray[0] + "  " + "strArray[1] = " + strArray[1]);

                    address = new InetSocketAddress(strArray[0], Integer.parseInt(strArray[1]));
                    LogUtil.i(TAG, "connect Device server ..............................");

                    restartConn++;
                    if (restartConn > 3) {
                        getInstance().context.sendBroadcast(new Intent(Actions.CONNECTION_FAILED));
                    } else {
                        getInstance().context.sendBroadcast(new Intent(Actions.CONNECTION_ING));
                    }


                    EventLoopGroup workerGroup = new NioEventLoopGroup();
                    try {
                        Bootstrap b = new Bootstrap(); // (1)
                        b.group(workerGroup); // (2)
                        b.channel(NioSocketChannel.class); // (3)
                        b.handler(new LoggingHandler(LogLevel.DEBUG));
                        b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
                        b.option(ChannelOption.TCP_NODELAY, true);//收到包立即发送不用等待
//					b.option(ChannelOption.SO_TIMEOUT, connection_timeout);
                        b.handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel ch) throws Exception {
                                ch.pipeline().addLast(new IdleStateHandler(60, 60, 60));//通道读、写空闲时间
                                ch.pipeline().addLast(new AppDelimiterBasedFrameDecoder(204800, false, Unpooled.copiedBuffer(new byte[]{(byte) 0x61, (byte) 0x76})));//包分隔
                                ch.pipeline().addLast(new MessageNettyDecoder());
                                ch.pipeline().addLast(new MessageNettyEncoder());
                                ch.pipeline().addLast(new ProcessNettyInHandler(getInstance().context));//注意和上面Encode和Encode的顺序，不能颠倒
//							    ch.pipeline().addLast(new ProcessNettyOutHandler(getInstance().context)); //开始连接也就是发出的一些操作。
                            }
                        });

//					ChannelFutureListener channelFutureListener = new ChannelFutureListener(){
//						@Override
//						public void operationComplete(ChannelFuture f) throws Exception {
//							if (f.isSuccess()) {
//								Log.d(TAG, "重新连接服务器成功");
//
//							} else {
//								Log.d(TAG, "重新连接服务器失败");
//								//  3秒后重新连接
//								f.channel().eventLoop().schedule(new Runnable() {
//									@Override
//									public void run() {
//										doConnect();
//									}
//								}, 3, TimeUnit.SECONDS);
//							}
//						}
//					};
                        // Start the client.
//                        b.connect(address).get();
//                        ChannelFuture f = b.connect(address).sync(); // (5)
                        ChannelFuture f = b.connect(address);
                        f.get(); //要用get方法获取结果，有异常会抛出来,再用下面的sync方法来进行异步，直接异步会使异常在主线程抛出来导致崩溃
                        f.sync();
                        // Wait until the connection is closed.
                        f.channel().closeFuture().sync();
                    } catch (InterruptedException e) {
                        Log.e(TAG, "连接服务器异常", e);
                    } catch (ExecutionException ex) {
                        Log.e(TAG, "连接服务器异常ExecutionException", ex);
                    } finally {
                        workerGroup.shutdownGracefully();
                    }
//                } else
//                {
//                    LogUtil.i(TAG, "开始连接服务器 : 当前无网络");
//                    getInstance().disconnect();
//                    getInstance().context.sendBroadcast(new Intent(Actions.CONNECTION_NONET));
//                }
            }
        });
    }

    public synchronized void disconnect() {
        LogUtil.i(TAG, "-------------------disconnect begin ----------------------");

        if (ctx!=null) {
//            JavaThreadPool.getInstance().excute(new Runnable() {
//                @Override
//                public void run() {
                    try {
                        ChannelFuture future = ctx.close();
                        future.get();
                        future.sync();
                    } catch (Exception e) {
                        Log.e(TAG, "关掉TCP连接异常", e);
                    }
//                }
//            });
        }
        LogUtil.i(TAG, "-------------------disconnect end ----------------------");
    }

    public boolean isConnected() {
        return (ctx != null && ctx.channel().isActive());
    }

    public synchronized int getSequenceId() {
        sequenceId = (sequenceId + 1) % 1000;
        return sequenceId;
    }

    public void login() throws IOException {
        LogUtil.i(TAG, "==========login ......");
        SyncMessage message = new SyncMessage();
        message.setCommand(SyncMessage.CommandMenu.rq_login.value());
        JSONObject obj = new JSONObject();
        obj.put("a", dcsp.getString(Constant.LOGIN_ACCOUNT, ""));
//        obj.put("p", dcsp.getString(Constant.LOGIN_PWD, ""));
        obj.put("p", SecurityUtil.MD5(SecurityUtil.MD5(dcsp.getString(Constant.LOGIN_CODE, "")).toLowerCase()+MainApplication.app.getAppGlobalConfig().getAppid()+MainApplication.app.getAppGlobalConfig().getAppSecret()+obj.getString("a")));
        obj.put("appid",MainApplication.app.getAppGlobalConfig().getAppid());
        obj.put("ct", "android");
        obj.put("cl", Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry());
        obj.put("tz", DateUtil.getCurrentTimeZone());
//        obj.put("mp", MainApplication.app.getAppGlobalConfig().getVersionPrefix());
        obj.put("uuid", UTDevice.getUtdid(context));
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            obj.put("version",packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {

        }
        message.setSyncBytes(obj.toJSONString().getBytes("UTF-8"));

        writeMessage(message);
        LogUtil.i(TAG, "==========login ......end");
    }

    public ChannelFuture writeMessage(SyncMessage message) {
        //心跳包只要通道空闲了 4秒钟 才发。否则不发
//		if (message.getCommand() == SyncMessage.CommandMenu.rq_keepalive.value()) {
////			if (ctx.channel().getLastReadTime() < System.currentTimeMillis() - 4*1000 && session.getLastWriteTime() < System.currentTimeMillis() - 4 * 1000) {
//			ctx.writeAndFlush(message);
////			}else{
////				return null;
////			}
//		}else{
        if (!isConnected()) {
            return null;
        }
        return ctx.writeAndFlush(message);
    }
}
