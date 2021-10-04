package com.echo.chapter2.c3;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

@Slf4j
public class TestNettyFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //1.创建一个NioEventLoopGroup
        NioEventLoopGroup group = new NioEventLoopGroup();
        //2.从group中取一个eventLoop
        EventLoop eventLoop = group.next();
        //3.提交一个任务给eventLoop
        Future<Integer> future = eventLoop.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                log.debug("执行计算");
                Thread.sleep(1000);
                return 70;
            }
        });

        //4.同步获取结果
//        log.debug("等待结果");
//        log.debug("结果是：{}",future.get());
        //5.异步获取结果
        future.addListener(new GenericFutureListener<Future<? super Integer>>() {
            @Override
            public void operationComplete(Future<? super Integer> future) throws Exception {
                log.debug("接受结果:{}",future.getNow());
            }
        });
    }
}
