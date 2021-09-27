package com.echo.chapter2.c1;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

public class HelloServer {
    public static void main(String[] args) {
        //1.ServerBootstrap：服务器端启动器，负责组装netty组件
        new ServerBootstrap()
                //2.NioEventLoopGroup:就是指之前的BossEventLoop和WorkerEventLoop
                //这两个Loop中包含监测事件的selector，以及该selector对应的线程thread
                .group(new NioEventLoopGroup())
                //3.NioServerSocketChannel.class:一个server socket channel的实现
                .channel(NioServerSocketChannel.class)
                //4.ChildHandler:处理分工，boss是负责处理连接的(accept),
                //而worker(netty里是child)负责处理读写。ChildHandler用来告诉worker(child)能执行哪些操作(handler)
                .childHandler(
                        //5.Channel代表和客户端连接后，进行读写的数据通道；Initializer代表一个初始化器，负责
                        //添加别的handler
                        new ChannelInitializer<NioSocketChannel>() {

                    @Override       //该方法只有在连接建立后才会触发
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        //6.在initChannel添加具体的handler
                        nioSocketChannel.pipeline().addLast(new StringDecoder());//将传输过来的ByteBuffer转为字符串
                        nioSocketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                            //自定义的handler
                            @Override   //处理读事件，在读事件触发之后，打印转换好的字符串
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                System.out.println(msg);
                            }
                        });
                    }
                })
                //7.绑定的监听端口
                .bind(8080);
    }
}
