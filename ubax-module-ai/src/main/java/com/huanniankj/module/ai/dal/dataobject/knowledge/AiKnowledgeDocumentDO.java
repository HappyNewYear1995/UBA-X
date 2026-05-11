package com.huanniankj.module.ai.dal.dataobject.knowledge;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huanniankj.framework.common.enums.CommonStatusEnum;
import com.huanniankj.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;

/**
 * AI 知识库-文档 DO
 *
 * @author zhaoff
 */
@TableName(value = "ai_knowledge_document")
@Data
public class AiKnowledgeDocumentDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 知识库编号
     * <p>
     * 关联 {@link AiKnowledgeDO#getId()}
     */
    private Long knowledgeId;

    /**
     * 文档名称
     */
    private String name;

    /**
     * 文件 URL
     */
    private String url;

    /**
     * 内容
     */
    private String content;

    /**
     * 文档长度
     */
    private Integer contentLength;

    /**
     * 文档 token 数量
     */
    private Integer tokens;

    /**
     * 分片最大 Token 数
     */
    private Integer segmentMaxTokens;

    /**
     * 召回次数
     */
    private Integer retrievalCount;

    /**
     * 状态
     * <p>
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
