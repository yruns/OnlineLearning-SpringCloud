package com.yruns.content;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yruns.base.model.PageResult;
import com.yruns.content.model.dto.CourseCategoryDto;
import com.yruns.content.model.dto.QueryCourseParamsDto;
import com.yruns.content.model.pojo.CourseBase;
import com.yruns.content.service.CourseBaseInfoService;
import com.yruns.content.service.CourseCategoryService;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * CourseBaseInfoServiceTests
 */
@SpringBootTest
public class CourseCategoryTests {

    @Autowired
    private CourseCategoryService courseCategoryService;

    @Test
    public void testCourseBaseInfoService() {
        List<CourseCategoryDto> courseCategoryDtos = courseCategoryService.getCategory("1");
        System.out.println(courseCategoryDtos);
    }
}
