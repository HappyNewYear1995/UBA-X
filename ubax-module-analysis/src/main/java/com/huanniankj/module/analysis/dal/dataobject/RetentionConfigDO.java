package com.huanniankj.module.analysis.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huanniankj.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * 留存分析配置 DO
 *
 * @author zhaoff
 */
@TableName("analysis_retention_config")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetentionConfigDO extends BaseDO {

    /**
     * 配置 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 配置名称
     */
    private String name;

    /**
     * 留存类型 (next_day/7_days/30_days)
     */
    private String retentionType;

    /**
     * 统计维度 (day/week/month)
     */
    private String dimension;

    /**
     * 起始事件（为空则统计所有新用户）
     */
    private String startEvent;

    /**
     * 回访事件（为空则统计所有活跃用户）
     */
    private String returnEvent;

    /**
     * 备注
     */
    private String remark;

}
