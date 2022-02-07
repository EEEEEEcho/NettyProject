package com.echo.chapter1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * 处理写事件的服务器,客户端一连接上之后,就给客户端写数据
 */
public class WriteServer {
    public static void main(String[] args) throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);   //非阻塞

        //注册selector
        Selector selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);

        //监听端口
        ssc.bind(new InetSocketAddress(8080));

        while (true){
            selector.select();
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()){
                SelectionKey key = iterator.next();
                if (key.isAcceptable()){
                    //因为只有ServerSocketChannel会被触发accept事件,所以,这个事件被触发后,肯定是
                    //ServerSocketChannel的accept触发的,直接简化调用ssc.accept()即可.
                    SocketChannel socketChannel = ssc.accept();
                    socketChannel.configureBlocking(false);
                    //将socketChannel注册到selector,并关注读事件
                    SelectionKey selectionKey = socketChannel.register(selector,SelectionKey.OP_READ,null);


                    //向客户端发送大量数据
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < 30000000; i++) {
                        sb.append("a");
                    }
                    ByteBuffer buffer = Charset.defaultCharset().encode(sb.toString());
                    //写入到channel
//                    while (buffer.hasRemaining()){
//                        //write并不能保证全部写入到客户端,有一个返回值,返回值代表实际写入的值
//                        int write = socketChannel.write(buffer);
//                        System.out.println(write);
//                    }
                    //如果缓冲区没有开辟一个完整的,那么这个write方法,就会循环去写,也同样造成了阻塞,
                    //所以采用新的写方式
                    if (buffer.hasRemaining()){
                        //关注可写事件,当发送缓冲区空了,进行触发,但是不能将原来的读事件给覆盖掉.
                        //所以,拿到原来已经关注的读事件,加上关注的写事件
                        selectionKey.interestOps(selectionKey.interestOps() + SelectionKey.OP_WRITE);
                        //将未写完的数据,挂到selectionKey上,
                        selectionKey.attach(buffer);
                    }
                }
                else if(key.isWritable()){
                    //缓冲区空了,可写了,触发了写事件,将未写完的,挂载到附件上的byteBuffer拿到.
                    ByteBuffer byteBuffer = (ByteBuffer) key.attachment();
                    //拿到通道
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    //写
                    int write = socketChannel.write(byteBuffer);
                    System.out.println(write);
                    //清理,如果写完了,将没有东西的buffer从附件拿走,防止内存占用
                    if(!byteBuffer.hasRemaining()){
                        key.attach(null);
                        //写完了,取消可写事件
                        key.interestOps(key.interestOps() - SelectionKey.OP_WRITE);
                    }
                }
                iterator.remove();
            }
        }
    }
}
