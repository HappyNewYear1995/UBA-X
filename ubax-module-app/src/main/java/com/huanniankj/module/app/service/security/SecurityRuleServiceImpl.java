package com.huanniankj.module.app.service.security;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.common.util.object.BeanUtils;
import com.huanniankj.module.app.controller.securityrule.vo.SecurityRulePageReqVO;
import com.huanniankj.module.app.controller.securityrule.vo.SecurityRuleSaveReqVO;
import com.huanniankj.module.app.dal.dataobject.security.SecurityRuleDO;
import com.huanniankj.module.app.dal.mysql.security.SecurityRuleMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

import static com.huanniankj.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.huanniankj.module.app.enums.ErrorCodeConstants.SECURITY_RULE_NAME_DUPLICATE;
import static com.huanniankj.module.app.enums.ErrorCodeConstants.SECURITY_RULE_NOT_EXISTS;

/**
 * 安全检测规则 Service 实现类
 *
 * @author zhaoff
 */
@Service
@Validated
public class SecurityRuleServiceImpl implements SecurityRuleService {

    @Resource
    private SecurityRuleMapper securityRuleMapper;

    @Override
    public Long createSecurityRule(SecurityRuleSaveReqVO createReqVO) {
        // 校验名称唯一
        validateNameUnique(null, createReqVO.getName());
        // 插入
        SecurityRuleDO securityRule = BeanUtils.toBean(createReqVO, SecurityRuleDO.class);
        securityRuleMapper.insert(securityRule);
        return securityRule.getId();
    }

    @Override
    public void updateSecurityRule(SecurityRuleSaveReqVO updateReqVO) {
        // 校验存在
        validateSecurityRuleExists(updateReqVO.getId());
        // 校验名称唯一
        validateNameUnique(updateReqVO.getId(), updateReqVO.getName());
        // 更新
        SecurityRuleDO updateObj = BeanUtils.toBean(updateReqVO, SecurityRuleDO.class);
        securityRuleMapper.updateById(updateObj);
    }

    @Override
    public void deleteSecurityRule(Long id) {
        // 校验存在
        validateSecurityRuleExists(id);
        // 删除
        securityRuleMapper.deleteById(id);
    }

    @Override
    public SecurityRuleDO getSecurityRule(Long id) {
        return securityRuleMapper.selectById(id);
    }

    @Override
    public PageResult<SecurityRuleDO> getSecurityRulePage(SecurityRulePageReqVO pageReqVO) {
        return securityRuleMapper.selectPage(pageReqVO, pageReqVO.getName(),
                pageReqVO.getDetectionType(), pageReqVO.getSeverity(), null, pageReqVO.getEnabled());
    }

    @Override
    public void updateSecurityRuleEnabled(Long id, Boolean enabled) {
        // 校验存在
        validateSecurityRuleExists(id);
        // 更新
        SecurityRuleDO updateObj = new SecurityRuleDO();
        updateObj.setId(id);
        updateObj.setEnabled(enabled);
        securityRuleMapper.updateById(updateObj);
    }

    @Override
    public void updateSecurityRuleTrigger(Long id) {
        // 校验存在
        SecurityRuleDO securityRule = validateSecurityRuleExists(id);
        // 更新
        SecurityRuleDO updateObj = new SecurityRuleDO();
        updateObj.setId(id);
        updateObj.setTriggerCount(securityRule.getTriggerCount() != null ? securityRule.getTriggerCount() + 1 : 1L);
        updateObj.setLastTriggeredTime(LocalDateTime.now());
        securityRuleMapper.updateById(updateObj);
    }

    private SecurityRuleDO validateSecurityRuleExists(Long id) {
        SecurityRuleDO securityRule = securityRuleMapper.selectById(id);
        if (securityRule == null) {
            throw exception(SECURITY_RULE_NOT_EXISTS);
        }
        return securityRule;
    }

    private void validateNameUnique(Long id, String name) {
        SecurityRuleDO securityRule = securityRuleMapper.selectByName(name).stream().findFirst().orElse(null);
        if (securityRule == null) {
            return;
        }
        if (id == null) {
            throw exception(SECURITY_RULE_NAME_DUPLICATE);
        }
        if (!securityRule.getId().equals(id)) {
            throw exception(SECURITY_RULE_NAME_DUPLICATE);
        }
    }

}
