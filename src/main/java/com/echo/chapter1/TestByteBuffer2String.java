package com.echo.chapter1;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static com.echo.chapter1.ByteBufferUtil.debugAll;

/**
 * Bytebuffer和String的相互转换
 */
public class TestByteBuffer2String {
    public static void main(String[] args) {
        //字符串转bytebuffer
        //1.字符串.getBytes()
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.put("Hello".getBytes());
        debugAll(buffer);   //这个buffer仍是写模式

        //2.Charset
        ByteBuffer buffer1 = StandardCharsets.UTF_8.encode("Hello");
        debugAll(buffer1);  //这个buffer是读模式

        //3.wrap
        ByteBuffer buffer2 = ByteBuffer.wrap("Hello".getBytes());
        debugAll(buffer2);  //这个buffer是读模式

        //bytebuffer转字符串
        //1.Charset
        //这个转换，转换的buffer必须是读模式的。否则可能会什么也没有
        String buffer1Str = StandardCharsets.UTF_8.decode(buffer1).toString();
        System.out.println(buffer1Str);

        //buffer.flip();
        String bufferStr = StandardCharsets.UTF_8.decode(buffer).toString();
        System.out.println(bufferStr);  //什么也不打印，或者乱码，因为该buffer仍是写模式
    }
}
