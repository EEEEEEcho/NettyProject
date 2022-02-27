package com.echo.chat.server.handler;

import com.echo.chat.message.RpcRequestMessage;
import com.echo.chat.message.RpcResponseMessage;
import com.echo.chat.server.service.HelloService;
import com.echo.chat.server.service.ServicesFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RpcRequestMessageHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage message){
        System.out.println("Hello");
        RpcResponseMessage response = new RpcResponseMessage();
        try {
            HelloService service =(HelloService) ServicesFactory.getService(Class.forName(message.getInterfaceName()));
            Method method = service.getClass().getMethod(message.getMethodName(), message.getParameterTypes());
            Object invoke = method.invoke(service, message.getParameterValue());
            System.out.println(invoke);
            response.setReturnValue(invoke);
        }
        catch (Exception e){
            e.printStackTrace();
            response.setExceptionValue(e);
        }
        ctx.writeAndFlush(response);
    }

//    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//        RpcRequestMessage message = new RpcRequestMessage(1,
//                "com.echo.chat.server.service.HelloService",
//                "sayHello",
//                String.class,
//                new Class[]{String.class},
//                new Object[]{"张三"}
//        );
//        Object service = ServicesFactory.getService(Class.forName(message.getInterfaceName()));
//        Method method = service.getClass().getMethod(message.getMethodName(), message.getParameterTypes());
//        Object invoke = method.invoke(service, message.getParameterValue());
//        System.out.println(invoke);
//    }
}
