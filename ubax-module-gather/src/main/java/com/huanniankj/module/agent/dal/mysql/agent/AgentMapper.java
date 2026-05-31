package com.huanniankj.module.agent.dal.mysql.agent;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.huanniankj.module.agent.controller.agent.vo.AgentPageReqVO;
import com.huanniankj.module.agent.dal.dataobject.AgentDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * Agent 探针 Mapper
 *
 * @author zhaoff
 */
@Mapper
public interface AgentMapper extends BaseMapperX<AgentDO> {

    default AgentDO selectByUUid(String uuid) {
        return selectOne(AgentDO::getUuid, uuid);
    }

    default PageResult<AgentDO> selectPage(AgentPageReqVO reqVO) {
        LambdaQueryWrapperX<AgentDO> query = new LambdaQueryWrapperX<AgentDO>()
                .likeIfPresent(AgentDO::getUuid, reqVO.getUuid())
                .likeIfPresent(AgentDO::getHostname, reqVO.getHostname())
                .likeIfPresent(AgentDO::getIp, reqVO.getIp())
                .eqIfPresent(AgentDO::getTerminal, reqVO.getTerminal())
                .eqIfPresent(AgentDO::getPlatform, reqVO.getPlatform())
                .eqIfPresent(AgentDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(AgentDO::getCreateTime, reqVO.getCreateTime());
        query.orderByDesc(AgentDO::getCreateTime); // 降序
        return selectPage(reqVO, query);
    }

}
