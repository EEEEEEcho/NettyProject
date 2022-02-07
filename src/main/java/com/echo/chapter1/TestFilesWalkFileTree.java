package com.echo.chapter1;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

public class TestFilesWalkFileTree {
    public static void main(String[] args) throws IOException {
        getAllDirAndFile();

        getAllJarFile();
    }

    /**
     * 统计/workspace/JavaProject/tbox_project/TspProject目录以及子目录下的所有jar包
     * @throws IOException
     */
    private static void getAllJarFile() throws IOException {
        AtomicInteger jarCount = new AtomicInteger(0);
        Files.walkFileTree(Paths.get("/workspace/JavaProject/tbox_project/TspProject"),
                new SimpleFileVisitor<Path>(){

                    /**
                     * 遍历到文件时的方法
                     * @param file
                     * @param attrs
                     * @return
                     * @throws IOException
                     */
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        System.out.println("===>" + file);
                        if (file.getFileName().toString().contains(".jar")){
                            jarCount.incrementAndGet();
                        }
                        return super.visitFile(file, attrs);
                    }

                });
        System.out.println(jarCount.get());
    }

    /**
     * 统计/workspace/JavaProject/tbox_project/TspProject目录下的子目录以及文件数目
     * @throws IOException
     */
    private static void getAllDirAndFile() throws IOException {
        AtomicInteger dirCount = new AtomicInteger();
        AtomicInteger fileCount = new AtomicInteger();
        /**
         * walkFileTree(起始目录,遍历目录时的行为)
         * Paths.get("/workspace/JavaProject/tbox_project/TspProject") 定义了起始遍历的目录
         * new SimpleFileVisitor<Path>()定义了遍历目录时的行为,访问者设计模式
         */
        Files.walkFileTree(Paths.get("/workspace/JavaProject/tbox_project/TspProject"),
                new SimpleFileVisitor<Path>(){
                    /**
                     * 遍历到目录之前的方法
                     * @param dir
                     * @param attrs
                     * @return
                     * @throws IOException
                     */
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        System.out.println("===>" + dir);
                        //因为匿名内部类中不能引用方法的局部变量，所以不能使用普通的整型，而是要使用计数器
                        dirCount.incrementAndGet();
                        //不需要修改return,只需添加自己的逻辑
                        return super.preVisitDirectory(dir, attrs);
                    }

                    /**
                     * 遍历到文件时的方法
                     * @param file
                     * @param attrs
                     * @return
                     * @throws IOException
                     */
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        System.out.println("===>" + file);
                        fileCount.incrementAndGet();
                        return super.visitFile(file, attrs);
                    }

                    /**
                     * 遍历文件失败时的方法
                     * @param file
                     * @param exc
                     * @return
                     * @throws IOException
                     */
                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        return super.visitFileFailed(file, exc);
                    }

                    /**
                     * 遍历目录之后的方法
                     * @param dir
                     * @param exc
                     * @return
                     * @throws IOException
                     */
                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        return super.postVisitDirectory(dir, exc);
                    }
                });

        System.out.println("DirCount:" + dirCount.get());
        System.out.println("FileCount:" + fileCount.get());
    }

    /**
     * 删除目录以及文件夹
     * @throws IOException
     */
    private static void deleteFile() throws IOException {
        Files.walkFileTree(Paths.get("/workspace/test"),new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                System.out.println("====> 进入" + dir);
                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file); //先删除文件
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return super.visitFileFailed(file, exc);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                //出文件夹时删除文件夹
                Files.delete(dir);
                return super.postVisitDirectory(dir, exc);
            }
        });
    }

    private static void fileCopy() throws IOException {
        String source = "/workspace/test";
        String target = "/workspace/test2";

        Files.walk(Paths.get(source)).forEach(path -> {
            String targetName = path.toString().replace(source,target);
            if (Files.isDirectory(path)){
                //如果path是一个目录
                try {
                    Files.createDirectory(Paths.get(targetName));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(Files.isRegularFile(path)){
                try {
                    Files.copy(path,Paths.get(targetName));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
