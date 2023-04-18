package com.yruns.media.api;

import com.alibaba.nacos.common.model.RestResult;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.yruns.base.exception.CustomException;
import com.yruns.base.model.Result;
import com.yruns.media.model.po.MediaFiles;
import com.yruns.media.service.MediaFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * OpenController for
 *
 * @Author yruns
 * @Version 2023/4/18
 */
@RestController
@RequestMapping("/open")
public class OpenController {

    @Autowired
    MediaFileService mediaFileService;

    @GetMapping("/preview/{mediaId}")
    public Result<String> getPlayUrlByMediaId(@PathVariable String mediaId) {
        MediaFiles mediaFiles = mediaFileService.getPlayUrlByMediaId(mediaId);
        if (mediaFiles == null || StringUtils.isBlank(mediaFiles.getUrl())) {
            CustomException.cast("视频未进行转码处理");
        }
        return Result.ok(mediaFiles.getUrl());
    }
}
