package com.huanniankj.module.processing.service.log;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.processing.controller.log.vo.DataLogPageReqVO;
import com.huanniankj.module.processing.controller.log.vo.DataLogSaveReqVO;
import com.huanniankj.module.processing.dal.dataobject.log.DataLogDO;

/**
 * 数据日志服务接口
 *
 * @author zhaoff
 */
public interface DataLogService {

    /**
     * 创建数据日志
     *
     * @param createReqVO 创建信息
     * @return 日志 ID
     */
    Long createDataLog(DataLogSaveReqVO createReqVO);

    /**
     * 删除数据日志
     *
     * @param id 日志 ID
     */
    void deleteDataLog(Long id);

    /**
     * 获得数据日志
     *
     * @param id 日志 ID
     * @return 数据日志
     */
    DataLogDO getDataLog(Long id);

    /**
     * 获得数据日志分页
     *
     * @param pageReqVO 分页查询
     * @return 数据日志分页
     */
    PageResult<DataLogDO> getDataLogPage(DataLogPageReqVO pageReqVO);

}
