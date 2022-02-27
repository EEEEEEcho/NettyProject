package com.echo.review;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LoggingHandler;

import java.nio.charset.StandardCharsets;

import static com.google.common.net.HttpHeaders.CONTENT_LENGTH;

public class HttpReview {
    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss,worker);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline pipeline = socketChannel.pipeline();
                    pipeline.addLast(new LoggingHandler());
                    //http的编解码器,netty已经内置好了
                    //将请求解析成了两个部分。一部分是请求头，一部分是请求体
                    pipeline.addLast(new HttpServerCodec());
//                    pipeline.addLast(new ChannelInboundHandlerAdapter(){
//                        @Override
//                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                            System.out.println(msg.getClass());
//                            if (msg instanceof HttpRequest){
//                                System.out.println("这是请求头和请求行");
//                            }
//                            else if(msg instanceof HttpContent){
//                                System.out.println("这是请求体");
//                            }
//                        }
//                    });
                    //使用SimpleChannelInboundHandler可以根据需求，过滤想要的消息类型
                    //该handler只会对HttpRequest类型的消息感兴趣
                    pipeline.addLast(new SimpleChannelInboundHandler<HttpRequest>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext channelHandlerContext, HttpRequest httpRequest) throws Exception {
                            System.out.println(httpRequest.uri());
                            //返回响应,设置响应的协议版本为请求的版本，状态码为200
                            DefaultFullHttpResponse response = new DefaultFullHttpResponse(httpRequest.protocolVersion(),
                                    HttpResponseStatus.OK);
                            //写入响应内容，
                            byte[] responseBytes = "<h1>Hello world</h1>".getBytes(StandardCharsets.UTF_8);
                            //写入响应的长度
                            response.headers().setInt(CONTENT_LENGTH,responseBytes.length);
                            //写入响应的内容
                            response.content().writeBytes(responseBytes);

                            //写出内容
                            channelHandlerContext.writeAndFlush(response);
                        }
                    });
                    //该handler只会对HttpContent类型的消息感兴趣
                    pipeline.addLast(new SimpleChannelInboundHandler<HttpContent>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext channelHandlerContext, HttpContent httpContent) throws Exception {
                            System.out.println(httpContent.toString());
                        }
                    });
                }
            });
            ChannelFuture channelFuture = serverBootstrap.bind(8080).sync();
            channelFuture.channel().closeFuture().sync();
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }
        finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
