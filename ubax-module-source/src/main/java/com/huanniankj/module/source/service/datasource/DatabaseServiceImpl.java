package com.huanniankj.module.source.service.datasource;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.source.controller.database.vo.DatabasePageReqVO;
import com.huanniankj.module.source.controller.database.vo.DatabaseRespVO;
import com.huanniankj.module.source.controller.database.vo.DatabaseSaveReqVO;
import com.huanniankj.module.source.controller.database.vo.DatabaseTestReqVO;
import com.huanniankj.module.source.convert.datasource.DatabaseSourceConvert;
import com.huanniankj.module.source.dal.dataobject.database.DatabaseDO;
import com.huanniankj.module.source.dal.mysql.database.DatabaseMapper;
import com.huanniankj.module.source.enums.datasource.DatabaseTypeEnum;
import com.huanniankj.module.source.framework.datasource.core.DataSourceManager;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.huanniankj.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.huanniankj.module.source.enums.ErrorCodeConstants.*;

/**
 * 数据库数据源服务实现
 *
 * @author zhaoff
 */
@Service
@Slf4j
public class DatabaseServiceImpl implements DatabaseService {

    @Resource
    private DatabaseMapper databaseMapper;

    @Resource
    private DataSourceManager dataSourceManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDatabaseSource(DatabaseSaveReqVO saveReqVO) {
        // 校验名称唯一性
        validateDatabaseSourceNameUnique(null, saveReqVO.getName());
        // 转换
        DatabaseDO databaseSource = DatabaseSourceConvert.INSTANCE.convert(saveReqVO);
        // 新增时状态默认为异常，需通过测试连接来更新状态
        databaseSource.setStatus(1);
        // 校验主机地址和端口连通性
        validateHostAndPort(databaseSource);
        // 保存
        databaseMapper.insert(databaseSource);
        // 测试数据库连接
        testConnection(databaseSource.getId());
        // 创建并缓存数据源连接池
        dataSourceManager.createAndCacheDataSource(databaseSource);
        return databaseSource.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDatabaseSource(DatabaseSaveReqVO saveReqVO) {
        // 校验数据源存在
        validateDatabaseSourceExists(saveReqVO.getId());
        // 校验名称唯一性
        validateDatabaseSourceNameUnique(saveReqVO.getId(), saveReqVO.getName());
        // 更新配置
        DatabaseDO updateObj = DatabaseSourceConvert.INSTANCE.convert(saveReqVO);
        // 如果密码为空或为脱敏值，则不更新密码字段（保留原密码）
        if (updateObj.getPassword() == null || updateObj.getPassword().isEmpty()
                || "******".equals(updateObj.getPassword())) {
            DatabaseDO existingConfig = databaseMapper.selectById(saveReqVO.getId());
            updateObj.setPassword(existingConfig.getPassword());
        }
        // 校验主机地址和端口连通性
        validateHostAndPort(updateObj);
        databaseMapper.updateById(updateObj);
        testConnection(updateObj.getId());
        // 刷新数据源连接池
        dataSourceManager.refreshDataSource(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDatabaseSource(Long id) {
        // 校验数据源存在
        validateDatabaseSourceExists(id);
        // 删除配置
        databaseMapper.deleteById(id);
        // 移除数据源连接池
        dataSourceManager.removeDataSource(id);
    }

    @Override
    public DatabaseRespVO getDatabaseSource(Long id) {
        DatabaseDO config = databaseMapper.selectById(id);
        if (config == null) {
            throw exception(SOURCE_DATASOURCE_NOT_EXISTS);
        }
        return convertToRespVO(config);
    }

    @Override
    public PageResult<DatabaseRespVO> getDatabaseSourcePage(DatabasePageReqVO pageReqVO) {
        PageResult<DatabaseDO> pageResult = databaseMapper.selectPage(pageReqVO);
        List<DatabaseRespVO> list = pageResult.getList().stream()
                .map(this::convertToRespVO)
                .toList();
        return new PageResult<>(list, pageResult.getTotal());
    }

    @Override
    public Boolean testConnection(Long id) {
        DatabaseDO databaseSource = databaseMapper.selectById(id);
        if (databaseSource == null) {
            throw exception(SOURCE_DATASOURCE_NOT_EXISTS);
        }

        boolean success = dataSourceManager.testConnection(databaseSource);
        // 根据测试结果自动更新状态：0-正常(连接成功) 1-异常(连接失败)
        Integer newStatus = success ? 0 : 1;
        if (!newStatus.equals(databaseSource.getStatus())) {
            databaseSource.setStatus(newStatus);
            databaseMapper.updateById(databaseSource);
        }
        if (!success) {
            log.warn("数据源连接测试失败: id={}, name={}", id, databaseSource.getName());
        }
        return success;
    }

    @Override
    public Boolean testConnectionByParams(DatabaseTestReqVO reqVO) {
        // 如果传入了 ID，使用已保存的配置测试
        if (reqVO.getId() != null) {
            return testConnection(reqVO.getId());
        }
        // 使用传入的连接参数构建临时 DO 进行测试
        DatabaseDO tempConfig = new DatabaseDO();
        tempConfig.setId(-1L); // 临时 ID
        tempConfig.setDbType(reqVO.getDbType());
        tempConfig.setHost(reqVO.getHost());
        tempConfig.setPort(reqVO.getPort());
        tempConfig.setDatabase(reqVO.getDatabase());
        tempConfig.setUsername(reqVO.getUsername());
        tempConfig.setPassword(reqVO.getPassword());
        tempConfig.setProtocol(reqVO.getProtocol());
        tempConfig.setSslCertPath(reqVO.getSslCertPath());
        tempConfig.setSslKeyPath(reqVO.getSslKeyPath());
        tempConfig.setSslCaPath(reqVO.getSslCaPath());
        tempConfig.setConnectionParams(reqVO.getConnectionParams());
        tempConfig.setMaxPoolSize(5); // 测试时使用较小的连接池
        tempConfig.setConnectionTimeout(10000L); // 测试时使用较短超时

        boolean success = dataSourceManager.testConnection(tempConfig);
        if (!success) {
            log.warn("数据源连接测试失败: host={}:{}, database={}", reqVO.getHost(), reqVO.getPort(), reqVO.getDatabase());
        }
        return success;
    }

    /**
     * 校验数据源是否存在
     *
     * @param id 数据源 ID
     */
    private void validateDatabaseSourceExists(Long id) {
        DatabaseDO databaseSource = databaseMapper.selectById(id);
        if (databaseSource == null) {
            throw exception(SOURCE_DATASOURCE_NOT_EXISTS);
        }
    }

    /**
     * 校验数据源名称唯一性
     *
     * @param id   数据源 ID
     * @param name 数据源名称
     */
    private void validateDatabaseSourceNameUnique(Long id, String name) {
        DatabaseDO existDatabase = databaseMapper.selectByName(name);
        if (existDatabase != null && !existDatabase.getId().equals(id)) {
            throw exception(SOURCE_DATASOURCE_NAME_DUPLICATE);
        }
    }

    /**
     * 校验主机地址和端口连通性
     * <p>
     * 通过尝试建立 TCP 连接来验证主机和端口是否可达，
     * 连接超时设为 5 秒，避免长时间阻塞。
     *
     * @param databaseSource 数据源配置
     */
    private void validateHostAndPort(DatabaseDO databaseSource) {
        String host = databaseSource.getHost();
        Integer port = databaseSource.getPort();
        if (host == null || host.isEmpty() || port == null) {
            throw exception(DATASOURCE_CONNECTION_FAILED);
        }
        try (java.net.Socket socket = new java.net.Socket()) {
            socket.connect(new java.net.InetSocketAddress(host, port), 5000);
        } catch (Exception e) {
            log.warn("数据源主机地址和端口校验失败: host={}:{}, error={}", host, port, e.getMessage());
            throw exception(DATASOURCE_CONNECTION_FAILED);
        }
    }

    /**
     * 将 DO 转换为 RespVO，并填充数据库类型名称，密码脱敏
     *
     * @param databaseSource DO 对象
     * @return RespVO 对象
     */
    private DatabaseRespVO convertToRespVO(DatabaseDO databaseSource) {
        DatabaseRespVO respVO = DatabaseSourceConvert.INSTANCE.convert(databaseSource);
        // 填充数据库类型名称
        DatabaseTypeEnum dbType = DatabaseTypeEnum.getByCode(databaseSource.getDbType());
        if (dbType != null) {
            respVO.setDbTypeName(dbType.getName());
        }
        // 密码脱敏
        if (respVO.getPassword() != null && !respVO.getPassword().isEmpty()) {
            respVO.setPassword("******");
        }
        return respVO;
    }

}
