package com.echo.chat.server.handler;

import com.echo.chat.server.session.SessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@ChannelHandler.Sharable
@Slf4j
public class QuitHandler extends ChannelInboundHandlerAdapter {

    //连接断开时，触发inactive事件
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //从session列表中移除断开的session
        SessionFactory.getSession().unbind(ctx.channel());
        log.debug("{} 已经断开",ctx.channel());
    }

    //当异常发生时，触发exceptionCaught
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        SessionFactory.getSession().unbind(ctx.channel());
        log.debug("{} 已经异常断开",ctx.channel(),cause.getMessage());
    }
}
