package com.echo.chapter4;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestConnectionTimeOut {
    public static void main(String[] args) {
        //1.客户端通过option()方法配置参数

        //服务端有两个可以配置参数的选项
        //new ServerBootstrap().option()  //给ServerSocketChannel配置参数
        //new ServerBootstrap().childOption() //给SocketChannel配置参数
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,3000)   //配置超时时间，3秒
                    .channel(NioSocketChannel.class)
                    .handler(new LoggingHandler());
            ChannelFuture future = bootstrap.connect("127.0.0.1", 8080);
            future.sync().channel().closeFuture().sync();
        }
        catch (Exception e){
            e.printStackTrace();
            log.debug("timeout");
        }
        finally {
            group.shutdownGracefully();
        }
    }
}
