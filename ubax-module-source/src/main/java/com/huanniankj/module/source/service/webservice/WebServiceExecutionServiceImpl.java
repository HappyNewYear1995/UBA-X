package com.huanniankj.module.source.service.webservice;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.huanniankj.module.source.controller.database.vo.DatabaseSqlExecuteRespVO;
import com.huanniankj.module.source.controller.webservice.vo.WebServiceExecuteReqVO;
import com.huanniankj.module.source.dal.dataobject.webservice.WebServiceDO;
import com.huanniankj.module.source.dal.mysql.webservice.WebServiceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.huanniankj.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.huanniankj.module.source.enums.ErrorCodeConstants.WEBSERVICE_DATASOURCE_NOT_EXISTS;

/**
 * WebService 执行服务实现
 * <p>
 * 支持 REST (GET/POST/PUT/DELETE) 和 SOAP 协议的 WebService 调用，
 * 解析 JSON/XML 响应数据，通过 JSONPath 提取数据并转换为统一的表格格式。
 *
 * @author zhaoff
 */
@Slf4j
@Service
public class WebServiceExecutionServiceImpl implements WebServiceExecutionService {

    @Resource
    private WebServiceMapper webServiceMapper;

    @Override
    public DatabaseSqlExecuteRespVO executeWebService(WebServiceExecuteReqVO reqVO) {
        long startTime = System.currentTimeMillis();
        DatabaseSqlExecuteRespVO respVO = new DatabaseSqlExecuteRespVO();

        try {
            // 获取 WebService 数据源配置
            WebServiceDO wsSource = webServiceMapper.selectById(reqVO.getDatabaseId());
            if (wsSource == null) {
                throw exception(WEBSERVICE_DATASOURCE_NOT_EXISTS);
            }

            // 合并请求参数：请求 VO 中的值优先，否则使用数据源默认值
            String url = StrUtil.isNotBlank(reqVO.getUrl()) ? reqVO.getUrl() : wsSource.getUrl();
            String method = StrUtil.isNotBlank(reqVO.getMethod()) ? reqVO.getMethod() : wsSource.getMethod();
            String headers = StrUtil.isNotBlank(reqVO.getHeaders()) ? reqVO.getHeaders() : wsSource.getHeaders();
            String body = StrUtil.isNotBlank(reqVO.getBody()) ? reqVO.getBody() : wsSource.getBody();
            String authType = StrUtil.isNotBlank(reqVO.getAuthType()) ? reqVO.getAuthType() : wsSource.getAuthType();
            String authToken = StrUtil.isNotBlank(reqVO.getAuthToken()) ? reqVO.getAuthToken() : wsSource.getAuthToken();
            String responsePath = StrUtil.isNotBlank(reqVO.getResponsePath()) ? reqVO.getResponsePath() : wsSource.getResponsePath();

            // URL 参数替换
            if (reqVO.getParams() != null && !reqVO.getParams().isEmpty()) {
                for (Map.Entry<String, String> entry : reqVO.getParams().entrySet()) {
                    url = url.replace("{{" + entry.getKey() + "}}", entry.getValue());
                }
            }

            // 判断是否为 SOAP 请求
            String soapAction = StrUtil.isNotBlank(reqVO.getSoapAction()) ? reqVO.getSoapAction() : wsSource.getSoapAction();
            String soapNamespace = StrUtil.isNotBlank(reqVO.getSoapNamespace()) ? reqVO.getSoapNamespace() : wsSource.getSoapNamespace();
            boolean isSoap = StrUtil.isNotBlank(soapAction);

            // 执行请求
            String responseBody;
            if (isSoap) {
                responseBody = executeSoapRequest(url, soapNamespace, soapAction, body, headers, authType, authToken, reqVO.getParams());
            } else {
                responseBody = executeRestRequest(url, method, headers, body, authType, authToken, reqVO.getParams());
            }

            // 解析响应数据
            List<Map<String, Object>> results = parseResponse(responseBody, responsePath, isSoap);

            // 构建列信息
            List<String> columns = new ArrayList<>();
            if (!results.isEmpty()) {
                columns.addAll(results.get(0).keySet());
            }

            respVO.setSuccess(true);
            respVO.setResults(results);
            respVO.setResultSetList(List.of(results));
            respVO.setResultSetColumns(List.of(columns));
            respVO.setAffectedRows(results.size());
            respVO.setCostTime(System.currentTimeMillis() - startTime);

            log.info("WebService 执行成功: wsId={}, url={}, costTime={}ms, rows={}",
                    reqVO.getDatabaseId(), url, respVO.getCostTime(), results.size());
        } catch (Exception e) {
            log.error("WebService 执行失败: wsId={}, error={}", reqVO.getDatabaseId(), e.getMessage(), e);
            respVO.setSuccess(false);
            respVO.setErrorMessage("WebService 执行失败: " + e.getMessage());
            respVO.setCostTime(System.currentTimeMillis() - startTime);
        }

        return respVO;
    }

    @Override
    public boolean testWebServiceConnection(Long id) {
        try {
            WebServiceExecuteReqVO reqVO = new WebServiceExecuteReqVO();
            reqVO.setDatabaseId(id);
            DatabaseSqlExecuteRespVO result = executeWebService(reqVO);
            return result.getSuccess();
        } catch (Exception e) {
            log.warn("WebService 连接测试失败: id={}, error={}", id, e.getMessage());
            return false;
        }
    }

    /**
     * 执行 REST 请求
     */
    private String executeRestRequest(String url, String method, String headers, String body,
                                       String authType, String authToken, Map<String, String> params) {
        method = StrUtil.isNotBlank(method) ? method.toUpperCase() : "GET";

        HttpRequest request = switch (method) {
            case "POST" -> HttpRequest.post(url);
            case "PUT" -> HttpRequest.put(url);
            case "DELETE" -> HttpRequest.delete(url);
            default -> HttpRequest.get(url);
        };

        request.timeout(30000);
        request.charset(StandardCharsets.UTF_8);

        // 设置请求头
        if (StrUtil.isNotBlank(headers)) {
            JSONObject headerObj = JSONUtil.parseObj(headers);
            for (String key : headerObj.keySet()) {
                request.header(key, headerObj.getStr(key));
            }
        }

        // 设置认证
        applyAuth(request, authType, authToken);

        // 设置 URL 查询参数
        if (params != null && !params.isEmpty() && "GET".equals(method)) {
            request.form(new HashMap<>(params));
        }

        // 设置请求体
        if (StrUtil.isNotBlank(body) && ("POST".equals(method) || "PUT".equals(method))) {
            request.body(body);
        }

        try (HttpResponse response = request.execute()) {
            return response.body();
        }
    }

    /**
     * 执行 SOAP 请求
     */
    private String executeSoapRequest(String url, String namespace, String soapAction, String body,
                                       String headers, String authType, String authToken, Map<String, String> params) {
        String soapBody = body;
        if (StrUtil.isNotBlank(namespace) && !body.contains(":Envelope")) {
            StringBuilder soapEnvelope = new StringBuilder();
            soapEnvelope.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            soapEnvelope.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"");
            if (StrUtil.isNotBlank(namespace)) {
                soapEnvelope.append(" xmlns:web=\"").append(namespace).append("\"");
            }
            soapEnvelope.append(">");
            soapEnvelope.append("<soapenv:Header/>");
            soapEnvelope.append("<soapenv:Body>");
            soapEnvelope.append(body);
            soapEnvelope.append("</soapenv:Body>");
            soapEnvelope.append("</soapenv:Envelope>");
            soapBody = soapEnvelope.toString();
        }

        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                soapBody = soapBody.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }
        }

        HttpRequest request = HttpRequest.post(url)
                .timeout(30000)
                .charset(StandardCharsets.UTF_8)
                .header("Content-Type", "text/xml; charset=utf-8")
                .header("SOAPAction", StrUtil.isNotBlank(namespace) ? namespace + "/" + soapAction : soapAction)
                .body(soapBody);

        if (StrUtil.isNotBlank(headers)) {
            JSONObject headerObj = JSONUtil.parseObj(headers);
            for (String key : headerObj.keySet()) {
                request.header(key, headerObj.getStr(key));
            }
        }

        applyAuth(request, authType, authToken);

        try (HttpResponse response = request.execute()) {
            return response.body();
        }
    }

    /**
     * 应用认证配置
     */
    private void applyAuth(HttpRequest request, String authType, String authToken) {
        if (StrUtil.isBlank(authType) || "none".equals(authType) || StrUtil.isBlank(authToken)) {
            return;
        }
        switch (authType.toLowerCase()) {
            case "basic" -> request.header("Authorization", "Basic " + authToken);
            case "bearer" -> request.header("Authorization", "Bearer " + authToken);
            case "apikey" -> request.header("X-API-Key", authToken);
            default -> log.warn("不支持的认证类型: {}", authType);
        }
    }

    /**
     * 解析响应数据
     */
    private List<Map<String, Object>> parseResponse(String responseBody, String responsePath, boolean isSoap) {
        if (StrUtil.isBlank(responseBody)) {
            return Collections.emptyList();
        }

        try {
            Object jsonData;
            if (isSoap || responseBody.trim().startsWith("<")) {
                jsonData = parseXmlResponse(responseBody);
            } else {
                jsonData = JSONUtil.parse(responseBody);
            }

            Object extractedData = jsonData;
            if (StrUtil.isNotBlank(responsePath)) {
                extractedData = extractByJsonPath(jsonData, responsePath);
            }

            return convertToTableData(extractedData);
        } catch (Exception e) {
            log.warn("响应数据解析失败，返回原始数据: {}", e.getMessage());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("response", responseBody);
            return List.of(row);
        }
    }

    /**
     * 简易 JSONPath 提取
     */
    private Object extractByJsonPath(Object data, String jsonPath) {
        if (!jsonPath.startsWith("$")) {
            return data;
        }
        String path = jsonPath.substring(1);
        if (path.startsWith(".")) {
            path = path.substring(1);
        }

        Object current = data;
        String[] segments = path.split("\\.(?![^\\[]*\\])");

        for (String segment : segments) {
            if (StrUtil.isBlank(segment)) {
                continue;
            }
            if (segment.contains("[")) {
                String key = segment.substring(0, segment.indexOf("["));
                String indexStr = segment.substring(segment.indexOf("[") + 1, segment.indexOf("]"));
                if (StrUtil.isNotBlank(key) && current instanceof JSONObject jsonObj) {
                    current = jsonObj.get(key);
                }
                if (current instanceof JSONArray jsonArr) {
                    int index = Integer.parseInt(indexStr);
                    current = index < jsonArr.size() ? jsonArr.get(index) : null;
                }
            } else {
                if (current instanceof JSONObject jsonObj) {
                    current = jsonObj.get(segment);
                }
            }
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    /**
     * 将提取的数据转换为表格格式
     */
    private List<Map<String, Object>> convertToTableData(Object data) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (data instanceof JSONArray jsonArray) {
            for (int i = 0; i < jsonArray.size(); i++) {
                Object item = jsonArray.get(i);
                if (item instanceof JSONObject jsonObj) {
                    result.add(flatJsonToMap(jsonObj));
                } else {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("value", item);
                    result.add(row);
                }
            }
        } else if (data instanceof JSONObject jsonObj) {
            result.add(flatJsonToMap(jsonObj));
        } else {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("value", data);
            result.add(row);
        }
        return result;
    }

    /**
     * 将 JSONObject 展平为 Map
     */
    private Map<String, Object> flatJsonToMap(JSONObject jsonObj) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (String key : jsonObj.keySet()) {
            Object value = jsonObj.get(key);
            if (value instanceof JSONObject || value instanceof JSONArray) {
                map.put(key, JSONUtil.toJsonStr(value));
            } else {
                map.put(key, value);
            }
        }
        return map;
    }

    /**
     * 解析 XML 响应为 JSON 对象
     */
    private Object parseXmlResponse(String xml) {
        return JSONUtil.parseFromXml(xml);
    }

}
