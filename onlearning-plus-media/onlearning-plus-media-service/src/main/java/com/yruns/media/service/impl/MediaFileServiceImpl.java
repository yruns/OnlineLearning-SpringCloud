package com.yruns.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.yruns.base.exception.CustomException;
import com.yruns.base.model.PageParams;
import com.yruns.base.model.PageResult;
import com.yruns.base.model.Result;
import com.yruns.media.mapper.MediaFilesMapper;
import com.yruns.media.model.dto.QueryMediaParamsDto;
import com.yruns.media.model.dto.UploadFileParamDto;
import com.yruns.media.model.dto.UploadFileResultDto;
import com.yruns.media.model.po.MediaFiles;
import com.yruns.media.service.MediaFileService;
import io.minio.*;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Slf4j
@Service
public class MediaFileServiceImpl extends ServiceImpl<MediaFilesMapper, MediaFiles> implements MediaFileService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MinioClient minioClient;

    // 存储普通文件
    @Value("${minio.bucket.files}")
    private String mediaFilesBucket;

    // 存储视频
    @Value("${minio.bucket.videofiles}")
    private String videoFilesBucket;

    @Autowired
    private MediaFileService currentProxy;


    @Override
    public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

    // 根据拓展名获取minioType
    private String getMimeType(String extension) {
        if (extension == null) {
            extension = "";
        }

        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }

        return mimeType;
    }

    // 上传文件到minio
    private boolean uploadFileToMinIO(String localFilePath, String mimeType, String bucket, String objectName) {
        try {
            UploadObjectArgs objectArgs = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .filename(localFilePath)
                    .object(objectName)
                    .contentType(mimeType)
                    .build();

            minioClient.uploadObject(objectArgs);

            return true;
        } catch (Exception e) {
            log.error("上传文件出错, bucket:{}, objectName: {}, 错误信息: {}", bucket, objectName, e.getMessage());
        }

        return false;
    }

    // 根据当前时间获取 年/月/日
    private String getDefaultFoldPath() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String folder = simpleDateFormat.format(new Date()).replace("-", "/") + "/";
        //   2023/03/18
        return folder;
    }

    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamDto uploadFileParamDto, String filePath) {

        // 得到文件名
        String filename = uploadFileParamDto.getFilename();
        // 获取拓展名
        String extension = filename.substring(filename.lastIndexOf("."));
        String mimeType = getMimeType(extension);

        // 获取年月日，和MD5拼接为文件路径
        String md5;
        try {
            md5 = DigestUtils.md5Hex(Files.newInputStream(Paths.get(filePath)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String ObjectName = getDefaultFoldPath() + md5 + extension;

        // 上传文件到Minio
        boolean flag = uploadFileToMinIO(filePath, mimeType, mediaFilesBucket, ObjectName);

        if (!flag) {
            CustomException.cast("文件上传失败");
        }

        // 文件信息保存到数据库
        // 1.判断该文件信息是否已经存在
        MediaFiles byId = this.getById(md5);
        if (byId != null) {
            log.info("{}文件已存在", md5 + extension);
            // 删除旧文件记录
            this.removeById(md5);
        }

        // 通过自身代理对象调用，否则会导致事务失效
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDB(companyId, md5, uploadFileParamDto, mediaFilesBucket, ObjectName);

        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);

        return uploadFileResultDto;
    }


    @Transactional
    public MediaFiles addMediaFilesToDB(Long companyId, String fileMd5, UploadFileParamDto uploadFileParamDto,
                                 String bucket, String objectName) {
        MediaFiles mediaFiles = new MediaFiles();
        BeanUtils.copyProperties(uploadFileParamDto, mediaFiles);
        mediaFiles.setId(fileMd5);
        mediaFiles.setCompanyId(companyId);
        mediaFiles.setBucket(bucket);
        mediaFiles.setFilePath(objectName);
        mediaFiles.setFileId(fileMd5);
        mediaFiles.setUrl("/" + bucket + "/" + objectName);
        mediaFiles.setCreateDate(LocalDateTime.now());
        mediaFiles.setStatus("1");
        mediaFiles.setAuditStatus("002003");

        // 插入数据库
        this.save(mediaFiles);

        return mediaFiles;
    }

    @Override
    public Result<Boolean> checkFile(String fileMd5) {
        // 先查询数据库中是否存在信息
        MediaFiles byId = this.getById(fileMd5);
        if (byId != null) {
            // 查询minio
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(byId.getBucket())
                    .object(byId.getFilePath())
                    .build();
            // 获取文件
            try {
                FilterInputStream filterInputStream = minioClient.getObject(getObjectArgs);
                if (filterInputStream != null) {
                    return Result.ok(true);
                }
            } catch (Exception e) {
                // 异常
                CustomException.cast("文件异常");
            }
        }

        return Result.ok(false);
    }

    @Override
    public Result<Boolean> checkChunk(String fileMd5, int chunk) {
        // MD5的前两位为子目录
        String chunkFileChunk = fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/chunk/" + chunk;

        // 查询minio
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(videoFilesBucket)
                .object(chunkFileChunk)
                .build();
        // 获取文件
        try {
            FilterInputStream filterInputStream = minioClient.getObject(getObjectArgs);
            if (filterInputStream != null) {
                return Result.ok(true);
            }
        } catch (Exception e) {
            // 异常
            e.printStackTrace();
        }
        return Result.ok(false);
    }

    @Override
    public Result<Boolean> uploadChunk(String filePath, String fileMd5, int chunk) {
        // 获取拓展名
        String extension = "";
        String mimeType = getMimeType(extension);
        String ObjectName = fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/chunk/" + chunk;

        boolean success = uploadFileToMinIO(filePath, mimeType, videoFilesBucket, ObjectName);
        return Result.ok(success);
    }

    /**
     * 从minio下载文件
     * @param bucket 桶
     * @param objectName 对象名称
     * @return 下载后的文件
     */
    public File downloadFileFromMinIO(String bucket, String objectName) {
        //临时文件
        File minioFile = null;
        FileOutputStream outputStream = null;
        try{
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            //创建临时文件
            minioFile= File.createTempFile("minio", ".merge");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(stream, outputStream);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    
    private void clearChunkFiles(String chunkFileFolderPath, int chunkTotal) throws Exception{
        for (int i = 0; i < chunkTotal; i++) {
            // 删除文件的信息
            RemoveObjectArgs objectArgs = RemoveObjectArgs.builder()
                    .bucket(videoFilesBucket)
                    .object(chunkFileFolderPath + i)
                    .build();

            // 删除
            minioClient.removeObject(objectArgs);
        }
    }

    @Override
    public Result<Boolean> mergeChunk(Long companyId, String fileMd5, String fileName, int chunkTotal,
                                      UploadFileParamDto uploadFileParamDto) {
        String chunkFileFolderPath = fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/chunk/";
        List<ComposeSource> composeSources = new ArrayList<>();
        for (int i = 0; i < chunkTotal; i++) {
            composeSources.add(
                    ComposeSource.builder()
                            .bucket(videoFilesBucket)
                            .object(chunkFileFolderPath + i)
                            .build()
            );
        }

        String mergeObject = fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileName;
        ComposeObjectArgs ca = ComposeObjectArgs.builder().bucket(videoFilesBucket)
                .sources(composeSources)
                .object(mergeObject)
                .build();

        try {
            minioClient.composeObject(ca);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail(false, "文件合并失败");
        }

        // 校验文件
        File mergeFile = downloadFileFromMinIO(videoFilesBucket, mergeObject);

        try(FileInputStream fileInputStream = new FileInputStream(mergeFile)){
            //计算合并后文件的md5
            String mergeFile_md5 = DigestUtils.md5Hex(fileInputStream);
            //比较原始md5和合并后文件的md5
            if(!fileMd5.equals(mergeFile_md5)){
                log.error("校验合并文件md5值不一致,原始文件:{},合并文件:{}",fileMd5,mergeFile_md5);
                return Result.fail(false,"文件校验失败");
            }
            //文件大小
            uploadFileParamDto.setFileSize(mergeFile.length());
        }catch (Exception e) {
            return Result.fail(false,"文件校验失败");
        }

        // 信息入库
        currentProxy.addMediaFilesToDB(companyId, fileMd5, uploadFileParamDto, videoFilesBucket, mergeObject);

        // 清理分块文件
        try {
            clearChunkFiles(chunkFileFolderPath, chunkTotal);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Result.ok(true);
    }

}
