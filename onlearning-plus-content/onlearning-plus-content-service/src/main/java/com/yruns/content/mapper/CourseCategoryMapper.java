package com.yruns.content.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yruns.content.model.dto.CourseCategoryDto;
import com.yruns.content.model.pojo.CourseCategory;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 课程分类 Mapper 接口
 * </p>
 *
 * @author yruns
 */
public interface CourseCategoryMapper extends BaseMapper<CourseCategory> {

    //使用递归查询分类
    List<CourseCategoryDto> selectTreeNodes(String id);

}
