package com.huanniankj.module.app.service.security;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.app.controller.securityevent.vo.SecurityEventHandleReqVO;
import com.huanniankj.module.app.controller.securityevent.vo.SecurityEventPageReqVO;
import com.huanniankj.module.app.dal.dataobject.security.SecurityEventDO;

/**
 * 安全检测事件 Service 接口
 *
 * @author zhaoff
 */
public interface SecurityEventService {

    /**
     * 创建安全检测事件（内部调用，检测触发时使用）
     *
     * @param event 安全检测事件
     * @return 编号
     */
    Long createSecurityEvent(SecurityEventDO event);

    /**
     * 处理安全检测事件
     *
     * @param handleReqVO 处理信息
     */
    void handleSecurityEvent(SecurityEventHandleReqVO handleReqVO);

    /**
     * 获得安全检测事件
     *
     * @param id 编号
     * @return 安全检测事件
     */
    SecurityEventDO getSecurityEvent(Long id);

    /**
     * 获得安全检测事件分页
     *
     * @param pageReqVO 分页查询
     * @return 安全检测事件分页
     */
    PageResult<SecurityEventDO> getSecurityEventPage(SecurityEventPageReqVO pageReqVO);

}
