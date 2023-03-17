package com.yruns.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yruns.content.model.pojo.CourseTeacher;

import java.util.List;

public interface CourseTeacherService extends IService<CourseTeacher> {
    List<CourseTeacher> queryCourseTeacherByCourseId(Long id);

    CourseTeacher addCourseTeacher(Long companyId, CourseTeacher courseTeacher);

}
