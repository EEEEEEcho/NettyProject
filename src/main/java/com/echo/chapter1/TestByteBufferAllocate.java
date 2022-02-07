package com.echo.chapter1;

import java.nio.ByteBuffer;

public class TestByteBufferAllocate {
    public static void main(String[] args) {
        //ByteBuffer内存分配是固定的
        ByteBuffer buffer = ByteBuffer.allocate(16);
        /*使用的是java的堆内存*/
        System.out.println(ByteBuffer.allocate(16).getClass());
        /*使用的是直接内存*/
        System.out.println(ByteBuffer.allocateDirect(16).getClass());
    }
}
