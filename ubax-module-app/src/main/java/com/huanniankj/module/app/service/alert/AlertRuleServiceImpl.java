package com.huanniankj.module.app.service.alert;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.common.util.object.BeanUtils;
import com.huanniankj.module.app.controller.alertrule.vo.AlertRulePageReqVO;
import com.huanniankj.module.app.controller.alertrule.vo.AlertRuleSaveReqVO;
import com.huanniankj.module.app.dal.dataobject.alert.AlertRuleDO;
import com.huanniankj.module.app.dal.mysql.alert.AlertRuleMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

import static com.huanniankj.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.huanniankj.module.app.enums.ErrorCodeConstants.ALERT_RULE_NAME_DUPLICATE;
import static com.huanniankj.module.app.enums.ErrorCodeConstants.ALERT_RULE_NOT_EXISTS;

/**
 * 告警规则 Service 实现类
 *
 * @author zhaoff
 */
@Service
@Validated
public class AlertRuleServiceImpl implements AlertRuleService {

    @Resource
    private AlertRuleMapper alertRuleMapper;

    @Override
    public Long createAlertRule(AlertRuleSaveReqVO createReqVO) {
        // 校验名称唯一
        validateNameUnique(null, createReqVO.getName());
        // 插入
        AlertRuleDO alertRule = BeanUtils.toBean(createReqVO, AlertRuleDO.class);
        alertRuleMapper.insert(alertRule);
        return alertRule.getId();
    }

    @Override
    public void updateAlertRule(AlertRuleSaveReqVO updateReqVO) {
        // 校验存在
        validateAlertRuleExists(updateReqVO.getId());
        // 校验名称唯一
        validateNameUnique(updateReqVO.getId(), updateReqVO.getName());
        // 更新
        AlertRuleDO updateObj = BeanUtils.toBean(updateReqVO, AlertRuleDO.class);
        alertRuleMapper.updateById(updateObj);
    }

    @Override
    public void deleteAlertRule(Long id) {
        // 校验存在
        validateAlertRuleExists(id);
        // 删除
        alertRuleMapper.deleteById(id);
    }

    @Override
    public AlertRuleDO getAlertRule(Long id) {
        return alertRuleMapper.selectById(id);
    }

    @Override
    public PageResult<AlertRuleDO> getAlertRulePage(AlertRulePageReqVO pageReqVO) {
        return alertRuleMapper.selectPage(pageReqVO, pageReqVO.getName(),
                pageReqVO.getAlertType(), null, pageReqVO.getEnabled());
    }

    @Override
    public void updateAlertRuleEnabled(Long id, Boolean enabled) {
        // 校验存在
        validateAlertRuleExists(id);
        // 更新
        AlertRuleDO updateObj = new AlertRuleDO();
        updateObj.setId(id);
        updateObj.setEnabled(enabled);
        alertRuleMapper.updateById(updateObj);
    }

    @Override
    public void updateAlertRuleTrigger(Long id) {
        // 校验存在
        AlertRuleDO alertRule = validateAlertRuleExists(id);
        // 更新
        AlertRuleDO updateObj = new AlertRuleDO();
        updateObj.setId(id);
        updateObj.setTriggerCount(alertRule.getTriggerCount() != null ? alertRule.getTriggerCount() + 1 : 1L);
        updateObj.setLastTriggeredTime(LocalDateTime.now());
        alertRuleMapper.updateById(updateObj);
    }

    private AlertRuleDO validateAlertRuleExists(Long id) {
        AlertRuleDO alertRule = alertRuleMapper.selectById(id);
        if (alertRule == null) {
            throw exception(ALERT_RULE_NOT_EXISTS);
        }
        return alertRule;
    }

    private void validateNameUnique(Long id, String name) {
        AlertRuleDO alertRule = alertRuleMapper.selectByName(name).stream().findFirst().orElse(null);
        if (alertRule == null) {
            return;
        }
        if (id == null) {
            throw exception(ALERT_RULE_NAME_DUPLICATE);
        }
        if (!alertRule.getId().equals(id)) {
            throw exception(ALERT_RULE_NAME_DUPLICATE);
        }
    }

}
