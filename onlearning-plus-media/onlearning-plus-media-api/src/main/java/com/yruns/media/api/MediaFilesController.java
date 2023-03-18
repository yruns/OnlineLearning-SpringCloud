package com.yruns.media.api;

import com.yruns.base.model.PageParams;
import com.yruns.base.model.PageResult;
import com.yruns.media.model.dto.QueryMediaParamsDto;
import com.yruns.media.model.dto.UploadFileParamDto;
import com.yruns.media.model.dto.UploadFileResultDto;
import com.yruns.media.model.po.MediaFiles;
import com.yruns.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理接口
 * @date 2022/9/6 11:29
 */
@Api(value = "媒资文件管理接口", tags = "媒资文件管理接口")
@RestController
public class MediaFilesController {


    @Autowired
    MediaFileService mediaFileService;


    @ApiOperation("媒资列表查询接口")
    @PostMapping("/files")
    public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto) {
        Long companyId = 1232141425L;
        return mediaFileService.queryMediaFiles(companyId, pageParams, queryMediaParamsDto);

    }

    @ApiOperation("上传接口")
    @PostMapping(value = "/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadFileResultDto upload(@RequestPart("filedata") MultipartFile filedata) throws IOException {
        Long companyId = 1232141425L;

        // 已经接收到文件了，在内存
        UploadFileParamDto uploadFileParamDto = new UploadFileParamDto();
        uploadFileParamDto.setFilename(filedata.getOriginalFilename());
        uploadFileParamDto.setFileSize(filedata.getSize());
        uploadFileParamDto.setFileType("001001");

        // 创建一个临时文件
        File minio = File.createTempFile("minio", ".temp");
        filedata.transferTo(minio);

        String filePath = minio.getAbsolutePath();

        return mediaFileService.uploadFile(companyId, uploadFileParamDto, filePath);
    }

}
