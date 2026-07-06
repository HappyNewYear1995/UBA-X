package com.huanniankj.module.source.service.processing;

import com.alibaba.fastjson.JSON;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.source.controller.database.vo.*;
import com.huanniankj.module.source.controller.processing.vo.*;
import com.huanniankj.module.source.controller.webservice.vo.WebServiceExecuteReqVO;
import com.huanniankj.module.source.dal.dataobject.processing.ProcessingScriptDO;
import com.huanniankj.module.source.dal.mysql.processing.ProcessingScriptMapper;
import com.huanniankj.module.source.service.datasource.DatabaseScriptService;
import com.huanniankj.module.source.service.datasource.GroovyExecutionService;
import com.huanniankj.module.source.service.webservice.WebServiceExecutionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.*;

import static com.huanniankj.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.huanniankj.module.source.enums.ErrorCodeConstants.*;

@Slf4j
@Service
public class ProcessingScriptServiceImpl implements ProcessingScriptService {

    @Resource
    private ProcessingScriptMapper processingScriptMapper;

    @Resource
    private GroovyExecutionService groovyExecutionService;

    @Resource
    private DatabaseScriptService databaseScriptService;

    @Resource
    private WebServiceExecutionService webServiceExecutionService;

    @Resource
    private DataSource businessDataSource;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createScript(ProcessingScriptSaveReqVO saveReqVO) {
        validateCodeUnique(null, saveReqVO.getCode());
        ProcessingScriptDO script = ProcessingScriptDO.builder()
                .name(saveReqVO.getName())
                .code(saveReqVO.getCode())
                .scriptContent(saveReqVO.getScriptContent())
                .description(saveReqVO.getDescription())
                .inputParams(saveReqVO.getInputParams())
                .resultTableName(saveReqVO.getResultTableName())
                .resultFieldMapping(saveReqVO.getResultFieldMapping())
                .cronExpression(saveReqVO.getCronExpression())
                .status(saveReqVO.getStatus() != null ? saveReqVO.getStatus() : 0)
                .executeCount(0)
                .remark(saveReqVO.getRemark())
                .build();
        processingScriptMapper.insert(script);
        return script.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateScript(ProcessingScriptSaveReqVO saveReqVO) {
        validateExists(saveReqVO.getId());
        validateCodeUnique(saveReqVO.getId(), saveReqVO.getCode());
        ProcessingScriptDO updateObj = ProcessingScriptDO.builder()
                .id(saveReqVO.getId())
                .name(saveReqVO.getName())
                .code(saveReqVO.getCode())
                .scriptContent(saveReqVO.getScriptContent())
                .description(saveReqVO.getDescription())
                .inputParams(saveReqVO.getInputParams())
                .resultTableName(saveReqVO.getResultTableName())
                .resultFieldMapping(saveReqVO.getResultFieldMapping())
                .cronExpression(saveReqVO.getCronExpression())
                .status(saveReqVO.getStatus())
                .remark(saveReqVO.getRemark())
                .build();
        processingScriptMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteScript(Long id) {
        validateExists(id);
        processingScriptMapper.deleteById(id);
    }

    @Override
    public ProcessingScriptRespVO getScript(Long id) {
        ProcessingScriptDO script = processingScriptMapper.selectById(id);
        if (script == null) {
            throw exception(PROCESSING_SCRIPT_NOT_EXISTS);
        }
        return convertToRespVO(script);
    }

    @Override
    public PageResult<ProcessingScriptRespVO> getScriptPage(ProcessingScriptPageReqVO pageReqVO) {
        PageResult<ProcessingScriptDO> pageResult = processingScriptMapper.selectPage(pageReqVO);
        return new PageResult<>(
                pageResult.getList().stream().map(this::convertToRespVO).toList(),
                pageResult.getTotal()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessingScriptExecuteRespVO executeScript(ProcessingScriptExecuteReqVO reqVO) {
        ProcessingScriptDO script = processingScriptMapper.selectById(reqVO.getScriptId());
        if (script == null) {
            throw exception(PROCESSING_SCRIPT_NOT_EXISTS);
        }

        long startTime = System.currentTimeMillis();
        ProcessingScriptExecuteRespVO respVO = new ProcessingScriptExecuteRespVO();

        try {
            // 构建调用辅助对象
            ScriptInvocationHelper invocationHelper = new ScriptInvocationHelper(
                    databaseScriptService, webServiceExecutionService, businessDataSource);

            // 通过 Groovy 执行引擎执行脚本，注入调用上下文
            DatabaseSqlExecuteRespVO groovyResult = groovyExecutionService.executeGroovyWithInvocation(
                    script.getScriptContent(), reqVO.getInputParams(), invocationHelper);

            long costTime = System.currentTimeMillis() - startTime;

            List<Map<String, Object>> results = groovyResult.getResults();
            boolean persistResult = reqVO.getPersistResult() != null && reqVO.getPersistResult() == 1;
            long persistRecordCount = 0;

            if (persistResult && results != null && !results.isEmpty() && script.getResultTableName() != null) {
                persistRecordCount = persistResult(script, results);
            }

            respVO.setSuccess(true);
            respVO.setCostTime(costTime);
            respVO.setResults(results);
            respVO.setResultSetList(groovyResult.getResultSetList());
            respVO.setResultSetColumns(groovyResult.getResultSetColumns());
            if (results != null) {
                respVO.setResultRecordCount((long) results.size());
            }
            respVO.setPersisted(persistResult);
            respVO.setPersistRecordCount(persistRecordCount);

            updateExecuteStats(script.getId(), 0);
            log.info("处理脚本执行成功: scriptId={}, costTime={}ms, rows={}", script.getId(), costTime, respVO.getResultRecordCount());
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            respVO.setSuccess(false);
            respVO.setCostTime(costTime);
            respVO.setErrorMessage(e.getMessage());
            updateExecuteStats(script.getId(), 1);
            log.error("处理脚本执行失败: scriptId={}, error={}", script.getId(), e.getMessage(), e);
        }

        return respVO;
    }

    /**
     * 持久化结果集到指定表
     */
    @SuppressWarnings("unchecked")
    private long persistResult(ProcessingScriptDO script, List<Map<String, Object>> results) {
        String tableName = script.getResultTableName();
        Map<String, String> fieldMapping = script.getResultFieldMapping() != null
                ? JSON.parseObject(script.getResultFieldMapping(), Map.class)
                : Collections.emptyMap();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(businessDataSource);
        int count = 0;
        for (Map<String, Object> row : results) {
            StringBuilder columns = new StringBuilder("INSERT INTO `").append(tableName).append("` (");
            StringBuilder values = new StringBuilder("VALUES (");
            boolean first = true;
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                String targetField = fieldMapping.isEmpty() ? entry.getKey() : fieldMapping.get(entry.getKey());
                if (targetField == null) continue;
                if (!first) { columns.append(", "); values.append(", "); }
                columns.append("`").append(targetField).append("`");
                values.append(formatValue(entry.getValue()));
                first = false;
            }
            columns.append(", `create_time`) ").append(values).append(", NOW())");
            jdbcTemplate.execute(columns.toString());
            count++;
        }
        log.info("处理脚本结果已持久化: table={}, count={}", tableName, count);
        return count;
    }

    private String formatValue(Object value) {
        if (value == null) return "NULL";
        if (value instanceof String) return "'" + ((String) value).replace("'", "''") + "'";
        if (value instanceof Date) return "'" + value + "'";
        return value.toString();
    }

    private void updateExecuteStats(Long scriptId, Integer status) {
        ProcessingScriptDO script = processingScriptMapper.selectById(scriptId);
        if (script != null) {
            processingScriptMapper.updateById(ProcessingScriptDO.builder()
                    .id(scriptId)
                    .executeCount(script.getExecuteCount() + 1)
                    .lastExecuteTime(LocalDateTime.now())
                    .lastExecuteStatus(status)
                    .build());
        }
    }

    private void validateExists(Long id) {
        if (processingScriptMapper.selectById(id) == null) {
            throw exception(PROCESSING_SCRIPT_NOT_EXISTS);
        }
    }

    private void validateCodeUnique(Long id, String code) {
        ProcessingScriptDO existing = processingScriptMapper.selectByCode(code);
        if (existing != null && !existing.getId().equals(id)) {
            throw exception(PROCESSING_SCRIPT_CODE_DUPLICATE);
        }
    }

    private ProcessingScriptRespVO convertToRespVO(ProcessingScriptDO script) {
        ProcessingScriptRespVO respVO = new ProcessingScriptRespVO();
        respVO.setId(script.getId());
        respVO.setName(script.getName());
        respVO.setCode(script.getCode());
        respVO.setScriptContent(script.getScriptContent());
        respVO.setDescription(script.getDescription());
        respVO.setInputParams(script.getInputParams());
        respVO.setResultTableName(script.getResultTableName());
        respVO.setResultFieldMapping(script.getResultFieldMapping());
        respVO.setCronExpression(script.getCronExpression());
        respVO.setExecuteCount(script.getExecuteCount());
        respVO.setLastExecuteTime(script.getLastExecuteTime());
        respVO.setLastExecuteStatus(script.getLastExecuteStatus());
        respVO.setStatus(script.getStatus());
        respVO.setRemark(script.getRemark());
        respVO.setCreateTime(script.getCreateTime());
        return respVO;
    }

    /**
     * 脚本调用辅助类，提供给 Groovy 脚本调用数据库脚本和 WebService
     */
    public static class ScriptInvocationHelper {

        private final DatabaseScriptService databaseScriptService;
        private final WebServiceExecutionService webServiceExecutionService;
        private final DataSource businessDataSource;

        public ScriptInvocationHelper(DatabaseScriptService databaseScriptService,
                                       WebServiceExecutionService webServiceExecutionService,
                                       DataSource businessDataSource) {
            this.databaseScriptService = databaseScriptService;
            this.webServiceExecutionService = webServiceExecutionService;
            this.businessDataSource = businessDataSource;
        }

        /**
         * 调用数据库脚本
         *
         * @param scriptId    数据库脚本 ID
         * @param inputParams 执行入参
         * @return 执行结果
         */
        public DatabaseScriptExecuteRespVO callDatabaseScript(Long scriptId, Map<String, Object> inputParams) {
            DatabaseScriptExecuteReqVO reqVO = new DatabaseScriptExecuteReqVO();
            reqVO.setScriptId(scriptId);
            reqVO.setInputParams(inputParams);
            return databaseScriptService.executeScript(reqVO);
        }

        /**
         * 调用 WebService
         *
         * @param wsId   WebService 数据源 ID
         * @param params 请求参数
         * @return 执行结果
         */
        public DatabaseSqlExecuteRespVO callWebService(Long wsId, Map<String, String> params) {
            WebServiceExecuteReqVO reqVO = new WebServiceExecuteReqVO();
            reqVO.setDatabaseId(wsId);
            reqVO.setParams(params);
            return webServiceExecutionService.executeWebService(reqVO);
        }

        /**
         * 获取业务数据库 JdbcTemplate（用于直接数据库操作和持久化）
         */
        public JdbcTemplate getJdbcTemplate() {
            return new JdbcTemplate(businessDataSource);
        }

    }

}
