package com.huanniankj.module.source.service.datasource;

import com.huanniankj.module.source.controller.database.vo.DatabaseProcedureReqVO;
import com.huanniankj.module.source.controller.database.vo.DatabaseSqlExecuteRespVO;
import com.huanniankj.module.source.dal.dataobject.database.DatabaseDO;
import com.huanniankj.module.source.dal.mysql.database.DatabaseMapper;
import com.huanniankj.module.source.framework.datasource.core.DataSourceManager;
import com.huanniankj.module.source.framework.datasource.core.SqlSecurityValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.huanniankj.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.huanniankj.module.source.enums.ErrorCodeConstants.DATASOURCE_NOT_INITIALIZED;
import static com.huanniankj.module.source.enums.ErrorCodeConstants.SOURCE_DATASOURCE_NOT_EXISTS;

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
        return executeSql(databaseId, sql, null);
    }

    @Override
    public DatabaseSqlExecuteRespVO executeSql(Long databaseId, String sql, List<Object> params) {
        // 1. SQL 安全校验
        if (!sqlSecurityValidator.isSafe(sql)) {
            log.warn("SQL 安全校验拦截: databaseId={}, sql={}", databaseId, sql);
            DatabaseSqlExecuteRespVO errorResp = new DatabaseSqlExecuteRespVO();
            errorResp.setSuccess(false);
            errorResp.setErrorMessage("SQL 安全校验失败，禁止执行高危操作");
            return errorResp;
        }

        // 2. 获取数据源
        DatabaseDO databaseSource = databaseMapper.selectById(databaseId);
        if (databaseSource == null) {
            throw exception(SOURCE_DATASOURCE_NOT_EXISTS);
        }

        DataSource dataSource = dataSourceManager.getOrCreateDataSource(databaseSource);
        if (dataSource == null) {
            throw exception(DATASOURCE_NOT_INITIALIZED);
        }

        // 3. 执行 SQL
        long startTime = System.currentTimeMillis();
        DatabaseSqlExecuteRespVO respVO = new DatabaseSqlExecuteRespVO();

        try (Connection conn = dataSource.getConnection()) {
            boolean hasParams = params != null && !params.isEmpty();

            if (hasParams) {
                // 参数化查询：使用 PreparedStatement
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    for (int i = 0; i < params.size(); i++) {
                        pstmt.setObject(i + 1, params.get(i));
                    }
                    boolean isResultSet = pstmt.execute();
                    processExecuteResult(isResultSet, pstmt, respVO);
                }
            } else {
                // 无参数查询：使用 Statement
                try (Statement stmt = conn.createStatement()) {
                    boolean isResultSet = stmt.execute(sql);
                    processExecuteResult(isResultSet, stmt, respVO);
                }
            }

            respVO.setSuccess(true);
            long costTime = System.currentTimeMillis() - startTime;
            respVO.setCostTime(costTime);
            log.info("SQL 执行成功: databaseId={}, costTime={}ms, resultSets={}, hasParams={}",
                    databaseId, costTime, respVO.getResultSetList() != null ? respVO.getResultSetList().size() : 0, hasParams);
        } catch (SQLException e) {
            log.error("SQL 执行失败: databaseId={}, sql={}, error={}", databaseId, sql, e.getMessage(), e);
            respVO.setSuccess(false);
            respVO.setErrorMessage("SQL 执行失败: " + e.getMessage());
            respVO.setCostTime(System.currentTimeMillis() - startTime);
        }

        return respVO;
    }

    /**
     * 处理执行结果（提取多结果集）
     *
     * @param isResultSet 是否为结果集
     * @param stmt        Statement 对象
     * @param respVO      响应对象
     * @throws SQLException SQL 异常
     */
    private void processExecuteResult(boolean isResultSet, Statement stmt, DatabaseSqlExecuteRespVO respVO) throws SQLException {
        List<List<Map<String, Object>>> resultSetList = new ArrayList<>();
        List<List<String>> resultSetColumns = new ArrayList<>();

        while (true) {
            if (isResultSet) {
                try (ResultSet rs = stmt.getResultSet()) {
                    ResultSetData data = extractResultSetWithColumns(rs);
                    resultSetList.add(data.rows);
                    resultSetColumns.add(data.columns);
                }
            } else {
                int updateCount = stmt.getUpdateCount();
                if (updateCount == -1) {
                    break;
                }
                respVO.setAffectedRows(updateCount);
            }
            isResultSet = stmt.getMoreResults();
        }

        // 兼容旧字段：单结果集时设置 results
        if (!resultSetList.isEmpty()) {
            respVO.setResults(resultSetList.get(0));
        }
        respVO.setResultSetList(resultSetList);
        respVO.setResultSetColumns(resultSetColumns);
    }

    @Override
    public DatabaseSqlExecuteRespVO executeProcedure(DatabaseProcedureReqVO reqVO) {
        DatabaseDO databaseSource = databaseMapper.selectById(reqVO.getDatabaseId());
        if (databaseSource == null) {
            throw exception(SOURCE_DATASOURCE_NOT_EXISTS);
        }

        DataSource dataSource = dataSourceManager.getOrCreateDataSource(databaseSource);
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

            // 处理多结果集，每个结果集单独存储
            List<List<Map<String, Object>>> resultSetList = new ArrayList<>();
            List<List<String>> resultSetColumns = new ArrayList<>();

            while (true) {
                if (hasResultSet) {
                    try (ResultSet rs = cstmt.getResultSet()) {
                        ResultSetData data = extractResultSetWithColumns(rs);
                        resultSetList.add(data.rows);
                        resultSetColumns.add(data.columns);
                    }
                } else {
                    int updateCount = cstmt.getUpdateCount();
                    if (updateCount == -1) {
                        break;
                    }
                    respVO.setAffectedRows(updateCount);
                }
                hasResultSet = cstmt.getMoreResults();
            }

            // 兼容旧字段：单结果集时设置 results
            if (!resultSetList.isEmpty()) {
                respVO.setResults(resultSetList.get(0));
            }
            respVO.setResultSetList(resultSetList);
            respVO.setResultSetColumns(resultSetColumns);

            // 读取输出参数值
            if (reqVO.getOutputParamNames() != null && !reqVO.getOutputParamNames().isEmpty()) {
                Map<String, Object> outputParamValues = new HashMap<>();
                for (int i = 0; i < reqVO.getOutputParamNames().size(); i++) {
                    String paramName = reqVO.getOutputParamNames().get(i);
                    Object paramValue = cstmt.getObject(inputParamCount + i + 1);
                    outputParamValues.put(paramName, paramValue);
                }
                respVO.setOutputParams(outputParamValues);
            }

            respVO.setSuccess(true);
            respVO.setCostTime(System.currentTimeMillis() - startTime);
            log.info("存储过程执行成功: databaseId={}, procedure={}, resultSets={}, outputParams={}",
                    reqVO.getDatabaseId(), reqVO.getProcedureName(), resultSetList.size(),
                    respVO.getOutputParams());
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
        return executeViewQuery(databaseId, viewName, null);
    }

    @Override
    public DatabaseSqlExecuteRespVO executeViewQuery(Long databaseId, String viewName, List<Object> params) {
        String sql = "SELECT * FROM " + viewName;
        return executeSql(databaseId, sql, params);
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
     * 结果集数据封装（行数据 + 列名列表）
     */
    private record ResultSetData(List<Map<String, Object>> rows, List<String> columns) {
    }

    /**
     * 提取 ResultSet 数据，同时返回行数据和列名列表
     *
     * @param rs ResultSet 对象
     * @return ResultSetData 包含行数据和列名列表
     * @throws SQLException SQL 异常
     */
    private ResultSetData extractResultSetWithColumns(ResultSet rs) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        List<String> columns = new ArrayList<>();

        if (rs == null) {
            return new ResultSetData(rows, columns);
        }

        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        // 提取列名
        for (int i = 1; i <= columnCount; i++) {
            columns.add(metaData.getColumnLabel(i));
        }

        // 提取行数据
        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(metaData.getColumnLabel(i), rs.getObject(i));
            }
            rows.add(row);
        }

        return new ResultSetData(rows, columns);
    }

}
