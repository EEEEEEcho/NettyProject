package com.echo.chapter2.c4;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

@Slf4j
public class PipeLineServer {
    public static void main(String[] args) {
        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel channel) throws Exception {
                        //1.连接建立以后，就可以拿到该连接的channel对象，通过channel获取pipeline
                        ChannelPipeline pipeline = channel.pipeline();
                        //2.添加处理器。在添加处理器之前，netty会自动帮我们添加两个处理器，一个叫headHandler
                        //一个叫tailHandler。我们自己添加的handler会添加到这两个handler之间
                        pipeline.addLast("handler1",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.debug("1");
                                //将消息转换为一个字符串s，进行处理
                                ByteBuf byteBuf = (ByteBuf) msg;
                                String s = byteBuf.toString(Charset.defaultCharset());
                                //将s传递给下一个handler处理，这里super.channelRead中会调用下一个handler
                                //如果不调用下一个handler，那么入站链就会断开
                                super.channelRead(ctx,s);
                            }
                        });
                        pipeline.addLast("handler2",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.debug("2");
                                //将上一个handler中处理的结果（就是字符串s)作为student对象的name属性值
                                Student student = new Student(msg.toString());
                                //将student对象传递给下一个handler
                                super.channelRead(ctx,student);
                            }
                        });
                        pipeline.addLast("handler3",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.debug("3");
                                log.debug("结果：{},类型:{}",msg,msg.getClass());
                                //最后一个入站handler，就不需要调用channelRead去调用下一个handler了
                                //super.channelRead(ctx,msg);
                                //写出数据
                                channel.writeAndFlush(ctx.alloc().buffer().writeBytes("Server".getBytes()));
                                /*注意！！！！！！！！！！！！！！！！！*/
                                /*ctx中也有一个方法:ctx.writeAndFlush(),该方法的处理逻辑与channel.writeAndFlush()有很大差别*/
                                /*ctx.writeAndFlush()会从当前handler向前寻找OutBoundHandler*/
                                /*比如，headHandler -> handler1 -> handler2 -> handler3 -> handler4 -> handler5 -> handler6 ->tailHandler*/
                                /*当前handler是handler3，调用ctx.writeAndFlush()后会从handler3开始，向前继续寻找OutBoundHandler*/
                                /*在这里handler3之前并没有其他OutBoundHandler*/
                                /*channel.writeAndFlush()则会从尾向前寻找*/
                            }
                        });
                        //headHandler -> handler1 -> handler2 -> handler3 -> tailHandler

                        pipeline.addLast("handler4",new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("4");
                                super.write(ctx, msg, promise);
                            }
                        });
                        pipeline.addLast("handler5",new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("5");
                                super.write(ctx, msg, promise);
                            }
                        });
                        pipeline.addLast("handler6",new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("6");
                                super.write(ctx, msg, promise);
                            }
                        });

                        //headHandler -> handler1 -> handler2 -> handler3 -> handler4 -> handler5 -> handler6 ->tailHandler
                    }
                })
                .bind(8080);
    }
}
class Student{
    String name;
    public Student(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                '}';
    }
}