package com.yruns.content.api;

import com.yruns.content.model.dto.CoursePreviewDto;
import com.yruns.content.service.CourseBaseInfoService;
import com.yruns.content.service.CoursePublishService;
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
    CoursePublishService coursePublishService;

    // 根据课程计划查询课程信息
    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getPreviewInfo(@PathVariable("courseId")Long courseId) {
        return coursePublishService.getCoursePreviewInfo(courseId);
    }

}
