package com.smartism.znzk.communication.handler;

import android.content.Context;
import android.util.Log;

import java.net.SocketAddress;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 *
 * tcp 连接建立时会触发这里面的方法 是否启用需要在{@code SyncClientNettyConnector.connect}中配置
 *
 * 可以在连接操作之前，之后 做一些事情 触发异常会抛给主线程处理，导致崩溃。所有异常都捕获掉并打印日志。
 *
 * 2016年10月25日  by 王建
 */
public class ProcessNettyOutHandler extends ChannelOutboundHandlerAdapter {
	private static final String TAG = "ProcessNettyOutHandler";
	@SuppressWarnings("unused")
	private Context context;

	public ProcessNettyOutHandler(Context context) {
		this.context = context;
	}

	@Override
	public void close(ChannelHandlerContext ctx, ChannelPromise promise) {
		try {
			super.close(ctx, promise);
		}catch (Exception ex){
			Log.i(TAG,"close exception: ",ex);
		}
	}

	@Override
	public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) {
		try {
			super.disconnect(ctx, promise);
		}catch (Exception ex){
			Log.i(TAG,"disconnect: ",ex);
		}
	}

	@Override
	public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) {
		try {
			super.deregister(ctx, promise);
		}catch (Exception ex){
			Log.i(TAG,"deregister: ",ex);
		}
	}

	@Override
	public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
		try{
			super.connect(ctx, remoteAddress, localAddress, promise);
		}catch (Exception ex){
			Log.i(TAG,"connection exception: ",ex);
		}
	}

	@Override
	public void read(ChannelHandlerContext ctx) throws Exception {
		try{
			super.read(ctx);
		}catch (Exception ex){
			Log.i(TAG,"read exception: ",ex);
		}
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		try{
			super.write(ctx, msg, promise);
		}catch (Exception ex){
			Log.i(TAG,"write exception: ",ex);
		}
	}

	@Override
	public void flush(ChannelHandlerContext ctx) throws Exception {
		try{
			super.flush(ctx);
		}catch (Exception ex){
			Log.i(TAG,"flush exception: ",ex);
		}
	}
}
