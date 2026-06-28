package com.huanniankj.module.source.dal.dataobject.database;

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

import java.time.LocalDateTime;

/**
 * 数据库脚本 DO
 *
 * @author zhaoff
 */
@TableName("source_database_script")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseScriptDO extends BaseDO {

    /**
     * 脚本ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 脚本名称
     */
    private String name;

    /**
     * 脚本编码
     */
    private String code;

    /**
     * 数据源ID
     */
    private Long databaseId;

    /**
     * 脚本类型（sql/procedure/view）
     */
    private String scriptType;

    /**
     * 脚本内容
     */
    private String scriptContent;

    /**
     * 脚本描述
     */
    private String description;

    /**
     * 结果映射表名
     */
    private String resultTableName;

    /**
     * 结果字段映射（JSON格式）
     */
    private String resultFieldMapping;

    /**
     * Cron表达式（定时执行）
     */
    private String cronExpression;

    /**
     * 执行次数
     */
    private Integer executeCount;

    /**
     * 最后执行时间
     */
    private LocalDateTime lastExecuteTime;

    /**
     * 最后执行状态（null-未执行 0-成功 1-失败）
     */
    private Integer lastExecuteStatus;

    /**
     * 状态（0-启用 1-禁用）
     */
    private Integer status;

    /**
     * 入参定义（JSON格式，如[{"name":"userId","type":"Long","required":true,"defaultValue":null}]）
     */
    private String inputParams;

    /**
     * 输出参数定义（JSON格式，存储过程用，如["out_code","out_msg"]）
     */
    private String outputParams;

    /**
     * 备注
     */
    private String remark;

}