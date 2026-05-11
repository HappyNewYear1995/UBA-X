package com.huanniankj.module.collect.service;

import com.huanniankj.module.collect.controller.app.vo.ClientEventTrackReqVO;
import com.huanniankj.module.collect.controller.app.vo.UniversalLogReqVO;
import jakarta.servlet.http.HttpServletRequest;

public interface EventCollectService {

    /**
     * 处理精简版的客户端行为事件上报 (例如来自 JS SDK、iOS、Android)
     * 该方法会将接收到的精简字段映射到大宽表结构，并交由后续或 Flink 补全
     * 
     * @param request HTTP请求对象，用于提取隐式信息 (IP, UserAgent, Cookie等)
     * @param reqVO 客户端传来的精简业务数据
     */
    void processClientEvent(HttpServletRequest request, ClientEventTrackReqVO reqVO);

    /**
     * 处理全量大宽表通用日志上报 (例如服务端埋点，或某些高级场景直接传完整结构)
     * 
     * @param request HTTP请求对象
     * @param reqVO 完整的日志宽表数据
     */
    void processUniversalLog(HttpServletRequest request, UniversalLogReqVO reqVO);
}
