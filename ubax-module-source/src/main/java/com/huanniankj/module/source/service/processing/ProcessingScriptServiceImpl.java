package com.huanniankj.module.source.service.processing;

import com.alibaba.fastjson.JSON;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.source.controller.database.vo.DatabaseSqlExecuteRespVO;
import com.huanniankj.module.source.controller.processing.vo.ProcessingScriptExecuteReqVO;
import com.huanniankj.module.source.controller.processing.vo.ProcessingScriptExecuteRespVO;
import com.huanniankj.module.source.controller.processing.vo.ProcessingScriptLogPageReqVO;
import com.huanniankj.module.source.controller.processing.vo.ProcessingScriptLogRespVO;
import com.huanniankj.module.source.controller.processing.vo.ProcessingScriptPageReqVO;
import com.huanniankj.module.source.controller.processing.vo.ProcessingScriptRespVO;
import com.huanniankj.module.source.controller.processing.vo.ProcessingScriptSaveReqVO;
import com.huanniankj.module.source.controller.webservice.vo.WebServiceExecuteReqVO;
import com.huanniankj.module.source.dal.dataobject.processing.ProcessingScriptDO;
import com.huanniankj.module.source.dal.dataobject.processing.ProcessingScriptLogDO;
import com.huanniankj.module.source.dal.mysql.processing.ProcessingScriptLogMapper;
import com.huanniankj.module.source.dal.mysql.processing.ProcessingScriptMapper;
import com.huanniankj.module.source.service.webservice.WebServiceExecutionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.huanniankj.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.huanniankj.module.source.enums.ErrorCodeConstants.PROCESSING_SCRIPT_CODE_DUPLICATE;
import static com.huanniankj.module.source.enums.ErrorCodeConstants.PROCESSING_SCRIPT_LOG_NOT_EXISTS;
import static com.huanniankj.module.source.enums.ErrorCodeConstants.PROCESSING_SCRIPT_NOT_EXISTS;

@Slf4j
@Service
public class ProcessingScriptServiceImpl implements ProcessingScriptService {

    @Resource
    private ProcessingScriptMapper processingScriptMapper;

    @Resource
    private ProcessingScriptLogMapper processingScriptLogMapper;

    @Resource
    private GroovyExecutionService groovyExecutionService;

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
        ProcessingScriptLogDO logDO = ProcessingScriptLogDO.builder()
                .scriptId(script.getId())
                .scriptName(script.getName())
                .scriptCode(script.getCode())
                .executeType("manual")
                .scriptContent(script.getScriptContent())
                .build();
        if (reqVO.getInputParams() != null && !reqVO.getInputParams().isEmpty()) {
            logDO.setInputParams(JSON.toJSONString(reqVO.getInputParams()));
        }

        try {
            ScriptInvocationHelper invocationHelper = new ScriptInvocationHelper(
                    webServiceExecutionService, businessDataSource);

            DatabaseSqlExecuteRespVO groovyResult = groovyExecutionService.executeGroovyWithInvocation(
                    script.getScriptContent(), reqVO.getInputParams(), invocationHelper);

            long costTime = System.currentTimeMillis() - startTime;

            List<Map<String, Object>> results = groovyResult.getResults();
            boolean persistResult = reqVO.getPersistResult() != null && reqVO.getPersistResult() == 1;
            long persistRecordCount = 0;

            if (persistResult && results != null && !results.isEmpty() && script.getResultTableName() != null) {
                try {
                    persistRecordCount = persistResult(script, results);
                    logDO.setPersisted(1);
                } catch (Exception e) {
                    logDO.setPersisted(0);
                    logDO.setPersistError(e.getMessage());
                }
            } else {
                logDO.setPersisted(0);
            }

            respVO.setSuccess(true);
            respVO.setCostTime(costTime);
            respVO.setResults(results);
            respVO.setResultSetList(groovyResult.getResultSetList());
            respVO.setResultSetColumns(groovyResult.getResultSetColumns());
            if (results != null) {
                respVO.setResultRecordCount((long) results.size());
                logDO.setResultRecordCount((long) results.size());
            }
            respVO.setPersisted(persistResult);
            respVO.setPersistRecordCount(persistRecordCount);

            logDO.setStatus(0);
            logDO.setCostTime(costTime);
            logDO.setExecuteResult(JSON.toJSONString(groovyResult));

            updateExecuteStats(script.getId(), 0);
            log.info("处理脚本执行成功: scriptId={}, costTime={}ms, rows={}", script.getId(), costTime, respVO.getResultRecordCount());
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            respVO.setSuccess(false);
            respVO.setCostTime(costTime);
            respVO.setErrorMessage(e.getMessage());

            logDO.setStatus(1);
            logDO.setCostTime(costTime);
            logDO.setErrorMessage(e.getMessage());

            updateExecuteStats(script.getId(), 1);
            log.error("处理脚本执行失败: scriptId={}, error={}", script.getId(), e.getMessage(), e);
        }

        processingScriptLogMapper.insert(logDO);
        return respVO;
    }

    @Override
    public PageResult<ProcessingScriptLogRespVO> getScriptLogPage(ProcessingScriptLogPageReqVO pageReqVO) {
        PageResult<ProcessingScriptLogDO> pageResult = processingScriptLogMapper.selectPage(pageReqVO);
        return new PageResult<>(
                pageResult.getList().stream().map(this::convertLogToRespVO).toList(),
                pageResult.getTotal()
        );
    }

    @Override
    public ProcessingScriptLogRespVO getScriptLog(Long id) {
        ProcessingScriptLogDO logDO = processingScriptLogMapper.selectById(id);
        if (logDO == null) {
            throw exception(PROCESSING_SCRIPT_LOG_NOT_EXISTS);
        }
        return convertLogToRespVO(logDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteScriptLog(Long id) {
        ProcessingScriptLogDO logDO = processingScriptLogMapper.selectById(id);
        if (logDO == null) {
            throw exception(PROCESSING_SCRIPT_LOG_NOT_EXISTS);
        }
        processingScriptLogMapper.deleteById(id);
    }

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
                if (!first) {
                    columns.append(", ");
                    values.append(", ");
                }
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

    private ProcessingScriptLogRespVO convertLogToRespVO(ProcessingScriptLogDO logDO) {
        ProcessingScriptLogRespVO respVO = new ProcessingScriptLogRespVO();
        respVO.setId(logDO.getId());
        respVO.setScriptId(logDO.getScriptId());
        respVO.setScriptName(logDO.getScriptName());
        respVO.setScriptCode(logDO.getScriptCode());
        respVO.setExecuteType(logDO.getExecuteType());
        respVO.setScriptContent(logDO.getScriptContent());
        respVO.setInputParams(logDO.getInputParams());
        respVO.setStatus(logDO.getStatus());
        respVO.setErrorMessage(logDO.getErrorMessage());
        respVO.setCostTime(logDO.getCostTime());
        respVO.setResultRecordCount(logDO.getResultRecordCount());
        respVO.setPersisted(logDO.getPersisted());
        respVO.setPersistError(logDO.getPersistError());
        respVO.setExecuteResult(logDO.getExecuteResult());
        respVO.setCreateTime(logDO.getCreateTime());
        return respVO;
    }

    public static class ScriptInvocationHelper {

        private final WebServiceExecutionService webServiceExecutionService;
        private final DataSource businessDataSource;

        public ScriptInvocationHelper(WebServiceExecutionService webServiceExecutionService,
                                      DataSource businessDataSource) {
            this.webServiceExecutionService = webServiceExecutionService;
            this.businessDataSource = businessDataSource;
        }

        public DatabaseSqlExecuteRespVO callWebService(Long wsId, Map<String, String> params) {
            WebServiceExecuteReqVO reqVO = new WebServiceExecuteReqVO();
            reqVO.setDatabaseId(wsId);
            reqVO.setParams(params);
            return webServiceExecutionService.executeWebService(reqVO);
        }

        public JdbcTemplate getJdbcTemplate() {
            return new JdbcTemplate(businessDataSource);
        }

    }

}
