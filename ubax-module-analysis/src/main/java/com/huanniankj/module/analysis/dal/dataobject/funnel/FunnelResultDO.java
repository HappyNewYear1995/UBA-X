package com.huanniankj.module.analysis.dal.dataobject.funnel;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huanniankj.framework.mybatis.core.dataobject.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

/**
 * 漏斗分析结果 DO
 *
 * @author zhaoff
 */
@TableName("analysis_funnel_result")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FunnelResultDO extends BaseDO {

    /**
     * 结果 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 漏斗配置 ID
     */
    private Long configId;

    /**
     * 统计日期
     */
    private LocalDate statDate;

    /**
     * 总用户数
     */
    private Long totalUsers;

    /**
     * 最终转化率(%)
     */
    private Double finalConversionRate;

    /**
     * 平均转化率(%)
     */
    private Double avgConversionRate;

    /**
     * 平均耗时(秒)
     */
    private Double avgDuration;

    /**
     * 漏斗步骤结果列表（JSON 格式，如 [{"stepName":"app_open","users":1200,"conversionRate":100.0,"overallRate":100.0,"lossRate":0.0,"avgTime":0.0}]）
     */
    private String steps;

}
