package com.huanniankj.module.storage.mq.consumer;

import com.huanniankj.module.storage.service.EventStorageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 监听 Kafka 中的通用日志大宽表数据
 */
@Component
@Slf4j
public class EventTrackConsumer {

    @Resource
    private EventStorageService eventStorageService;

    // 修改消费的 Topic 名称与上报端对齐
    private static final String TOPIC_EVENT_TRACK = "ubax.user.behavior.clean";

    // TODO: 目前是单条消费。建议开启 Spring Kafka 的批量消费 (batch-listener=true) 并按 List<ConsumerRecord> 接收，以大幅提高吞吐量
   // @KafkaListener(topics = TOPIC_EVENT_TRACK, groupId = "ubax-storage-group")
    public void onMessage(ConsumerRecord<String, String> record) {
        if (record == null || record.value() == null) {
            return;
        }
        
        log.info("【存储模块】从 Kafka 拉取到 1 条通用日志数据");

        List<String> eventJsonList = new ArrayList<>(1);
        eventJsonList.add(record.value());

        try {
            // 调用 Service 执行 ClickHouse 的批量写入
            eventStorageService.saveEventsBatch(eventJsonList);
        } catch (Exception e) {
            log.error("【存储模块】批量写入 ClickHouse 失败", e);
        }
    }
}
