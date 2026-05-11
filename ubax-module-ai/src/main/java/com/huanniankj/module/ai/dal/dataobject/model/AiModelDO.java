package com.huanniankj.module.ai.dal.dataobject.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huanniankj.framework.common.enums.CommonStatusEnum;
import com.huanniankj.framework.mybatis.core.dataobject.BaseDO;
import com.huanniankj.module.ai.enums.model.AiModelTypeEnum;
import com.huanniankj.module.ai.enums.model.AiPlatformEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 模型 DO
 * <p>
 * 默认模型：{@link #status} 为开启，并且 {@link #sort} 排序第一
 *
 * @author zhaoff
 */
@TableName("ai_model")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiModelDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * API 秘钥编号
     * <p>
     * 关联 {@link AiApiKeyDO#getId()}
     */
    private Long keyId;

    /**
     * 模型名称
     */
    private String name;

    /**
     * 模型标志
     */
    private String model;

    /**
     * 平台
     * <p>
     * 枚举 {@link AiPlatformEnum}
     */
    private String platform;

    /**
     * 类型
     * <p>
     * 枚举 {@link AiModelTypeEnum}
     */
    private Integer type;

    /**
     * 排序值
     */
    private Integer sort;

    /**
     * 状态
     * <p>
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

    // ========== 对话配置 ==========

    /**
     * 温度参数
     * <p>
     * 用于调整生成回复的随机性和多样性程度：较低的温度值会使输出更收敛于高频词汇，较高的则增加多样性
     */
    private Double temperature;

    /**
     * 单条回复的最大 Token 数量
     */
    private Integer maxTokens;

    /**
     * 上下文的最大 Message 数量
     */
    private Integer maxContexts;

}
