package com.huanniankj.module.source.dal.dataobject.event;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huanniankj.framework.mybatis.core.dataobject.BaseDO;
import com.huanniankj.module.source.enums.event.MatchLogicEnum;
import com.huanniankj.module.source.enums.event.MatchPositionEnum;
import com.huanniankj.module.source.enums.event.MatchTypeEnum;
import lombok.*;

/**
 * 事件配置 DO
 *
 * @author zhaoff
 */
@TableName("gather_event_config")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventConfigDO extends BaseDO {

    /**
     * 配置 ID
     */
    @TableId
    private Long id;

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 配置描述
     */
    private String configDesc;

    /**
     * 匹配位置
     * <p>
     * 枚举 {@link MatchPositionEnum}
     */
    private String matchPosition;

    /**
     * 匹配类型
     * <p>
     * 枚举 {@link MatchTypeEnum}
     */
    private String matchType;

    /**
     * 匹配规则值
     */
    private String matchValue;

    /**
     * 匹配逻辑
     * <p>
     * 枚举 {@link MatchLogicEnum}
     */
    private String matchLogic;

    /**
     * 匹配后事件类型
     */
    private String eventType;

    /**
     * 匹配后事件级别
     */
    private Integer eventLevel;

    /**
     * 匹配后事件标题模板
     */
    private String eventTitleTemplate;

    /**
     * 是否启用 (true: 启用, false: 禁用)
     */
    private Boolean enabled;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;

}
