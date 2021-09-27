package com.echo.chapter2.c2;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;

public class EventLoopClient {
    public static void main(String[] args) throws InterruptedException {
        //1.启动类
        Channel channel = new Bootstrap()
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
                .connect(new InetSocketAddress("localhost", 8080))
                .sync() //阻塞方法，直到连接建立
                .channel();//代表客户端和服务器之间的channel对象

        System.out.println(channel);
        System.out.println("");

    }
}
