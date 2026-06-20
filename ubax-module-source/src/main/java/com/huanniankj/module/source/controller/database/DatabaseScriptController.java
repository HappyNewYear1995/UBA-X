package com.huanniankj.module.source.controller.database;

import com.huanniankj.framework.common.pojo.CommonResult;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.source.controller.database.vo.*;
import com.huanniankj.module.source.service.datasource.DatabaseScriptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.huanniankj.framework.common.pojo.CommonResult.success;

/**
 * 数据库脚本管理控制器
 *
 * @author zhaoff
 */
@Slf4j
@Tag(name = "数据库脚本管理", description = "数据库脚本与执行管理")
@RestController
@RequestMapping("/source/database-script")
@Validated
public class DatabaseScriptController {

    @Resource
    private DatabaseScriptService databaseScriptService;

    @PostMapping("/create")
    @Operation(summary = "创建脚本", description = "新增数据库脚本")
    @PermitAll
    public CommonResult<Long> createScript(@Validated @RequestBody DatabaseScriptSaveReqVO saveReqVO) {
        return success(databaseScriptService.createScript(saveReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新脚本", description = "修改数据库脚本")
    @PermitAll
    public CommonResult<Boolean> updateScript(@Validated @RequestBody DatabaseScriptSaveReqVO saveReqVO) {
        databaseScriptService.updateScript(saveReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除脚本", description = "删除指定脚本及关联日志")
    @Parameter(name = "id", description = "脚本ID", required = true, example = "1")
    @PermitAll
    public CommonResult<Boolean> deleteScript(@RequestParam("id") Long id) {
        databaseScriptService.deleteScript(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取脚本详情", description = "查看指定脚本信息")
    @Parameter(name = "id", description = "脚本ID", required = true, example = "1")
    @PermitAll
    public CommonResult<DatabaseScriptRespVO> getScript(@RequestParam("id") Long id) {
        return success(databaseScriptService.getScript(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获取脚本分页", description = "分页查询脚本列表")
    @PermitAll
    public CommonResult<PageResult<DatabaseScriptRespVO>> getScriptPage(@Validated DatabaseScriptPageReqVO pageReqVO) {
        return success(databaseScriptService.getScriptPage(pageReqVO));
    }

    @PostMapping("/execute")
    @Operation(summary = "执行脚本", description = "执行指定脚本并可选持久化结果")
    @PermitAll
    public CommonResult<DatabaseScriptExecuteRespVO> executeScript(@Validated @RequestBody DatabaseScriptExecuteReqVO reqVO) {
        return success(databaseScriptService.executeScript(reqVO));
    }

    @GetMapping("/log/page")
    @Operation(summary = "获取执行日志分页", description = "分页查询脚本执行日志")
    @PermitAll
    public CommonResult<PageResult<DatabaseScriptLogRespVO>> getScriptLogPage(@Validated DatabaseScriptLogPageReqVO pageReqVO) {
        return success(databaseScriptService.getScriptLogPage(pageReqVO));
    }

    @GetMapping("/log/get")
    @Operation(summary = "获取执行日志详情", description = "查看指定执行日志")
    @Parameter(name = "id", description = "日志ID", required = true, example = "1")
    @PermitAll
    public CommonResult<DatabaseScriptLogRespVO> getScriptLog(@RequestParam("id") Long id) {
        return success(databaseScriptService.getScriptLog(id));
    }

    @DeleteMapping("/log/delete")
    @Operation(summary = "删除执行日志", description = "删除指定执行日志")
    @Parameter(name = "id", description = "日志ID", required = true, example = "1")
    @PermitAll
    public CommonResult<Boolean> deleteScriptLog(@RequestParam("id") Long id) {
        databaseScriptService.deleteScriptLog(id);
        return success(true);
    }

}
