package com.echo.chapter1;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import static com.echo.chapter1.ByteBufferUtil.debugRead;
@Slf4j
public class ServerNonBlock {
    public static void main(String[] args) throws IOException {
        //用来存储的ByteBuffer
        ByteBuffer buffer = ByteBuffer.allocate(16);

        //使用NIO来理解阻塞模式，单线程
        //1.创建服务器
        ServerSocketChannel ssc = ServerSocketChannel.open();

        /*** 切换为非阻塞模式,影响accept()方法为非阻塞 ***/
        ssc.configureBlocking(false);

        //2.绑定监听端口
        ssc.bind(new InetSocketAddress(8080));

        //3.连接集合
        List<SocketChannel> channels = new ArrayList<>();
        while (true){
            //4.accept tcp三次握手后建立与客户端的连接 SocketChannel 与客户端之间通信
            //log.debug("Connecting ....");
            SocketChannel sc = ssc.accept();   //非阻塞，线程还会继续运行，如果没有建立连接，accept()方法返回null
            if(sc != null){
                log.debug("Connected ....");
                //SocketChannel也设置成非阻塞模式，影响read()方法为非阻塞
                sc.configureBlocking(false);
                channels.add(sc);
            }

            //5.接收客户端发送的数据
            for(SocketChannel channel :channels){
                //6.获取客户端发送的数据,存储到buffer中
                //log.debug("Before Read .... {}",channel);
                int readSize = channel.read(buffer); //read方法被设置为非阻塞，线程仍会继续运行，如果没有读到数据，就会返回 0 （读取到的数据的长度）
                if (readSize > 0){
                    //7.查看buffer中的数据
                    buffer.flip();
                    debugRead(buffer);
                    //8.清空buffer,切换回写模式接收新的数据
                    buffer.clear();
                    log.debug("After Read .... {}",channel);
                }
            }
        }
    }
}
