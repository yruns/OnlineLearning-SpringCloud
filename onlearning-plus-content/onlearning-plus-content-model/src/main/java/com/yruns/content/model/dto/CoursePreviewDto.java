package com.yruns.content.model.dto;

import lombok.Data;

import java.util.List;

/**
 * CoursePreviewDto for
 *
 * @Author yruns
 * @Version 2023/3/20
 */
@Data
public class CoursePreviewDto {

    // 课程基本信息
    // 课程营销信息
    private CourseBaseInfoDto courseBase;

    // 课程计划信息
    private List<TeachplanDto> teachplans;

    // 课程师资信息
}
