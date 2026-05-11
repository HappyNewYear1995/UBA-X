package com.huanniankj.module.ai.dal.dataobject.workflow;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huanniankj.framework.common.enums.CommonStatusEnum;
import com.huanniankj.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;

/**
 * AI 工作流 DO
 *
 * @author zhaoff
 */
@TableName(value = "ai_workflow", autoResultMap = true)
@Data
public class AiWorkflowDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 工作流名称
     */
    private String name;

    /**
     * 工作流标识
     */
    private String code;

    /**
     * 工作流模型 JSON 数据
     */
    private String graph;

    /**
     * 备注
     */
    private String remark;

    /**
     * 状态
     * <p>
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
