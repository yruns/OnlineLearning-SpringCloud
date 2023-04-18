package com.yruns.content.service.impl;

import com.yruns.content.model.dto.CoursePreviewDto;
import com.yruns.content.service.CourseBaseInfoService;
import com.yruns.content.service.CoursePublishService;
import com.yruns.content.service.TeachPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * CoursePublishServiceImpl for
 *
 * @Author yruns
 * @Version 2023/3/20
 */
@Service
public class CoursePublishServiceImpl implements CoursePublishService {

    @Autowired
    private CourseBaseInfoServiceImpl courseBaseInfoService;

    @Autowired
    private TeachPlanServiceImpl teachPlanService;

    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long id) {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        // 查询课程基本信息
        coursePreviewDto.setCourseBase(courseBaseInfoService.queryCourseById(id));
        // 查询课程计划信息
        coursePreviewDto.setTeachplans(teachPlanService.selectTreeNodes(id));

        return coursePreviewDto;
    }
}
