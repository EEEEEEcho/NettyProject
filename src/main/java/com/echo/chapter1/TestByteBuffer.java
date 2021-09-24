package com.echo.chapter1;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
@Slf4j
public class TestByteBuffer {
    public static void main(String[] args) {
        //FileChannel 文件读写通道,通道是双向的
        //获取通道的方法：1.输入输出流 2.RandomAccessFile
        try(FileChannel channel = new FileInputStream("data.txt").getChannel()){
            //准备缓冲区
            ByteBuffer byteBuffer = ByteBuffer.allocate(10);
            while (true){

                //从channel读取，读取的内容写入到缓冲区，读到流的结束会返回-1
                int len = channel.read(byteBuffer);
                log.debug("读取到的字节数:{}",len);
                if (len == -1){
                    break;
                }
                //打印Buffer的内容
                byteBuffer.flip();  //切换成读模式,为了从byteBuffer中读
                while (byteBuffer.hasRemaining()){//检查是否还有剩余的数据
                    byte b = byteBuffer.get();
                    log.debug("读取到的字节:{}",(char) b);//字节强转成字符打印
                }
                byteBuffer.clear(); //切换为写模式，为了让从channel中读取到的数据，写入到buffer
            }
            channel.read(byteBuffer);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
