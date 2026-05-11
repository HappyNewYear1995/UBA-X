package com.huanniankj.module.ai.controller.admin.model.vo.tool;

import com.huanniankj.framework.common.enums.CommonStatusEnum;
import com.huanniankj.framework.common.pojo.PageParam;
import com.huanniankj.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.huanniankj.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - AI 工具分页 Request VO")
@Data
public class AiToolPageReqVO extends PageParam {

    @Schema(description = "工具名称", example = "ubax")
    private String name;

    @Schema(description = "工具描述", example = "ubax")
    private String description;

    @Schema(description = "状态", example = "1")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
