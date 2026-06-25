package com.huanniankj.module.analysis.dal.dataobject.funnel;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huanniankj.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * 漏斗分析配置 DO
 *
 * @author zhaoff
 */
@TableName("analysis_funnel_config")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FunnelConfigDO extends BaseDO {

    /**
     * 配置 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 漏斗名称
     */
    private String name;

    /**
     * 漏斗步骤事件列表（JSON 数组格式，如 ["app_open","home_view","product_detail"]）
     */
    private String steps;

    /**
     * 窗口时间（秒，同一用户完成所有步骤的最大时间间隔，0 表示不限）
     */
    private Integer windowTime;

    /**
     * 备注
     */
    private String remark;

}
