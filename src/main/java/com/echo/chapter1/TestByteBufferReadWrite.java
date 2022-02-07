package com.echo.chapter1;

import java.nio.ByteBuffer;

import static com.echo.chapter1.ByteBufferUtil.debugAll;

public class TestByteBufferReadWrite {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put((byte) 0x61);    //'a'
        debugAll(buffer);
        buffer.put(new byte[]{0x62,0x63,0x64});
        debugAll(buffer);
        //不切换读模式的话，读的是当前位置
        //System.out.println(buffer.get());

        //切换读模式
        buffer.flip();
        System.out.println(buffer.get());
        debugAll(buffer);
        //压缩前移
        buffer.compact();
        debugAll(buffer);
        //写入数据
        buffer.put(new byte[]{0x65,0x6f});
        debugAll(buffer);
    }
}
