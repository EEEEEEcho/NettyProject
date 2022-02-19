package com.echo.chapter2.c1;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;

public class HelloClient {
    public static void main(String[] args) throws InterruptedException {
        //1.启动类
        new Bootstrap()
                //2.添加EventLoop
                /*客户端在监听到连接建立以后，会调用initChannel中的方法，为该连接建立后的channel添加
                一个处理器,即StringEncoder
                *
                * */
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
                .connect(new InetSocketAddress("localhost",8080))
                .sync() //阻塞方法，直到连接建立
                .channel()//代表客户端和服务器之间的channel对象
                //6.向服务器发送数据,
                .writeAndFlush("Hello,world");
                //发送数据后，发送的数据会走到initChannel方法添加的handler中，此处为StringEncoder()，将Hello world
                //字符串转换成ByteBuf发送出去
    }
}
