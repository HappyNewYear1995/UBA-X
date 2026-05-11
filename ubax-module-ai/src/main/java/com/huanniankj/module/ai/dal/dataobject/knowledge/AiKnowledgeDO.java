package com.huanniankj.module.ai.dal.dataobject.knowledge;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huanniankj.framework.common.enums.CommonStatusEnum;
import com.huanniankj.framework.mybatis.core.dataobject.BaseDO;
import com.huanniankj.module.ai.dal.dataobject.model.AiModelDO;
import lombok.Data;

/**
 * AI 知识库 DO
 *
 * @author zhaoff
 */
@TableName(value = "ai_knowledge", autoResultMap = true)
@Data
public class AiKnowledgeDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 知识库名称
     */
    private String name;

    /**
     * 知识库描述
     */
    private String description;

    /**
     * 向量模型编号
     * <p>
     * 关联 {@link AiModelDO#getId()}
     */
    private Long embeddingModelId;

    /**
     * 模型标识
     * <p>
     * 冗余 {@link AiModelDO#getModel()}
     */
    private String embeddingModel;

    /**
     * topK
     */
    private Integer topK;

    /**
     * 相似度阈值
     */
    private Double similarityThreshold;

    /**
     * 状态
     * <p>
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
