package com.yruns.content.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yruns.content.model.pojo.CourseTeacher;
import com.yruns.content.service.CourseTeacherService;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CourseTeacherController
 */
@RestController
public class CourseTeacherController {

    @Autowired
    private CourseTeacherService courseTeacherService;

    @ApiOperation("查询课程相关老师")
    @GetMapping("/courseTeacher/list/{id}")
    public List<CourseTeacher> queryCourseTeacher(@PathVariable Long id) {
        return courseTeacherService.queryCourseTeacherByCourseId(id);
    }

    @ApiOperation("添加课程相关老师")
    @PostMapping("/courseTeacher")
    public CourseTeacher queryCourseTeacher(@RequestBody CourseTeacher courseTeacher) {

        Long companyId = 1232141425L;
        return courseTeacherService.addCourseTeacher(companyId, courseTeacher);
    }

    @ApiOperation("修改课程相关老师信息")
    @PutMapping("/courseTeacher")   // 该方法前端接口有误，post应为put
    public CourseTeacher updateCourseTeacher(@RequestBody CourseTeacher courseTeacher) {

        courseTeacherService.updateById(courseTeacher);
        return courseTeacher;
    }

    @ApiOperation("删除课程相关老师信息")
    @DeleteMapping("courseTeacher/course/{courseId}/{teacherId}")   // 该方法前端接口有误，post应为put
    public void deleteCourseTeacher(@PathVariable Long courseId, @PathVariable Long teacherId) {

        LambdaQueryWrapper<CourseTeacher> eq = new LambdaQueryWrapper<CourseTeacher>()
                .eq( CourseTeacher::getId, teacherId)
                .eq( CourseTeacher::getCourseId, courseId);
        courseTeacherService.remove(eq);
    }

}
