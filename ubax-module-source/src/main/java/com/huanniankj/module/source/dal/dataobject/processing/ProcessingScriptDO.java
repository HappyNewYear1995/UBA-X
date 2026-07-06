package com.huanniankj.module.source.dal.dataobject.processing;

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
 * 处理脚本 DO
 * <p>
 * Groovy 脚本，支持传参，可调用数据库脚本和 WebService，支持结果持久化。
 *
 * @author zhaoff
 */
@TableName("source_processing_script")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingScriptDO extends BaseDO {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    private String code;

    private String scriptContent;

    private String description;

    private String inputParams;

    private String resultTableName;

    private String resultFieldMapping;

    private String cronExpression;

    private Integer executeCount;

    private LocalDateTime lastExecuteTime;

    /**
     * 最后执行状态（null-未执行 0-成功 1-失败）
     */
    private Integer lastExecuteStatus;

    private Integer status;

    private String remark;

}
