package com.yruns.content.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * UpdateCourseDto
 */
@Data
public class UpdateCourseDto extends AddCourseDto {

    @ApiModelProperty("课程id")
    @NotNull
    private Long Id;
}
