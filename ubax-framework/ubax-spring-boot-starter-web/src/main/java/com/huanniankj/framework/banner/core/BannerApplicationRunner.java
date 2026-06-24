package com.huanniankj.framework.banner.core;

import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.util.ClassUtils;

import java.util.concurrent.TimeUnit;

/**
 * 项目启动成功后，提供文档相关的地址
 *
 * @author zhaoff
 */
@Slf4j
public class BannerApplicationRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        ThreadUtil.execute(() -> {
            ThreadUtil.sleep(1, TimeUnit.SECONDS); // 延迟 1 秒，保证输出到结尾
            log.info("\n----------------------------------------------------------\n\t" +
                    "项目启动成功！\n\t" +
                    "----------------------------------------------------------");

            if (isNotPresent("com.huanniankj.module.source.framework.security.config.SecurityConfiguration")) {
                System.out.println("[数据源模块 ubax-module-source - 已禁用]");
            } else if (isNotPresent("com.huanniankj.module.processing.framework.security.config.SecurityConfiguration")) {
                System.out.println("[数据处理模块 ubax-module-processing - 已禁用]");
            } else if (isNotPresent("com.huanniankj.module.analysis.framework.security.config.SecurityConfiguration")) {
                System.out.println("[数据分析模块 ubax-module-analysis - 已禁用]");
            } else if (isNotPresent("com.huanniankj.module.app.framework.security.config.SecurityConfiguration")) {
                System.out.println("[数据应用模块 ubax-module-app - 已禁用]");
            }
        });
    }

    private static boolean isNotPresent(String className) {
        return !ClassUtils.isPresent(className, ClassUtils.getDefaultClassLoader());
    }

}
