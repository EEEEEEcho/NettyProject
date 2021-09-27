package com.echo.chapter2.c3;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.concurrent.Callable;

public class TestNettyFuture {
    public static void main(String[] args) {
        //1.创建一个NioEventLoopGroup
        NioEventLoopGroup group = new NioEventLoopGroup();
        //2.
        EventLoop eventLoop = group.next();
        eventLoop.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                Thread.sleep(1000);
                return 70;
            }
        });
    }
}
