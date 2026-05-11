package com.huanniankj.module.ai.dal.dataobject.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huanniankj.framework.mybatis.core.dataobject.BaseDO;
import com.huanniankj.module.ai.tool.function.DirectoryListToolFunction;
import com.huanniankj.module.ai.tool.function.WeatherQueryToolFunction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 工具 DO
 *
 * @author zhaoff
 */
@TableName("ai_tool")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiToolDO extends BaseDO {

    /**
     * 工具编号
     */
    @TableId
    private Long id;

    /**
     * 工具名称
     * <p>
     * 对应 Bean 的名字，例如说：
     * 1. {@link DirectoryListToolFunction} 的 Bean 名字是 directory_list
     * 2. {@link WeatherQueryToolFunction} 的 Bean 名字是 weather_query
     */
    private String name;

    /**
     * 工具描述
     */
    private String description;

    /**
     * 状态
     * <p>
     * 枚举 {@link com.huanniankj.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;

}
