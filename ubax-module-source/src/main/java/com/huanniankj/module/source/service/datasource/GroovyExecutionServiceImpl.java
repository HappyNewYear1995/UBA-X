package com.huanniankj.module.source.service.datasource;

import com.huanniankj.module.source.controller.database.vo.DatabaseSqlExecuteRespVO;
import com.huanniankj.module.source.framework.datasource.core.DataSourceManager;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;
import javax.sql.DataSource;
import java.util.*;



/**
 * Groovy 脚本执行服务实现
 * <p>
 * 通过 JSR-223 ScriptEngine 执行 Groovy 脚本，为脚本注入数据库访问能力和数据处理上下文。
 * <p>
 * 脚本中可用的内置变量：
 * <ul>
 *   <li>{@code dataSource} - javax.sql.DataSource（带数据库上下文时可用）</li>
 *   <li>{@code jdbcTemplate} - org.springframework.jdbc.core.JdbcTemplate（带数据库上下文时可用）</li>
 *   <li>{@code inputParams} - java.util.Map，执行入参</li>
 *   <li>{@code logger} - org.slf4j.Logger，日志记录器</li>
 *   <li>{@code data} - java.util.List（纯数据处理模式下，输入数据）</li>
 * </ul>
 * <p>
 * 脚本返回值约定：
 * <ul>
 *   <li>返回 {@code List<Map>} → 作为多行结果</li>
 *   <li>返回 {@code Map} → 包装为单行结果</li>
 *   <li>返回其他类型 → 包装为 {"result": value} 单行结果</li>
 * </ul>
 *
 * @author zhaoff
 */
@Slf4j
@Service
public class GroovyExecutionServiceImpl implements GroovyExecutionService {

    @Resource
    private DataSourceManager dataSourceManager;

    private static final ScriptEngine GROOVY_ENGINE;

    static {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("groovy");
        if (engine == null) {
            log.error("Groovy ScriptEngine 未找到，请确认 groovy-jsr223 依赖已正确引入");
            throw new IllegalStateException("Groovy ScriptEngine not available");
        }
        GROOVY_ENGINE = engine;
    }

    @Override
    public DatabaseSqlExecuteRespVO executeGroovy(Long databaseId, String scriptContent, Map<String, Object> inputParams) {
        long startTime = System.currentTimeMillis();
        DatabaseSqlExecuteRespVO respVO = new DatabaseSqlExecuteRespVO();

        try {
            // 获取数据源
            DataSource ds = dataSourceManager.getDataSource(databaseId);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);

            // 构建脚本上下文
            javax.script.ScriptContext context = new SimpleScriptContext();
            context.setAttribute("dataSource", ds, javax.script.ScriptContext.ENGINE_SCOPE);
            context.setAttribute("jdbcTemplate", jdbcTemplate, javax.script.ScriptContext.ENGINE_SCOPE);
            context.setAttribute("inputParams", inputParams != null ? inputParams : Collections.emptyMap(), javax.script.ScriptContext.ENGINE_SCOPE);
            context.setAttribute("logger", log, javax.script.ScriptContext.ENGINE_SCOPE);

            // 执行脚本
            Object result = GROOVY_ENGINE.eval(scriptContent, context);

            // 转换结果
            List<Map<String, Object>> results = convertResult(result);
            List<String> columns = results.isEmpty() ? Collections.emptyList() : new ArrayList<>(results.get(0).keySet());

            respVO.setSuccess(true);
            respVO.setResults(results);
            respVO.setResultSetList(List.of(results));
            respVO.setResultSetColumns(List.of(columns));
            respVO.setAffectedRows(results.size());
            respVO.setCostTime(System.currentTimeMillis() - startTime);

            log.info("Groovy 脚本执行成功: databaseId={}, costTime={}ms, rows={}",
                    databaseId, respVO.getCostTime(), results.size());
        } catch (Exception e) {
            log.error("Groovy 脚本执行失败: databaseId={}, error={}", databaseId, e.getMessage(), e);
            respVO.setSuccess(false);
            respVO.setErrorMessage("Groovy 脚本执行失败: " + e.getMessage());
            respVO.setCostTime(System.currentTimeMillis() - startTime);
        }

        return respVO;
    }

    @Override
    public DatabaseSqlExecuteRespVO executeGroovyForData(String scriptContent,
                                                          List<Map<String, Object>> data,
                                                          Map<String, Object> inputParams) {
        long startTime = System.currentTimeMillis();
        DatabaseSqlExecuteRespVO respVO = new DatabaseSqlExecuteRespVO();

        try {
            // 构建脚本上下文
            javax.script.ScriptContext context = new SimpleScriptContext();
            context.setAttribute("data", data != null ? data : Collections.emptyList(), javax.script.ScriptContext.ENGINE_SCOPE);
            context.setAttribute("inputParams", inputParams != null ? inputParams : Collections.emptyMap(), javax.script.ScriptContext.ENGINE_SCOPE);
            context.setAttribute("logger", log, javax.script.ScriptContext.ENGINE_SCOPE);

            // 执行脚本
            Object result = GROOVY_ENGINE.eval(scriptContent, context);

            // 转换结果
            List<Map<String, Object>> results = convertResult(result);
            List<String> columns = results.isEmpty() ? Collections.emptyList() : new ArrayList<>(results.get(0).keySet());

            respVO.setSuccess(true);
            respVO.setResults(results);
            respVO.setResultSetList(List.of(results));
            respVO.setResultSetColumns(List.of(columns));
            respVO.setAffectedRows(results.size());
            respVO.setCostTime(System.currentTimeMillis() - startTime);

            log.info("Groovy 数据处理脚本执行成功: costTime={}ms, rows={}",
                    respVO.getCostTime(), results.size());
        } catch (Exception e) {
            log.error("Groovy 数据处理脚本执行失败: error={}", e.getMessage(), e);
            respVO.setSuccess(false);
            respVO.setErrorMessage("Groovy 数据处理脚本执行失败: " + e.getMessage());
            respVO.setCostTime(System.currentTimeMillis() - startTime);
        }

        return respVO;
    }

    @Override
    public DatabaseSqlExecuteRespVO executeGroovyWithInvocation(String scriptContent,
                                                                  Map<String, Object> inputParams,
                                                                  Object invocationHelper) {
        long startTime = System.currentTimeMillis();
        DatabaseSqlExecuteRespVO respVO = new DatabaseSqlExecuteRespVO();

        try {
            javax.script.ScriptContext context = new SimpleScriptContext();
            context.setAttribute("invoker", invocationHelper, javax.script.ScriptContext.ENGINE_SCOPE);
            context.setAttribute("inputParams", inputParams != null ? inputParams : Collections.emptyMap(), javax.script.ScriptContext.ENGINE_SCOPE);
            context.setAttribute("logger", log, javax.script.ScriptContext.ENGINE_SCOPE);

            Object result = GROOVY_ENGINE.eval(scriptContent, context);

            List<Map<String, Object>> results = convertResult(result);
            List<String> columns = results.isEmpty() ? Collections.emptyList() : new ArrayList<>(results.get(0).keySet());

            respVO.setSuccess(true);
            respVO.setResults(results);
            respVO.setResultSetList(List.of(results));
            respVO.setResultSetColumns(List.of(columns));
            respVO.setAffectedRows(results.size());
            respVO.setCostTime(System.currentTimeMillis() - startTime);

            log.info("Groovy 处理脚本执行成功: costTime={}ms, rows={}", respVO.getCostTime(), results.size());
        } catch (Exception e) {
            log.error("Groovy 处理脚本执行失败: error={}", e.getMessage(), e);
            respVO.setSuccess(false);
            respVO.setErrorMessage("Groovy 处理脚本执行失败: " + e.getMessage());
            respVO.setCostTime(System.currentTimeMillis() - startTime);
        }

        return respVO;
    }

    /**
     * 转换脚本返回值为统一的结果集格式
     *
     * @param result 脚本返回值
     * @return 结果集
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> convertResult(Object result) {
        if (result == null) {
            return Collections.emptyList();
        }
        if (result instanceof List<?> list) {
            if (list.isEmpty()) {
                return Collections.emptyList();
            }
            List<Map<String, Object>> results = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    results.add(new LinkedHashMap<>((Map<String, Object>) map));
                } else {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("value", item);
                    results.add(row);
                }
            }
            return results;
        }
        if (result instanceof Map<?, ?> map) {
            return List.of(new LinkedHashMap<>((Map<String, Object>) map));
        }
        // 其他类型包装为单行结果
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("result", result);
        return List.of(row);
    }

}
