package com.smartism.znzk.communication.connector;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Created by zhihuimao on 16/10/25.  未用
 */

public class NettyTcpClient
{
    private EventLoopGroup loop = new NioEventLoopGroup();
    public static void main( String[] args )
    {
        new NettyTcpClient().run();
    }
    public Bootstrap createBootstrap(Bootstrap bootstrap, EventLoopGroup eventLoop) {
        if (bootstrap != null) {
            final NettyInBoundHandler handler = new NettyInBoundHandler(null,this);
            bootstrap.group(eventLoop);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(handler);
                }
            });
            bootstrap.remoteAddress("localhost", 8888);
            bootstrap.connect().addListener(new NettyTcpConnectionListener(this));
        }
        return bootstrap;
    }
    public void run() {
        createBootstrap(new Bootstrap(), loop);
    }
}
