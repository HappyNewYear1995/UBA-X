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
 * 异常日志 DO
 * <p>
 * 记录数据采集过程中出现的异常信息，包括格式错误、重复数据、非法字符等。
 *
 * @author zhaoff
 */
@TableName("collect_error_log")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorLogDO extends BaseDO {

    /**
     * 日志 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 异常类型（format_error/duplicate_data/invalid_char/timestamp_error/other）
     */
    private String errorType;

    /**
     * 数据来源
     */
    private String source;

    /**
     * 异常记录数
     */
    private Integer errorCount;

    /**
     * 处理动作（auto_filter/auto_dedup/mark_pending/manual_handle）
     */
    private String action;

    /**
     * 异常详情（JSON格式）
     */
    private String detail;

    /**
     * 关联清洗管道ID
     */
    private Long pipelineId;

    /**
     * 关联事件ID
     */
    private Long eventId;

    /**
     * 处理状态（0=待处理 1=已处理 2=已忽略）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

}
