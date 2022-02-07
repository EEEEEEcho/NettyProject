package com.echo.chat.server.handler;

import com.echo.chat.message.GroupChatRequestMessage;
import com.echo.chat.message.GroupChatResponseMessage;
import com.echo.chat.server.session.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

@ChannelHandler.Sharable
public class GroupChatRequestMessageHandler extends SimpleChannelInboundHandler<GroupChatRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupChatRequestMessage message) throws Exception {
        //根据组名字，得到组内成员的所有Channel
        List<Channel> membersChannel = GroupSessionFactory.getGroupSession().getMembersChannel(message.getGroupName());
        for (Channel channel : membersChannel){
            channel.writeAndFlush(new GroupChatResponseMessage(message.getFrom(), message.getContent()));
        }
    }
}
