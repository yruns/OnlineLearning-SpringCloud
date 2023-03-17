package com.yruns.content.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yruns.base.exception.CustomException;
import com.yruns.content.mapper.CourseBaseMapper;
import com.yruns.content.mapper.CourseTeacherMapper;
import com.yruns.content.model.pojo.CourseTeacher;
import com.yruns.content.service.CourseBaseInfoService;
import com.yruns.content.service.CourseTeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * CourseTeacherServiceImpl
 */
@Service
public class CourseTeacherServiceImpl extends ServiceImpl<CourseTeacherMapper, CourseTeacher> implements CourseTeacherService {

    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    @Override
    public List<CourseTeacher> queryCourseTeacherByCourseId(Long id) {
        return this.query().eq("course_id", id).list();
    }

    @Override
    public CourseTeacher addCourseTeacher(Long companyId, CourseTeacher courseTeacher) {

        // 查询该课程所属的机构id
        Long courseCompanyId = courseBaseInfoService.getById(courseTeacher.getCourseId()).getCompanyId();
        if (!Objects.equals(companyId, courseCompanyId)) {
            CustomException.cast("只能修改本机构的课程老师信息");
        }

        // 插入数据
        courseTeacher.setCreateDate(LocalDateTime.now());
        this.save(courseTeacher);

        return courseTeacher;
    }
}
