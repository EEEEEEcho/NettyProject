package com.echo.chapter3.c2;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class TestRedis {
    /**
     * 模拟redis的协议:redis协议就是这样设计的。
     * set name zhangsan
     * *3           # 这条消息三个字符串
     * $3           # 第一个字符串的长度
     * set          # 第一个字符串的内容
     * $4           # 第二个字符串的长度
     * name         # 第二个字符串的内容
     * $8           # 第三个字符串的长度
     * zhangsan     # 第三个字符串的内容
     * @param args
     */
    public static void main(String[] args) {
        final byte[] LINE = new byte[]{13,10};// /t/n
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try{
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(worker);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new LoggingHandler());
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            ByteBuf buffer = ctx.alloc().buffer();
                            buffer.writeBytes("*3".getBytes(StandardCharsets.UTF_8));
                            buffer.writeBytes(LINE);
                            buffer.writeBytes("$3".getBytes(StandardCharsets.UTF_8));
                            buffer.writeBytes(LINE);
                            buffer.writeBytes("set".getBytes(StandardCharsets.UTF_8));
                            buffer.writeBytes(LINE);
                            buffer.writeBytes("$4".getBytes(StandardCharsets.UTF_8));
                            buffer.writeBytes(LINE);
                            buffer.writeBytes("name".getBytes(StandardCharsets.UTF_8));
                            buffer.writeBytes(LINE);
                            buffer.writeBytes("$8".getBytes(StandardCharsets.UTF_8));
                            buffer.writeBytes(LINE);
                            buffer.writeBytes("zhangsan".getBytes(StandardCharsets.UTF_8));
                            buffer.writeBytes(LINE);
                            ctx.writeAndFlush(buffer);
                        }

                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            //接收redis返回的响应
                            ByteBuf reps = (ByteBuf) msg;
                            System.out.println(reps.toString(StandardCharsets.UTF_8));
                        }
                    });
                }
            });
            ChannelFuture channelFuture = bootstrap.connect("localhost", 6379).sync();
            channelFuture.channel().closeFuture().sync();
        }
        catch (InterruptedException e){
            log.error("client error");
            e.printStackTrace();
        }
        finally {
            worker.shutdownGracefully();
        }
    }
}
