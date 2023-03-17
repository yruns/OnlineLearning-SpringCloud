package com.yruns.content.api;

import com.yruns.content.model.dto.AddTeachplanDto;
import com.yruns.content.model.dto.TeachplanDto;
import com.yruns.content.service.TeachPlanService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * TeachPlanController
 */
@RestController
public class TeachPlanController {

    @Autowired
    private TeachPlanService teachPlanService;

    // 查询课程计划
    @GetMapping("/teachplan/{id}/tree-nodes")
    @ApiOperation("查询课程计划树形结构")
    public List<TeachplanDto> getTeachPlan(@PathVariable Long id) {

        return teachPlanService.selectTreeNodes(id);
    }

    @PostMapping("/teachplan")
    @ApiOperation("查询课程计划树形结构")
    public void saveTeachplan(@RequestBody AddTeachplanDto addTeachplanDto) {

        teachPlanService.saveTeachplan(addTeachplanDto);
    }

    @DeleteMapping("/teachplan/{id}")
    @ApiOperation("删除章节")
    public void deleteTeachplan(@PathVariable Long id) {

        teachPlanService.deleteTeachplanById(id);
    }

    @PostMapping("/teachplan/movedown/{id}")
    @ApiOperation("下移动章节")
    public void moveDown(@PathVariable Long id) {

        teachPlanService.moveDown(id);
    }

    @PostMapping("/teachplan/moveup/{id}")
    @ApiOperation("上移动章节")
    public void moveUp(@PathVariable Long id) {

        teachPlanService.moveUp(id);
    }


}
