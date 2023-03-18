package com.yruns.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.yruns.base.exception.CustomException;
import com.yruns.base.model.PageParams;
import com.yruns.base.model.PageResult;
import com.yruns.media.mapper.MediaFilesMapper;
import com.yruns.media.model.dto.QueryMediaParamsDto;
import com.yruns.media.model.dto.UploadFileParamDto;
import com.yruns.media.model.dto.UploadFileResultDto;
import com.yruns.media.model.po.MediaFiles;
import com.yruns.media.service.MediaFileService;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
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
        Page<MediaFiles> page = new Page<>(pageParams.getPage(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPage(), pageParams.getPageSize());
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

}
