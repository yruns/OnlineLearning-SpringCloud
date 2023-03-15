package com.yruns.content;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yruns.base.model.PageResult;
import com.yruns.content.model.dto.QueryCourseParamsDto;
import com.yruns.content.model.pojo.CourseBase;
import com.yruns.content.service.CourseBaseInfoService;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * CourseBaseInfoServiceTests
 */
@SpringBootTest
public class CourseBaseInfoServiceTests {

    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    @Test
    public void testCourseBaseInfoService() {

        QueryCourseParamsDto queryCourseParamsDto = new QueryCourseParamsDto();
        queryCourseParamsDto.setCourseName("java");//课程名称查询条件

        Page<CourseBase> pageResult = courseBaseInfoService.query()
                .like("name", queryCourseParamsDto.getCourseName())
                .eq(StringUtils.isNotBlank(queryCourseParamsDto.getPublishStatus()), "status", queryCourseParamsDto.getPublishStatus())
                .eq(StringUtils.isNotBlank(queryCourseParamsDto.getAuditStatus()), "audit_status", queryCourseParamsDto.getAuditStatus())
                .page(new Page<>(1, 3));

        // 封装为定义的PageResult
        PageResult<CourseBase> result = new PageResult<>(pageResult.getRecords(), pageResult.getTotal(),
                1L, 3L);
    }
}
