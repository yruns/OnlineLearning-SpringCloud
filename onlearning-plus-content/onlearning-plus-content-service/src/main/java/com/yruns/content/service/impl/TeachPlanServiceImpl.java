package com.yruns.content.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yruns.content.mapper.TeachplanMapper;
import com.yruns.content.model.dto.AddTeachplanDto;
import com.yruns.content.model.dto.TeachplanDto;
import com.yruns.content.model.pojo.Teachplan;
import com.yruns.content.service.TeachPlanService;
import net.bytebuddy.asm.Advice;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TeachPlanServiceImpl
 */
@Service
public class TeachPlanServiceImpl extends ServiceImpl<TeachplanMapper, Teachplan> implements TeachPlanService {


    @Override
    public List<TeachplanDto> selectTreeNodes(Long id) {
        return this.baseMapper.selectTreeNodes(id);
    }


    /**
     * 新增/修改/保存课程计划
     * @param addTeachplanDto
     * @return
     */
    @Override
    public void saveTeachplan(AddTeachplanDto addTeachplanDto) {
        // 根据课程计划id是否为空判断是新增还是修改保存
        Long id = addTeachplanDto.getId();
        if (id == null) {
            // 新增
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(addTeachplanDto, teachplan);
            // 获取当前的最大排序字段（同级节点个数 + 1）
            int nextOrderBy = this.query()
                    .eq("course_id", addTeachplanDto.getCourseId())
                    .eq("parentid", addTeachplanDto.getParentid()).count() + 1;
            teachplan.setOrderby(nextOrderBy);
            teachplan.setCreateDate(LocalDateTime.now());

            this.save(teachplan);
        } else {
            // 修改
            Teachplan teachplan = this.baseMapper.selectById(id);
            // 复制
            BeanUtils.copyProperties(addTeachplanDto, teachplan);
            teachplan.setChangeDate(LocalDateTime.now());
            this.updateById(teachplan);
        }
    }
}
