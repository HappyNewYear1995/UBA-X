package com.huanniankj.module.processing.dal.dataobject.log;

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
 * 数据日志 DO
 * <p>
 * 记录采集到的原始数据日志信息，包括来源应用、设备、用户等维度。
 *
 * @author zhaoff
 */
@TableName("processing_data_log")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataLogDO extends BaseDO {

    /**
     * 日志 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 日志唯一标识
     */
    private String logId;

    /**
     * 来源应用标识
     */
    private String appId;

    /**
     * 事件类型
     */
    private String eventType;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 平台
     */
    private String platform;

    /**
     * IP地址
     */
    private String ip;

    /**
     * User Agent
     */
    private String userAgent;

    /**
     * 事件属性（JSON格式）
     */
    private String properties;

    /**
     * 关联事件ID
     */
    private Long eventId;

    /**
     * 关联清洗管道ID
     */
    private Long pipelineId;

    /**
     * 采集时间
     */
    private LocalDateTime collectTime;

    /**
     * 备注
     */
    private String remark;

}
