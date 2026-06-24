package com.huanniankj.module.processing.controller;

import com.huanniankj.module.processing.controller.vo.ClientEventTrackReqVO;
import com.huanniankj.module.processing.controller.vo.UniversalLogReqVO;
import com.huanniankj.module.processing.service.EventCollectService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.annotation.Resource;

/**
 * 客户端数据采集网关
 */
@RestController
@RequestMapping("/collect")
public class EventCollectController {

    @Resource
    private EventCollectService eventCollectService;

    /**
     * 1. 核心精简接口：供 Web / App SDK 上报用户行为 (如点击、浏览)
     * 客户端只传 10 个左右的核心字段，其余的几十个字段（如 IP、地域、浏览器、OS）由后端/Flink 自动补全
     */
    @PostMapping("/track")
    public String trackClientEvent(HttpServletRequest request, @RequestBody ClientEventTrackReqVO reqVO) {
        eventCollectService.processClientEvent(request, reqVO);
        return "success";
    }

    /**
     * 2. 全量底层接口：供服务端或特殊代理直接上报大宽表数据 (例如：把已有数据的清洗结果重新打入)
     * 这是一个比较重型的接口，供有能力的系统使用
     */
    @PostMapping("/universal")
    public String trackUniversalLog(HttpServletRequest request, @RequestBody UniversalLogReqVO reqVO) {
        eventCollectService.processUniversalLog(request, reqVO);
        return "success";
    }
}
