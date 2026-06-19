package com.huanniankj.module.gather.service.datasource;

import com.huanniankj.module.gather.controller.database.vo.DatabaseProcedureReqVO;
import com.huanniankj.module.gather.controller.database.vo.DatabaseSqlExecuteRespVO;
import com.huanniankj.module.gather.dal.dataobject.database.DatabaseDO;
import com.huanniankj.module.gather.dal.mysql.database.DatabaseMapper;
import com.huanniankj.module.gather.framework.datasource.core.DataSourceManager;
import com.huanniankj.module.gather.framework.datasource.core.SqlSecurityValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.huanniankj.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.huanniankj.module.gather.enums.ErrorCodeConstants.DATASOURCE_NOT_INITIALIZED;
import static com.huanniankj.module.gather.enums.ErrorCodeConstants.SOURCE_DATASOURCE_NOT_EXISTS;

/**
 * SQL 执行引擎服务实现
 * <p>
 * 使用 JDBC 底层 API 执行 SQL，支持多结果集处理和存储过程调用。
 *
 * @author zhaoff
 */
@Slf4j
@Service
public class DatabaseScriptExecutionServiceImpl implements DatabaseScriptExecutionService {

    private final DatabaseMapper databaseMapper;

    private final DataSourceManager dataSourceManager;

    private final SqlSecurityValidator sqlSecurityValidator;

    public DatabaseScriptExecutionServiceImpl(DatabaseMapper databaseMapper,
                                              DataSourceManager dataSourceManager,
                                              SqlSecurityValidator sqlSecurityValidator) {
        this.databaseMapper = databaseMapper;
        this.dataSourceManager = dataSourceManager;
        this.sqlSecurityValidator = sqlSecurityValidator;
    }

    @Override
    public DatabaseSqlExecuteRespVO executeSql(Long databaseId, String sql) {
        // 1. SQL 安全校验
        if (!sqlSecurityValidator.isSafe(sql)) {
            log.warn("SQL 安全校验拦截: databaseId={}, sql={}", databaseId, sql);
            DatabaseSqlExecuteRespVO errorResp = new DatabaseSqlExecuteRespVO();
            errorResp.setSuccess(false);
            errorResp.setErrorMessage("SQL 安全校验失败，禁止执行高危操作");
            return errorResp;
        }

        // 2. 获取数据源
        DatabaseDO config = databaseMapper.selectById(databaseId);
        if (config == null) {
            throw exception(SOURCE_DATASOURCE_NOT_EXISTS);
        }

        DataSource dataSource = dataSourceManager.getOrCreateDataSource(config);
        if (dataSource == null) {
            throw exception(DATASOURCE_NOT_INITIALIZED);
        }

        // 3. 执行 SQL
        long startTime = System.currentTimeMillis();
        DatabaseSqlExecuteRespVO respVO = new DatabaseSqlExecuteRespVO();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            boolean isResultSet = stmt.execute(sql);

            if (isResultSet) {
                // SELECT 查询，处理结果集
                List<Map<String, Object>> results = new ArrayList<>();
                try (ResultSet rs = stmt.getResultSet()) {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            row.put(metaData.getColumnLabel(i), rs.getObject(i));
                        }
                        results.add(row);
                    }
                }
                respVO.setResults(results);
                respVO.setSuccess(true);
            } else {
                // INSERT/UPDATE/DELETE，返回影响行数
                int affectedRows = stmt.getUpdateCount();
                respVO.setAffectedRows(affectedRows);
                respVO.setSuccess(true);
            }

            long costTime = System.currentTimeMillis() - startTime;
            respVO.setCostTime(costTime);
            log.info("SQL 执行成功: databaseId={}, costTime={}ms", databaseId, costTime);

        } catch (SQLException e) {
            log.error("SQL 执行失败: databaseId={}, sql={}, error={}", databaseId, sql, e.getMessage(), e);
            respVO.setSuccess(false);
            respVO.setErrorMessage("SQL 执行失败: " + e.getMessage());
            respVO.setCostTime(System.currentTimeMillis() - startTime);
        }

        return respVO;
    }

    @Override
    public DatabaseSqlExecuteRespVO executeProcedure(DatabaseProcedureReqVO reqVO) {
        DatabaseDO config = databaseMapper.selectById(reqVO.getDatabaseId());
        if (config == null) {
            throw exception(SOURCE_DATASOURCE_NOT_EXISTS);
        }

        DataSource dataSource = dataSourceManager.getOrCreateDataSource(config);
        if (dataSource == null) {
            throw exception(DATASOURCE_NOT_INITIALIZED);
        }

        long startTime = System.currentTimeMillis();
        DatabaseSqlExecuteRespVO respVO = new DatabaseSqlExecuteRespVO();

        // 构建存储过程调用语句: {call procedure_name(?, ?, ...)}
        int inputParamCount = reqVO.getInputParams() != null ? reqVO.getInputParams().size() : 0;
        int outputParamCount = reqVO.getOutputParamNames() != null ? reqVO.getOutputParamNames().size() : 0;
        int totalParams = inputParamCount + outputParamCount;

        StringBuilder callSql = new StringBuilder("{call ");
        callSql.append(reqVO.getProcedureName()).append("(");
        for (int i = 0; i < totalParams; i++) {
            if (i > 0) {
                callSql.append(",");
            }
            callSql.append("?");
        }
        callSql.append(")}");

        try (Connection conn = dataSource.getConnection();
             CallableStatement cstmt = conn.prepareCall(callSql.toString())) {

            // 设置输入参数
            if (reqVO.getInputParams() != null) {
                for (int i = 0; i < reqVO.getInputParams().size(); i++) {
                    cstmt.setObject(i + 1, reqVO.getInputParams().get(i));
                }
            }

            // 注册输出参数
            if (reqVO.getOutputParamNames() != null) {
                for (int i = 0; i < reqVO.getOutputParamNames().size(); i++) {
                    cstmt.registerOutParameter(inputParamCount + i + 1, Types.VARCHAR);
                }
            }

            // 执行存储过程
            boolean hasResultSet = cstmt.execute();

            // 处理结果集 (支持多结果集)
            List<Map<String, Object>> results = new ArrayList<>();
            if (hasResultSet) {
                try (ResultSet rs = cstmt.getResultSet()) {
                    results = extractResultSet(rs);
                }
            }

            // 遍历更多结果集
            while (cstmt.getMoreResults()) {
                try (ResultSet rs = cstmt.getResultSet()) {
                    results.addAll(extractResultSet(rs));
                }
            }

            respVO.setResults(results);
            respVO.setSuccess(true);
            respVO.setCostTime(System.currentTimeMillis() - startTime);
            log.info("存储过程执行成功: databaseId={}, procedure={}", reqVO.getDatabaseId(), reqVO.getProcedureName());

        } catch (SQLException e) {
            log.error("存储过程执行失败: databaseId={}, procedure={}, error={}",
                    reqVO.getDatabaseId(), reqVO.getProcedureName(), e.getMessage(), e);
            respVO.setSuccess(false);
            respVO.setErrorMessage("存储过程执行失败: " + e.getMessage());
            respVO.setCostTime(System.currentTimeMillis() - startTime);
        }

        return respVO;
    }

    @Override
    public DatabaseSqlExecuteRespVO executeViewQuery(Long databaseId, String viewName) {
        String sql = "SELECT * FROM " + viewName;
        return executeSql(databaseId, sql);
    }

    @Override
    public List<DatabaseSqlExecuteRespVO> executeBatchSql(Long databaseId, List<String> sqlList) {
        List<DatabaseSqlExecuteRespVO> results = new ArrayList<>();
        for (String sql : sqlList) {
            results.add(executeSql(databaseId, sql));
        }
        return results;
    }

    /**
     * 提取 ResultSet 数据为 Map 列表
     *
     * @param rs ResultSet 对象
     * @return Map 列表
     * @throws SQLException SQL 异常
     */
    private List<Map<String, Object>> extractResultSet(ResultSet rs) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        if (rs == null) {
            return results;
        }

        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(metaData.getColumnLabel(i), rs.getObject(i));
            }
            results.add(row);
        }
        return results;
    }
}
