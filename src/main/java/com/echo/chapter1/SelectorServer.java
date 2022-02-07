package com.echo.chapter1;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;

import static com.echo.chapter1.ByteBufferUtil.debugAll;
import static com.echo.chapter1.ByteBufferUtil.debugRead;

@Slf4j
public class SelectorServer {
    public static void main(String[] args) throws IOException {
        //1.创建selector,管理多个channel
        Selector selector = Selector.open();
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
                    System.out.println("触发了accept");
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    //建立连接
                    SocketChannel sc = channel.accept();
                    log.debug("Server Socket:{}",sc);
                    //设置SocketChannel为非阻塞
                    sc.configureBlocking(false);


                    //将SocketChannel与Selector注册到一起
                    //创建一个ByteBuffer作为附件注册到SocketChannel的SelectionKey上,用来读取
                    ByteBuffer byteBuffer = ByteBuffer.allocate(16);
                    //scKey只关注sc上发生的事件
                    SelectionKey scKey = sc.register(selector, 0, byteBuffer);
                    scKey.interestOps(SelectionKey.OP_READ);    //只关注读事件
                }
                else if(key.isReadable()){  //如果是read事件
                    //拿到触发事件的Channel
                    try {
                        System.out.println("触发了read");
                        SocketChannel channel = (SocketChannel) key.channel();
                        //拿到SocketChannel的SelectionKey上的附件
                        ByteBuffer byteBuffer =(ByteBuffer) key.attachment();

                        int read = channel.read(byteBuffer);
                        if (read == -1){
                            //如果客户端读到的字节数为空，就会返回-1，表示正常断开
                            key.cancel();   //将key从selector反注册
                            channel.close();    //关闭socket通道
                        }
                        else{
//                            byteBuffer.flip();
//                            //debugRead(byteBuffer);
//                            System.out.println(Charset.defaultCharset().decode(byteBuffer));
                            //以固定分隔符'\n'的方式,拆分byteBuffer(解决黏包)
                            split(byteBuffer);
                            if (byteBuffer.position() == byteBuffer.limit()){
                                //如果没有换行符(解释在split方法的注释中),扩容
                                ByteBuffer newBuffer = ByteBuffer.allocate(2 * byteBuffer.capacity());
                                byteBuffer.flip();  //旧buffer切换为读模式
                                newBuffer.put(byteBuffer);  //扩容后的buffer将旧buffer写入
                                key.attach(newBuffer);  //替换掉key上原有的附件(旧的未扩容buffer)
                            }
                        }
                    }
                    catch (Exception e){
                        key.cancel();   //如果客户端断开了，为了防止异常，需要将Key反注册掉。从Selector的key集合中，真正删除
                        e.printStackTrace();
                    }

                }
                iterator.remove();  //一定要从selectedKeys()中移除，将处理过的key移除！！！！
            }
        }
    }

    private static void split(ByteBuffer source){
        //切换到读模式
        source.flip();
        for (int i = 0; i < source.limit(); i++) {
            //找到一条完整消息
            if (source.get(i) == '\n') {

                // 计算要分配的ByteBuffer的长度
                int length = i + 1 - source.position(); //这里的position是起始位置，因为get方法不会移动position
                // 把这条完整消息存入新的ByteBuffer
                ByteBuffer target = ByteBuffer.allocate(length);
                // 从source读，写入target
                for (int j = 0; j < length; j++) {
                    target.put(source.get());   //这次的get会移动position指针
                }
                debugAll(target);
            }
        }
        //把完整的消息找完了，然后切换到写模式，继续接收消息
        //写模式要用compact，因为不能丢弃原来不完整的消息
        //compact 方法，是把未读完的部分向前压缩，然后切换至写模式,
        //如果一条消息没有结束符\n,在进行完上述操作后,position其实一直都没有动
        //然后调用compact方法时,将未读完的部分向前压缩,然后切换至写模式,就相当于postion指针和limit重合
        //如:a    b     c    d    e    f
        //position                    limit
        //调用compact
        //   a    b     c    d    e    f
        //                           limit
        //                          position
        source.compact();
    }
}
