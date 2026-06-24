package com.huanniankj.module.infra.service.db;

import com.huanniankj.module.infra.controller.db.vo.DataSourceConfigSaveReqVO;
import com.huanniankj.module.infra.dal.dataobject.db.DataSourceConfigDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 数据源配置服务接口
 *
 * @author zhaoff
 */
public interface DataSourceConfigService {

    /**
     * 创建数据源配置
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDataSourceConfig(@Valid DataSourceConfigSaveReqVO createReqVO);

    /**
     * 更新数据源配置
     *
     * @param updateReqVO 更新信息
     */
    void updateDataSourceConfig(@Valid DataSourceConfigSaveReqVO updateReqVO);

    /**
     * 删除数据源配置
     *
     * @param id 编号
     */
    void deleteDataSourceConfig(Long id);

    /**
     * 批量删除数据源配置
     *
     * @param ids 编号列表
     */
    void deleteDataSourceConfigList(List<Long> ids);

    /**
     * 获得数据源配置
     *
     * @param id 编号
     * @return 数据源配置
     */
    DataSourceConfigDO getDataSourceConfig(Long id);

    /**
     * 获得数据源配置列表
     *
     * @return 数据源配置列表
     */
    List<DataSourceConfigDO> getDataSourceConfigList();

}
