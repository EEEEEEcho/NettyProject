package com.echo.chapter1;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static com.echo.chapter1.ByteBufferUtil.debugAll;

public class TestScatteringReads {
    public static void main(String[] args) {
        try (FileChannel channel = new RandomAccessFile("words.txt", "r").getChannel()) {
            ByteBuffer buffer1 = ByteBuffer.allocate(3);
            ByteBuffer buffer2 = ByteBuffer.allocate(3);
            ByteBuffer buffer3 = ByteBuffer.allocate(5);
            //将数据分散读取到3个ByteBuffer中
            channel.read(new ByteBuffer[]{buffer1,buffer2,buffer3});
            buffer1.flip();
            buffer2.flip();
            buffer3.flip();
            debugAll(buffer1);
            debugAll(buffer2);
            debugAll(buffer3);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
