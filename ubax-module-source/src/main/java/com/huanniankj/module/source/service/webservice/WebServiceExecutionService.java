package com.huanniankj.module.source.service.webservice;

import com.huanniankj.module.source.controller.database.vo.DatabaseSqlExecuteRespVO;
import com.huanniankj.module.source.controller.webservice.vo.WebServiceExecuteReqVO;

/**
 * WebService 执行服务接口
 * <p>
 * 支持 REST/SOAP 协议的 WebService 调用，解析响应数据并持久化。
 *
 * @author zhaoff
 */
public interface WebServiceExecutionService {

    /**
     * 执行 WebService 请求
     *
     * @param reqVO 请求参数
     * @return 执行结果（复用 SQL 执行响应格式）
     */
    DatabaseSqlExecuteRespVO executeWebService(WebServiceExecuteReqVO reqVO);

    /**
     * 测试 WebService 连接
     *
     * @param databaseId 数据源 ID
     * @return 连接是否成功
     */
    boolean testWebServiceConnection(Long databaseId);

}
