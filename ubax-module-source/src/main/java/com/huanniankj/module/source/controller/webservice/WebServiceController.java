package com.huanniankj.module.source.controller.webservice;

import com.huanniankj.framework.common.pojo.CommonResult;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.source.controller.database.vo.DatabaseSqlExecuteRespVO;
import com.huanniankj.module.source.controller.database.vo.WebServiceExecuteReqVO;
import com.huanniankj.module.source.controller.webservice.vo.*;
import com.huanniankj.module.source.service.datasource.WebServiceExecutionService;
import com.huanniankj.module.source.service.webservice.WebServiceSourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.huanniankj.framework.common.pojo.CommonResult.success;

/**
 * WebService 数据源控制层
 *
 * @author zhaoff
 */
@Slf4j
@Tag(name = "WebService 数据源管理", description = "WebService 数据源配置与请求执行")
@RestController
@RequestMapping("/source/webservice")
@Validated
public class WebServiceController {

    @Resource
    private WebServiceSourceService webServiceSourceService;

    @Resource
    private WebServiceExecutionService webServiceExecutionService;

    @PostMapping("/create")
    @Operation(summary = "创建 WebService 数据源", description = "新增 WebService 接口配置")
    @PreAuthorize("@ss.hasPermission('source:webservice:create')")
    public CommonResult<Long> createWebServiceSource(@Validated @RequestBody WebServiceSaveReqVO saveReqVO) {
        return success(webServiceSourceService.createWebServiceSource(saveReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新 WebService 数据源", description = "修改 WebService 接口配置")
    @PreAuthorize("@ss.hasPermission('source:webservice:update')")
    public CommonResult<Boolean> updateWebServiceSource(@Validated @RequestBody WebServiceSaveReqVO saveReqVO) {
        webServiceSourceService.updateWebServiceSource(saveReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除 WebService 数据源", description = "删除指定 WebService 数据源")
    @Parameter(name = "id", description = "数据源 ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('source:webservice:delete')")
    public CommonResult<Boolean> deleteWebServiceSource(@RequestParam("id") Long id) {
        webServiceSourceService.deleteWebServiceSource(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取 WebService 数据源详情", description = "查看指定数据源的配置信息")
    @Parameter(name = "id", description = "数据源 ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('source:webservice:query')")
    public CommonResult<WebServiceRespVO> getWebServiceSource(@RequestParam("id") Long id) {
        return success(webServiceSourceService.getWebServiceSource(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获取 WebService 数据源分页", description = "分页查询 WebService 数据源列表")
    @PreAuthorize("@ss.hasPermission('source:webservice:query')")
    public CommonResult<PageResult<WebServiceRespVO>> getWebServiceSourcePage(@Validated WebServicePageReqVO pageReqVO) {
        return success(webServiceSourceService.getWebServiceSourcePage(pageReqVO));
    }

    @PostMapping("/test")
    @Operation(summary = "测试 WebService 连接", description = "测试指定 WebService 数据源的连接是否可用")
    @Parameter(name = "id", description = "数据源 ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('source:webservice:query')")
    public CommonResult<Boolean> testConnection(@RequestParam("id") Long id) {
        return success(webServiceSourceService.testConnection(id));
    }

    @PostMapping("/execute")
    @Operation(summary = "执行 WebService 请求", description = "调用 WebService 接口获取数据（支持 REST/SOAP）")
    @PreAuthorize("@ss.hasPermission('source:webservice:execute')")
    public CommonResult<DatabaseSqlExecuteRespVO> executeWebService(@Validated @RequestBody WebServiceExecuteReqVO reqVO) {
        return success(webServiceExecutionService.executeWebService(reqVO));
    }

}
