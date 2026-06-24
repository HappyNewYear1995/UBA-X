package com.huanniankj.module.processing.service;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.processing.controller.vo.ErrorLogPageReqVO;
import com.huanniankj.module.processing.controller.vo.ErrorLogSaveReqVO;
import com.huanniankj.module.processing.dal.dataobject.ErrorLogDO;

/**
 * 异常日志服务接口
 *
 * @author zhaoff
 */
public interface ErrorLogService {

    /**
     * 创建异常日志
     *
     * @param createReqVO 创建信息
     * @return 日志 ID
     */
    Long createErrorLog(ErrorLogSaveReqVO createReqVO);

    /**
     * 更新异常日志
     *
     * @param updateReqVO 更新信息
     */
    void updateErrorLog(ErrorLogSaveReqVO updateReqVO);

    /**
     * 删除异常日志
     *
     * @param id 日志 ID
     */
    void deleteErrorLog(Long id);

    /**
     * 获得异常日志
     *
     * @param id 日志 ID
     * @return 异常日志
     */
    ErrorLogDO getErrorLog(Long id);

    /**
     * 获得异常日志分页
     *
     * @param pageReqVO 分页查询
     * @return 异常日志分页
     */
    PageResult<ErrorLogDO> getErrorLogPage(ErrorLogPageReqVO pageReqVO);

}
