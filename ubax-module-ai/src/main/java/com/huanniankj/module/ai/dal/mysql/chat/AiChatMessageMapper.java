package com.huanniankj.module.ai.dal.mysql.chat;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.common.util.collection.CollectionUtils;
import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.huanniankj.module.ai.controller.admin.chat.vo.message.AiChatMessagePageReqVO;
import com.huanniankj.module.ai.dal.dataobject.chat.AiChatMessageDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * AI 聊天对话 Mapper
 *
 * @author zhaoff
 */
@Mapper
public interface AiChatMessageMapper extends BaseMapperX<AiChatMessageDO> {

    default List<AiChatMessageDO> selectListByConversationId(Long conversationId) {
        return selectList(new LambdaQueryWrapperX<AiChatMessageDO>()
                .eq(AiChatMessageDO::getConversationId, conversationId)
                .orderByAsc(AiChatMessageDO::getId));
    }

    default Map<Long, Integer> selectCountMapByConversationId(Collection<Long> conversationIds) {
        // SQL count 查询
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<AiChatMessageDO>()
                .select("COUNT(id) AS count, conversation_id AS conversationId")
                .in("conversation_id", conversationIds)
                .groupBy("conversation_id"));
        if (CollUtil.isEmpty(result)) {
            return Collections.emptyMap();
        }
        // 转换数据
        return CollectionUtils.convertMap(result,
                record -> MapUtil.getLong(record, "conversationId"),
                record -> MapUtil.getInt(record, "count"));
    }

    default PageResult<AiChatMessageDO> selectPage(AiChatMessagePageReqVO pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<AiChatMessageDO>()
                .eqIfPresent(AiChatMessageDO::getConversationId, pageReqVO.getConversationId())
                .eqIfPresent(AiChatMessageDO::getUserId, pageReqVO.getUserId())
                .likeIfPresent(AiChatMessageDO::getContent, pageReqVO.getContent())
                .betweenIfPresent(AiChatMessageDO::getCreateTime, pageReqVO.getCreateTime())
                .orderByDesc(AiChatMessageDO::getId));
    }

}
