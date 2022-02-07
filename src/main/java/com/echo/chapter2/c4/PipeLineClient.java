package com.echo.chapter2.c4;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Scanner;
@Slf4j
public class PipeLineClient {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        ChannelFuture channelFuture = new Bootstrap()
                .group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {

                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        //以Debug模式来打印日志
                        nioSocketChannel.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));

                        nioSocketChannel.pipeline().addLast(new StringEncoder());
                    }
                })
                .connect(new InetSocketAddress("localhost", 8080));
        Channel channel = channelFuture.sync().channel();
        log.debug("{}",channel);


        //获取用户输入的线程
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true){
                String s = scanner.nextLine();
                if ("q".equals(s)){
                    channel.close();    //close()方法也是一个异步操作
                    break;
                }
                channel.writeAndFlush(s);
            }
        },"Input-Thread").start();


        ChannelFuture closeFuture = channel.closeFuture();
        //2）异步处理关闭
        closeFuture.addListener((ChannelFutureListener) channelFuture1 -> {
            log.debug("处理 关闭之后的操作");
            //关闭EventLoopGroup
            eventLoopGroup.shutdownGracefully();
        });
    }
}
