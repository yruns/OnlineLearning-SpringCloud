package com.yruns.media.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yruns.base.model.PageParams;
import com.yruns.base.model.PageResult;
import com.yruns.media.model.dto.QueryMediaParamsDto;
import com.yruns.media.model.dto.UploadFileParamDto;
import com.yruns.media.model.dto.UploadFileResultDto;
import com.yruns.media.model.po.MediaFiles;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理业务类
 * @date 2022/9/10 8:55
 */
public interface MediaFileService extends IService<MediaFiles> {

    /**
     * @param pageParams          分页参数
     * @param queryMediaParamsDto 查询条件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
     * @description 媒资文件查询方法
     * @author Mr.M
     * @date 2022/9/10 8:57
     */
    PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);


    UploadFileResultDto uploadFile(Long companyId, UploadFileParamDto uploadFileParamDto, String filePath);

    MediaFiles addMediaFilesToDB(Long companyId, String fileMd5, UploadFileParamDto uploadFileParamDto,
                                        String bucket, String objectName);
}
