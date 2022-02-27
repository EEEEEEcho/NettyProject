package com.echo.review;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class NettyPromiseTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //创建一个eventLoopGroup，相当于线程池
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        EventLoop eventLoop = eventLoopGroup.next();
        //可以主动创建一个promise
        DefaultPromise<Integer> promise = new DefaultPromise<>(eventLoop);

        new Thread(() -> {
            //任意一个线程执行计算，计算完毕之后向promise中填充结果
            System.out.println("开始计算");
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int i = 4 * 20;
            //向promise中填充结果
            promise.setSuccess(i);
        }).start();

        //在主线程中接受结果
        System.out.println("等待结果...");
        //调用get时，主线程会阻塞
        System.out.println("结果是:" + promise.get());
    }
}
