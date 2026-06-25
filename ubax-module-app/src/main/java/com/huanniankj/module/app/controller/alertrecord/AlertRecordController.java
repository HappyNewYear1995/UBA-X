package com.huanniankj.module.app.controller.alertrecord;

import com.huanniankj.framework.common.pojo.CommonResult;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.common.util.object.BeanUtils;
import com.huanniankj.module.app.controller.alertrecord.vo.AlertRecordAckReqVO;
import com.huanniankj.module.app.controller.alertrecord.vo.AlertRecordPageReqVO;
import com.huanniankj.module.app.controller.alertrecord.vo.AlertRecordRespVO;
import com.huanniankj.module.app.dal.dataobject.alert.AlertRecordDO;
import com.huanniankj.module.app.service.alert.AlertRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.huanniankj.framework.common.pojo.CommonResult.success;

@Tag(name = "告警记录")
@RestController
@RequestMapping("/app/alert-record")
@Validated
public class AlertRecordController {

    @Resource
    private AlertRecordService alertRecordService;

    @GetMapping("/get")
    @Operation(summary = "获得告警记录")
    @Parameter(name = "id", description = "告警记录 ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('app:alert-record:query')")
    public CommonResult<AlertRecordRespVO> getAlertRecord(@RequestParam("id") Long id) {
        AlertRecordDO alertRecord = alertRecordService.getAlertRecord(id);
        return success(BeanUtils.toBean(alertRecord, AlertRecordRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得告警记录分页")
    @PreAuthorize("@ss.hasPermission('app:alert-record:query')")
    public CommonResult<PageResult<AlertRecordRespVO>> getAlertRecordPage(@Valid AlertRecordPageReqVO pageReqVO) {
        PageResult<AlertRecordDO> pageResult = alertRecordService.getAlertRecordPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AlertRecordRespVO.class));
    }

    @PutMapping("/ack")
    @Operation(summary = "确认告警记录")
    @PreAuthorize("@ss.hasPermission('app:alert-record:ack')")
    public CommonResult<Boolean> ackAlertRecord(@Valid @RequestBody AlertRecordAckReqVO ackReqVO) {
        alertRecordService.ackAlertRecord(ackReqVO);
        return success(true);
    }

}
