package com.huanniankj.module.analysis.dal.dataobject.path;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huanniankj.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * 路径分析配置 DO
 *
 * @author zhaoff
 */
@TableName("analysis_path_config")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PathConfigDO extends BaseDO {

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
     * 起始事件（为空则从所有事件开始）
     */
    private String startEvent;

    /**
     * 最大路径深度
     */
    private Integer maxDepth;

    /**
     * 路径窗口时间（秒，同一用户路径中相邻事件的最大时间间隔）
     */
    private Integer windowTime;

    /**
     * 备注
     */
    private String remark;

}
