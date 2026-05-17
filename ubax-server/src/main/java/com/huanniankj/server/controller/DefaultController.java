package com.huanniankj.server.controller;

import com.huanniankj.framework.common.pojo.CommonResult;
import com.huanniankj.framework.common.util.servlet.ServletUtils;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 默认 Controller
 *
 * @author zhaoff
 */
@RestController
@Slf4j
public class DefaultController {

    /**
     * 测试接口：打印 query、header、body
     */
    @RequestMapping(value = {"/test"})
    @PermitAll
    public CommonResult<Boolean> test(HttpServletRequest request) {
        // 打印查询参数
        log.info("Query: {}", ServletUtils.getParamMap(request));
        // 打印请求头
        log.info("Header: {}", ServletUtils.getHeaderMap(request));
        // 打印请求体
        log.info("Body: {}", ServletUtils.getBody(request));
        return CommonResult.success(true);
    }

}
