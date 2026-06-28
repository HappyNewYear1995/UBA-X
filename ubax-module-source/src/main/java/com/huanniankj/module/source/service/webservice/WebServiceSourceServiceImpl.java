package com.huanniankj.module.source.service.webservice;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.source.controller.database.vo.DatabaseSqlExecuteRespVO;
import com.huanniankj.module.source.controller.webservice.vo.WebServiceExecuteReqVO;
import com.huanniankj.module.source.controller.webservice.vo.WebServicePageReqVO;
import com.huanniankj.module.source.controller.webservice.vo.WebServiceRespVO;
import com.huanniankj.module.source.controller.webservice.vo.WebServiceSaveReqVO;
import com.huanniankj.module.source.convert.datasource.WebServiceSourceConvert;
import com.huanniankj.module.source.dal.dataobject.webservice.WebServiceDO;
import com.huanniankj.module.source.dal.mysql.webservice.WebServiceMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.huanniankj.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.huanniankj.module.source.enums.ErrorCodeConstants.*;

/**
 * WebService 数据源服务实现
 *
 * @author zhaoff
 */
@Service
@Slf4j
public class WebServiceSourceServiceImpl implements WebServiceSourceService {

    @Resource
    private WebServiceMapper webServiceMapper;

    @Resource
    @Lazy
    private WebServiceExecutionService webServiceExecutionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createWebServiceSource(WebServiceSaveReqVO saveReqVO) {
        // 校验名称唯一性
        validateWebServiceSourceNameUnique(null, saveReqVO.getName());
        // 转换
        WebServiceDO webServiceSource = WebServiceSourceConvert.INSTANCE.convert(saveReqVO);
        // 新增时状态默认为异常，需通过测试连接来更新状态
        webServiceSource.setStatus(1);
        // 校验 URL 格式
        validateUrl(webServiceSource.getUrl());
        // 保存
        webServiceMapper.insert(webServiceSource);
        // 测试连接
        testConnection(webServiceSource.getId());
        return webServiceSource.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWebServiceSource(WebServiceSaveReqVO saveReqVO) {
        // 校验数据源存在
        validateWebServiceSourceExists(saveReqVO.getId());
        // 校验名称唯一性
        validateWebServiceSourceNameUnique(saveReqVO.getId(), saveReqVO.getName());
        // 更新配置
        WebServiceDO updateObj = WebServiceSourceConvert.INSTANCE.convert(saveReqVO);
        // 认证凭据脱敏处理：如果为空或脱敏值，保留原值
        if (updateObj.getAuthToken() == null || updateObj.getAuthToken().isEmpty()
                || "******".equals(updateObj.getAuthToken())) {
            WebServiceDO existingConfig = webServiceMapper.selectById(saveReqVO.getId());
            updateObj.setAuthToken(existingConfig.getAuthToken());
        }
        // 校验 URL 格式
        if (updateObj.getUrl() != null && !updateObj.getUrl().isEmpty()) {
            validateUrl(updateObj.getUrl());
        }
        webServiceMapper.updateById(updateObj);
        testConnection(updateObj.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWebServiceSource(Long id) {
        // 校验数据源存在
        validateWebServiceSourceExists(id);
        // 删除配置
        webServiceMapper.deleteById(id);
    }

    @Override
    public WebServiceRespVO getWebServiceSource(Long id) {
        WebServiceDO config = webServiceMapper.selectById(id);
        if (config == null) {
            throw exception(WEBSERVICE_DATASOURCE_NOT_EXISTS);
        }
        return convertToRespVO(config);
    }

    @Override
    public PageResult<WebServiceRespVO> getWebServiceSourcePage(WebServicePageReqVO pageReqVO) {
        PageResult<WebServiceDO> pageResult = webServiceMapper.selectPage(pageReqVO);
        List<WebServiceRespVO> list = pageResult.getList().stream()
                .map(this::convertToRespVO)
                .toList();
        return new PageResult<>(list, pageResult.getTotal());
    }

    @Override
    public Boolean testConnection(Long id) {
        WebServiceDO webServiceSource = webServiceMapper.selectById(id);
        if (webServiceSource == null) {
            throw exception(WEBSERVICE_DATASOURCE_NOT_EXISTS);
        }

        // 构建 WebService 执行请求进行测试
        WebServiceExecuteReqVO reqVO = new WebServiceExecuteReqVO();
        reqVO.setDatabaseId(id);
        // 使用数据源默认配置发起测试请求
        boolean success;
        try {
            DatabaseSqlExecuteRespVO result = webServiceExecutionService.executeWebService(reqVO);
            success = result.getSuccess();
        } catch (Exception e) {
            log.warn("WebService 连接测试失败: id={}, name={}, error={}", id, webServiceSource.getName(), e.getMessage());
            success = false;
        }

        // 根据测试结果自动更新状态
        Integer newStatus = success ? 0 : 1;
        if (!newStatus.equals(webServiceSource.getStatus())) {
            webServiceSource.setStatus(newStatus);
            webServiceMapper.updateById(webServiceSource);
        }
        if (!success) {
            log.warn("WebService 连接测试失败: id={}, name={}", id, webServiceSource.getName());
        }
        return success;
    }

    /**
     * 校验数据源是否存在
     *
     * @param id 数据源 ID
     */
    private void validateWebServiceSourceExists(Long id) {
        WebServiceDO webServiceSource = webServiceMapper.selectById(id);
        if (webServiceSource == null) {
            throw exception(WEBSERVICE_DATASOURCE_NOT_EXISTS);
        }
    }

    /**
     * 校验数据源名称唯一性
     *
     * @param id   数据源 ID
     * @param name 数据源名称
     */
    private void validateWebServiceSourceNameUnique(Long id, String name) {
        WebServiceDO existWebService = webServiceMapper.selectByName(name);
        if (existWebService != null && !existWebService.getId().equals(id)) {
            throw exception(WEBSERVICE_DATASOURCE_NAME_DUPLICATE);
        }
    }

    /**
     * 校验 URL 格式
     *
     * @param url 服务地址
     */
    private void validateUrl(String url) {
        if (url == null || url.isEmpty()) {
            throw exception(WEBSERVICE_URL_INVALID);
        }
        try {
            new java.net.URL(url);
        } catch (Exception e) {
            log.warn("WebService URL 格式校验失败: url={}, error={}", url, e.getMessage());
            throw exception(WEBSERVICE_URL_INVALID);
        }
    }

    /**
     * 将 DO 转换为 RespVO，认证凭据脱敏
     *
     * @param webServiceSource DO 对象
     * @return RespVO 对象
     */
    private WebServiceRespVO convertToRespVO(WebServiceDO webServiceSource) {
        WebServiceRespVO respVO = WebServiceSourceConvert.INSTANCE.convert(webServiceSource);
        // 认证凭据脱敏
        if (respVO.getAuthToken() != null && !respVO.getAuthToken().isEmpty()) {
            respVO.setAuthToken("******");
        }
        return respVO;
    }

}
