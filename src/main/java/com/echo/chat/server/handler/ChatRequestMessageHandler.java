package com.echo.chat.server.handler;

import com.echo.chat.message.ChatRequestMessage;
import com.echo.chat.message.ChatResponseMessage;
import com.echo.chat.server.session.SessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class ChatRequestMessageHandler extends SimpleChannelInboundHandler<ChatRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatRequestMessage msg) throws Exception {
        //消息的目的地
        String to = msg.getTo();
        //从channel和username的对应关系中，找到对方的channel
        Channel channel = SessionFactory.getSession().getChannel(to);

        if(channel != null){
            //在线,就给对方把消息发过去
            channel.writeAndFlush(new ChatResponseMessage(msg.getFrom(), msg.getContent()));
        }
        else{
        //离线
            ctx.writeAndFlush(new ChatResponseMessage(false,"对方用户不在线"));
        }
    }
}
