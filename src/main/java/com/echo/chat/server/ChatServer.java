package com.echo.chat.server;

import com.echo.chat.protocol.MessageCodec;
import com.echo.chat.protocol.MessageCodecSharable;
import com.echo.chat.protocol.ProtocolFrameDecoder;
import com.echo.chat.server.handler.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChatServer {
    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        //LoggingHandler是可以线程共享的
        LoggingHandler LOGGING_HANDLER = new LoggingHandler();
        MessageCodecSharable CODEC_HANDLER = new MessageCodecSharable();
        LoginRequestMessageHandler LOGIN_REQUEST_HANDLER = new LoginRequestMessageHandler();
        ChatRequestMessageHandler CHAT_REQUEST_HANDLER = new ChatRequestMessageHandler();
        GroupCreateRequestMessageHandler GROUP_CREATE_HANDLER = new GroupCreateRequestMessageHandler();
        GroupJoinRequestMessageHandler GROUP_JOIN_HANDLER = new GroupJoinRequestMessageHandler();
        GroupMembersRequestMessageHandler GROUP_MEMBER_HANDLER = new GroupMembersRequestMessageHandler();
        GroupQuitRequestMessageHandler GROUP_QUIT_HANDLER = new GroupQuitRequestMessageHandler();
        GroupChatRequestMessageHandler GROUP_CHAT_HANDLER = new GroupChatRequestMessageHandler();
        QuitHandler QUIT_HANDLER = new QuitHandler();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.group(boss,worker);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new ProtocolFrameDecoder());
                    socketChannel.pipeline().addLast(LOGGING_HANDLER);
                    socketChannel.pipeline().addLast(CODEC_HANDLER);

                    //加入空闲状态检测器，检测假死现象
                    //用来判断是不是读空闲时间过长或者写空闲事件过长
                    //参数一：最大读空闲时长。这里的5代表，5秒内如果没有读到一个channel的
                    //数据，会触发一个IdleState#READER_IDLE事件
                    //将触发到的IdleState事件传递给ChannelDuplexHandler处理
                    socketChannel.pipeline().addLast(new IdleStateHandler(5,0,0));
                    //ChannelDuplexHandler可以作为一个双向的处理器，处理入站和出站事件
                    socketChannel.pipeline().addLast(new ChannelDuplexHandler(){
                        //用来处理触发的特殊事件。这里用来处理我们在IdleStateHandler中监听到的读空闲时间过长的事件
                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//                            super.userEventTriggered(ctx, evt);
                            IdleStateEvent event = (IdleStateEvent) evt;
                            //触发了读空闲事件
                            if (event.state() == IdleState.READER_IDLE) {
                                log.debug("已经 5s 没有读到数据了");
                                //发现连接假死，释放服务器资源
                                ctx.channel().close();
                            }
                        }
                    });


                    // 添加一个只对LoginRequestMessage的消息感兴趣的handler
                    socketChannel.pipeline().addLast(LOGIN_REQUEST_HANDLER);
                    // 添加一个只对ChatRequestMessage感兴趣的handler
                    socketChannel.pipeline().addLast(CHAT_REQUEST_HANDLER);
                    // 其余同理
                    socketChannel.pipeline().addLast(GROUP_CREATE_HANDLER);
                    socketChannel.pipeline().addLast(GROUP_JOIN_HANDLER);
                    socketChannel.pipeline().addLast(GROUP_MEMBER_HANDLER);
                    socketChannel.pipeline().addLast(GROUP_QUIT_HANDLER);
                    socketChannel.pipeline().addLast(GROUP_CHAT_HANDLER);
                    socketChannel.pipeline().addLast(QUIT_HANDLER);
                }
            });

            ChannelFuture future = serverBootstrap.bind(8080).sync();
            future.channel().closeFuture().sync();
        }
        catch (InterruptedException e){
            log.debug("ERROR!");
            e.printStackTrace();
        }
        finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

}
