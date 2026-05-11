package com.huanniankj.framework.tracer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * BizTracer配置类
 *
 * @author zhaoff
 */
@ConfigurationProperties("ubax.tracer")
@Data
public class TracerProperties {
}
