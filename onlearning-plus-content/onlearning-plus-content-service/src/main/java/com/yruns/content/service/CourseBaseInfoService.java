package com.yruns.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yruns.base.model.PageParams;
import com.yruns.base.model.PageResult;
import com.yruns.content.model.dto.*;
import com.yruns.content.model.pojo.CourseBase;

public interface CourseBaseInfoService extends IService<CourseBase> {

    /**
     * @param pageParams
     * @param queryCourseParamsDto
     * @return
     */
    PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    /**
     * 新增课程
     * @param companyId
     * @param addCourseDto
     * @return
     */
    CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);

    CourseBaseInfoDto queryCourseById(Long id);

    CourseBaseInfoDto updateCourseBase(Long companyId, UpdateCourseDto updateCourseDto);

    void deleteCourseBase(Long id);

}
