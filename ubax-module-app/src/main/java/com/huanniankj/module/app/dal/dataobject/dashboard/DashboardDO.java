package com.huanniankj.module.app.dal.dataobject.dashboard;

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
 * 数据看板 DO
 *
 * @author zhaoff
 */
@TableName("app_dashboard")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDO extends BaseDO {

    /**
     * 看板ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 看板名称
     */
    private String name;

    /**
     * 看板描述
     */
    private String description;

    /**
     * 布局配置(JSON)
     */
    private String layoutConfig;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 备注
     */
    private String remark;

}
