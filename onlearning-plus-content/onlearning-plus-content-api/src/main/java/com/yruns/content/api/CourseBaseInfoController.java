package com.yruns.content.api;

import com.yruns.base.model.PageParams;
import com.yruns.base.model.PageResult;
import com.yruns.content.model.dto.AddCourseDto;
import com.yruns.content.model.dto.CourseBaseInfoDto;
import com.yruns.content.model.dto.QueryCourseParamsDto;
import com.yruns.content.model.pojo.CourseBase;
import com.yruns.content.service.CourseBaseInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * CourseBaseInfoController
 */
@RestController
@Api(value = "课程信息管理接口", tags = "课程信息管理接口")
public class CourseBaseInfoController {


    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    @ApiOperation("课程查询接口")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams,
                                       @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto) {
        return courseBaseInfoService.queryCourseBaseList(pageParams, queryCourseParamsDto);
    }

    @ApiOperation("课程添加接口")
    @PostMapping("/course")
    public CourseBaseInfoDto addCourse(@RequestBody @Validated AddCourseDto addCourseDto) {

        Long companyId = 123214125L;

        return courseBaseInfoService.createCourseBase(companyId, addCourseDto);
    }
}
