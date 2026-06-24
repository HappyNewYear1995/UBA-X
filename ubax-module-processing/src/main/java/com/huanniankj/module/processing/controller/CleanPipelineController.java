package com.huanniankj.module.processing.controller;

import com.huanniankj.framework.common.pojo.CommonResult;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.common.util.object.BeanUtils;
import com.huanniankj.module.processing.controller.vo.CleanPipelinePageReqVO;
import com.huanniankj.module.processing.controller.vo.CleanPipelineRespVO;
import com.huanniankj.module.processing.controller.vo.CleanPipelineSaveReqVO;
import com.huanniankj.module.processing.dal.dataobject.CleanPipelineDO;
import com.huanniankj.module.processing.service.CleanPipelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.huanniankj.framework.common.pojo.CommonResult.success;

/**
 * 清洗管道
 *
 * @author zhaoff
 */
@Tag(name = "清洗管道")
@RestController
@RequestMapping("/collect/clean-pipeline")
@Validated
public class CleanPipelineController {

    @Resource
    private CleanPipelineService cleanPipelineService;

    @PostMapping("/create")
    @Operation(summary = "创建清洗管道")
    @PreAuthorize("@ss.hasPermission('collect:clean-pipeline:create')")
    public CommonResult<Long> createCleanPipeline(@Valid @RequestBody CleanPipelineSaveReqVO createReqVO) {
        return success(cleanPipelineService.createCleanPipeline(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新清洗管道")
    @PreAuthorize("@ss.hasPermission('collect:clean-pipeline:update')")
    public CommonResult<Boolean> updateCleanPipeline(@Valid @RequestBody CleanPipelineSaveReqVO updateReqVO) {
        cleanPipelineService.updateCleanPipeline(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除清洗管道")
    @Parameter(name = "id", description = "管道 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('collect:clean-pipeline:delete')")
    public CommonResult<Boolean> deleteCleanPipeline(@RequestParam("id") Long id) {
        cleanPipelineService.deleteCleanPipeline(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得清洗管道")
    @Parameter(name = "id", description = "管道 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('collect:clean-pipeline:query')")
    public CommonResult<CleanPipelineRespVO> getCleanPipeline(@RequestParam("id") Long id) {
        CleanPipelineDO cleanPipeline = cleanPipelineService.getCleanPipeline(id);
        return success(BeanUtils.toBean(cleanPipeline, CleanPipelineRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得清洗管道分页")
    @PreAuthorize("@ss.hasPermission('collect:clean-pipeline:query')")
    public CommonResult<PageResult<CleanPipelineRespVO>> getCleanPipelinePage(@Validated CleanPipelinePageReqVO pageReqVO) {
        PageResult<CleanPipelineDO> pageResult = cleanPipelineService.getCleanPipelinePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, CleanPipelineRespVO.class));
    }

    @GetMapping("/list-all-simple")
    @Operation(summary = "获取所有启用的清洗管道精简列表", description = "主要用于前端的下拉选项")
    public CommonResult<List<CleanPipelineRespVO>> getSimpleCleanPipelineList() {
        List<CleanPipelineDO> list = cleanPipelineService.getCleanPipelineList();
        return success(BeanUtils.toBean(list, CleanPipelineRespVO.class));
    }

}
