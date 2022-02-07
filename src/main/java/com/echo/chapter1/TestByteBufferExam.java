package com.echo.chapter1;

import java.nio.ByteBuffer;

import static com.echo.chapter1.ByteBufferUtil.debugAll;

public class TestByteBufferExam {
    public static void main(String[] args) {
        /*
        * 网络上有多条数据发送给服务端，数据之间使用 \n 进行分隔
       但由于某种原因这些数据在接收时，被进行了重新组合，例如原始数据有3条为
        Hello,world\n
        I'm zhangsan\n
        How are you?\n
       变成了下面的两个 byteBuffer (黏包，半包)
        Hello,world\nI'm zhangsan\nHo
        w are you?\n
        * */
        ByteBuffer byteBuffer = ByteBuffer.allocate(32);
        byteBuffer.put("Hello,world\nI'm zhangsan\nHo".getBytes());
        split(byteBuffer);
        byteBuffer.put("w are you?\n".getBytes());
        split(byteBuffer);
    }

    private static void split(ByteBuffer source){
        //切换到读模式
        source.flip();
        for (int i = 0; i < source.limit(); i++) {
            //找到一条完整消息
            if (source.get(i) == '\n') {

                // 计算要分配的ByteBuffer的长度
                int length = i + 1 - source.position(); //这里的position是起始位置，因为get方法不会移动position
                // 把这条完整消息存入新的ByteBuffer
                ByteBuffer target = ByteBuffer.allocate(length);
                // 从source读，写入target
                for (int j = 0; j < length; j++) {
                    target.put(source.get());   //这次的get会移动position指针
                }
                debugAll(target);
            }
        }
        //把完整的消息找完了，然后切换到写模式，继续接收消息
        //写模式要用compact，因为不能丢弃原来不完整的消息
        source.compact();
    }
}
