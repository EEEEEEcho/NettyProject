package com.echo.chapter1;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.echo.chapter1.ByteBufferUtil.debugAll;

public class TestByteBufferRead {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put(new byte[]{'a','b','c','d'});
        //切换读模式
        buffer.flip();

        //rewind 从头开始读
        byte[] bytes = new byte[4];
        buffer.get(bytes);    //将读到的内容放到bytes中
        System.out.println(Arrays.toString(bytes));
        debugAll(buffer);

        buffer.rewind();    //其实就是将position设置为0，从头开始读
        System.out.println((char)buffer.get());

        //mark & reset
        //mark 做一个标记，记录打算把position设置到什么位置，
        //reset 是将position重置到mark的位置
        System.out.println((char) buffer.get());    //这时position应该在2号位置（下标从0）
        buffer.mark();  //加标记，索引2的位置
        System.out.println((char) buffer.get());
        System.out.println((char) buffer.get());
        buffer.reset(); //将position重置到mark的位置，即索引2的位置
        System.out.println((char) buffer.get());
        System.out.println((char) buffer.get());

        //get(i),获取指定索引的元素，并且不会改变position的位置
        buffer.rewind();//重置position
        System.out.println((char) buffer.get(3));   //不会改变读索引的位置
        debugAll(buffer);
    }
}
