package com.echo.chapter2.c2;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

@Slf4j
public class EventLoopServer {
    public static void main(String[] args) {
        //细分2：创建一个独立的EventLoopGroup,处理耗时的I/O
        EventLoopGroup group = new DefaultEventLoopGroup();


        new ServerBootstrap()
                //划分group，一个group作为boss，只负责ServerSocketChannel上accept事件处理
                // 第二个group作为worker 只负责socketChannel上的读写
                // 因为一个worker只和一个channel绑定，所以作为boss的group只能占用一个线程
                .group(new NioEventLoopGroup(),new NioEventLoopGroup(2))
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline().addLast("handler1",new ChannelInboundHandlerAdapter(){
                            @Override                          //没有使用StringDecoder，这个msg是ByteBuf类型
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf byteBuf = (ByteBuf)msg;
                                log.debug(byteBuf.toString(Charset.defaultCharset()));
                                //将消息传递给下一个handler
                                ctx.fireChannelRead(msg);
                            }
                        })
                                //再添加一个handler，名字为handler2，绑定的方法为HandlerAdapter中的channelRead,
                                //这个handler2，是由group这个EventLoopGroup执行的
                        .addLast(group,"handler2",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf byteBuf = (ByteBuf)msg;
                                log.debug(byteBuf.toString(Charset.defaultCharset()));
                            }
                        });
                    }
                })
                .bind(8080);
    }
}
