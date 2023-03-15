package com.yruns.content.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yruns.content.mapper.CourseCategoryMapper;
import com.yruns.content.model.dto.CourseCategoryDto;
import com.yruns.content.model.pojo.CourseCategory;
import com.yruns.content.service.CourseCategoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CourseCategoryServiceImpl
 */
@Service
public class CourseCategoryServiceImpl extends ServiceImpl<CourseCategoryMapper, CourseCategory> implements CourseCategoryService {

    @Override
    public List<CourseCategoryDto> getCategory(String id) {

        List<CourseCategoryDto> courseCategoryDtos = this.baseMapper.selectTreeNodes(id);
        // 按接口封装（找到每个节点的子节点）
        Map<String, CourseCategoryDto> dtomap = courseCategoryDtos.stream()
                .filter(item -> !id.equals(item.getId()))   // 排除根节点
                .collect(Collectors.toMap(
                CourseCategoryDto::getId, value -> value, (key1, key2) -> key2));

        ArrayList<CourseCategoryDto> result = new ArrayList<>();

        courseCategoryDtos.stream().filter(item -> !id.equals(item.getId()))
                .forEach(item -> {
                    if (item.getParentid().equals(id)) {
                        result.add(item);
                    }

                    // 将子节点放入父节点中
                    CourseCategoryDto parent = dtomap.get(item.getParentid());
                    if (parent != null) {
                        if (parent.getChildrenTreeNodes() == null) {
                            // 当前父节点的子节点集合为空，new List
                            parent.setChildrenTreeNodes(new ArrayList<>());
                        }
                        // 直接加入
                        parent.getChildrenTreeNodes().add(item);
                    }

                });

        return result;
    }
}
