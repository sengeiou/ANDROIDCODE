package com.smartism.znzk.communication.connector;

import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;

/**
 * Created by zhihuimao on 16/10/25.  未用
 */

public class NettyTcpConnectionListener implements ChannelFutureListener {
        private NettyTcpClient client;
        public NettyTcpConnectionListener(NettyTcpClient client) {
            this.client = client;
        }
        @Override
        public void operationComplete(ChannelFuture channelFuture) throws Exception {
            if (!channelFuture.isSuccess()) {
                System.out.println("Reconnect");
                final EventLoop loop = channelFuture.channel().eventLoop();
                loop.schedule(new Runnable() {
                    @Override
                    public void run() {
                        client.createBootstrap(new Bootstrap(), loop);
                    }
                }, 1L, TimeUnit.SECONDS);
            }
        }
    }