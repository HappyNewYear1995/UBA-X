package com.huanniankj.module.collect.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.huanniankj.framework.common.util.servlet.ServletUtils;
import com.huanniankj.framework.ip.core.Area;
import com.huanniankj.framework.ip.core.utils.IPUtils;
import com.huanniankj.module.collect.controller.app.vo.ClientEventTrackReqVO;
import com.huanniankj.module.collect.controller.app.vo.UniversalLogReqVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 事件数据采集与清洗服务实现类
 */
@Service
@Slf4j
public class EventCollectServiceImpl implements EventCollectService {

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    // 前端上报的数据进入 Flink 前的原始缓冲队列
    private static final String TOPIC_RAW_LOG = "ubax.user.behavior.web.raw";

    @Override
    public void processClientEvent(HttpServletRequest request, ClientEventTrackReqVO clientReqVO) {
        if (StrUtil.isBlank(clientReqVO.getEventName())) {
            log.warn("精简接口收到无效上报: eventName 为空");
            return;
        }

        // TODO: 可在此处接入 Schema Registry 验证 clientReqVO.getProperties() 的合法性

        // 1. 将客户端精简字段适配转换为宽表 DO 结构
        UniversalLogReqVO reqVO = new UniversalLogReqVO();
        reqVO.setLogType("ClickLog"); // SDK 发来的默认当做用户行为事件日志
        reqVO.setLogId(IdUtil.fastSimpleUUID());
        reqVO.setUserId(clientReqVO.getUserId());
        reqVO.setDeviceId(clientReqVO.getDeviceId());
        reqVO.setUuid(clientReqVO.getSessionId()); // 这里的 uuid 在你的宽表里如果是 sessionId 概念的话
        reqVO.setRequestUri(clientReqVO.getUrl());
        reqVO.setHttpReferer(clientReqVO.getReferrer());
        reqVO.setProperties(clientReqVO.getProperties());
        
        // 客户端如果有传准确时间，则优先用客户端的，否则用服务端接收时间
        long currentTimeMillis = System.currentTimeMillis();
        reqVO.setStartTime(clientReqVO.getTimestamp() != null ? clientReqVO.getTimestamp() : currentTimeMillis);
        
        // msec 字段在 ClickHouse 中是 Nullable(String) 格式的 Unix 时间，精确到毫秒
        reqVO.setMsec(String.valueOf(reqVO.getStartTime()));

        // 2. 调用通用的处理逻辑，由后端环境补全 IP, Header 等隐式信息
        this.processUniversalLog(request, reqVO);
    }

    @Override
    public void processUniversalLog(HttpServletRequest request, UniversalLogReqVO reqVO) {
        // TODO: 可在此处增加基于 appId 或 IP 的限流逻辑 (Rate Limiting)，防止恶意刷量
        
        // 1. 基础数据校验，如果没有传入 logId，则自动生成
        if (StrUtil.isBlank(reqVO.getLogId())) {
            reqVO.setLogId(IdUtil.fastSimpleUUID());
        }

        // 2. 时间处理：如果客户端没有传 startTime，用服务器当前时间补全
        long serverTime = System.currentTimeMillis();
        if (reqVO.getStartTime() == null) {
            reqVO.setStartTime(serverTime);
        }
        
        // 同样为 msec 字段补全毫秒级时间戳字符串
        if (StrUtil.isBlank(reqVO.getMsec())) {
            reqVO.setMsec(String.valueOf(reqVO.getStartTime()));
        }

        // 3. 提取和补全基础 HTTP 环境信息
        // (注意：IP解析、UserAgent解析、时间维度拆分等计算密集型清洗逻辑已经移交到 Flink 处理)
        if (StrUtil.isBlank(reqVO.getRemoteAddr())) {
            reqVO.setRemoteAddr(ServletUtils.getClientIP(request));
        }
        if (StrUtil.isBlank(reqVO.getHttpUserAgent())) {
            reqVO.setHttpUserAgent(ServletUtils.getUserAgent(request));
        }
        if (StrUtil.isBlank(reqVO.getCookie())) {
            reqVO.setCookie(request.getHeader("Cookie"));
        }
        if (StrUtil.isBlank(reqVO.getHttpReferer())) {
            reqVO.setHttpReferer(request.getHeader("Referer"));
        }
        if (StrUtil.isBlank(reqVO.getRequestMethod())) {
            reqVO.setRequestMethod(request.getMethod());
        }
        if (StrUtil.isBlank(reqVO.getScheme())) {
            reqVO.setScheme(request.getScheme());
        }

        // 4. 统一发往 Kafka (发送给 Flink 处理的原始数据队列)
        String jsonMsg = JSONUtil.toJsonStr(reqVO);
        log.info("【日志采集】基础数据补全完毕，发往 Flink 队列, LogId={}, LogType={}, Topic={}", reqVO.getLogId(), reqVO.getLogType(), TOPIC_RAW_LOG);
        
        String routeKey = StrUtil.isNotBlank(reqVO.getDeviceId()) ? reqVO.getDeviceId() : 
                          StrUtil.isNotBlank(reqVO.getUuid()) ? reqVO.getUuid() : reqVO.getLogId();
                          
        kafkaTemplate.send(TOPIC_RAW_LOG, routeKey, jsonMsg);
    }
}
