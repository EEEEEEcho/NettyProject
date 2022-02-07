package com.echo.chapter3.c1;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.nio.charset.StandardCharsets;

public class TestLengthFieldDecoder {
    public static void main(String[] args) {
        EmbeddedChannel channel = new EmbeddedChannel(
                /**
                 * 第一个参数:数据的最大长度
                 * 第二个参数:长度字段的偏移量（从哪里开始是数据的长度字段）
                 * 第三个参数:长度字段的长度。一个int占用四个字节
                 * 第四个参数:长度之后还需不需要调整，这里的1代表：需要调整一个字节，因为消息是按照“四个字节描述字节内容的长度，
                 * 后面接着是一个字节协议版本号，最后面是消息内容”设置的，所以要将一个字节的协议版本号进行调整
                 * 第五个参数:解析的结果是否需要剥离，这里的5代表：因为数据长度是int类型，占了四个字节，协议版本号占用一个字节
                 * 将长度占用的这四个字节和协议版本号占用的一个字节从消息中剥离出去
                 *
                 */
                new LengthFieldBasedFrameDecoder(1024,0,4,1,5),
                new LoggingHandler(LogLevel.DEBUG));

        //传输消息的格式：用四个字节描述字节内容的长度，后面接着是一个字节协议版本号，最后面是消息内容
        //0x000C0x14140x8657
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        send(buffer,"Hello, world");
        send(buffer,"Hi!");

        channel.writeInbound(buffer);

    }

    private static void send(ByteBuf buffer,String s) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);  //实际内容
        int length = bytes.length;  //实际内容长度
        buffer.writeInt(length);    //写入长度
        buffer.writeByte(1);   //写入版本号1
        buffer.writeBytes(bytes);   //写入内容
    }
}
