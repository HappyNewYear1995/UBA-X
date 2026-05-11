package com.huanniankj.module.ai.dal.dataobject.mindmap;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huanniankj.framework.mybatis.core.dataobject.BaseDO;
import com.huanniankj.module.ai.dal.dataobject.model.AiModelDO;
import com.huanniankj.module.ai.enums.model.AiPlatformEnum;
import lombok.Data;

/**
 * AI 思维导图 DO
 *
 * @author zhaoff
 */
@TableName(value = "ai_mind_map")
@Data
public class AiMindMapDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 用户编号
     * <p>
     * 关联 AdminUserDO 的 userId 字段
     */
    private Long userId;

    /**
     * 平台
     * <p>
     * 枚举 {@link AiPlatformEnum}
     */
    private String platform;

    /**
     * 模型编号
     * <p>
     * 关联 {@link AiModelDO#getId()}
     */
    private Long modelId;

    /**
     * 模型
     */
    private String model;

    /**
     * 生成内容提示
     */
    private String prompt;

    /**
     * 生成的内容
     */
    private String generatedContent;

    /**
     * 错误信息
     */
    private String errorMessage;

}
