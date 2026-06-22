package com.huanniankj.module.analysis.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "留存分析结果响应")
public class RetentionResultRespVO {

    @Schema(description = "结果 ID")
    private Long id;

    @Schema(description = "留存配置 ID")
    private Long configId;

    @Schema(description = "统计日期")
    private LocalDate statDate;

    @Schema(description = "新增用户数")
    private Long newUsers;

    @Schema(description = "各天留存用户数")
    private Map<Integer, Long> retentionUsers;

    @Schema(description = "各天留存率(%)")
    private Map<Integer, Double> retentionRates;

}
