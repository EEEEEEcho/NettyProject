package com.echo.chapter3.c2;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;

@Slf4j
public class TestHttp {
    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.group(boss,worker);
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));

                    //使用Netty自带的Http协议编/解码器
                    socketChannel.pipeline().addLast(new HttpServerCodec());
                    //自定义handler对编/解码器的结果进行处理
//                    socketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
//                        办法一：对msg的类型使用if/else进行判断
//                        @Override
//                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
////                            super.channelRead(ctx, msg);
//                            //log.debug("{}",msg.getClass());
//                            //对Http请求的处理会分为两部分，一个是请求头，一个是请求体
//                            if(msg instanceof HttpRequest){
//                                //请求行，请求头
//                            }
//                            else if(msg instanceof HttpContent){
//                                //请求体
//                            }
//                        }
//                    });
                    //办法二：使用SimpleChannelInboundHandler对特定的消息类型进行处理，通过指定泛型，
                    //该handler只会处理该泛型所指定的消息类型，所以该handler只会处理HttpRequest类型的消息
                    socketChannel.pipeline().addLast(new SimpleChannelInboundHandler<HttpRequest>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, HttpRequest httpRequest) throws Exception {
                            //获取请求
                            log.debug(httpRequest.uri());

                            //返回响应
                            DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                                    httpRequest.protocolVersion(),  //协议版本
                                    HttpResponseStatus.OK //状态码
                            );
                            //设置内容长度
                            byte[] bytes = "<h1>Hello world!</h1>".getBytes(StandardCharsets.UTF_8);
                            response.headers().setInt(CONTENT_LENGTH,bytes.length);
                            response.content().writeBytes(bytes);
                            //将响应写回channel
                            ctx.writeAndFlush(response);
                        }
                    });
                }
            });
            ChannelFuture channelFuture = bootstrap.bind(8080).sync();
            channelFuture.channel().closeFuture().sync();
        }
        catch (InterruptedException e){
            log.debug("server error");
            e.printStackTrace();
        }
        finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
