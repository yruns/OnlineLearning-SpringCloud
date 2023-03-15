package com.yruns.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yruns.base.model.PageParams;
import com.yruns.base.model.PageResult;
import com.yruns.content.mapper.CourseBaseMapper;
import com.yruns.content.model.dto.QueryCourseParamsDto;
import com.yruns.content.model.pojo.CourseBase;
import com.yruns.content.service.CourseBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * CourseBaseInfoServiceImpl
 */
@Slf4j
@Service
public class CourseBaseInfoServiceImpl extends ServiceImpl<CourseBaseMapper, CourseBase> implements CourseBaseInfoService {


    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {

        Page<CourseBase> pageResult = this.query()
                .like("name", queryCourseParamsDto.getCourseName())
                .eq(StringUtils.isNotBlank(queryCourseParamsDto.getPublishStatus()), "status", queryCourseParamsDto.getPublishStatus())
                .eq(StringUtils.isNotBlank(queryCourseParamsDto.getAuditStatus()), "audit_status", queryCourseParamsDto.getAuditStatus())
                .page(new Page<>(pageParams.getPage(), pageParams.getPageSize()));

        // 封装为定义的PageResult
        PageResult<CourseBase> result = new PageResult<>(pageResult.getRecords(), pageResult.getTotal(),
                pageParams.getPage(), pageParams.getPageSize());

        return result;
    }
}
