package com.echo.chapter1;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static com.echo.chapter1.ByteBufferUtil.debugAll;
@Slf4j
public class AioFileChannel {
    public static void main(String[] args) throws IOException {
        try(AsynchronousFileChannel channel = AsynchronousFileChannel.open(Paths.get("data.txt"),
                StandardOpenOption.READ)){
            //参数1：ByteBuffer
            //参数2：读取的起始位置
            //参数3：附件，一个用来读取剩余数据的byteBuffer
            //参数4：回调对象
            ByteBuffer byteBuffer = ByteBuffer.allocate(16);
            log.debug("read begin...");
            //这个CompletionHandler的回调是一个守护线程，所以要注意，主线程结束之后，该守护线程也会随之结束
            //所以，要保证主线程先不结束，使用System.in.read();
            channel.read(byteBuffer, 0, byteBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                /**
                 * 读取成功后回调的函数
                 * @param result
                 * @param attachment
                 */
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    log.debug("read completed...");
                    attachment.flip();
                    debugAll(attachment);
                }

                /**
                 * 读取失败后回调的函数
                 * @param exc
                 * @param attachment
                 */
                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    exc.printStackTrace();
                }
            });
            log.debug("read finish....");
        }
        catch (IOException e){
            e.printStackTrace();
        }
        System.in.read();
    }
}
