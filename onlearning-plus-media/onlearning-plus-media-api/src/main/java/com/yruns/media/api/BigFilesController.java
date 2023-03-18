package com.yruns.media.api;

import com.yruns.base.model.Result;
import com.yruns.media.model.dto.UploadFileParamDto;
import com.yruns.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * BigFilesController for
 *
 * @Author yruns
 * @Version 2023/3/18
 */
@Api(value = "大文件管理接口", tags = "大文件管理接口")
@RestController
public class BigFilesController {

    @Autowired
    MediaFileService mediaFileService;


    @ApiOperation("检查文件是否存在")
    @PostMapping("/upload/checkfile")
    public Result<Boolean> checkFile(@RequestParam("fileMd5") String fileMd5) {
        return mediaFileService.checkFile(fileMd5);
    }

    @ApiOperation("检查分块是否存在")
    @PostMapping("/upload/checkchunk")
    public Result<Boolean> checkChunk(@RequestParam("fileMd5") String fileMd5,
                                     @RequestParam("chunk") int chunk) {
        return mediaFileService.checkChunk(fileMd5, chunk);
    }

    @ApiOperation("上传分块")
    @PostMapping("/upload/uploadchunk")
    public Result<Boolean> uploadChunk(@RequestParam("file") MultipartFile file,
                                      @RequestParam("fileMd5") String fileMd5,
                                      @RequestParam("chunk") int chunk) throws IOException {
        // 创建临时文件
        File tempFile = File.createTempFile("chunk", "temp");
        file.transferTo(tempFile);
        String filePath = tempFile.getAbsolutePath();
        return mediaFileService.uploadChunk(filePath, fileMd5, chunk);
    }


    @ApiOperation("合并分块")
    @PostMapping("/upload/mergechunks")
    public Result<Boolean> uploadChunk(@RequestParam("fileMd5") String fileMd5,
                                       @RequestParam("fileName") String fileName,
                                       @RequestParam("chunkTotal") int chunkTotal) throws IOException {
        Long companyId = 1232141425L;
        UploadFileParamDto uploadFileParamsDto = new UploadFileParamDto();
        uploadFileParamsDto.setFilename(fileName);
        uploadFileParamsDto.setTags("视频文件");
        uploadFileParamsDto.setFileType("001002");

        return mediaFileService.mergeChunk(companyId, fileMd5, fileName, chunkTotal, uploadFileParamsDto);
    }


}
