package com.huanniankj.module.analysis.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huanniankj.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

import java.time.LocalDate;

/**
 * 路径分析结果 DO
 *
 * @author zhaoff
 */
@TableName("analysis_path_result")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PathResultDO extends BaseDO {

    /**
     * 结果 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 路径配置 ID
     */
    private Long configId;

    /**
     * 统计日期
     */
    private LocalDate statDate;

    /**
     * 桑基图节点列表（JSON 格式，如 [{"name":"app_open"},{"name":"home_view"}]）
     */
    private String nodes;

    /**
     * 桑基图链接列表（JSON 格式，如 [{"source":"app_open","target":"home_view","value":1200}]）
     */
    private String links;

    /**
     * 路径统计列表（JSON 格式，如 [{"path":"app_open → home_view","users":1200,"percentage":45.0,"avgDuration":12.5}]）
     */
    private String pathStats;

}
