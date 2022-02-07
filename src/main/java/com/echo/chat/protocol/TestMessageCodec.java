package com.echo.chat.protocol;

import com.echo.chat.config.Config;
import com.echo.chat.message.LoginRequestMessage;
import com.echo.chat.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;
import org.omg.CORBA.PUBLIC_MEMBER;

public class TestMessageCodec {
    public static void main(String[] args) throws Exception {
//        EmbeddedChannel channel = new EmbeddedChannel(
//                new LengthFieldBasedFrameDecoder(1024,12,4,0,0), //解决黏包、半包问题
//                new LoggingHandler(),
//                new MessageCodec());
//        //注意，这里面的FrameDecoder与LoggingHandler虽然功能是相同的，但是不能单独抽取出来。
//        //因为：因为一个Handler是一道工序，会有不同的工人（线程）来访问这道工序，这个FrameDecoder在遇到半包的情况下
//        //会将半包中的数据先进行个保留，等到整个包发过来之后，才进行解析。但是如果又多个线程访问同一个FrameDecoder并且
//        //发生了半包现象之后，那么就会导致FrameDecoder之中的数据错乱
//
//        //所以，含有状态的handler是不能多个线程公用的，无状态的handler是可以多线程公用的，Handler上添加了@Sharable注解的
//        //handler是可以多线程使用的，只需创建一个实例即可。没有添加@Sharable注解的handler是无法多线程共享的
//        LoginRequestMessage message = new LoginRequestMessage("Echo","123456");
//
//        channel.writeOutbound(message); //让message出站
//
//
//        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
//        //将这个message编码为ByteBuf
//        new MessageCodec().encode(null,message,buffer);
//        //测试入站
//        channel.writeInbound(buffer);


        MessageCodecSharable CODEC = new MessageCodecSharable();
        LoggingHandler LOGGING = new LoggingHandler();
        EmbeddedChannel channel = new EmbeddedChannel(LOGGING, CODEC, LOGGING);

        LoginRequestMessage message = new LoginRequestMessage("zhangsan", "123");
        channel.writeOutbound(message);

        ByteBuf byteBuf = messageToBytes(message);
        channel.writeInbound(byteBuf);
    }

    public static ByteBuf messageToBytes(Message msg){
        int algorithm = Config.getSerializerAlgorithm().ordinal();
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        buffer.writeBytes(new byte[]{1,2,3,4});
        buffer.writeByte(1);
        buffer.writeByte(algorithm);
        buffer.writeByte(msg.getMessageType());
        buffer.writeInt(msg.getSequenceId());
        buffer.writeByte(0xff);
        byte[] bytes = Serializer.Algorithm.values()[algorithm].serialize(msg);
        buffer.writeInt(bytes.length);
        buffer.writeBytes(bytes);
        return buffer;
    }
}
