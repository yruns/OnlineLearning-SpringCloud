package com.yruns.content.api;

import com.yruns.content.model.dto.CourseCategoryDto;
import com.yruns.content.service.CourseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * CourseCategoryController
 */
@RestController
public class CourseCategoryController {

    @Autowired
    private CourseCategoryService courseCategoryService;

    @GetMapping("/course-category/tree-nodes")
    public List<CourseCategoryDto> getCourseCategory() {

        return courseCategoryService.getCategory("1");
    }
}
