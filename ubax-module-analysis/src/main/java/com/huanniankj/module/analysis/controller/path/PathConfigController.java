package com.huanniankj.module.analysis.controller.path;

import com.huanniankj.framework.common.pojo.CommonResult;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.analysis.controller.path.vo.PathConfigPageReqVO;
import com.huanniankj.module.analysis.controller.path.vo.PathConfigRespVO;
import com.huanniankj.module.analysis.controller.path.vo.PathConfigSaveReqVO;
import com.huanniankj.module.analysis.controller.path.vo.PathResultRespVO;
import com.huanniankj.module.analysis.service.path.PathConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static com.huanniankj.framework.common.pojo.CommonResult.success;

/**
 * 路径分析配置控制层
 *
 * @author zhaoff
 */
@Tag(name = "路径分析配置", description = "路径分析配置的增删改查及结果查询")
@RestController
@RequestMapping("/analysis/path-config")
@Validated
public class PathConfigController {

    @Resource
    private PathConfigService pathConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建路径分析配置")
    @PermitAll
    public CommonResult<Long> createPathConfig(@Validated @RequestBody PathConfigSaveReqVO saveReqVO) {
        return success(pathConfigService.createPathConfig(saveReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新路径分析配置")
    @PermitAll
    public CommonResult<Boolean> updatePathConfig(@Validated @RequestBody PathConfigSaveReqVO saveReqVO) {
        pathConfigService.updatePathConfig(saveReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除路径分析配置")
    @Parameter(name = "id", description = "配置 ID", required = true)
    @PermitAll
    public CommonResult<Boolean> deletePathConfig(@RequestParam("id") Long id) {
        pathConfigService.deletePathConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取路径分析配置详情")
    @Parameter(name = "id", description = "配置 ID", required = true)
    @PermitAll
    public CommonResult<PathConfigRespVO> getPathConfig(@RequestParam("id") Long id) {
        return success(pathConfigService.getPathConfig(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获取路径分析配置分页")
    @PermitAll
    public CommonResult<PageResult<PathConfigRespVO>> getPathConfigPage(@Validated PathConfigPageReqVO pageReqVO) {
        return success(pathConfigService.getPathConfigPage(pageReqVO));
    }

    @GetMapping("/results")
    @Operation(summary = "查询路径分析结果")
    @PermitAll
    public CommonResult<List<PathResultRespVO>> getPathResults(
            @RequestParam("configId") Long configId,
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return success(pathConfigService.getPathResults(configId, startDate, endDate));
    }

}
