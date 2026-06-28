package com.huanniankj.module.source.controller.database;

import com.huanniankj.framework.common.pojo.CommonResult;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.source.controller.database.vo.*;
import com.huanniankj.module.source.service.datasource.DatabaseScriptExecutionService;
import com.huanniankj.module.source.service.datasource.DatabaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.huanniankj.framework.common.pojo.CommonResult.success;

/**
 * 数据库数据源控制层
 *
 * @author zhaoff
 */
@Slf4j
@Tag(name = "数据库数据源管理", description = "数据库数据源配置与 SQL 执行引擎")
@RestController
@RequestMapping("/source/database")
@Validated
public class DatabaseController {

    @Resource
    private DatabaseService databaseService;

    @Resource
    private DatabaseScriptExecutionService databaseScriptExecutionService;

    @PostMapping("/create")
    @Operation(summary = "创建数据库数据源", description = "新增数据库连接配置")
    @PreAuthorize("@ss.hasPermission('source:database:create')")
    public CommonResult<Long> createDatabaseSource(@Validated @RequestBody DatabaseSaveReqVO saveReqVO) {
        return success(databaseService.createDatabaseSource(saveReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新数据库数据源", description = "修改数据库连接配置")
    @PreAuthorize("@ss.hasPermission('source:database:update')")
    public CommonResult<Boolean> updateDatabaseSource(@Validated @RequestBody DatabaseSaveReqVO saveReqVO) {
        databaseService.updateDatabaseSource(saveReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除数据库数据源", description = "删除指定数据库数据源")
    @Parameter(name = "id", description = "数据源 ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('source:database:delete')")
    public CommonResult<Boolean> deleteDatabaseSource(@RequestParam("id") Long id) {
        databaseService.deleteDatabaseSource(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取数据库数据源详情", description = "查看指定数据源的配置信息")
    @Parameter(name = "id", description = "数据源 ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('source:database:query')")
    public CommonResult<DatabaseRespVO> getDatabaseSource(@RequestParam("id") Long id) {
        return success(databaseService.getDatabaseSource(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获取数据库数据源分页", description = "分页查询数据库数据源列表")
    @PreAuthorize("@ss.hasPermission('source:database:query')")
    public CommonResult<PageResult<DatabaseRespVO>> getDatabaseSourcePage(@Validated DatabasePageReqVO pageReqVO) {
        return success(databaseService.getDatabaseSourcePage(pageReqVO));
    }

    @PostMapping("/test")
    @Operation(summary = "测试数据源连接", description = "测试指定数据源的连接是否可用")
    @PreAuthorize("@ss.hasPermission('source:database:query')")
    public CommonResult<Boolean> testConnection(@Validated @RequestBody DatabaseTestReqVO reqVO) {
        return success(databaseService.testConnectionByParams(reqVO));
    }

    @PostMapping("/sql/execute")
    @Operation(summary = "执行 SQL 语句", description = "在指定数据源上执行 SQL (支持 SELECT/INSERT/UPDATE/DELETE)")
    @PreAuthorize("@ss.hasPermission('source:database:execute')")
    public CommonResult<DatabaseSqlExecuteRespVO> executeSql(@Validated @RequestBody DatabaseSqlExecuteReqVO reqVO) {
        return success(databaseScriptExecutionService.executeSql(reqVO.getDatabaseId(), reqVO.getSql()));
    }

    @PostMapping("/sql/execute-batch")
    @Operation(summary = "批量执行 SQL 语句", description = "在指定数据源上批量执行多条 SQL")
    @PreAuthorize("@ss.hasPermission('source:database:execute')")
    public CommonResult<List<DatabaseSqlExecuteRespVO>> executeBatchSql(
            @RequestParam("databaseId") Long databaseId,
            @RequestBody List<String> sqlList) {
        return success(databaseScriptExecutionService.executeBatchSql(databaseId, sqlList));
    }

    @PostMapping("/sql/execute-procedure")
    @Operation(summary = "执行存储过程", description = "在指定数据源上调用存储过程")
    @PreAuthorize("@ss.hasPermission('source:database:execute')")
    public CommonResult<DatabaseSqlExecuteRespVO> executeProcedure(@Validated @RequestBody DatabaseProcedureReqVO reqVO) {
        return success(databaseScriptExecutionService.executeProcedure(reqVO));
    }

    @PostMapping("/sql/execute-view")
    @Operation(summary = "执行视图查询", description = "在指定数据源上查询视图数据")
    @PreAuthorize("@ss.hasPermission('source:database:execute')")
    public CommonResult<DatabaseSqlExecuteRespVO> executeViewQuery(
            @RequestParam("databaseId") Long databaseId,
            @RequestParam("viewName") String viewName) {
        return success(databaseScriptExecutionService.executeViewQuery(databaseId, viewName));
    }

}
