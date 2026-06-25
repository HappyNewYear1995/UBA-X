package com.huanniankj.module.app.service.security;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.app.controller.securityrule.vo.SecurityRulePageReqVO;
import com.huanniankj.module.app.controller.securityrule.vo.SecurityRuleSaveReqVO;
import com.huanniankj.module.app.dal.dataobject.security.SecurityRuleDO;

/**
 * 安全检测规则 Service 接口
 *
 * @author zhaoff
 */
public interface SecurityRuleService {

    /**
     * 创建安全检测规则
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSecurityRule(SecurityRuleSaveReqVO createReqVO);

    /**
     * 更新安全检测规则
     *
     * @param updateReqVO 更新信息
     */
    void updateSecurityRule(SecurityRuleSaveReqVO updateReqVO);

    /**
     * 删除安全检测规则
     *
     * @param id 编号
     */
    void deleteSecurityRule(Long id);

    /**
     * 获得安全检测规则
     *
     * @param id 编号
     * @return 安全检测规则
     */
    SecurityRuleDO getSecurityRule(Long id);

    /**
     * 获得安全检测规则分页
     *
     * @param pageReqVO 分页查询
     * @return 安全检测规则分页
     */
    PageResult<SecurityRuleDO> getSecurityRulePage(SecurityRulePageReqVO pageReqVO);

    /**
     * 更新安全检测规则启用状态
     *
     * @param id      编号
     * @param enabled 是否启用
     */
    void updateSecurityRuleEnabled(Long id, Boolean enabled);

    /**
     * 更新安全检测规则触发信息（递增触发次数并设置最后触发时间）
     *
     * @param id 编号
     */
    void updateSecurityRuleTrigger(Long id);

}
