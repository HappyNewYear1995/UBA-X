package com.huanniankj.module.app.dal.mysql.alert;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.huanniankj.module.app.dal.dataobject.alert.AlertRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 告警记录 Mapper
 *
 * @author zhaoff
 */
@Mapper
public interface AlertRecordMapper extends BaseMapperX<AlertRecordDO> {

    default PageResult<AlertRecordDO> selectPage(com.huanniankj.framework.common.pojo.PageParam pageParam,
                                                  Long ruleId, Integer alertType, Integer alertLevel,
                                                  Integer notificationStatus, Boolean acknowledged) {
        return selectPage(pageParam, new LambdaQueryWrapperX<AlertRecordDO>()
                .eqIfPresent(AlertRecordDO::getRuleId, ruleId)
                .eqIfPresent(AlertRecordDO::getAlertType, alertType)
                .eqIfPresent(AlertRecordDO::getAlertLevel, alertLevel)
                .eqIfPresent(AlertRecordDO::getNotificationStatus, notificationStatus)
                .eqIfPresent(AlertRecordDO::getAcknowledged, acknowledged)
                .orderByDesc(AlertRecordDO::getId));
    }

    default List<AlertRecordDO> selectByRuleId(Long ruleId) {
        return selectList(AlertRecordDO::getRuleId, ruleId);
    }

    default List<AlertRecordDO> selectByAcknowledged(Boolean acknowledged) {
        return selectList(AlertRecordDO::getAcknowledged, acknowledged);
    }

}
