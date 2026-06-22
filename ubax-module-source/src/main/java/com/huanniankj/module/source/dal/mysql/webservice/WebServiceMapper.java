package com.huanniankj.module.source.dal.mysql.webservice;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.huanniankj.module.source.controller.webservice.vo.WebServicePageReqVO;
import com.huanniankj.module.source.dal.dataobject.webservice.WebServiceDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * WebService 数据源 Mapper
 *
 * @author zhaoff
 */
@Mapper
public interface WebServiceMapper extends BaseMapperX<WebServiceDO> {

    /**
     * 分页查询 WebService 数据源
     *
     * @param pageReqVO 分页查询条件
     * @return 分页结果
     */
    default PageResult<WebServiceDO> selectPage(WebServicePageReqVO pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<WebServiceDO>()
                .likeIfPresent(WebServiceDO::getName, pageReqVO.getName())
                .eqIfPresent(WebServiceDO::getStatus, pageReqVO.getStatus())
                .orderByDesc(WebServiceDO::getId));
    }

    /**
     * 根据数据源名称查询
     *
     * @param name 数据源名称
     * @return 数据源配置
     */
    default WebServiceDO selectByName(String name) {
        return selectOne(WebServiceDO::getName, name);
    }

}
