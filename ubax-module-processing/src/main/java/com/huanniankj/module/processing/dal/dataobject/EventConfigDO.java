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
 * 事件配置 DO
 * <p>
 * 事件配置即为事件的清洗规则，定义如何从数据源中提取和转换数据生成标准化事件。
 *
 * @author zhaoff
 */
@TableName("collect_event_config")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventConfigDO extends BaseDO {

    /**
     * 事件配置 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 事件配置名称
     */
    private String name;

    /**
     * 事件编码（唯一标识，如 page_view、button_click、purchase）
     */
    private String code;

    /**
     * 数据源 ID
     */
    private Long dataSourceId;

    /**
     * 数据源类型（database / webservice / sdk）
     */
    private String dataSourceType;

    /**
     * 事件类型（page_view / click / custom）
     */
    private String eventType;

    /**
     * 事件属性定义（JSON 格式，定义事件包含哪些属性）
     */
    private String properties;

    /**
     * 过滤条件（JSON 格式，从数据源中筛选数据的规则）
     */
    private String filterCondition;

    /**
     * 字段映射规则（JSON 格式，数据源字段到事件属性的映射）
     */
    private String fieldMapping;

    /**
     * 状态（0=禁用 1=启用）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

}
