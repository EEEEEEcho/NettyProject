package com.echo.chat.protocol;

import com.echo.chat.config.Config;
import com.echo.chat.message.Message;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * 继承自MessageToMessageCodec，可以使用Sharable注解，
 * 因为MessageToMessageCodec默认每次收到的消息都是完整的，不会存在半包现象
 */
@ChannelHandler.Sharable
@Slf4j
public class MessageCodecSharable extends MessageToMessageCodec<ByteBuf,Message> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Message message, List list) throws Exception {
        ByteBuf byteBuf = ctx.alloc().buffer();
        //出站前，将message编码为ByteBuf

        //四个字节的魔数
        byteBuf.writeBytes(new byte[]{1,2,3,4});
        //一个字节的版本
        byteBuf.writeByte(1);
        //一个字节的序列化方式:0 JDK的序列化方式，1 json的序列化方式 从配置中获取序列化器
        byteBuf.writeByte(Config.getSerializerAlgorithm().ordinal());
        //一个字节的指令类型：登录、响应、聊天、创建聊天室....
        byteBuf.writeByte(message.getMessageType());
        //四个字节的请求序号
        byteBuf.writeInt(message.getSequenceId());
        //一个字节的对齐填充，因为上面一共15个字节
        byteBuf.writeByte(0xff);



//        ByteOutputStream bos = new ByteOutputStream();
//        ObjectOutputStream oos = new ObjectOutputStream(bos);
//        //由于message实现了Serializable,所以可以转换为字节数组,然后通过oos间接的将其转换为一个bos，从而可以获取其字节数组
//        oos.writeObject(message);
//        byte[] bytes = bos.toByteArray();
        //通过序列化器完成上述的序列化工作
        byte[] bytes = Config.getSerializerAlgorithm().serialize(message);

        byteBuf.writeInt(bytes.length); //消息的长度
        byteBuf.writeBytes(bytes);      //写入内容

        //传递给下一个出站handler
        list.add(byteBuf);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        //入站时，将ByteBuf编码为message
        int magicNum = byteBuf.readInt();   //前4个字节的魔数
        byte version = byteBuf.readByte();  //1个字节的版本
        byte serializeType = byteBuf.readByte();    //1字节的序列化方式
        byte messageType = byteBuf.readByte();  //1字节的指令类型
        int sequenceId = byteBuf.readInt(); //1字节的序列号
        byteBuf.readByte(); //跳过填充字节
        int length = byteBuf.readInt(); //消息的长度

        //分配消息的缓冲区
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes,0,length);  //将消息读到bytes
        if (serializeType == 0){
            //jdk的反序列化方式



//            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
//            Message message = (Message) ois.readObject();
            //使用序列化器，代替上述反序列化工作
            //Message message1 = Serializer.Algorithm.values()[serializeType].deserialize(Message.class, bytes);

            //找到反序列化算法
            Serializer.Algorithm algorithm = Serializer.Algorithm.values()[serializeType];
            //确定具体消息类型
            Class<? extends Message> messageClass = Message.getMessageClass(messageType);
            //反序列化
            Object deserialize = algorithm.deserialize(messageClass, bytes);
            //打印日志
//            log.debug("{},{},{},{},{},{}",magicNum,version,serializeType,messageType,sequenceId,length);
//            log.debug("{}",message);

            //为了给下一个handler使用，需要将消息放到list中
            list.add(deserialize);
        }
    }
}
