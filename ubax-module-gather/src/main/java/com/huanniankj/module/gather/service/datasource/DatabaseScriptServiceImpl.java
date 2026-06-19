package com.huanniankj.module.gather.service.datasource;

import com.alibaba.fastjson.JSON;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.gather.controller.database.vo.*;
import com.huanniankj.module.gather.convert.script.ScriptConfigConvert;
import com.huanniankj.module.gather.dal.dataobject.database.DatabaseDO;
import com.huanniankj.module.gather.dal.dataobject.database.DatabaseScriptDO;
import com.huanniankj.module.gather.dal.dataobject.database.DatabaseScriptLogDO;
import com.huanniankj.module.gather.dal.mysql.database.DatabaseMapper;
import com.huanniankj.module.gather.dal.mysql.database.DatabaseScriptLogMapper;
import com.huanniankj.module.gather.dal.mysql.database.DatabaseScriptMapper;
import com.huanniankj.module.gather.framework.datasource.core.DataSourceManager;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.huanniankj.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.huanniankj.module.gather.enums.ErrorCodeConstants.*;

/**
 * 数据库脚本服务实现
 *
 * @author zhaoff
 */
@Service
@Slf4j
public class DatabaseScriptServiceImpl implements DatabaseScriptService {

    @Resource
    private DatabaseScriptMapper databaseScriptMapper;

    @Resource
    private DatabaseScriptLogMapper executionLogMapper;

    @Resource
    private DatabaseMapper databaseMapper;

    @Resource
    private DatabaseScriptExecutionService databaseScriptExecutionService;

    @Resource
    private DataSourceManager dataSourceManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createScript(DatabaseScriptSaveReqVO saveReqVO) {
        validateScriptCodeUnique(null, saveReqVO.getCode());
        validateDataSourceExists(saveReqVO.getDatabaseId());
        DatabaseScriptDO script = ScriptConfigConvert.INSTANCE.convert(saveReqVO);
        script.setExecuteCount(0);

        databaseScriptMapper.insert(script);
        return script.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateScript(DatabaseScriptSaveReqVO saveReqVO) {
        validateScriptExists(saveReqVO.getId());
        validateScriptCodeUnique(saveReqVO.getId(), saveReqVO.getCode());
        validateDataSourceExists(saveReqVO.getDatabaseId());

        DatabaseScriptDO updateObj = ScriptConfigConvert.INSTANCE.convert(saveReqVO);
        databaseScriptMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteScript(Long id) {
        validateScriptExists(id);
        databaseScriptMapper.deleteById(id);
        executionLogMapper.delete(DatabaseScriptLogDO::getScriptId, id);
    }

    @Override
    public DatabaseScriptRespVO getScript(Long id) {
        DatabaseScriptDO config = databaseScriptMapper.selectById(id);
        if (config == null) {
            throw exception(DATABASE_SCRIPT_NOT_EXISTS);
        }
        return convertToRespVO(config);
    }

    @Override
    public PageResult<DatabaseScriptRespVO> getScriptPage(DatabaseScriptPageReqVO pageReqVO) {
        PageResult<DatabaseScriptDO> pageResult = databaseScriptMapper.selectPage(pageReqVO);
        List<DatabaseScriptRespVO> list = pageResult.getList().stream()
                .map(this::convertToRespVO)
                .toList();
        return new PageResult<>(list, pageResult.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DatabaseScriptExecuteRespVO executeScript(DatabaseScriptExecuteReqVO reqVO) {
        DatabaseScriptDO config = databaseScriptMapper.selectById(reqVO.getScriptId());
        if (config == null) {
            throw exception(DATABASE_SCRIPT_NOT_EXISTS);
        }

        DatabaseScriptExecuteRespVO respVO = new DatabaseScriptExecuteRespVO();
        DatabaseScriptLogDO logDO = new DatabaseScriptLogDO();
        logDO.setScriptId(config.getId());
        logDO.setScriptName(config.getName());
        logDO.setScriptCode(config.getCode());
        logDO.setDatabaseId(config.getDatabaseId());
        logDO.setExecuteType("manual");
        logDO.setScriptContent(config.getScriptContent());

        long startTime = System.currentTimeMillis();

        try {
            Object executeResult = executeScriptInternal(config);
            long costTime = System.currentTimeMillis() - startTime;

            boolean persistResult = reqVO.getPersistResult() != null && reqVO.getPersistResult() == 1;
            long persistRecordCount = 0;

            if (persistResult && executeResult instanceof List<?> results) {
                persistRecordCount = persistExecuteResult(config, results);
                logDO.setPersisted(1);
            } else {
                logDO.setPersisted(0);
            }

            respVO.setSuccess(true);
            respVO.setCostTime(costTime);
            if (executeResult instanceof List<?> results) {
                respVO.setResults((List<Map<String, Object>>) executeResult);
                respVO.setResultRecordCount((long) results.size());
                logDO.setExecuteResult(JSON.toJSONString(executeResult));
                logDO.setResultRecordCount((long) results.size());
            } else if (executeResult instanceof Long affectedRows) {
                respVO.setAffectedRows(affectedRows);
                logDO.setAffectedRows(affectedRows);
            }
            respVO.setPersisted(persistResult);
            respVO.setPersistRecordCount(persistRecordCount);

            logDO.setStatus(0);
            logDO.setCostTime(costTime);

            updateScriptExecuteStats(config.getId(), 0);

            log.info("脚本执行成功: scriptId={}, costTime={}ms, recordCount={}", config.getId(), costTime, respVO.getResultRecordCount());
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;

            respVO.setSuccess(false);
            respVO.setCostTime(costTime);
            respVO.setErrorMessage(e.getMessage());

            logDO.setStatus(1);
            logDO.setErrorMessage(e.getMessage());
            logDO.setCostTime(costTime);

            updateScriptExecuteStats(config.getId(), 1);

            log.error("脚本执行失败: scriptId={}, error={}", config.getId(), e.getMessage(), e);
        }

        executionLogMapper.insert(logDO);
        return respVO;
    }

    @Override
    public PageResult<DatabaseScriptLogRespVO> getScriptLogPage(DatabaseScriptLogPageReqVO pageReqVO) {
        PageResult<DatabaseScriptLogDO> pageResult = executionLogMapper.selectPage(pageReqVO);
        return ScriptConfigConvert.INSTANCE.convertLogPage(pageResult);
    }

    @Override
    public DatabaseScriptLogRespVO getScriptLog(Long id) {
        DatabaseScriptLogDO logDO = executionLogMapper.selectById(id);
        if (logDO == null) {
            throw exception(DATABASE_SCRIPT_LOG_NOT_EXISTS);
        }
        return ScriptConfigConvert.INSTANCE.convertLog(logDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteScriptLog(Long id) {
        DatabaseScriptLogDO logDO = executionLogMapper.selectById(id);
        if (logDO == null) {
            throw exception(DATABASE_SCRIPT_LOG_NOT_EXISTS);
        }
        executionLogMapper.deleteById(id);
    }

    /**
     * 执行数据库脚本
     *
     * @param config 数据库脚本
     * @return 执行结果
     */
    private Object executeScriptInternal(DatabaseScriptDO config) {
        String scriptType = config.getScriptType();
        Long databaseId = config.getDatabaseId();
        String scriptContent = config.getScriptContent();

        return switch (scriptType) {
            case "sql" -> databaseScriptExecutionService.executeSql(databaseId, scriptContent);
            case "procedure" -> {
                DatabaseProcedureReqVO reqVO = new DatabaseProcedureReqVO();
                reqVO.setDatabaseId(databaseId);
                reqVO.setProcedureName(scriptContent);
                yield databaseScriptExecutionService.executeProcedure(reqVO);
            }
            case "view" -> databaseScriptExecutionService.executeViewQuery(databaseId, scriptContent);
            default -> throw exception(DATABASE_SCRIPT_TYPE_NOT_SUPPORTED);
        };
    }

    private long persistExecuteResult(DatabaseScriptDO config, List<?> results) {
        if (config.getResultTableName() == null || config.getResultTableName().isEmpty()) {
            throw exception(DATABASE_SCRIPT_RESULT_TABLE_NOT_CONFIGURED);
        }

        String tableName = config.getResultTableName();
        Map<String, String> fieldMapping = parseFieldMapping(config.getResultFieldMapping());

        try {
            DataSource dataSource = dataSourceManager.getDataSource(config.getDatabaseId());
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            ensureTableExists(jdbcTemplate, tableName, results, fieldMapping);

            int count = 0;
            for (Object result : results) {
                Map<String, Object> row = (Map<String, Object>) result;
                String sql = buildInsertSql(tableName, row, fieldMapping);
                jdbcTemplate.execute(sql);
                count++;
            }

            log.info("脚本执行结果已持久化: table={}, count={}", tableName, count);
            return count;

        } catch (Exception e) {
            log.error("脚本执行结果持久化失败: table={}, error={}", tableName, e.getMessage(), e);
            throw exception(DATABASE_SCRIPT_RESULT_PERSIST_ERROR, e.getMessage());
        }
    }

    private Map<String, String> parseFieldMapping(String fieldMappingJson) {
        if (fieldMappingJson == null || fieldMappingJson.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return JSON.parseObject(fieldMappingJson, Map.class);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private void ensureTableExists(JdbcTemplate jdbcTemplate, String tableName, List<?> results, Map<String, String> fieldMapping) throws SQLException {
        DataSource dataSource = jdbcTemplate.getDataSource();
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getTables(null, null, tableName.toUpperCase(), new String[]{"TABLE"})) {
                if (!rs.next()) {
                    String createSql = buildCreateTableSql(tableName, (Map<String, Object>) results.get(0), fieldMapping);
                    jdbcTemplate.execute(createSql);
                    log.info("自动创建结果表: {}", tableName);
                }
            }
        }
    }

    private String buildCreateTableSql(String tableName, Map<String, Object> sampleRow, Map<String, String> fieldMapping) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS `").append(tableName).append("` (\n");
        sb.append("    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,\n");

        for (Map.Entry<String, Object> entry : sampleRow.entrySet()) {
            String sourceField = entry.getKey();
            String targetField = fieldMapping.getOrDefault(sourceField, sourceField);
            String sqlType = getSqlType(entry.getValue());
            sb.append("    `").append(targetField).append("` ").append(sqlType).append(",\n");
        }

        sb.append("    `create_time` DATETIME\n");
        sb.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

        return sb.toString();
    }

    private String getSqlType(Object value) {
        if (value instanceof Number) {
            if (value instanceof Long || value instanceof Integer) {
                return "BIGINT";
            }
            return "DECIMAL(18,4)";
        } else if (value instanceof String) {
            return "VARCHAR(255)";
        } else if (value instanceof Date) {
            return "DATETIME";
        }
        return "TEXT";
    }

    private String buildInsertSql(String tableName, Map<String, Object> row, Map<String, String> fieldMapping) {
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();

        columns.append("INSERT INTO `").append(tableName).append("` (");
        values.append("VALUES (");

        boolean first = true;
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (!first) {
                columns.append(", ");
                values.append(", ");
            }
            String targetField = fieldMapping.getOrDefault(entry.getKey(), entry.getKey());
            columns.append("`").append(targetField).append("`");
            values.append(formatValue(entry.getValue()));
            first = false;
        }

        columns.append(", `create_time`");
        values.append(", NOW()");

        columns.append(") ");
        values.append(")");

        return columns.toString() + values.toString();
    }

    private String formatValue(Object value) {
        if (value == null) {
            return "NULL";
        } else if (value instanceof String) {
            return "'" + escapeSql((String) value) + "'";
        } else if (value instanceof Date) {
            return "'" + value + "'";
        }
        return value.toString();
    }

    private String escapeSql(String value) {
        return value.replace("'", "''");
    }

    /**
     * 更新脚本执行状态
     *
     * @param scriptId 脚本ID
     * @param status   状态
     */
    private void updateScriptExecuteStats(Long scriptId, Integer status) {
        DatabaseScriptDO script = databaseScriptMapper.selectById(scriptId);
        if (script != null) {
            databaseScriptMapper.updateById(DatabaseScriptDO.builder()
                    .id(scriptId)
                    .executeCount(script.getExecuteCount() + 1)
                    .lastExecuteTime(LocalDateTime.now())
                    .lastExecuteStatus(status)
                    .build());
        }
    }

    /**
     * 校验数据库脚本是否存在
     *
     * @param id 脚本ID
     */
    private void validateScriptExists(Long id) {
        if (databaseScriptMapper.selectById(id) == null) {
            throw exception(DATABASE_SCRIPT_NOT_EXISTS);
        }
    }

    /**
     * 校验数据库脚本编码是否唯一
     *
     * @param id   脚本ID
     * @param code 脚本编码
     */
    private void validateScriptCodeUnique(Long id, String code) {
        DatabaseScriptDO existScript = databaseScriptMapper.selectByCode(code);
        if (existScript != null && !existScript.getId().equals(id)) {
            throw exception(DATABASE_SCRIPT_CODE_DUPLICATE);
        }
    }

    /**
     * 校验数据库数据源是否存在
     *
     * @param databaseId 数据库数据源ID
     */
    private void validateDataSourceExists(Long databaseId) {
        if (databaseMapper.selectById(databaseId) == null) {
            throw exception(SOURCE_DATASOURCE_NOT_EXISTS);
        }
    }

    /**
     * 将 DO 转换为 RespVO，并填充数据库数据源相关数据
     *
     * @param script DO 对象
     * @return RespVO 对象
     */
    private DatabaseScriptRespVO convertToRespVO(DatabaseScriptDO script) {
        DatabaseScriptRespVO respVO = ScriptConfigConvert.INSTANCE.convert(script);

        DatabaseDO dataSource = databaseMapper.selectById(script.getDatabaseId());
        if (dataSource != null) {
            respVO.setDataSourceName(dataSource.getName());
        }

        String scriptTypeName = switch (script.getScriptType()) {
            case "sql" -> "SQL脚本";
            case "procedure" -> "存储过程";
            case "view" -> "视图查询";
            default -> script.getScriptType();
        };
        respVO.setScriptTypeName(scriptTypeName);

        return respVO;
    }

}
