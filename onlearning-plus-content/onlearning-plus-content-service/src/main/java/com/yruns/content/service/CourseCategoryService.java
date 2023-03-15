package com.yruns.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yruns.content.model.dto.CourseCategoryDto;
import com.yruns.content.model.pojo.CourseCategory;

import java.util.List;

public interface CourseCategoryService extends IService<CourseCategory> {

    List<CourseCategoryDto> getCategory(String id);
}
