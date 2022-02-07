package com.echo.chat.client;

import com.echo.chat.message.*;
import com.echo.chat.protocol.MessageCodec;
import com.echo.chat.protocol.MessageCodecSharable;
import com.echo.chat.protocol.ProtocolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ChatClient {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        //LoggingHandler是可以线程共享的
        LoggingHandler LOGGING_HANDLER = new LoggingHandler();
        MessageCodecSharable CODEC_HANDLER = new MessageCodecSharable();
        //接收服务端返回消息的线程是NIO的线程
        //等待继续向下运行的线程是我们自己创建的System-in的线程
        //为了能够使线程同步，即接收到服务端的消息后，System-in线程才能继续运行，需要进行线程间通信同步
        //这里使用Count Down Latch 计数锁
        CountDownLatch waitForLogin = new CountDownLatch(1);
        //在线程之间传递是否登录成功
        AtomicBoolean login = new AtomicBoolean(false);
        try{
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(group);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new ProtocolFrameDecoder());
                    socketChannel.pipeline().addLast(LOGGING_HANDLER);
                    socketChannel.pipeline().addLast(CODEC_HANDLER);

                    //加入空闲状态检测器，检测假死现象，用来判断是不是读空闲时间过长或者写空闲时间过长
                    //参数二：最大写空闲时长。这里的3代表，3秒内如果没有向服务器发送一个
                    //数据，会触发一个IdleState#WRITE_IDLE事件
                    socketChannel.pipeline().addLast(new IdleStateHandler(0,3,0));
                    //ChannelDuplexHandler可以作为一个双向的处理器，处理入站和出站事件
                    socketChannel.pipeline().addLast(new ChannelDuplexHandler(){
                        //用来处理触发的特殊事件。这里用来处理我们在IdleStateHandler中监听到的写空闲时间过长的事件
                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                            IdleStateEvent event = (IdleStateEvent) evt;
                            //触发了写空闲事件，如果触发了，就向服务器发一个心跳包，告诉服务器自己还活着
                            if (event.state() == IdleState.WRITER_IDLE) {
                                log.debug("3s 没有写数据了，发送一个心跳包");
                                ctx.writeAndFlush(new PingMessage());
                            }
                        }
                    });


                    socketChannel.pipeline().addLast("client handler",new ChannelInboundHandlerAdapter(){
                        //在连接建立之后触发active事件，往服务端发送消息
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            //获取用户在控制台的输入，向服务器发送各种消息，单开线程是为了不让输入阻塞 eventloop
                            new Thread(() -> {
                                Scanner scanner = new Scanner(System.in);
                                System.out.println("请输入用户名：");
                                String username = scanner.nextLine();
                                System.out.println("请输入密码：");
                                String password = scanner.nextLine();
                                //构造消息对象
                                LoginRequestMessage message = new LoginRequestMessage(username, password);
                                //发送消息
                                //发送消息会从当前handler往前找，找出战处理器来接收这条消息。
                                //当前handler就会触发MessageCodec来将消息编码为ByteBuf
                                ctx.writeAndFlush(message);

                                System.out.println("等待");
                                try {
                                    //等待被唤醒
                                    waitForLogin.await();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                if(!login.get()){
                                    //如果登录失败
                                    ctx.channel().close();
                                    return;
                                }
                                while (true){
                                    System.out.println("==================================");
                                    System.out.println("send [username] [content]");
                                    System.out.println("gsend [group name] [content]");
                                    System.out.println("gcreate [group name] [m1,m2,m3...]");
                                    System.out.println("gmembers [group name]");
                                    System.out.println("gjoin [group name]");
                                    System.out.println("gquit [group name]");
                                    System.out.println("quit");
                                    System.out.println("==================================");

                                    String command = scanner.nextLine();
                                    String[] s = command.split(" ");
                                    switch (s[0]){
                                        case "send":
                                            //发送一条聊天的消息
                                            ctx.writeAndFlush(new ChatRequestMessage(username,s[1],s[2]));
                                            break;
                                        case "gsend":
                                            //往聊天组发送一条消息
                                            ctx.writeAndFlush(new GroupChatRequestMessage(username,s[1],s[2]));
                                            break;
                                        case "gcreate":
                                            //创建一个聊天组
                                            //取出所有成员
                                            HashSet<String> memebers = new HashSet<>(Arrays.asList(s[2].split(",")));
                                            memebers.add(username); //加入自己
                                            ctx.writeAndFlush(new GroupCreateRequestMessage(s[1],memebers));
                                            break;
                                        case "gmembers":
                                            //获取聊天组的成员
                                            ctx.writeAndFlush(new GroupMembersRequestMessage(s[1]));
                                            break;
                                        case "gjoin":
                                            //加入组
                                            ctx.writeAndFlush(new GroupJoinRequestMessage(username,s[1]));
                                            break;
                                        case "gquit":
                                            //退出组
                                            ctx.writeAndFlush(new GroupQuitRequestMessage(username,s[1]));
                                            break;
                                        case "quit":
                                            //退出
                                            ctx.channel().close();
                                            return;
                                    }
                                }

                            },"system-in").start();
                        }

                        //接收服务端响应的消息
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            log.debug("msg: {}" , msg);
                            if(msg instanceof LoginResponseMessage){
                                LoginResponseMessage response = (LoginResponseMessage) msg;
                                if (response.isSuccess()){
                                    //如果登录成功，标记为成功
                                    login.set(true);
                                }
                                waitForLogin.countDown();   //计数减一，唤醒等待的线程
                            }

                        }
                    });
                }
            });
            Channel channel = bootstrap.connect("localhost", 8080).sync().channel();
            channel.closeFuture().sync();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            group.shutdownGracefully();
        }
    }
}
