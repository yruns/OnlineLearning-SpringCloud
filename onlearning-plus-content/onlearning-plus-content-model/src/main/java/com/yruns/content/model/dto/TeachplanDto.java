package com.yruns.content.model.dto;

import com.yruns.content.model.pojo.Teachplan;
import com.yruns.content.model.pojo.TeachplanMedia;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * TeachplanDto
 */
@Data
@ToString
public class TeachplanDto extends Teachplan {


    //与媒资管理的信息
    private TeachplanMedia teachPlanMedia;

    //小章节list
    private List<TeachplanDto> teachPlanTreeNodes;
}
