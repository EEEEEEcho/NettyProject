package com.echo.chapter1;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

import static com.echo.chapter1.ByteBufferUtil.debugAll;

/**
 * 多线程处理连接
 * 其中Boss线程，专门用来处理连接，
 * worker线程专门进行读写操作
 */@Slf4j
public class MultiThreadServer {
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
        Worker worker = new Worker("worker-0");

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
                    worker.register(sc);    //boss
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
        //用来进行线程间的通信，将主线程中的SocketChannel传递给work线程的run方法，实现线程间通信
        private ConcurrentLinkedDeque<Runnable> queue = new ConcurrentLinkedDeque<>();

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
            //向队列中，添加了任务，但是任务并没有执行。
            queue.add(() -> {
                try {
                    sc.register(selector,SelectionKey.OP_READ,null);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            });
            selector.wakeup();  //唤醒select，让select从阻塞状态激活，
        }

        @Override
        public void run() {
            while (true){
                try {
                    selector.select();  //worker-0
                    //将队列中添加的任务拿出来，执行，因为每次在队列中添加任务之后，会调用selector.wakeup()激活selector,然后就能将事件注册
                    Runnable task = queue.poll();   //这个任务就是SocketChannel注册到selector的代码
                    if (task != null){
                        task.run();
                    }

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
