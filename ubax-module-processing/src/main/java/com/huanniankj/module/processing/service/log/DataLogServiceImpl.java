package com.huanniankj.module.processing.service.log;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.common.util.object.BeanUtils;
import com.huanniankj.module.processing.controller.log.vo.DataLogPageReqVO;
import com.huanniankj.module.processing.controller.log.vo.DataLogSaveReqVO;
import com.huanniankj.module.processing.dal.dataobject.log.DataLogDO;
import com.huanniankj.module.processing.dal.mysql.log.DataLogMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static com.huanniankj.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.huanniankj.module.processing.enums.ErrorCodeConstants.*;

/**
 * 数据日志 Service 实现类
 *
 * @author zhaoff
 */
@Service
@Validated
public class DataLogServiceImpl implements DataLogService {

    @Resource
    private DataLogMapper dataLogMapper;

    @Override
    public Long createDataLog(DataLogSaveReqVO createReqVO) {
        // 插入
        DataLogDO dataLog = BeanUtils.toBean(createReqVO, DataLogDO.class);
        dataLogMapper.insert(dataLog);
        return dataLog.getId();
    }

    @Override
    public void deleteDataLog(Long id) {
        // 校验存在
        validateDataLogExists(id);
        // 删除
        dataLogMapper.deleteById(id);
    }

    @Override
    public DataLogDO getDataLog(Long id) {
        return dataLogMapper.selectById(id);
    }

    @Override
    public PageResult<DataLogDO> getDataLogPage(DataLogPageReqVO pageReqVO) {
        return dataLogMapper.selectPage(pageReqVO);
    }

    private void validateDataLogExists(Long id) {
        if (id == null) {
            return;
        }
        if (dataLogMapper.selectById(id) == null) {
            throw exception(DATA_LOG_NOT_EXISTS);
        }
    }

}
