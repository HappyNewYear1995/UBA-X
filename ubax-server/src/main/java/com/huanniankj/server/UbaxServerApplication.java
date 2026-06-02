package com.huanniankj.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 项目的启动类
 *
 * @author zhaoff
 */
@SuppressWarnings("SpringComponentScan") // 忽略 IDEA 无法识别 ${ubax.info.base-package}
@SpringBootApplication(scanBasePackages = {"${ubax.info.base-package}.server", "${ubax.info.base-package}.module"})
@EnableScheduling
public class UbaxServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(UbaxServerApplication.class, args);
    }

}
