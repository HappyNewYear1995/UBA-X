package com.huanniankj.module.gather.dal.dataobject.database;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huanniankj.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * 数据库脚本执行日志 DO
 *
 * @author zhaoff
 */
@TableName("source_database_script_log")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseScriptLogDO extends BaseDO {

    /**
     * 日志ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 脚本ID
     */
    private Long scriptId;

    /**
     * 脚本名称
     */
    private String scriptName;

    /**
     * 脚本编码
     */
    private String scriptCode;

    /**
     * 数据源ID
     */
    private Long databaseId;

    /**
     * 执行类型（manual-手动执行/scheduled-定时执行）
     */
    private String executeType;

    /**
     * 执行的脚本内容
     */
    private String scriptContent;

    /**
     * 执行结果（JSON格式）
     */
    private String executeResult;

    /**
     * 影响行数
     */
    private Long affectedRows;

    /**
     * 输出参数（存储过程用，JSON格式）
     */
    private String outputParams;

    /**
     * 执行状态（0-成功 1-失败）
     */
    private Integer status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 执行耗时（毫秒）
     */
    private Long costTime;

    /**
     * 结果记录数
     */
    private Long resultRecordCount;

    /**
     * 是否已持久化结果（0-未持久化 1-已持久化）
     */
    private Integer persisted;

    /**
     * 持久化错误信息
     */
    private String persistError;

}