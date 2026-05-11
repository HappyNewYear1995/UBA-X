package com.huanniankj.module.ai.dal.dataobject.chat;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.huanniankj.framework.mybatis.core.dataobject.BaseDO;
import com.huanniankj.framework.mybatis.core.type.LongListTypeHandler;
import com.huanniankj.framework.mybatis.core.type.StringListTypeHandler;
import com.huanniankj.module.ai.dal.dataobject.knowledge.AiKnowledgeSegmentDO;
import com.huanniankj.module.ai.dal.dataobject.model.AiChatRoleDO;
import com.huanniankj.module.ai.dal.dataobject.model.AiModelDO;
import com.huanniankj.module.ai.framework.ai.core.webserch.AiWebSearchResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.MessageType;

import java.util.List;

/**
 * AI Chat 消息 DO
 *
 * @author zhaoff
 */
@TableName(value = "ai_chat_message", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatMessageDO extends BaseDO {

    /**
     * 编号，作为每条聊天记录的唯一标识符
     */
    @TableId
    private Long id;

    /**
     * 对话编号
     * <p>
     * 关联 {@link AiChatConversationDO#getId()} 字段
     */
    private Long conversationId;
    /**
     * 回复消息编号
     * <p>
     * 关联 {@link #id} 字段
     * <p>
     * 大模型回复的消息编号，用于“问答”的关联
     */
    private Long replyId;

    /**
     * 消息类型
     * <p>
     * 也等价于 OpenAPI 的 role 字段
     * <p>
     * 枚举 {@link MessageType}
     */
    private String type;

    /**
     * 用户编号
     * <p>
     * 关联 AdminUserDO 的 userId 字段
     */
    private Long userId;

    /**
     * 角色编号
     * <p>
     * 关联 {@link AiChatRoleDO#getId()} 字段
     */
    private Long roleId;

    /**
     * 模型标志
     * <p>
     * 冗余 {@link AiModelDO#getModel()}
     */
    private String model;

    /**
     * 模型编号
     * <p>
     * 关联 {@link AiModelDO#getId()} 字段
     */
    private Long modelId;

    /**
     * 聊天内容
     */
    private String content;

    /**
     * 推理内容
     */
    private String reasoningContent;

    /**
     * 是否携带上下文
     */
    private Boolean useContext;

    /**
     * 知识库段落编号数组
     * <p>
     * 关联 {@link AiKnowledgeSegmentDO#getId()} 字段
     */
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> segmentIds;

    /**
     * 联网搜索的网页内容数组
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<AiWebSearchResponse.WebPage> webSearchPages;

    /**
     * 附件 URL 数组
     */
    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> attachmentUrls;

}
