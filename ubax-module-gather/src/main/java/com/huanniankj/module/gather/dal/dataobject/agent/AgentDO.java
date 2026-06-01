package com.huanniankj.module.gather.dal.dataobject.agent;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huanniankj.framework.mybatis.core.dataobject.BaseDO;
import com.huanniankj.module.gather.enums.agent.AgentStatusEnum;
import com.huanniankj.module.gather.enums.agent.CollectorStatusEnum;
import com.huanniankj.module.gather.enums.agent.PlatformEnum;
import com.huanniankj.module.gather.enums.agent.TerminalEnum;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Agent 探针 DO
 *
 * @author zhaoff
 */
@TableName("gather_agent")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentDO extends BaseDO {

    /**
     * Agent ID
     */
    @TableId
    private Long id;

    /**
     * Agent UUID
     */
    private String uuid;

    /**
     * 主机名
     */
    private String hostname;

    /**
     * Agent 版本
     */
    private String version;

    /**
     * 终端类型
     * <p>
     * 枚举 {@link TerminalEnum}
     */
    private Integer terminal;

    /**
     * 平台类型
     * <p>
     * 枚举 {@link PlatformEnum}
     */
    private Integer platform;

    /**
     * 状态
     * 枚举 {@link AgentStatusEnum}
     */
    private Integer status;

    /**
     * 采集器状态 (unknown/running/stopped)
     * 枚举 {@link CollectorStatusEnum}
     */
    private String collectorStatus;

    /**
     * 在线状态 (true: 在线, false: 离线)
     */
    private Boolean online;

    /**
     * 最后心跳时间
     */
    private LocalDateTime lastHeartbeat;

    /**
     * IP 地址
     */
    private String ip;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 配置信息
     */
    private String config;

    /**
     * 备注
     */
    private String remark;

}
