package com.huanniankj.module.app.service.alert;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.app.controller.alertrecord.vo.AlertRecordAckReqVO;
import com.huanniankj.module.app.controller.alertrecord.vo.AlertRecordPageReqVO;
import com.huanniankj.module.app.dal.dataobject.alert.AlertRecordDO;

/**
 * 告警记录 Service 接口
 *
 * @author zhaoff
 */
public interface AlertRecordService {

    /**
     * 创建告警记录（内部调用，告警触发时使用）
     *
     * @param record 告警记录
     * @return 编号
     */
    Long createAlertRecord(AlertRecordDO record);

    /**
     * 确认告警记录
     *
     * @param ackReqVO 确认信息
     */
    void ackAlertRecord(AlertRecordAckReqVO ackReqVO);

    /**
     * 获得告警记录
     *
     * @param id 编号
     * @return 告警记录
     */
    AlertRecordDO getAlertRecord(Long id);

    /**
     * 获得告警记录分页
     *
     * @param pageReqVO 分页查询
     * @return 告警记录分页
     */
    PageResult<AlertRecordDO> getAlertRecordPage(AlertRecordPageReqVO pageReqVO);

}
