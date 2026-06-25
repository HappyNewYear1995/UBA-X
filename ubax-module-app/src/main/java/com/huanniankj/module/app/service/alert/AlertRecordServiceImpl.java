package com.huanniankj.module.app.service.alert;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.app.controller.alertrecord.vo.AlertRecordAckReqVO;
import com.huanniankj.module.app.controller.alertrecord.vo.AlertRecordPageReqVO;
import com.huanniankj.module.app.dal.dataobject.alert.AlertRecordDO;
import com.huanniankj.module.app.dal.mysql.alert.AlertRecordMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

import static com.huanniankj.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.huanniankj.module.app.enums.ErrorCodeConstants.ALERT_RECORD_NOT_EXISTS;

/**
 * 告警记录 Service 实现类
 *
 * @author zhaoff
 */
@Service
@Validated
public class AlertRecordServiceImpl implements AlertRecordService {

    @Resource
    private AlertRecordMapper alertRecordMapper;

    @Override
    public Long createAlertRecord(AlertRecordDO record) {
        alertRecordMapper.insert(record);
        return record.getId();
    }

    @Override
    public void ackAlertRecord(AlertRecordAckReqVO ackReqVO) {
        // 校验存在
        AlertRecordDO record = validateAlertRecordExists(ackReqVO.getId());
        // 更新
        AlertRecordDO updateObj = new AlertRecordDO();
        updateObj.setId(ackReqVO.getId());
        updateObj.setAcknowledged(true);
        updateObj.setAcknowledgedBy(record.getAcknowledgedBy()); // 保留原确认人
        updateObj.setAcknowledgedTime(LocalDateTime.now());
        updateObj.setRemark(ackReqVO.getRemark());
        alertRecordMapper.updateById(updateObj);
    }

    @Override
    public AlertRecordDO getAlertRecord(Long id) {
        return alertRecordMapper.selectById(id);
    }

    @Override
    public PageResult<AlertRecordDO> getAlertRecordPage(AlertRecordPageReqVO pageReqVO) {
        return alertRecordMapper.selectPage(pageReqVO, pageReqVO.getRuleId(),
                pageReqVO.getAlertType(), pageReqVO.getAlertLevel(),
                pageReqVO.getNotificationStatus(), pageReqVO.getAcknowledged());
    }

    private AlertRecordDO validateAlertRecordExists(Long id) {
        AlertRecordDO record = alertRecordMapper.selectById(id);
        if (record == null) {
            throw exception(ALERT_RECORD_NOT_EXISTS);
        }
        return record;
    }

}
