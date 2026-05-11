package com.huanniankj.module.ai.dal.dataobject.knowledge;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huanniankj.framework.common.enums.CommonStatusEnum;
import com.huanniankj.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;

/**
 * AI 知识库-文档分段 DO
 *
 * @author zhaoff
 */
@TableName(value = "ai_knowledge_segment")
@Data
public class AiKnowledgeSegmentDO extends BaseDO {

    /**
     * 向量库的编号 - 空值
     */
    public static final String VECTOR_ID_EMPTY = "";

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
     * 文档编号
     * <p>
     * 关联 {@link AiKnowledgeDocumentDO#getId()}
     */
    private Long documentId;

    /**
     * 切片内容
     */
    private String content;

    /**
     * 切片内容长度
     */
    private Integer contentLength;

    /**
     * 向量库的编号
     */
    private String vectorId;

    /**
     * token 数量
     */
    private Integer tokens;

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
