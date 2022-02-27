package com.echo.review;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class RedisReview {
    /**
     * set key value
     * *3
     * $3
     * set
     * $4
     * name
     * $4
     * echo
     * @param args
     */
    public static void main(String[] args) {
        NioEventLoopGroup worker = new NioEventLoopGroup();
        //换行
        final byte[] LINE = {13,10};
        try{
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(worker);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                    ChannelPipeline pipeline = nioSocketChannel.pipeline();
                    pipeline.addLast(new LoggingHandler());
                    pipeline.addLast(new ChannelInboundHandlerAdapter(){
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
                            buffer.writeBytes("$4".getBytes(StandardCharsets.UTF_8));
                            buffer.writeBytes(LINE);
                            buffer.writeBytes("echo".getBytes(StandardCharsets.UTF_8));
                            buffer.writeBytes(LINE);
                            super.channelActive(ctx);
                        }
                    });
                }

                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    ByteBuf buf = (ByteBuf) msg;
                    System.out.println(buf.toString(Charset.defaultCharset()));
                }
            });
            ChannelFuture channelFuture = bootstrap.connect("localhost", 6379).sync();
            channelFuture.channel().closeFuture().sync();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            worker.shutdownGracefully();
        }
    }
}
