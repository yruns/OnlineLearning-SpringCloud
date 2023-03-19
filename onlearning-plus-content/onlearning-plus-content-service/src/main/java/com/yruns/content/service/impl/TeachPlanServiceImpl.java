package com.yruns.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yruns.base.exception.CustomException;
import com.yruns.content.mapper.TeachplanMapper;
import com.yruns.content.mapper.TeachplanMediaMapper;
import com.yruns.content.model.dto.AddTeachplanDto;
import com.yruns.content.model.dto.BindTeachplanMediaDto;
import com.yruns.content.model.dto.TeachplanDto;
import com.yruns.content.model.pojo.Teachplan;
import com.yruns.content.model.pojo.TeachplanMedia;
import com.yruns.content.service.TeachPlanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * TeachPlanServiceImpl
 */
@Service
public class TeachPlanServiceImpl extends ServiceImpl<TeachplanMapper, Teachplan> implements TeachPlanService {

    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;

    @Override
    public List<TeachplanDto> selectTreeNodes(Long id) {
        List<TeachplanDto> teachplanDtos = this.baseMapper.selectTreeNodes(id);

        // 从teach_media表中查出完整的media信息
        teachplanDtos.forEach(teachplanDto -> {
            List<TeachplanDto> teachPlanTreeNodes = teachplanDto.getTeachPlanTreeNodes();
            teachPlanTreeNodes.forEach(teachplanDtoNode -> {
                TeachplanMedia teachPlanMedia = teachplanDtoNode.getTeachPlanMedia();
                if (teachPlanMedia != null) {
                    Long teachMediaId = teachPlanMedia.getId();
                    TeachplanMedia teachplanMedia = teachplanMediaMapper.selectById(teachMediaId);
                    teachplanDtoNode.setTeachPlanMedia(teachplanMedia);
                }
            });
        });

        return teachplanDtos;
    }


    /**
     * 新增/修改/保存课程计划
     * @param addTeachplanDto
     * @return
     */
    @Override
    @Transactional
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

    @Override
    @Transactional
    public void deleteTeachplanById(Long id) {
        this.removeById(id);
        // 如果章节存在媒体资源也一并删除
        teachplanMediaMapper.deleteById(id);
        // 如果存在子小节也删除
        ArrayList<Long> deleteIds = new ArrayList<>();
        this.query().eq("parentid", id).list().forEach(item -> {
            deleteIds.add(item.getId());
        });
        this.removeByIds(deleteIds);
    }

    @Override
    @Transactional
    public void moveUp(Long id) {
        Teachplan teachplan = this.getById(id);
        // 查询前一个章节的orderBy字段
        // select orderBy from teachplan where parentid = ? and orderBy < ? orderBy orderby desc limit 1
        Teachplan preTeachplan = this.query()
                .eq("parentid", teachplan.getParentid())
                .lt("orderby", teachplan.getOrderby())
                .orderByDesc("orderby")
                .last(" limit 1")
                .one();

        if (preTeachplan == null) {
            // 已经在最上面了
            CustomException.cast("该章节已在最顶部");
        }

        // 互换orderby
        Integer preOrderBy = preTeachplan.getOrderby();
        preTeachplan.setOrderby(teachplan.getOrderby());
        teachplan.setOrderby(preOrderBy);

        // 更新互换后的记录
        this.updateById(teachplan);
        this.updateById(preTeachplan);
    }

    @Override
    @Transactional
    public void bindTeachplanMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {
        // 查出课程计划
        Teachplan teachplan = this.getById(bindTeachplanMediaDto.getTeachplanId());
        if (teachplan == null) {
            CustomException.cast("该课程计划不存在");
        }

        if (teachplan.getGrade() != 2) {
            CustomException.cast("只允许向第二季教学计划绑定媒资文件");
        }

        Long courseId = teachplan.getCourseId();

        // 先删除原有记录
        LambdaQueryWrapper<TeachplanMedia> lqw = new LambdaQueryWrapper<>();
        lqw.eq(TeachplanMedia::getMediaId, bindTeachplanMediaDto.getMediaId())
           .eq(TeachplanMedia::getTeachplanId, bindTeachplanMediaDto.getTeachplanId());
        teachplanMediaMapper.delete(lqw);

        TeachplanMedia teachplanMedia = new TeachplanMedia();
        BeanUtils.copyProperties(bindTeachplanMediaDto, teachplanMedia);
        // TeachplanMedia和BindTeachplanMediaDto mediafilename名称不一致导致该字段拷贝失败
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        teachplanMedia.setCourseId(courseId);
        teachplanMedia.setCreateDate(LocalDateTime.now());

        teachplanMediaMapper.insert(teachplanMedia);
    }

    @Override
    @Transactional
    public void moveDown(Long id) {
        Teachplan teachplan = this.getById(id);
        // 查询后一个章节的orderBy字段
        // SELECT * FROM teachplan WHERE parentid = 266 AND orderby > 1  ORDER BY orderby ASC LIMIT 1
        List<Teachplan> list = this.query()
                .eq("parentid", teachplan.getParentid())
                .gt("orderby", teachplan.getOrderby())
                .orderByAsc("orderby")
                .list();

        if (list.isEmpty()) {
            // 已经在最下面了
            CustomException.cast("该章节已在最底部");
        }

        Teachplan nextTeachplan = list.get(0);

        // 互换orderby
        Integer preOrderBy = nextTeachplan.getOrderby();
        nextTeachplan.setOrderby(teachplan.getOrderby());
        teachplan.setOrderby(preOrderBy);

        // 更新互换后的记录
        this.updateById(teachplan);
        this.updateById(nextTeachplan);
    }
}
