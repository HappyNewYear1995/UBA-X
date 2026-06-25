package com.huanniankj.module.processing.dal.dataobject.event;

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
 * 事件 DO
 * <p>
 * 经过清洗管道处理后的标准化事件实体，采用分层属性模型设计：
 * <ul>
 *   <li>基础标识层：事件的全局唯一标识、类型、来源等基础信息</li>
 *   <li>主体层（Who）：触发事件的行为主体信息</li>
 *   <li>时空层（When & Where）：事件发生的时间和空间信息</li>
 *   <li>行为层（What & How）：事件的具体行为、目标和结果</li>
 *   <li>扩展层：原始数据、补充信息、标签等扩展属性</li>
 * </ul>
 * <p>
 * 事件为不可变对象（Immutable），一旦创建不可修改，仅支持删除操作。
 *
 * @author zhaoff
 */
@TableName("processing_event")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDO extends BaseDO {

    // ========== 基础标识层 ==========

    /**
     * 全局唯一标识
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 事件唯一标识（Snowflake/UUID v7）
     */
    private String eventId;

    /**
     * 事件类型编码（分层命名空间，如 auth.login.success）
     */
    private String eventType;

    /**
     * 事件大类（authentication/access/network/admin/system）
     */
    private String eventCategory;

    /**
     * 产生该事件的数据源标识
     */
    private String sourceId;

    /**
     * 数据清洗管道标识
     */
    private Long pipelineId;

    // ========== 主体层（Who） ==========

    /**
     * 行为主体唯一标识
     */
    private String actorId;

    /**
     * 主体类型（user/service_account/device/application）
     */
    private String actorType;

    /**
     * 主体附加属性快照（JSON）
     */
    private String actorAttributes;

    // ========== 时空层（When & Where） ==========

    /**
     * 事件发生的原始时间
     */
    private LocalDateTime eventTime;

    /**
     * 事件被系统摄入的时间
     */
    private LocalDateTime ingestTime;

    /**
     * 事件经清洗管道处理完成的时间
     */
    private LocalDateTime processTime;

    /**
     * 原始时区信息
     */
    private String eventTimeZone;

    /**
     * 地理位置信息
     */
    private String location;

    // ========== 行为层（What & How） ==========

    /**
     * 具体动作（login/file_download/api_call等）
     */
    private String action;

    /**
     * 操作的目标资源
     */
    private String targetResource;

    /**
     * 目标资源类型
     */
    private String targetResourceType;

    /**
     * 事件结果（success/failure/denied）
     */
    private String result;

    /**
     * 事件严重等级（info/low/medium/high/critical）
     */
    private String severity;

    // ========== 扩展层 ==========

    /**
     * 原始事件数据完整保留（JSON）
     */
    private String rawEvent;

    /**
     * 清洗管道补充的上下文信息（JSON）
     */
    private String enrichments;

    /**
     * 标签数组（JSON array）
     */
    private String tags;

    /**
     * 关联的会话标识
     */
    private String sessionId;

    /**
     * 关联标识（串联相关事件）
     */
    private String correlationId;

    /**
     * 备注
     */
    private String remark;

}
