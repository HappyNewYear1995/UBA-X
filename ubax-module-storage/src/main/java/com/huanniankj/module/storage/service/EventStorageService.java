package com.huanniankj.module.storage.service;

import java.util.List;

/**
 * ClickHouse 存储服务接口
 */
public interface EventStorageService {

    /**
     * 批量将消息保存到 ClickHouse 数据库中
     *
     * @param eventJsonList JSON 格式的事件消息列表
     */
    void saveEventsBatch(List<String> eventJsonList);
}
