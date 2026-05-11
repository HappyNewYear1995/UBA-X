package com.huanniankj.module.flink.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Arrays;
import java.util.List;

/**
 * Flink 流处理主任务：用户行为日志处理 (Data Standardization, Anti-Fraud, Enriching)
 * 流程: Kafka (Raw Topics) -> Flink -> Kafka (Clean Topic)
 */
@Slf4j
public class UserBehaviorLogProcessJob {

    // TODO: 生产环境应从 properties 文件或配置中心读取 Kafka 地址
    private static final String KAFKA_BROKERS = "127.0.0.1:9092";

    // TODO: 如果需要支持动态新增 Topic，可以考虑使用正则表达式匹配 Topic
    // 数据来源 topic 列表
    private static final List<String> RAW_TOPICS = Arrays.asList(
//            "ubax.user.behavior.nginx.raw",
            "ubax.user.behavior.web.raw"
    );
    private static final String CLEAN_TOPIC = "ubax.user.behavior.clean";
    private static final String CONSUMER_GROUP = "flink_user_behavior_processor";

    public static void main(String[] args) throws Exception {
        // 1. 设置运行环境
        Configuration conf = new Configuration();
        // 本地调试时，开启 Web UI，
        conf.setInteger(RestOptions.PORT, 8082);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf);

        // TODO: 生产环境应配置 Checkpoint (例如每 1-5 分钟一次)，并配置 State Backend (如 RocksDB)，确保精准一次语义
        // env.enableCheckpointing(60000);

        // 2. 配置 Kafka Source (读取原始日志)
        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers(KAFKA_BROKERS)
                .setTopics(RAW_TOPICS)
                .setGroupId(CONSUMER_GROUP)
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        // 将 Kafka Source 接入 DataStream
        DataStream<String> rawLogStream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Raw Log Source");

        // 3. 核心清洗与转换逻辑
        DataStream<String> cleanLogStream = rawLogStream
                // 3.1 过滤无效的脏数据 (Filter)
                .filter(jsonStr -> {
                    try {
                        if (jsonStr.startsWith("\"") && jsonStr.endsWith("\"")) {
                            jsonStr = jsonStr.substring(1, jsonStr.length() - 1).replace("\\\"", "\"");
                        }
                        JSONObject json = JSON.parseObject(jsonStr);
                        return json != null;
                    } catch (Exception e) {
                        log.error("无效的 JSON 格式, 直接丢弃: {}", jsonStr);
                        return false;
                    }
                })
                // 3.2 数据标准化、UserAgent 解析、维度充实 (Map)
                .map(new org.apache.flink.api.common.functions.RichMapFunction<String, String>() {
                    private transient LogProcess logProcessService;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        super.open(parameters);
                        // 在每个 TaskManager 节点真正运行时才初始化 Service，完美避开序列化问题
                        logProcessService = new LogProcess();
                    }

                    @Override
                    public String map(String jsonStr) throws Exception {
                        if (jsonStr.startsWith("\"") && jsonStr.endsWith("\"")) {
                            jsonStr = jsonStr.substring(1, jsonStr.length() - 1).replace("\\\"", "\"");
                        }

                        JSONObject json = JSON.parseObject(jsonStr);

                        // 调用提取出去的独立清洗服务
                        json = logProcessService.process(json);

                        // 最后将处理完毕的 JSON 转回字符串
                        return json.toJSONString();
                    }
                });

        // 4. 配置 Kafka Sink (将清洗后的数据写回 Kafka，供 Storage 模块消费落盘 ClickHouse)
        KafkaSink<String> sink = KafkaSink.<String>builder()
                .setBootstrapServers(KAFKA_BROKERS)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic(CLEAN_TOPIC)
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build()
                )
                .setDeliveryGuarantee(org.apache.flink.connector.base.DeliveryGuarantee.AT_LEAST_ONCE)
                .build();

        // 将流写入 Sink
        cleanLogStream.sinkTo(sink).name("Kafka Clean Log Sink");

        // 5. 触发执行
        log.info("开始提交 Flink 任务: UserBehaviorLogProcessJob");
        env.execute("UBAX User Behavior Log Process Job");
    }
}
