package com.echo.chapter1;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import static com.echo.chapter1.ByteBufferUtil.debugRead;
@Slf4j
public class Server {
    public static void main(String[] args) throws IOException {
        //用来存储的ByteBuffer
        ByteBuffer buffer = ByteBuffer.allocate(16);

        //使用NIO来理解阻塞模式，单线程
        //1.创建服务器
        ServerSocketChannel ssc = ServerSocketChannel.open();
        //2.绑定监听端口
        ssc.bind(new InetSocketAddress(8080));

        //3.连接集合
        List<SocketChannel> channels = new ArrayList<>();
        while (true){
            //4.accept tcp三次握手后建立与客户端的连接 SocketChannel 与客户端之间通信
            log.debug("Connecting ....");
            SocketChannel sc = ssc.accept();   //阻塞方法，线程停止运行，等待客户端
            log.debug("Connected ....{}",sc);
            channels.add(sc);
            //5.接收客户端发送的数据
            for(SocketChannel channel :channels){
                //6.获取客户端发送的数据,存储到buffer中
                log.debug("Before Read .... {}",channel);
                channel.read(buffer); //read方法也是一个阻塞方法，等待数据的写入
                //7.查看buffer中的数据
                buffer.flip();
                debugRead(buffer);
                //8.清空buffer,切换回写模式接收新的数据
                buffer.clear();
                log.debug("After Read .... {}",channel);
            }
        }
//        ByteBuffer buffer = ByteBuffer.allocate(16);
//        ServerSocketChannel ssc = ServerSocketChannel.open();
//        ssc.bind(new InetSocketAddress(8080));
//        List<SocketChannel> channels = new ArrayList<>();
//        while (true){
//            //建立连接
//            SocketChannel sc = ssc.accept();
//            channels.add(sc);
//            for (SocketChannel channel : channels){
//                channel.read(buffer);
//                buffer.flip();
//                debugRead(buffer);
//                buffer.clear();
//            }
//        }
    }
}
