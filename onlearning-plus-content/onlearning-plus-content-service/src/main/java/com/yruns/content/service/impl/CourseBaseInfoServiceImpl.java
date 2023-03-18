package com.yruns.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yruns.base.exception.CustomException;
import com.yruns.base.model.PageParams;
import com.yruns.base.model.PageResult;
import com.yruns.content.mapper.CourseBaseMapper;
import com.yruns.content.mapper.CourseCategoryMapper;
import com.yruns.content.mapper.CourseMarketMapper;
import com.yruns.content.model.dto.AddCourseDto;
import com.yruns.content.model.dto.CourseBaseInfoDto;
import com.yruns.content.model.dto.QueryCourseParamsDto;
import com.yruns.content.model.dto.UpdateCourseDto;
import com.yruns.content.model.pojo.CourseBase;
import com.yruns.content.model.pojo.CourseMarket;
import com.yruns.content.model.pojo.CourseTeacher;
import com.yruns.content.service.CourseBaseInfoService;
import com.yruns.content.service.CourseTeacherService;
import com.yruns.content.service.TeachPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

/**
 * CourseBaseInfoServiceImpl
 */
@Slf4j
@Service
public class CourseBaseInfoServiceImpl extends ServiceImpl<CourseBaseMapper, CourseBase> implements CourseBaseInfoService {

    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Autowired
    private TeachPlanService teachPlanService;

    @Autowired
    private CourseTeacherService courseTeacherService;


    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {

        Page<CourseBase> pageResult = this.query()
                .like("name", queryCourseParamsDto.getCourseName())
                .eq(StringUtils.isNotBlank(queryCourseParamsDto.getPublishStatus()), "status", queryCourseParamsDto.getPublishStatus())
                .eq(StringUtils.isNotBlank(queryCourseParamsDto.getAuditStatus()), "audit_status", queryCourseParamsDto.getAuditStatus())
                .page(new Page<>(pageParams.getPageNo(), pageParams.getPageSize()));

        // 封装为定义的PageResult
        PageResult<CourseBase> result = new PageResult<>(pageResult.getRecords(), pageResult.getTotal(),
                pageParams.getPageNo(), pageParams.getPageSize());

        return result;
    }

    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto) {
        // 向课程基本信息表写入数据
        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(addCourseDto, courseBase);
        courseBase.setCompanyId(companyId);     // 机构id
        courseBase.setCreateDate(LocalDateTime.now()); // 创建时间
        // todo: 添加创建人
        courseBase.setAuditStatus("202002"); // 默认未审核
        courseBase.setStatus("203001"); // 默认未提交

        boolean flag = this.save(courseBase);
        if (!flag) {
            CustomException.cast("添加课程失败");
        }

        // 向课程营销表写入数据
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(addCourseDto, courseMarket);
        // 设置主键
        courseMarket.setId(courseBase.getId());
        // 保存营销信息
        saveCourseMarket(courseMarket);

        // 返回全部信息
        return getCourseBaseInfoById(courseBase.getId());
    }

    @Override
    public CourseBaseInfoDto queryCourseById(Long id) {
        return getCourseBaseInfoById(id);
    }

    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, UpdateCourseDto updateCourseDto) {

        // 查询该信息是否存在
        CourseBase courseBase = this.getById(updateCourseDto.getId());
        if (courseBase == null) {
            CustomException.cast("该记录为空");
        }

        if (!Objects.equals(courseBase.getCompanyId(), companyId)) {
            CustomException.cast("只能修改所在机构的课程");
        }

        BeanUtils.copyProperties(updateCourseDto, courseBase);
        courseBase.setCompanyId(companyId);
        courseBase.setChangeDate(LocalDateTime.now());
        // todo: add setChangPeople
        // 修改基本信息表
        this.updateById(courseBase);

        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(updateCourseDto, courseMarket);
        // 修改课程营销信息
        courseMarketMapper.updateById(courseMarket);

        return getCourseBaseInfoById(updateCourseDto.getId());
    }

    @Override
    public void deleteCourseBase(Long id) {
        // 删除CourseBase表中记录
        this.removeById(id);
        // 删除CourseMarket表中记录
        courseMarketMapper.deleteById(id);
        // 删除教师表中记录
        LambdaQueryWrapper<CourseTeacher> lqw1 = new LambdaQueryWrapper<CourseTeacher>()
                .eq(CourseTeacher::getCourseId, id);
        courseTeacherService.remove(lqw1);
        // 删除教学计划表
        teachPlanService.query()
                .eq("course_id", id)
                .list()
                .forEach(item -> {
                    teachPlanService.deleteTeachplanById(item.getId());
                });

    }

    // 保存营销信息
    private void saveCourseMarket(CourseMarket courseMarket) {
        // 参数合法性校验
        String charge = courseMarket.getCharge();
        if (charge.isEmpty()) {
            CustomException.cast("收费方式不能为空");
        }
        // 如果课程收费，价格没有填写也抛出异常
        if ("201001".equals(charge)) {
            if (courseMarket.getPrice() == null || courseMarket.getPrice() <= 0) {
                CustomException.cast("课程价格不为空且大于0");
            }
        }

        int count;
        CourseMarket cm = courseMarketMapper.selectById(courseMarket.getId());
        if (cm == null) {
            // 插入
            courseMarketMapper.insert(courseMarket);
        } else {
            // 更新
            courseMarketMapper.updateById(courseMarket);
        }
    }

    // 查询课程信息
    public CourseBaseInfoDto getCourseBaseInfoById(Long courseId) {
        // 从基本信息表中查询
        CourseBase courseBaseInfo = this.getById(courseId);
        if (courseBaseInfo == null) {
            CustomException.cast("课程信息获取异常");
        }

        // 从营销信息表中查询
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        if (courseMarket == null) {
            CustomException.cast("课程营销信息获取异常");
        }

        // 组装数据
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBaseInfo, courseBaseInfoDto);
        BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);

        // 设置分类名称（该字段数据表中没有）
        String mtName = courseCategoryMapper.selectById(courseBaseInfoDto.getMt()).getName();
        String stName = courseCategoryMapper.selectById(courseBaseInfoDto.getSt()).getName();
        courseBaseInfoDto.setMtName(mtName);
        courseBaseInfoDto.setStName(stName);

        return courseBaseInfoDto;
    }
}
