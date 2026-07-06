package com.huanniankj.module.source.service.datasource;

import com.alibaba.fastjson.JSON;
import com.huanniankj.framework.common.exception.ServiceException;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.source.controller.database.vo.*;
import com.huanniankj.module.source.controller.webservice.vo.WebServiceExecuteReqVO;
import com.huanniankj.module.source.convert.script.ScriptConfigConvert;
import com.huanniankj.module.source.dal.dataobject.database.DatabaseDO;
import com.huanniankj.module.source.dal.dataobject.database.DatabaseScriptDO;
import com.huanniankj.module.source.dal.dataobject.database.DatabaseScriptLogDO;
import com.huanniankj.module.source.dal.mysql.database.DatabaseMapper;
import com.huanniankj.module.source.dal.mysql.database.DatabaseScriptLogMapper;
import com.huanniankj.module.source.dal.mysql.database.DatabaseScriptMapper;
import com.huanniankj.module.source.framework.datasource.core.DataSourceManager;
import com.huanniankj.module.source.service.webservice.WebServiceExecutionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.*;

import static com.huanniankj.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.huanniankj.module.source.enums.ErrorCodeConstants.*;

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
    private DatabaseScriptLogMapper databaseScriptLogMapper;

    @Resource
    private DatabaseMapper databaseMapper;

    @Resource
    private DatabaseScriptExecutionService databaseScriptExecutionService;

    @Resource
    private WebServiceExecutionService webServiceExecutionService;

    @Resource
    private DataSourceManager dataSourceManager;

    @Resource
    private DataSource businessDataSource;

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
        databaseScriptLogMapper.delete(DatabaseScriptLogDO::getScriptId, id);
    }

    @Override
    public DatabaseScriptRespVO getScript(Long id) {
        DatabaseScriptDO script = databaseScriptMapper.selectById(id);
        if (script == null) {
            throw exception(DATABASE_SCRIPT_NOT_EXISTS);
        }
        return convertToRespVO(script);
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
        DatabaseScriptDO script = databaseScriptMapper.selectById(reqVO.getScriptId());
        if (script == null) {
            throw exception(DATABASE_SCRIPT_NOT_EXISTS);
        }
        // 校验必填入参
        validateInputParams(script.getInputParams(), reqVO.getInputParams());

        DatabaseScriptExecuteRespVO respVO = new DatabaseScriptExecuteRespVO();
        DatabaseScriptLogDO logDO = new DatabaseScriptLogDO();
        logDO.setScriptId(script.getId());
        logDO.setScriptName(script.getName());
        logDO.setScriptCode(script.getCode());
        logDO.setDatabaseId(script.getDatabaseId());
        logDO.setExecuteType("manual");
        logDO.setScriptContent(script.getScriptContent());
        if (reqVO.getInputParams() != null && !reqVO.getInputParams().isEmpty()) {
            logDO.setInputParams(JSON.toJSONString(reqVO.getInputParams()));
        }

        long startTime = System.currentTimeMillis();

        try {
            DatabaseSqlExecuteRespVO executeResult = executeScriptInternal(script, reqVO.getInputParams());
            long costTime = System.currentTimeMillis() - startTime;

            // 提取执行结果
            List<Map<String, Object>> results = executeResult.getResults();
            List<List<Map<String, Object>>> resultSetList = executeResult.getResultSetList();
            Integer affectedRows = executeResult.getAffectedRows();

            // 持久化结果
            boolean persistResult = reqVO.getPersistResult() != null && reqVO.getPersistResult() == 1;
            long persistRecordCount = 0;

            if (persistResult && results != null && !results.isEmpty()) {
                persistRecordCount = persistExecuteResult(script, results);
                logDO.setPersisted(1);
            } else {
                logDO.setPersisted(0);
            }

            // 构建响应
            respVO.setSuccess(true);
            respVO.setCostTime(costTime);
            respVO.setResults(results);
            respVO.setResultSetList(resultSetList);
            respVO.setResultSetColumns(executeResult.getResultSetColumns());
            if (affectedRows != null) {
                respVO.setAffectedRows(affectedRows);
                logDO.setAffectedRows(Long.valueOf(affectedRows));
            }
            if (results != null) {
                respVO.setResultRecordCount((long) results.size());
                logDO.setResultRecordCount((long) results.size());
            }
            respVO.setPersisted(persistResult);
            respVO.setPersistRecordCount(persistRecordCount);

            // 存储过程输出参数
            if (executeResult.getOutputParams() != null && !executeResult.getOutputParams().isEmpty()) {
                respVO.setOutputParams(executeResult.getOutputParams());
                logDO.setOutputParams(JSON.toJSONString(executeResult.getOutputParams()));
            }

            // 记录日志
            logDO.setStatus(0);
            logDO.setCostTime(costTime);
            logDO.setExecuteResult(JSON.toJSONString(executeResult));

            updateScriptExecuteStats(script.getId(), 0);

            log.info("脚本执行成功: scriptId={}, costTime={}ms, recordCount={}", script.getId(), costTime, respVO.getResultRecordCount());
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;

            respVO.setSuccess(false);
            respVO.setCostTime(costTime);
            respVO.setErrorMessage(e.getMessage());

            logDO.setStatus(1);
            logDO.setErrorMessage(e.getMessage());
            logDO.setCostTime(costTime);

            updateScriptExecuteStats(script.getId(), 1);

            log.error("脚本执行失败: scriptId={}, error={}", script.getId(), e.getMessage(), e);
        }

        databaseScriptLogMapper.insert(logDO);
        return respVO;
    }

    @Override
    public PageResult<DatabaseScriptLogRespVO> getScriptLogPage(DatabaseScriptLogPageReqVO pageReqVO) {
        PageResult<DatabaseScriptLogDO> pageResult = databaseScriptLogMapper.selectPage(pageReqVO);
        return ScriptConfigConvert.INSTANCE.convertLogPage(pageResult);
    }

    @Override
    public DatabaseScriptLogRespVO getScriptLog(Long id) {
        DatabaseScriptLogDO logDO = databaseScriptLogMapper.selectById(id);
        if (logDO == null) {
            throw exception(DATABASE_SCRIPT_LOG_NOT_EXISTS);
        }
        return ScriptConfigConvert.INSTANCE.convertLog(logDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteScriptLog(Long id) {
        DatabaseScriptLogDO logDO = databaseScriptLogMapper.selectById(id);
        if (logDO == null) {
            throw exception(DATABASE_SCRIPT_LOG_NOT_EXISTS);
        }
        databaseScriptLogMapper.deleteById(id);
    }

    /**
     * 执行数据库脚本
     *
     * @param script      数据库脚本
     * @param inputParams 执行入参
     * @return SQL 执行响应
     */
    private DatabaseSqlExecuteRespVO executeScriptInternal(DatabaseScriptDO script, Map<String, Object> inputParams) {
        String scriptType = script.getScriptType();
        Long databaseId = script.getDatabaseId();
        String scriptContent = script.getScriptContent();

        // 将入参 Map 转为有序参数列表（按 inputParams 定义顺序）
        List<Object> paramList = buildParamList(script.getInputParams(), inputParams);

        return switch (scriptType) {
            case "sql" -> databaseScriptExecutionService.executeSql(databaseId, scriptContent, paramList);
            case "procedure" -> {
                DatabaseProcedureReqVO reqVO = new DatabaseProcedureReqVO();
                reqVO.setDatabaseId(databaseId);
                reqVO.setProcedureName(scriptContent);
                if (paramList != null && !paramList.isEmpty()) {
                    reqVO.setInputParams(paramList);
                }
                // 解析输出参数定义
                List<String> outputParamNames = parseOutputParamNames(script.getOutputParams());
                if (outputParamNames != null && !outputParamNames.isEmpty()) {
                    reqVO.setOutputParamNames(outputParamNames);
                }
                yield databaseScriptExecutionService.executeProcedure(reqVO);
            }
            case "view" -> databaseScriptExecutionService.executeSql(databaseId, scriptContent, paramList);
            case "webservice" -> {
                // WebService 脚本：scriptContent 为请求体，入参作为模板参数
                WebServiceExecuteReqVO wsReqVO = new WebServiceExecuteReqVO();
                wsReqVO.setDatabaseId(databaseId);
                wsReqVO.setBody(scriptContent);
                // 将入参转为 String Map 用于模板替换
                if (inputParams != null && !inputParams.isEmpty()) {
                    Map<String, String> params = new LinkedHashMap<>();
                    inputParams.forEach((k, v) -> params.put(k, v != null ? v.toString() : ""));
                    wsReqVO.setParams(params);
                }
                yield webServiceExecutionService.executeWebService(wsReqVO);
            }
            default -> throw exception(DATABASE_SCRIPT_TYPE_NOT_SUPPORTED);
        };
    }

    /**
     * 校验必填入参
     *
     * @param inputParamsDef 入参定义（JSON格式）
     * @param inputParams    执行入参
     */
    @SuppressWarnings("unchecked")
    private void validateInputParams(String inputParamsDef, Map<String, Object> inputParams) {
        if (inputParamsDef == null || inputParamsDef.isEmpty()) {
            return;
        }
        try {
            List<Map<String, Object>> paramDefs = JSON.parseObject(inputParamsDef, List.class);
            for (Map<String, Object> paramDef : paramDefs) {
                String name = (String) paramDef.get("name");
                Boolean required = (Boolean) paramDef.get("required");
                if (Boolean.TRUE.equals(required) && (inputParams == null || !inputParams.containsKey(name))) {
                    throw exception(DATABASE_SCRIPT_INPUT_PARAM_REQUIRED, name);
                }
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.warn("解析入参定义失败: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 根据入参定义和执行入参，构建有序参数列表
     *
     * @param inputParamsDef 入参定义（JSON格式）
     * @param inputParams    执行入参
     * @return 有序参数列表
     */
    @SuppressWarnings("unchecked")
    private List<Object> buildParamList(String inputParamsDef, Map<String, Object> inputParams) {
        if (inputParamsDef == null || inputParamsDef.isEmpty() || inputParams == null || inputParams.isEmpty()) {
            return null;
        }
        try {
            List<Map<String, Object>> paramDefs = JSON.parseObject(inputParamsDef, List.class);
            List<Object> paramList = new ArrayList<>();
            for (Map<String, Object> paramDef : paramDefs) {
                String name = (String) paramDef.get("name");
                Object value = inputParams.get(name);
                // 如果未传值，使用默认值
                if (value == null) {
                    value = paramDef.get("defaultValue");
                }
                paramList.add(value);
            }
            return paramList;
        } catch (Exception e) {
            log.warn("构建参数列表失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 解析输出参数定义
     *
     * @param outputParamsDef 输出参数定义（JSON格式，如["out_code","out_msg"]）
     * @return 输出参数名称列表
     */
    @SuppressWarnings("unchecked")
    private List<String> parseOutputParamNames(String outputParamsDef) {
        if (outputParamsDef == null || outputParamsDef.isEmpty()) {
            return null;
        }
        try {
            return JSON.parseObject(outputParamsDef, List.class);
        } catch (Exception e) {
            log.warn("解析输出参数定义失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 持久化结果集
     *
     * @param script  数据库脚本
     * @param results 结果集
     * @return 持久化记录数
     */
    private long persistExecuteResult(DatabaseScriptDO script, List<?> results) {
        if (script.getResultTableName() == null || script.getResultTableName().isEmpty()) {
            throw exception(DATABASE_SCRIPT_RESULT_TABLE_NOT_CONFIGURED);
        }

        String tableName = script.getResultTableName();
        Map<String, String> fieldMapping = parseFieldMapping(script.getResultFieldMapping());

        try {
            // 使用业务数据库持久化结果
            JdbcTemplate jdbcTemplate = new JdbcTemplate(businessDataSource);
            // 校验目标表是否存在
            validateTableExists(jdbcTemplate, tableName);
            int count = 0;
            for (Object result : results) {
                Map<String, Object> row = (Map<String, Object>) result;
                String sql = buildInsertSql(tableName, row, fieldMapping);
                jdbcTemplate.execute(sql);
                count++;
            }

            log.info("脚本执行结果已持久化: table={}, count={}", tableName, count);
            return count;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("脚本执行结果持久化失败: table={}, error={}", tableName, e.getMessage(), e);
            throw exception(DATABASE_SCRIPT_RESULT_PERSIST_ERROR, e.getMessage());
        }
    }

    /**
     * 解析字段映射
     *
     * @param fieldMappingJson 字段映射字符串
     * @return 字段映射字典
     */
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

    /**
     * 校验目标表是否存在，不存在则抛出异常
     *
     * @param jdbcTemplate JdbcTemplate
     * @param tableName    表名
     */
    private void validateTableExists(JdbcTemplate jdbcTemplate, String tableName) {
        DataSource dataSource = jdbcTemplate.getDataSource();
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
                if (!rs.next()) {
                    // 同时尝试大写表名
                    try (ResultSet rsUpper = metaData.getTables(null, null, tableName.toUpperCase(), new String[]{"TABLE"})) {
                        if (!rsUpper.next()) {
                            throw exception(DATABASE_SCRIPT_RESULT_TABLE_NOT_EXISTS, tableName);
                        }
                    }
                }
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("校验结果表是否存在失败: table={}, error={}", tableName, e.getMessage(), e);
            throw exception(DATABASE_SCRIPT_RESULT_PERSIST_ERROR, "校验结果表失败: " + e.getMessage());
        }
    }

    /**
     * 构建插入SQL
     *
     * @param tableName    表名
     * @param row          执行结果
     * @param fieldMapping 字段映射
     * @return 插入SQL语句
     */
    private String buildInsertSql(String tableName, Map<String, Object> row, Map<String, String> fieldMapping) {
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();

        columns.append("INSERT INTO `").append(tableName).append("` (");
        values.append("VALUES (");

        boolean first = true;
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            String sourceField = entry.getKey();
            String targetField;
            if (fieldMapping.isEmpty()) {
                // 未配置字段映射时，全部字段持久化，源字段名即目标字段名
                targetField = sourceField;
            } else if (fieldMapping.containsKey(sourceField)) {
                // 配置了字段映射时，只持久化映射中定义的字段
                targetField = fieldMapping.get(sourceField);
            } else {
                // 不在映射中的字段跳过
                continue;
            }
            if (!first) {
                columns.append(", ");
                values.append(", ");
            }
            columns.append("`").append(targetField).append("`");
            values.append(formatValue(entry.getValue()));
            first = false;
        }

        columns.append(", `create_time`");
        values.append(", NOW()");

        columns.append(") ");
        values.append(")");

        return columns + values.toString();
    }

    /**
     * 格式化SQL字段值
     *
     * @param value SQL字段值
     * @return 格式化后的SQL字段值
     */
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

    /**
     * SQL转义
     *
     * @param value SQL字符串
     * @return 转义后的SQL字符串
     */
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
            case "webservice" -> "WebService";
            default -> script.getScriptType();
        };
        respVO.setScriptTypeName(scriptTypeName);

        return respVO;
    }

}
