package com.echo.chat.protocol;

import com.echo.chat.message.Message;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

@Slf4j
public class MessageCodec extends ByteToMessageCodec<Message> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Message message, ByteBuf byteBuf) throws Exception {
        //出站前，将message编码为ByteBuf
        byteBuf.writeBytes(new byte[]{1,2,3,4});    //四个字节的魔数
        byteBuf.writeByte(1);   //一个字节的版本
        byteBuf.writeByte(0);   //一个字节的序列化方式:0 JDK的序列化方式，1 json的序列化方式
        byteBuf.writeByte(message.getMessageType());    //一个字节的指令类型：登录、响应、聊天、创建聊天室....
        byteBuf.writeInt(message.getSequenceId());  //四个字节的请求序号
        byteBuf.writeByte(0xff);    //一个字节的对齐填充，因为上面一共15个字节


        ByteOutputStream bos = new ByteOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        //由于message实现了Serializable,所以可以转换为字节数组,然后通过oos间接的将其转换为一个bos，从而可以获取其字节数组
        oos.writeObject(message);
        byte[] bytes = bos.toByteArray();

        byteBuf.writeInt(bytes.length); //消息的长度
        byteBuf.writeBytes(bytes);      //写入内容
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        //入站时，将ByteBuf编码为message
        int magicNum = byteBuf.readInt();   //前4个字节的魔数
        byte version = byteBuf.readByte();  //1个字节的版本
        byte serializeType = byteBuf.readByte();    //1字节的序列化方式
        byte messageType = byteBuf.readByte();  //1字节的指令类型
        int sequenceId = byteBuf.readInt(); //4字节的序列号
        byteBuf.readByte(); //跳过填充字节
        int length = byteBuf.readInt(); //消息的长度

        //分配消息的缓冲区
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes,0,length);  //将消息读到bytes
        if (serializeType == 0){
            //jdk的反序列化方式
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
            Message message = (Message) ois.readObject();

            //打印日志
            log.debug("{},{},{},{},{},{}",magicNum,version,serializeType,messageType,sequenceId,length);
            log.debug("{}",message);

            //为了给下一个handler使用，需要将消息放到list中
            list.add(message);

        }
    }
}
