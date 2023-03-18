package com.yruns.media;

import com.j256.simplemagic.ContentInfo;
import io.minio.*;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.errors.*;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.io.IOUtil;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * MinioTest for
 *
 * @Author yruns
 * @Version 2023/3/18
 */
public class MinioTest {

    MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://127.0.0.1:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();



    @SneakyThrows
    @Test
    public void testUpload() {

        //通过扩展名得到媒体资源类型 mimeType
        //根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".mp4");
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mimeType，字节流
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }

        //上传文件的参数信息
        UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                .bucket("onlearningbucket")//桶
                .filename("C:\\Users\\86185\\Videos\\20221223_233242.mp4") //指定本地文件路径
//                .object("1.mp4")//对象名 在桶下存储该文件
                .object("test/01/1.mp4")//对象名 放在子目录下
                .contentType(mimeType)//设置媒体文件类型
                .build();

        //上传文件
        minioClient.uploadObject(uploadObjectArgs);
    }

    @SneakyThrows
    @Test
    public void testDelete() {


        // 删除文件的信息
        RemoveObjectArgs onlearningbucket = RemoveObjectArgs.builder().bucket("onlearningbucket")
                .object("test/01/1.mp4").build();

        //上传文件
        minioClient.removeObject(onlearningbucket);
    }

    @SneakyThrows
    @Test
    public void testDownload() {

        // 下载文件的信息
        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket("onlearningbucket")
                .object("test/01/1.mp4").build();

        // 获取文件
        FilterInputStream filterInputStream = minioClient.getObject(getObjectArgs);
        FileOutputStream fileOutputStream = new FileOutputStream("H:/1.mp4");
        // 流拷贝
        IOUtils.copy(filterInputStream, fileOutputStream);

        // 校验文件完整性
        String origin = DigestUtils.md5Hex(Files.newInputStream(Paths.get("C:\\Users\\86185\\Videos\\20221223_233242.mp4")));
        String local = DigestUtils.md5Hex(Files.newInputStream(Paths.get("H:/1.mp4")));

        if (Objects.equals(origin, local)) {
            System.out.println("下载成功");
        }
    }

    @Test
    public void testUpLoadChunk() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        //上传文件的参数信息
        UploadObjectArgs uploadObjectArgs = null;
        for (int i = 0; i < 8; i++) {
            uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket("onlearningbucket")//桶
                    .filename("H:\\testChunk\\" + i) //指定本地文件路径
    //                .object("1.mp4")//对象名 在桶下存储该文件
                    .object("chunk/" + i)//对象名 放在子目录下
                    .build();
            //上传文件
            minioClient.uploadObject(uploadObjectArgs);
            System.out.println("上传成功" + i);
        }
    }

    @SneakyThrows
    @Test
    public void testMergeChunk() {
        List<ComposeSource> composeSources = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            composeSources.add(
                    ComposeSource.builder().bucket("onlearningbucket").object("chunk/" + i).build()
            );
        }
        ComposeObjectArgs ca = ComposeObjectArgs.builder().bucket("onlearningbucket")
                .sources(composeSources)
                .object("merge.mp4")
                .build();

        minioClient.composeObject(ca);
    }
}
