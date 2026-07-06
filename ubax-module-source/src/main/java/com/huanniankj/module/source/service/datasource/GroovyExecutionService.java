package com.huanniankj.module.source.service.datasource;

import com.huanniankj.module.source.controller.database.vo.DatabaseSqlExecuteRespVO;

import java.util.List;
import java.util.Map;

/**
 * Groovy 脚本执行服务接口
 *
 * @author zhaoff
 */
public interface GroovyExecutionService {

    /**
     * 执行 Groovy 脚本（带数据库上下文）
     */
    DatabaseSqlExecuteRespVO executeGroovy(Long databaseId, String scriptContent, Map<String, Object> inputParams);

    /**
     * 执行 Groovy 脚本（纯数据处理，无数据库上下文）
     */
    DatabaseSqlExecuteRespVO executeGroovyForData(String scriptContent,
                                                   List<Map<String, Object>> data,
                                                   Map<String, Object> inputParams);

    /**
     * 执行 Groovy 脚本（带调用上下文，支持调用数据库脚本和 WebService）
     * <p>
     * 脚本中可用的内置变量：
     * <ul>
     *   <li>{@code invoker} - 调用辅助对象，提供 callDatabaseScript/callWebService/getJdbcTemplate 方法</li>
     *   <li>{@code inputParams} - 执行入参 Map</li>
     *   <li>{@code logger} - SLF4J Logger</li>
     * </ul>
     *
     * @param scriptContent    Groovy 脚本内容
     * @param inputParams      执行入参
     * @param invocationHelper 调用辅助对象
     * @return 执行结果
     */
    DatabaseSqlExecuteRespVO executeGroovyWithInvocation(String scriptContent,
                                                          Map<String, Object> inputParams,
                                                          Object invocationHelper);

}
