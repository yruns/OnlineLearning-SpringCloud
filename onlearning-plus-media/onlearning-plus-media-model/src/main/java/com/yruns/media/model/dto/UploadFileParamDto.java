package com.yruns.media.model.dto;

import lombok.Data;

/**
 * UploadFileParamDto for
 *
 * @Author yruns
 * @Version 2023/3/18
 */
@Data
public class UploadFileParamDto {
    /**
     * 文件名称
     */
    private String filename;


    /**
     * 文件类型（文档，音频，视频）
     */
    private String fileType;
    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 标签
     */
    private String tags;
    /**
     * 上传人
     */
    private String username;
    /**
     * 备注
     */
    private String remark;

}
