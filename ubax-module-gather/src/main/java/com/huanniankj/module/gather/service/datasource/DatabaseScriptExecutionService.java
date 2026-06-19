package com.huanniankj.module.gather.service.datasource;

import com.huanniankj.module.gather.controller.database.vo.DatabaseProcedureReqVO;
import com.huanniankj.module.gather.controller.database.vo.DatabaseSqlExecuteRespVO;

import java.util.List;

/**
 * SQL 执行引擎服务接口
 * <p>
 * 绕过 ORM 框架，直接封装 JDBC 底层 API，提供对原生 SQL、视图查询及存储过程的统一调用接口。
 *
 * @author zhaoff
 */
public interface DatabaseScriptExecutionService {

    /**
     * 执行 SQL 语句 (支持 SELECT/INSERT/UPDATE/DELETE)
     *
     * @param databaseId 数据源 ID
     * @param sql        SQL 语句
     * @return 执行结果
     */
    DatabaseSqlExecuteRespVO executeSql(Long databaseId, String sql);

    /**
     * 执行存储过程
     *
     * @param reqVO 存储过程请求参数
     * @return 执行结果
     */
    DatabaseSqlExecuteRespVO executeProcedure(DatabaseProcedureReqVO reqVO);

    /**
     * 执行视图查询
     * <p>
     * 视图在 SQL 层面等同于普通表，直接执行 SELECT 语句即可。
     *
     * @param databaseId 数据源 ID
     * @param viewName   视图名称
     * @return 查询结果
     */
    DatabaseSqlExecuteRespVO executeViewQuery(Long databaseId, String viewName);

    /**
     * 批量执行 SQL 语句
     *
     * @param databaseId 数据源 ID
     * @param sqlList    SQL 语句列表
     * @return 执行结果列表
     */
    List<DatabaseSqlExecuteRespVO> executeBatchSql(Long databaseId, List<String> sqlList);

}
