package com.huanniankj.module.source.service.webservice;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.source.controller.webservice.vo.WebServicePageReqVO;
import com.huanniankj.module.source.controller.webservice.vo.WebServiceRespVO;
import com.huanniankj.module.source.controller.webservice.vo.WebServiceSaveReqVO;

/**
 * WebService 数据源服务接口
 *
 * @author zhaoff
 */
public interface WebServiceSourceService {

    /**
     * 创建 WebService 数据源
     *
     * @param saveReqVO 创建请求
     * @return 数据源 ID
     */
    Long createWebServiceSource(WebServiceSaveReqVO saveReqVO);

    /**
     * 更新 WebService 数据源
     *
     * @param saveReqVO 更新请求
     */
    void updateWebServiceSource(WebServiceSaveReqVO saveReqVO);

    /**
     * 删除 WebService 数据源
     *
     * @param id 数据源 ID
     */
    void deleteWebServiceSource(Long id);

    /**
     * 获取 WebService 数据源详情
     *
     * @param id 数据源 ID
     * @return 数据源详情
     */
    WebServiceRespVO getWebServiceSource(Long id);

    /**
     * 分页查询 WebService 数据源
     *
     * @param pageReqVO 分页查询条件
     * @return 分页结果
     */
    PageResult<WebServiceRespVO> getWebServiceSourcePage(WebServicePageReqVO pageReqVO);

    /**
     * 测试 WebService 连接
     *
     * @param id 数据源 ID
     * @return 连接是否成功
     */
    Boolean testConnection(Long id);

}
