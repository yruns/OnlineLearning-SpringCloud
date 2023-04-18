package com.yruns.content.service;

import com.yruns.content.model.dto.CoursePreviewDto;

/**
 * CoursePublishService for
 *
 * @Author yruns
 * @Version 2023/3/20
 */
public interface CoursePublishService {

    CoursePreviewDto getCoursePreviewInfo(Long id);
}
