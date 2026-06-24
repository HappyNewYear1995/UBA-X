package com.huanniankj.module.processing.dal.dataobject;

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

/**
 * 清洗管道 DO
 * <p>
 * 用于定义数据清洗的管道配置，包括格式转换、数据清洗、数据映射、数据过滤等类型。
 *
 * @author zhaoff
 */
@TableName("collect_clean_pipeline")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CleanPipelineDO extends BaseDO {

    /**
     * 管道 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 管道名称
     */
    private String name;

    /**
     * 管道类型（format_convert/data_clean/data_map/data_filter）
     */
    private String type;

    /**
     * 管道描述
     */
    private String description;

    /**
     * 管道配置（JSON格式，定义清洗规则）
     */
    private String config;

    /**
     * 关联事件配置ID
     */
    private Long eventId;

    /**
     * 状态（0=禁用 1=启用）
     */
    private Integer status;

    /**
     * 已处理数据量
     */
    private Long processedCount;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;

}
