package com.echo.chapter2.c5;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;

import static com.echo.chapter2.c5.TestByteBuf.log;

public class TestCompositeByteBuf {
    public static void main(String[] args) {
        /*将小的ByteBuf组合成大的ByteBuf*/
        ByteBuf buf1 = ByteBufAllocator.DEFAULT.buffer();
        buf1.writeBytes(new byte[]{1,2,3,4,5});

        ByteBuf buf2 = ByteBufAllocator.DEFAULT.buffer();
        buf2.writeBytes(new byte[]{6,7,8,9,10});

        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        //两次写入会造成两次数据复制，数据量大的时候，会对性能产生影响
//        buffer.writeBytes(buf1).writeBytes(buf2);
//        log(buffer);

        //解决办法
        CompositeByteBuf compositeByteBuf = ByteBufAllocator.DEFAULT.compositeBuffer();
        //true参数表示是否移动读写指针
        compositeByteBuf.addComponents(true,buf1,buf2);
        log(compositeByteBuf);

    }
}
