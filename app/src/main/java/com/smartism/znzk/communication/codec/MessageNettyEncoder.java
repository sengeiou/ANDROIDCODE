package com.smartism.znzk.communication.codec;

import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessage.CommandMenu;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.SecurityUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MessageNettyEncoder extends MessageToByteEncoder<SyncMessage> {
	private static final String TAG = "MessageNettyEncoder";

	@Override
	protected void encode(ChannelHandlerContext channelHandlerContext, SyncMessage message, ByteBuf out) throws Exception {
		ByteBuf buffer = null;
		if (message.getCommand() == CommandMenu.rq_keepalive.value()) { //心跳包只发送包头和名字共10个字符
			buffer = out.alloc().buffer(10);
			buffer.writeByte((byte)Integer.parseInt("61", 16));
			buffer.writeByte((byte)Integer.parseInt("76", 16));
			buffer.writeInt(8);  //不包含包头
			buffer.writeInt(message.getCommand());
			LogUtil.i("BAO", "发往服务器的心跳包为:"+ ByteBufUtil.hexDump(buffer));
			out.writeBytes(buffer);
		}else{
			if (message.getSyncBytes().length>0) {
				message.setSyncBytes(SecurityUtil.crypt(message.getSyncBytes(), Constant.KEY_TCP));
			}
			buffer = out.alloc().buffer(message.getSyncBytes().length+22);
			buffer.writeByte((byte)Integer.parseInt("61", 16));
			buffer.writeByte((byte)Integer.parseInt("76", 16));
			buffer.writeInt(message.getSyncBytes().length+20);  //不包含包头
			buffer.writeInt(message.getCommand());
			buffer.writeInt(message.getCode());
			buffer.writeLong(message.getDeviceid());
			buffer.writeBytes(message.getSyncBytes());
			LogUtil.i("BAO", "发往服务器的包为:"+ByteBufUtil.hexDump(buffer));
			out.writeBytes(buffer);
		}
	}
}
