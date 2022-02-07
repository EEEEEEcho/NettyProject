package com.echo.chapter2.c5;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import static com.echo.chapter2.c5.TestByteBuf.log;

public class TestSlice {
    public static void main(String[] args) {
        /*slice将一个大的ByteBuf分割为多个小的ByteBuf*/

        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(10);

        buffer.writeBytes(new byte[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j'});
        log(buffer);

        //0-5作为一个切片
        ByteBuf slice1 = buffer.slice(0, 5);
        //5-10作为另一个切片
        ByteBuf slice2 = buffer.slice(5, 5);
        //在切片过程中并没有发生数据的复制
        //切片后切片的容量会受到限制，不能超出范围去写
        log(slice1);
        log(slice2);
        slice1.setByte(0,'z');
        //buffer.release();   //如果release之后，会影响两个切片,如果要保证原有切片的正常，要保留原有切片的一个引用
        slice1.retain();//保留原有切片的一个引用

        log(slice1);
        log(buffer);
    }
}
