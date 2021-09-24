package com.echo.chapter1;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

import static com.echo.chapter1.ByteBufferUtil.debugRead;

@Slf4j
public class SelectorServer {
    public static void main(String[] args) throws IOException {
        //1.创建selector,管理多个channel
        Selector selector = Selector.open();

        ByteBuffer buffer = ByteBuffer.allocate(16);
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);   //非阻塞

        //2.建立select和channel的联系（注册）
        // selectionKey就是将来事件发生后，通过它可以知道事件类型和哪个channel发生的事件,一个管理ssc的管理员
        /**
         * 事件类型：
         * accept: ServerSocket独有的事件，会在有连接请求时触发
         * connect:连接建立后，客户端触发的事件
         * read:数据可读事件
         * write:数据可写事件
         */
        SelectionKey sscKey = ssc.register(selector, 0, null);
        sscKey.interestOps(SelectionKey.OP_ACCEPT); //指明了sscKey只关注accept事件，上面的ops：0表示不关注任何事件
        log.debug("Register Key:{}",sscKey);

        ssc.bind(new InetSocketAddress(8080));

        while (true){
            //3.select方法:没有事件发生，线程阻塞，有事件发生了，让线程恢复运行，处理事件
            //有事件就工作，没事件歇着
            selector.select();

            //4.处理事件
            //拿到事件集合：selectedKeys事件集合，内部包含了所有可用的事件
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()){
                SelectionKey key = iterator.next();
                log.debug("Trigger Key:{}",key);
                //区分事件类型,因为这一个selector上注册了ServerSocketChannel的accept事件和
                //SocketChannel的read事件，两个不同的事件。要根据事件类型的不同去做相应的操作
                if (key.isAcceptable()) {   //如果是accept事件
                    //拿到触发该事件的channel
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    //建立连接
                    SocketChannel sc = channel.accept();
                    log.debug("Server Socket:{}",sc);
                    //设置SocketChannel为非阻塞
                    sc.configureBlocking(false);
                    //将SocketChannel与Selector注册到一起
                    //scKey只关注sc上发生的事件
                    SelectionKey scKey = sc.register(selector, 0, null);
                    scKey.interestOps(SelectionKey.OP_READ);    //只关注读事件
                }
                else if(key.isReadable()){  //如果是read事件
                    //拿到触发事件的Channel
                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(16);
                    channel.read(byteBuffer);
                    buffer.flip();
                    debugRead(buffer);
                }

            }
        }
    }
}
