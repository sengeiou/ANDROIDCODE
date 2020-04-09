package com.smartism.znzk.communication.connector;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.smartism.znzk.communication.data.SyncDataDispatcher;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.util.Actions;

import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoop;

/**
 *
 * Netty TCP client handler class
 *
 * 处理连接、收到数据等业务逻辑  未用
 *
 * 2016年10月15日  by 王建
 */
public class NettyInBoundHandler extends ChannelInboundHandlerAdapter {
	private static final String TAG = "NettyInBoundHandler";
	private NettyTcpClient client;
	@SuppressWarnings("unused")
	private Context context;

	public NettyInBoundHandler(Context context,NettyTcpClient client) {
		this.context = context;
		this.client = client;
	}

	/**
	 * 收到服务器的包
	 * @param ctx
	 * @param message
	 * @throws Exception
     */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
//		if (ctx == SyncClientNettyConnector.getInstance().getCtx()) {
			SyncMessage syncMessage = (SyncMessage) message;
			SyncDataDispatcher.getInstance(context).dispatch(syncMessage);
//		}else{
//			Log.i(TAG,"TCP收到不是当前连接的包。。。");
//		}
	}


	/**
	 * 连接建立成功
	 * @param ctx
	 * @throws Exception
     */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		final EventLoop eventLoop = ctx.channel().eventLoop();
		eventLoop.schedule(new Runnable() {
			@Override
			public void run() {
				client.createBootstrap(new Bootstrap(), eventLoop);
			}
		}, 1L, TimeUnit.SECONDS);
		super.channelInactive(ctx);

		SyncClientNettyConnector.getInstance().setCtx(ctx);
		SyncClientNettyConnector.getInstance().login();
	}

	/**
	 * 连接出现异常 会主动调用channelInactive方法
	 * @param ctx
	 * @param cause
	 * @throws Exception
     */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		Log.e(TAG,"连接异常",cause);
	}

	/**
	 * 连接已断开
	 * @param ctx
	 * @throws Exception
     */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (ctx == SyncClientNettyConnector.getInstance().getCtx()){
			context.sendBroadcast(new Intent(Actions.CONNECTION_ING));
			SyncClientNettyConnector.getInstance().setCtx(null);
		}
	}
}
