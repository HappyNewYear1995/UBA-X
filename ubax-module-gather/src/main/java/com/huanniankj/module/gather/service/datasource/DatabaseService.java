package com.huanniankj.module.gather.service.datasource;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.gather.controller.database.vo.DatabasePageReqVO;
import com.huanniankj.module.gather.controller.database.vo.DatabaseRespVO;
import com.huanniankj.module.gather.controller.database.vo.DatabaseSaveReqVO;
import com.huanniankj.module.gather.controller.database.vo.DatabaseTestReqVO;

/**
 * 数据库数据源服务接口
 *
 * @author zhaoff
 */
public interface DatabaseService {

    /**
     * 创建数据库数据源
     *
     * @param saveReqVO 保存请求
     * @return 数据源 ID
     */
    Long createDatabaseSource(DatabaseSaveReqVO saveReqVO);

    /**
     * 更新数据库数据源
     *
     * @param saveReqVO 保存请求
     */
    void updateDatabaseSource(DatabaseSaveReqVO saveReqVO);

    /**
     * 删除数据库数据源
     *
     * @param id 数据源 ID
     */
    void deleteDatabaseSource(Long id);

    /**
     * 获取数据库数据源详情
     *
     * @param id 数据源 ID
     * @return 数据源响应
     */
    DatabaseRespVO getDatabaseSource(Long id);

    /**
     * 获取数据库数据源分页
     *
     * @param pageReqVO 分页查询条件
     * @return 分页结果
     */
    PageResult<DatabaseRespVO> getDatabaseSourcePage(DatabasePageReqVO pageReqVO);

    /**
     * 测试数据源连接（通过已保存的数据源 ID）
     *
     * @param id 数据源 ID
     * @return 连接是否成功
     */
    Boolean testConnection(Long id);

    /**
     * 测试数据源连接（通过连接参数，用于新增/编辑时校验）
     *
     * @param reqVO 连接参数
     * @return 连接是否成功
     */
    Boolean testConnectionByParams(DatabaseTestReqVO reqVO);

}
