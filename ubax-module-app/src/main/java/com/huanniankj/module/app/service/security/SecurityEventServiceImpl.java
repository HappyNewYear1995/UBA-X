package com.huanniankj.module.app.service.security;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.app.controller.securityevent.vo.SecurityEventHandleReqVO;
import com.huanniankj.module.app.controller.securityevent.vo.SecurityEventPageReqVO;
import com.huanniankj.module.app.dal.dataobject.security.SecurityEventDO;
import com.huanniankj.module.app.dal.mysql.security.SecurityEventMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

import static com.huanniankj.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.huanniankj.module.app.enums.ErrorCodeConstants.SECURITY_EVENT_NOT_EXISTS;

/**
 * 安全检测事件 Service 实现类
 *
 * @author zhaoff
 */
@Service
@Validated
public class SecurityEventServiceImpl implements SecurityEventService {

    @Resource
    private SecurityEventMapper securityEventMapper;

    @Override
    public Long createSecurityEvent(SecurityEventDO event) {
        securityEventMapper.insert(event);
        return event.getId();
    }

    @Override
    public void handleSecurityEvent(SecurityEventHandleReqVO handleReqVO) {
        // 校验存在
        validateSecurityEventExists(handleReqVO.getId());
        // 更新
        SecurityEventDO updateObj = new SecurityEventDO();
        updateObj.setId(handleReqVO.getId());
        updateObj.setHandled(true);
        updateObj.setHandleTime(LocalDateTime.now());
        updateObj.setHandleRemark(handleReqVO.getHandleRemark());
        securityEventMapper.updateById(updateObj);
    }

    @Override
    public SecurityEventDO getSecurityEvent(Long id) {
        return securityEventMapper.selectById(id);
    }

    @Override
    public PageResult<SecurityEventDO> getSecurityEventPage(SecurityEventPageReqVO pageReqVO) {
        return securityEventMapper.selectPage(pageReqVO, pageReqVO.getRuleId(),
                pageReqVO.getDetectionType(), pageReqVO.getSeverity(),
                pageReqVO.getHandled(), pageReqVO.getSourceIp());
    }

    private void validateSecurityEventExists(Long id) {
        if (securityEventMapper.selectById(id) == null) {
            throw exception(SECURITY_EVENT_NOT_EXISTS);
        }
    }

}
