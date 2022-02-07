package com.echo.chapter1;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class TestFileChannelTransferTo {
    public static void main(String[] args) {
        try (FileChannel from = new FileInputStream("data.txt").getChannel();
             FileChannel to = new FileOutputStream("to.txt").getChannel();
        ) {
            //三个参数：要传输的起始位置，传输多少数据，传输的目标
            from.transferTo(0,from.size(),to);
            /**
             * 代码量低，效率高，底层会利用操作系统的零拷贝进行优化
             */

            //transferTo最大能传输的文件大小只能是2G，所以可以采用多次传输的方式
            long size = from.size();
            //left标识还剩余多少数据需要传输
            for(long left = size;left >0; ){
                final long transfered = from.transferTo(size - left, size, to);
                left -= transfered;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
