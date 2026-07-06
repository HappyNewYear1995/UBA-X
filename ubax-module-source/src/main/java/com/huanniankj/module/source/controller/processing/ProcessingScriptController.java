package com.huanniankj.module.source.controller.processing;

import com.huanniankj.framework.common.pojo.CommonResult;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.source.controller.processing.vo.ProcessingScriptExecuteReqVO;
import com.huanniankj.module.source.controller.processing.vo.ProcessingScriptExecuteRespVO;
import com.huanniankj.module.source.controller.processing.vo.ProcessingScriptPageReqVO;
import com.huanniankj.module.source.controller.processing.vo.ProcessingScriptRespVO;
import com.huanniankj.module.source.controller.processing.vo.ProcessingScriptSaveReqVO;
import com.huanniankj.module.source.service.processing.ProcessingScriptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.huanniankj.framework.common.pojo.CommonResult.success;

/**
 * 处理脚本控制层
 *
 * @author zhaoff
 */
@Tag(name = "数据源管理 - 处理脚本")
@RestController
@RequestMapping("/source/processing-script")
@Validated
public class ProcessingScriptController {

    @Resource
    private ProcessingScriptService processingScriptService;

    @PostMapping("/create")
    @Operation(summary = "创建处理脚本")
    @PreAuthorize("@ss.hasPermission('source:processing-script:create')")
    public CommonResult<Long> createScript(@Valid @RequestBody ProcessingScriptSaveReqVO saveReqVO) {
        return success(processingScriptService.createScript(saveReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新处理脚本")
    @PreAuthorize("@ss.hasPermission('source:processing-script:update')")
    public CommonResult<Boolean> updateScript(@Valid @RequestBody ProcessingScriptSaveReqVO saveReqVO) {
        processingScriptService.updateScript(saveReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除处理脚本")
    @PreAuthorize("@ss.hasPermission('source:processing-script:delete')")
    public CommonResult<Boolean> deleteScript(@RequestParam("id") Long id) {
        processingScriptService.deleteScript(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取处理脚本详情")
    @PreAuthorize("@ss.hasPermission('source:processing-script:query')")
    public CommonResult<ProcessingScriptRespVO> getScript(@RequestParam("id") Long id) {
        return success(processingScriptService.getScript(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获取处理脚本分页")
    @PreAuthorize("@ss.hasPermission('source:processing-script:query')")
    public CommonResult<PageResult<ProcessingScriptRespVO>> getScriptPage(@Valid ProcessingScriptPageReqVO pageReqVO) {
        return success(processingScriptService.getScriptPage(pageReqVO));
    }

    @PostMapping("/execute")
    @Operation(summary = "执行处理脚本")
    @PreAuthorize("@ss.hasPermission('source:processing-script:execute')")
    public CommonResult<ProcessingScriptExecuteRespVO> executeScript(@Valid @RequestBody ProcessingScriptExecuteReqVO reqVO) {
        return success(processingScriptService.executeScript(reqVO));
    }

}
