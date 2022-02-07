package com.echo.chat.server.handler;

import com.echo.chat.message.LoginRequestMessage;
import com.echo.chat.message.LoginResponseMessage;
import com.echo.chat.server.service.UserService;
import com.echo.chat.server.service.UserServiceFactory;
import com.echo.chat.server.session.SessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
@ChannelHandler.Sharable    //无共享消息
public class LoginRequestMessageHandler extends SimpleChannelInboundHandler<LoginRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginRequestMessage message) throws Exception {
        //因为前面的消息已经经过了解码器解码
        String username = message.getUsername();
        String password = message.getPassword();
        //判断用户名和密码的正确性 并返回
        UserService userService = UserServiceFactory.getUserService();
        boolean loginSuccess = userService.login(username, password);
        LoginResponseMessage responseMessage = null;
        if (loginSuccess) {
            //登陆成功之后，将channel和用户名绑定
            SessionFactory.getSession().bind(ctx.channel(), username);
            responseMessage = new LoginResponseMessage(true, "登陆成功");
        } else {
            responseMessage = new LoginResponseMessage(false, "用户名或密码错误");
        }
        ctx.writeAndFlush(responseMessage);
    }
}
