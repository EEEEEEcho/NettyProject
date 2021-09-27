package com.echo.chapter2.c2;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
@Slf4j
public class ChannelFutureDemo {
    public static void main(String[] args) throws InterruptedException {
        ChannelFuture channelFuture = new Bootstrap()   //带有Future,Promise的方法都是和异步方法配套使用的，用来处理结果
                //2.添加EventLoop
                .group(new NioEventLoopGroup())
                //3.选择客户端channel实现
                .channel(NioSocketChannel.class)
                //4.添加处理器
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override   //在连接建立后被调用
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        //将字符串编码为ByteBuffer
                        ch.pipeline().addLast(new StringEncoder());
                    }
                })
                //5.连接到服务器
                //connect方法是一个异步非阻塞的，main线程调用了connect，并不关心过connect是否连接上了
                //真正执行底层connect的是NIO线程，主线程只是发起了调用
                .connect(new InetSocketAddress("localhost", 8080));

        //如果不调用sync()等待连接建立，主线程就会直接建立channel，这时，连接可能还没有建立
        //解决：1.使用sync()方法同步处理结果，等待NIO线程建立连接,同步谁发起的谁等结果（主线程发起的，主线程等NIO建立完成）
//        channelFuture.sync();//阻塞住当前线程，直到NIO线程建立连接完毕
//        Channel channel = channelFuture.channel();
//        log.debug("{}",channel);
//        channel.writeAndFlush("Hello , world");

        //解决：2.使用addListener(回调对象)方法处理异步结果（谁发起的，别人处理结果。主线程发起的，NIO处理结果）
        //NIO建立好连接之后，会调用(NIO线程调用，不是main线程调用)回调对象中的operationComplete方法处理结果
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                //这个形参ChannelFuture和调用addListener的ChannelFuture是同一个
                Channel channel = channelFuture.channel();
                log.debug("{}",channel);
                channel.writeAndFlush("Hello world");
            }
        });
    }
}
