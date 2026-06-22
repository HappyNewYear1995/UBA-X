package com.huanniankj.module.source.convert.datasource;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.source.controller.webservice.vo.WebServiceRespVO;
import com.huanniankj.module.source.controller.webservice.vo.WebServiceSaveReqVO;
import com.huanniankj.module.source.dal.dataobject.webservice.WebServiceDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * WebService 数据源 Convert
 *
 * @author zhaoff
 */
@Mapper
public interface WebServiceSourceConvert {

    WebServiceSourceConvert INSTANCE = Mappers.getMapper(WebServiceSourceConvert.class);

    /**
     * DO 转 RespVO
     *
     * @param bean DO 对象
     * @return RespVO 对象
     */
    WebServiceRespVO convert(WebServiceDO bean);

    /**
     * DO 列表转 RespVO 列表
     *
     * @param list DO 列表
     * @return RespVO 列表
     */
    List<WebServiceRespVO> convertList(List<WebServiceDO> list);

    /**
     * DO 分页转 RespVO 分页
     *
     * @param page DO 分页
     * @return RespVO 分页
     */
    PageResult<WebServiceRespVO> convertPage(PageResult<WebServiceDO> page);

    /**
     * SaveReqVO 转 DO
     *
     * @param reqVO 保存请求
     * @return DO 对象
     */
    WebServiceDO convert(WebServiceSaveReqVO reqVO);

}
