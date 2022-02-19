package com.echo.review;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

public class Review1 {
    public static void main(String[] args) throws IOException {
        Path path = Paths.get("D:\\Java");
        AtomicInteger dirs = new AtomicInteger(0);
        AtomicInteger files = new AtomicInteger(0);
        Files.walkFileTree(path,new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                System.out.println(dir);
                dirs.incrementAndGet();
                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                System.out.println(file);
                if (file.toFile().getName().endsWith(".jar")){
                    files.incrementAndGet();
                }
                return super.visitFile(file,attrs);
            }
        });
        System.out.println(dirs.get());
        System.out.println(files.get());
    }
}
