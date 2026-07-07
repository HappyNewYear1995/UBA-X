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

@TableName("source_processing_script_log")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingScriptLogDO extends BaseDO {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long scriptId;

    private String scriptName;

    private String scriptCode;

    private String executeType;

    private String scriptContent;

    private String inputParams;

    private Integer status;

    private String errorMessage;

    private Long costTime;

    private Long resultRecordCount;

    private Integer persisted;

    private String persistError;

    private String executeResult;

}
