package com.huanniankj.module.system.controller.dept.vo.post;

import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 岗位信息的精简 Response VO
 *
 * @author zhaoff
 */
@Schema(description = "岗位信息的精简 Response VO")
@Data
public class PostSimpleRespVO {

    @Schema(description = "岗位序号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("岗位序号")
    private Long id;

    @Schema(description = "岗位名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "ubax")
    @ExcelProperty("岗位名称")
    private String name;

}
