package com.echo.chapter1;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

import static com.echo.chapter1.ByteBufferUtil.debugAll;
@Slf4j
public class MultiThreadServer2 {
    public static void main(String[] args) throws IOException {
        Thread.currentThread().setName("Boss");
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        Selector boss = Selector.open();
        SelectionKey bossKey = ssc.register(boss, 0, null);
        bossKey.interestOps(SelectionKey.OP_ACCEPT);
        ssc.bind(new InetSocketAddress(8080));

        //并不是创建一个连接就创建一个worker，而是一个或几个（cpu核数）个worker去处理多个channel
        //因为不能无限制的去创建Thread
        //所以要创建固定数量的worker
        //Runtime.getRuntime().availableProcessors() 获取本机上有几个核心可用
        Worker[] workers = new Worker[Runtime.getRuntime().availableProcessors()];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Worker("worker-" + i);
        }
        AtomicInteger counter = new AtomicInteger(0);


        while (true){
            boss.select();
            Iterator<SelectionKey> iter = boss.selectedKeys().iterator();
            while (iter.hasNext()){
                SelectionKey key = iter.next();
                iter.remove();
                if (key.isAcceptable()){
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    log.debug("connected ... {}",sc.getRemoteAddress());
                    //2.关联到worker的selector上，而不是关联到Boss的selector上
                    log.debug("before register ... {}",sc.getRemoteAddress());
                    //round robin轮询处理不同客户端的连接
                    workers[counter.getAndIncrement() % workers.length].register(sc);    //boss
                    log.debug("after register ... {}",sc.getRemoteAddress());
                }
            }
        }
    }

    static class Worker implements Runnable{
        private Thread thread;  //处理selector的线程
        private Selector selector;
        private String name;
        private volatile boolean start = false;  //用来标记线程和selector是否初始化
        //无线程通信的案例

        public Worker(String name){
            this.name = name;
        }
        //初始化线程和Selector
        public void register(SocketChannel sc) throws IOException {
            if(!start){
                selector = Selector.open();
                thread = new Thread(this,name);
                thread.start();
                start = true;
            }
            //每次注册，直接调用wakeup()方法，类似于Park,UnPark的机制。只要调用了wakeup().就算后面调用了select()
            //也不会进行阻塞
            selector.wakeup();
            sc.register(selector,SelectionKey.OP_READ,null);
        }

        @Override
        public void run() {
            while (true){
                try {
                    selector.select();  //worker-0
                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                    while (iter.hasNext()){
                        SelectionKey key = iter.next();
                        iter.remove();
                        if(key.isReadable()){
                            ByteBuffer byteBuffer = ByteBuffer.allocate(16);
                            SocketChannel channel = (SocketChannel) key.channel();
                            log.debug("read ... {}",channel.getRemoteAddress());
                            channel.read(byteBuffer);
                            byteBuffer.flip();
                            debugAll(byteBuffer);
                        }
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}
