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
                //这两个EventLoop中包含监测事件的selector，以及该selector对应的线程thread
                //EventLoop = selector + thread  EventLoopGroup = 多个EventLoop
                /*这里面的EventLoop会监听accept()事件，在事件发生后netty内部会进行处理，建立连接
                在连接建立后调用initChannel方法，initChannel方法就会为建立好的channel添加两个处理器
                在这里的一个处理器是StringDecoder()另一个是自定义的处理器
                */
                .group(new NioEventLoopGroup())
                //3.NioServerSocketChannel.class:一个server socket channel的实现
                .channel(NioServerSocketChannel.class)
                //4.ChildHandler:处理分工，boss是负责处理连接的(accept),
                //而worker(netty里是child)负责处理读写。ChildHandler用来告诉worker(child)能执行哪些操作(handler)
                //childHandler其实就是多路复用中的worker，用于处理channel上的读写或者其他事件
                .childHandler(
                        //5.Channel代表和客户端连接后，进行读写的数据通道；Initializer代表一个初始化器，负责
                        //添加别的handler
                        new ChannelInitializer<NioSocketChannel>() {

                            @Override       //该方法只有在连接建立后才会触发
                            protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                                //6.在initChannel添加具体的handler
                                nioSocketChannel.pipeline().addLast(new StringDecoder());//将传输过来的ByteBuffer转为字符串
                                nioSocketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                    //自定义的handler
                                    @Override   //处理该channel上的读事件，在读事件触发之后，打印转换好的字符串
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        System.out.println(msg);
                                    }
                                });
                            }
                        })
                //7.绑定的监听端口
                .bind(8080);



                //客户端发送数据后，服务端开始接收，EventLoopGroup中有监听read事件的selector，该selector会处理该读事件
                //并按照initChannel方法中定义的handler的顺序，将消息进行处理。本例中是先调用StringDecoder handler将
                //ByteBuf转换成字符串String。然后将这个转换后的String交给下一个Handler，即我们自定义的handler。然后
                //打印该字符串
    }
}
