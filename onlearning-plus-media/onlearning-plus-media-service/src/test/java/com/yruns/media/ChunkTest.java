package com.yruns.media;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

/**
 * ChunkTest for 测试大文件上传方法
 *
 * @Author yruns
 * @Version 2023/3/18
 */
public class ChunkTest {

    // 分块测试
    @Test
    public void testChunk() throws IOException {
        // 源文件
        File sourceFile = new File("C:\\Users\\86185\\Videos\\20230313_224704.mp4");
        // 分块文件存储路径
        String chunkFilePath = "H:\\testChunk\\";
        // 分块文件大小
        int chunkSize = 1024 * 1024 * 5; // 5MB
        // 分块文件个数
        int chunkNum = (int) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
        // 使用流从源文件读数据，向分块文件写数据
        RandomAccessFile raf = new RandomAccessFile(sourceFile, "r");
        // 缓冲区
        byte[] buffer = new byte[1024];
        for (int i = 0; i < chunkNum; i++) {
            File file = new File(chunkFilePath + i);
            // 分块文件写入流
            RandomAccessFile waf = new RandomAccessFile(file, "rw");
            int len;
            while ((len = raf.read(buffer)) != -1) {
                waf.write(buffer, 0, len);
                if (file.length() >= chunkSize) {
                    break;
                }
            }
            waf.close();
        }
        raf.close();

    }

    // 合并测试
    @Test
    public void testMerge() throws Exception {
        // 快文件目录
        File chunkFileFolder = new File("H:\\testChunk\\");
        // 源文件
        File sourceFile = new File("C:\\Users\\86185\\Videos\\20230313_224704.mp4");
        // 合并后的文件
        File mergeFile = new File("H:\\testChunk\\a_merge.mp4");

        // 取出所有分块
        File[] files = chunkFileFolder.listFiles();
        // 将数组转成List
        assert files != null;
        List<File> fileList = Arrays.asList(files);

        System.out.println(fileList);

        // 对元素进行排序
        Collections.sort(fileList, (o1, o2) -> {
            if (Integer.parseInt(o1.getName().substring(o1.getName().lastIndexOf("/") + 1)) <
                    Integer.parseInt(o2.getName().substring(o2.getName().lastIndexOf("/") + 1))) {
                return -1;
            }
            return 1;
        });

        RandomAccessFile rw = new RandomAccessFile(mergeFile, "rw");
        byte[] buffer = new byte[1024];
        // 遍历分块文件
        for (File file : fileList) {
            RandomAccessFile r = new RandomAccessFile(file, "r");
            int len;
            while ((len = r.read(buffer)) != -1) {
                rw.write(buffer, 0, len);
            }
            r.close();
        }
        rw.close();

        // 对文件进行校验
        String sourceMd5 = DigestUtils.md5Hex(Files.newInputStream(sourceFile.toPath()));
        String mergedMd5 = DigestUtils.md5Hex(Files.newInputStream(mergeFile.toPath()));

        if (Objects.equals(sourceMd5, mergedMd5)) {
            System.out.println("文件相同");
        } else {
            System.out.println("文件受损");
        }

    }
}
