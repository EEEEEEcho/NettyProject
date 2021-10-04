package com.echo.chapter2.c3;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

@Slf4j
public class TestNettyPromise {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //1.准备EventLoop对象
        EventLoop eventLoop = new NioEventLoopGroup().next();

        //2.主动创建promise，而不是在提交结果之后被动的获取promise
        //promise实际上就是一个结果的容器
        DefaultPromise<Integer> promise = new DefaultPromise<>(eventLoop);

        new Thread(() -> {
            //3.任意一个线程执行计算，计算完毕之后向promise填充结果
            log.debug("开始计算....");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int result = 20 + 30;
            promise.setSuccess(result);
        }).start();

        //4.接受结果的线程(同步)
//        log.debug("等待结果");
//        log.debug("从promise中拿到结果:{}",promise.get());

        //5.接收结果（异步）
        promise.addListener(new GenericFutureListener<Future<? super Integer>>() {
            @Override
            public void operationComplete(Future<? super Integer> future) throws Exception {
                log.debug("异步获取结果:{}",future.getNow());
            }
        });

    }
}
