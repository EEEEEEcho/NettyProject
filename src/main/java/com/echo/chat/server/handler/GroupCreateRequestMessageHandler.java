package com.echo.chat.server.handler;

import com.echo.chat.message.GroupCreateRequestMessage;
import com.echo.chat.message.GroupCreateResponseMessage;
import com.echo.chat.server.session.Group;
import com.echo.chat.server.session.GroupSession;
import com.echo.chat.server.session.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;
import java.util.Set;

@ChannelHandler.Sharable
public class GroupCreateRequestMessageHandler extends SimpleChannelInboundHandler<GroupCreateRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupCreateRequestMessage message) throws Exception {
        String groupName = message.getGroupName();
        Set<String> members = message.getMembers();
        //群管理器
        GroupSession groupSession = GroupSessionFactory.getGroupSession();
        Group group = groupSession.createGroup(groupName, members);
        if (group == null){
            //给所有的且在线的member发送拉群消息
            List<Channel> channels = groupSession.getMembersChannel(groupName);
            for (Channel channel : channels){
                channel.writeAndFlush(new GroupCreateResponseMessage(true,"您已被拉入 " + groupName));
            }
            //发送成功消息
            ctx.writeAndFlush(new GroupCreateResponseMessage(true,groupName + "创建成功"));
        }
        else{
            ctx.writeAndFlush(new GroupCreateResponseMessage(true,groupName + "已经存在"));
        }
    }
}
