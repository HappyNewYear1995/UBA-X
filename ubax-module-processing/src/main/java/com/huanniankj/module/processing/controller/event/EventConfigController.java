package com.huanniankj.module.processing.controller.event;

import com.huanniankj.framework.common.pojo.CommonResult;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.common.util.object.BeanUtils;
import com.huanniankj.module.processing.controller.event.vo.EventConfigPageReqVO;
import com.huanniankj.module.processing.controller.event.vo.EventConfigRespVO;
import com.huanniankj.module.processing.controller.event.vo.EventConfigSaveReqVO;
import com.huanniankj.module.processing.dal.dataobject.event.EventConfigDO;
import com.huanniankj.module.processing.service.event.EventConfigService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

import java.util.Collections;
import java.util.List;

import static com.huanniankj.framework.common.pojo.CommonResult.success;

/**
 * 事件配置
 *
 * @author zhaoff
 */
@Tag(name = "事件配置")
@RestController
@RequestMapping("/processing/event-config")
@Validated
public class EventConfigController {

    @Resource
    private EventConfigService eventConfigService;

    @Resource
    private ObjectMapper objectMapper;

    @PostMapping("/create")
    @Operation(summary = "创建事件配置")
    @PreAuthorize("@ss.hasPermission('processing:event-config:create')")
    public CommonResult<Long> createEventConfig(@Valid @RequestBody EventConfigSaveReqVO createReqVO) {
        EventConfigDO eventConfig = BeanUtils.toBean(createReqVO, EventConfigDO.class);
        // List<Long> → JSON String
        eventConfig.setDataSourceIds(toJsonString(createReqVO.getDataSourceIds()));
        eventConfigService.createEventConfig(eventConfig);
        return success(eventConfig.getId());
    }

    @PutMapping("/update")
    @Operation(summary = "更新事件配置")
    public CommonResult<Boolean> updateEventConfig(@Valid @RequestBody EventConfigSaveReqVO updateReqVO) {
        EventConfigDO updateObj = BeanUtils.toBean(updateReqVO, EventConfigDO.class);
        // List<Long> → JSON String
        updateObj.setDataSourceIds(toJsonString(updateReqVO.getDataSourceIds()));
        eventConfigService.updateEventConfig(updateObj);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除事件配置")
    @Parameter(name = "id", description = "事件配置 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('processing:event-config:delete')")
    public CommonResult<Boolean> deleteEventConfig(@RequestParam("id") Long id) {
        eventConfigService.deleteEventConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得事件配置")
    @Parameter(name = "id", description = "事件配置 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('processing:event-config:query')")
    public CommonResult<EventConfigRespVO> getEventConfig(@RequestParam("id") Long id) {
        EventConfigDO eventConfig = eventConfigService.getEventConfig(id);
        return success(toRespVO(eventConfig));
    }

    @GetMapping("/page")
    @Operation(summary = "获得事件配置分页")
    @PreAuthorize("@ss.hasPermission('processing:event-config:query')")
    public CommonResult<PageResult<EventConfigRespVO>> getEventConfigPage(@Validated EventConfigPageReqVO pageReqVO) {
        PageResult<EventConfigDO> pageResult = eventConfigService.getEventConfigPage(pageReqVO);
        return success(toRespVOPage(pageResult));
    }

    @GetMapping("/list")
    @Operation(summary = "获取所有启用的事件配置列表", description = "主要用于前端的下拉选项")
    public CommonResult<List<EventConfigRespVO>> getEventConfigList() {
        List<EventConfigDO> list = eventConfigService.getEventConfigList();
        return success(list.stream().map(this::toRespVO).toList());
    }

    // ===== 转换辅助方法 =====

    private EventConfigRespVO toRespVO(EventConfigDO eventConfig) {
        EventConfigRespVO respVO = BeanUtils.toBean(eventConfig, EventConfigRespVO.class);
        if (respVO != null) {
            respVO.setDataSourceIds(parseJsonList(eventConfig.getDataSourceIds()));
        }
        return respVO;
    }

    private PageResult<EventConfigRespVO> toRespVOPage(PageResult<EventConfigDO> pageResult) {
        List<EventConfigRespVO> list = pageResult.getList().stream().map(this::toRespVO).toList();
        return new PageResult<>(list, pageResult.getTotal());
    }

    private String toJsonString(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(ids);
        } catch (Exception e) {
            return null;
        }
    }

    private List<Long> parseJsonList(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<Long>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

}
