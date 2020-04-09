package com.smartism.znzk.communication.codec;

import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.SecurityUtil;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.EmptyByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class MessageNettyDecoder extends ByteToMessageDecoder {
	private static final String TAG = "MessageNettyDecoder";
	@SuppressWarnings("unused")
	private final CharsetDecoder charsetDecoder;

	public MessageNettyDecoder() {
		charsetDecoder = Charset.forName("utf-8").newDecoder();
	}

	@Override
	protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
		if (in instanceof EmptyByteBuf) {
			return;
		}

		LogUtil.i("BAO", "收到服务器的包为:"+ ByteBufUtil.hexDump(in));
		//读取缓冲区中请求消息的长度
//		in.markReaderIndex(); // 设置当前readindex到mark变量中
		int size = in.getInt(2);  //获取总包的长度，不包括包阻断字符 get方法不会改变读取指向后移
//		in.resetReaderIndex();
		if(in.readableBytes() >= size && size == 8){  //心跳8长度包
			SyncMessage message = new SyncMessage();
			in.skipBytes(2);
			message.setTotalLength(in.readInt());
			message.setCommand(in.readInt());
			out.add(message);
		}else if (in.readableBytes() >= size) {
			in.skipBytes(2); //跳过包头
			SyncMessage message = new SyncMessage();
			message.setTotalLength(in.readInt());
			message.setCommand(in.readInt());
			message.setCode(in.readInt());
			message.setDeviceid(in.readLong());
			byte[] temp = new byte[size - 20];
			in.readBytes(temp, 0, size - 20);
			if (temp.length > 0) {
				temp = SecurityUtil.decrypt(temp, Constant.KEY_TCP);
//					LogUtil.i("BAO", "收到服务器的包解密之后的报文为:"+PacketUtil.byteArrayToHexString(temp));
			}
			message.setSyncBytes(temp);
			out.add(message);
		}
	}
}
