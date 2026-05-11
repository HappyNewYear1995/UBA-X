package com.huanniankj.module.flink.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "地址")
public class RegionDTO {
    private String clientIp = "";

    private String country = "";

    private String province = "";

    private String city = "";
}
