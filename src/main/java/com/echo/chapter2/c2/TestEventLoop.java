package com.echo.chapter2.c2;

import io.netty.channel.DefaultEventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class TestEventLoop {
    public static void main(String[] args) {
        //1.创建事件循环组
        //构造方法的源码：默认线程数的获取
        //private static final int DEFAULT_EVENT_LOOP_THREADS
        // = Math.max(1, SystemPropertyUtil.getInt("io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2));
        EventLoopGroup group = new NioEventLoopGroup(2); //能够处理I/O，普通任务和定时任务
//        EventLoopGroup defaultGroup = new DefaultEventLoop();   //只能处理普通任务和定时任务
        //2.获取下一个事件循环对象
        System.out.println(group.next());
        System.out.println(group.next());
        System.out.println(group.next());
        //3.执行普通任务
        group.next().submit(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //提交给事件循环组中某一个事件循环对象来执行
            log.debug("Ok");
        });

        log.debug("In Main Thread");

        //4.执行定时任务
        //四个参数：第一个是一个任务对象，指定要执行的任务，第二个是延迟执行的时间，这里是0就是不延迟
        //第三个是执行任务的时间间隔，这里是1，每隔一秒执行一次，第四个是时间的单位，这里是秒。如果
        //设置成小时，那么就会每隔一小时执行一次
        group.next().scheduleAtFixedRate(()->{
            log.debug("啊呀呀呀");
        },0,1, TimeUnit.SECONDS);
    }
}
