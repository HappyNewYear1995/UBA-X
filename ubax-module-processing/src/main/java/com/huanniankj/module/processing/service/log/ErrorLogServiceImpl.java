package com.huanniankj.module.processing.service.log;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.common.util.object.BeanUtils;
import com.huanniankj.module.processing.controller.log.vo.ErrorLogPageReqVO;
import com.huanniankj.module.processing.controller.log.vo.ErrorLogSaveReqVO;
import com.huanniankj.module.processing.dal.dataobject.log.ErrorLogDO;
import com.huanniankj.module.processing.dal.mysql.log.ErrorLogMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static com.huanniankj.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.huanniankj.module.processing.enums.ErrorCodeConstants.*;

/**
 * 异常日志服务实现类
 *
 * @author zhaoff
 */
@Service
@Validated
public class ErrorLogServiceImpl implements ErrorLogService {

    @Resource
    private ErrorLogMapper errorLogMapper;

    @Override
    public Long createErrorLog(ErrorLogSaveReqVO createReqVO) {
        // 插入
        ErrorLogDO errorLog = BeanUtils.toBean(createReqVO, ErrorLogDO.class);
        errorLogMapper.insert(errorLog);
        return errorLog.getId();
    }

    @Override
    public void updateErrorLog(ErrorLogSaveReqVO updateReqVO) {
        // 校验存在
        validateErrorLogExists(updateReqVO.getId());
        // 更新
        ErrorLogDO updateObj = BeanUtils.toBean(updateReqVO, ErrorLogDO.class);
        errorLogMapper.updateById(updateObj);
    }

    @Override
    public void deleteErrorLog(Long id) {
        // 校验存在
        validateErrorLogExists(id);
        // 删除
        errorLogMapper.deleteById(id);
    }

    @Override
    public ErrorLogDO getErrorLog(Long id) {
        return errorLogMapper.selectById(id);
    }

    @Override
    public PageResult<ErrorLogDO> getErrorLogPage(ErrorLogPageReqVO pageReqVO) {
        return errorLogMapper.selectPage(pageReqVO);
    }

    private void validateErrorLogExists(Long id) {
        if (id == null) {
            return;
        }
        if (errorLogMapper.selectById(id) == null) {
            throw exception(ERROR_LOG_NOT_EXISTS);
        }
    }

}
