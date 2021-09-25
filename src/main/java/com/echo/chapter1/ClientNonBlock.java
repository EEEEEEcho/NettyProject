package com.echo.chapter1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class ClientNonBlock {
    public static void main(String[] args) throws IOException {
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress("localhost",8080));
        //sc.write(Charset.defaultCharset().encode("hello\nworld\n"));
        //如果发送的消息比Bytebuffer分配的长度要大(Bytebuffer在这里分了16字节)
        sc.write(Charset.defaultCharset().encode("0123456789874563210\n1236547896547893310\n"));
        System.out.println("waiting....");
    }
}
