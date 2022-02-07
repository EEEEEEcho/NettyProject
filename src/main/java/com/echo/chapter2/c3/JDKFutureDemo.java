package com.echo.chapter2.c3;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
@Slf4j
public class JDKFutureDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //1.线程池
        ExecutorService pool = Executors.newFixedThreadPool(2);
        //2.线程池提交任务，返回Future对象
        Future<Integer> future = pool.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                log.debug("执行计算");
                TimeUnit.SECONDS.sleep(1);
                return 50;
            }
        });
        //3.主线程通过future来获取结果,主线程开启任务，主线程等待结果，所以是同步的
        log.debug("等待结果");
        log.debug("结果是:{}",future.get());
    }
}
