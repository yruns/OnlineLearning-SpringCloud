package com.yruns.content.model.dto;

import com.yruns.content.model.pojo.CourseCategory;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * CourseCategoryDto
 */
@Data
public class CourseCategoryDto extends CourseCategory implements Serializable {

    List<CourseCategory> childrenTreeNodes;
}
