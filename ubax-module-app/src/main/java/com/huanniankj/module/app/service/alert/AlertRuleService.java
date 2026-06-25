package com.huanniankj.module.app.service.alert;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.app.controller.alertrule.vo.AlertRulePageReqVO;
import com.huanniankj.module.app.controller.alertrule.vo.AlertRuleSaveReqVO;
import com.huanniankj.module.app.dal.dataobject.alert.AlertRuleDO;

/**
 * 告警规则 Service 接口
 *
 * @author zhaoff
 */
public interface AlertRuleService {

    /**
     * 创建告警规则
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createAlertRule(AlertRuleSaveReqVO createReqVO);

    /**
     * 更新告警规则
     *
     * @param updateReqVO 更新信息
     */
    void updateAlertRule(AlertRuleSaveReqVO updateReqVO);

    /**
     * 删除告警规则
     *
     * @param id 编号
     */
    void deleteAlertRule(Long id);

    /**
     * 获得告警规则
     *
     * @param id 编号
     * @return 告警规则
     */
    AlertRuleDO getAlertRule(Long id);

    /**
     * 获得告警规则分页
     *
     * @param pageReqVO 分页查询
     * @return 告警规则分页
     */
    PageResult<AlertRuleDO> getAlertRulePage(AlertRulePageReqVO pageReqVO);

    /**
     * 更新告警规则启用状态
     *
     * @param id      编号
     * @param enabled 是否启用
     */
    void updateAlertRuleEnabled(Long id, Boolean enabled);

    /**
     * 更新告警规则触发信息（递增触发次数并设置最后触发时间）
     *
     * @param id 编号
     */
    void updateAlertRuleTrigger(Long id);

}
