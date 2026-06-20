package com.huanniankj.module.source.dal.dataobject.event;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huanniankj.framework.mybatis.core.dataobject.BaseDO;
import com.huanniankj.module.source.enums.event.EventLevelEnum;
import com.huanniankj.module.source.enums.event.EventSourceEnum;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 事件管理 DO
 *
 * @author zhaoff
 */
@TableName("gather_event")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDO extends BaseDO {

    /**
     * 事件 ID
     */
    @TableId
    private Long id;

    /**
     * Agent UUID
     */
    private String agentUuid;

    /**
     * 事件类型
     */
    private String eventType;

    /**
     * 事件级别
     * <p>
     * 枚举 {@link EventLevelEnum}
     */
    private Integer eventLevel;

    /**
     * 事件来源
     * <p>
     * 枚举 {@link EventSourceEnum}
     */
    private String eventSource;

    /**
     * 事件标题
     */
    private String title;

    /**
     * 事件详情
     */
    private String content;

    /**
     * 事件标签，逗号分隔
     */
    private String tags;

    /**
     * 事件发生时间
     */
    private LocalDateTime eventTime;

    /**
     * 是否已处理 (true: 已处理, false: 未处理)
     */
    private Boolean handled;

    /**
     * 处理人
     */
    private String handler;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 处理备注
     */
    private String handleRemark;

}
