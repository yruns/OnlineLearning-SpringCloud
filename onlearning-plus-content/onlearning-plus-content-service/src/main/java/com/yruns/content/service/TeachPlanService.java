package com.yruns.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yruns.content.model.dto.AddTeachplanDto;
import com.yruns.content.model.dto.BindTeachplanMediaDto;
import com.yruns.content.model.dto.TeachplanDto;
import com.yruns.content.model.pojo.Teachplan;

import java.util.List;

public interface TeachPlanService extends IService<Teachplan> {

    List<TeachplanDto> selectTreeNodes(Long id);

    void saveTeachplan(AddTeachplanDto addTeachplanDto);

    void deleteTeachplanById(Long id);

    void moveDown(Long id);

    void moveUp(Long id);

    void bindTeachplanMedia(BindTeachplanMediaDto bindTeachplanMediaDto);
}
