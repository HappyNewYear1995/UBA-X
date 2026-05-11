package com.huanniankj.module.flink.job;

import com.alibaba.fastjson.JSONObject;
import com.huanniankj.module.flink.utils.DataCacheUtil;
import com.ip2location.IP2Location;
import com.ip2location.IPResult;
import lombok.extern.slf4j.Slf4j;
import nl.basjes.parse.useragent.AbstractUserAgentAnalyzer;
import nl.basjes.parse.useragent.AgentField;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.apache.commons.validator.routines.InetAddressValidator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * 日志清洗与加工服务类 (在 Flink 的算子中调用)
 * 注意：必须实现 Serializable 接口才能在 Flink 中分布式传输
 */
@Slf4j
public class LogProcess {

    // Yauaa: UserAgent 分析器
    private transient AbstractUserAgentAnalyzer userAgentAnalyzer;

    // IP2Location 分析器
    private transient IP2Location locIpV4;
    private transient IP2Location locIpV6;
    private transient InetAddressValidator validator;

    /**
     * 延迟初始化 UserAgentAnalyzer (因为 Yauaa 初始化较慢，且不可序列化)
     */
    private AbstractUserAgentAnalyzer getUserAgentAnalyzer() {
        if (userAgentAnalyzer == null) {
            userAgentAnalyzer = UserAgentAnalyzer
                    .newBuilder()
                    .hideMatcherLoadStats()
                    .withCache(10000)
                    .build();
        }
        return userAgentAnalyzer;
    }

    /**
     * 延迟初始化 IP2Location
     */
    private void initIp2Location() {
        if (validator == null) {
            validator = InetAddressValidator.getInstance();
        }
        if (locIpV4 == null) {
            locIpV4 = new IP2Location();
            try {
                // 优先从外部环境变量/系统属性读取路径，方便生产环境配置 (例如：-Dip2location.v4.path=/data/xxx.BIN)
                String ipV4Path = System.getProperty("ip2location.v4.path");

                if (ipV4Path != null && !ipV4Path.isEmpty()) {
                    locIpV4.Open(ipV4Path, true);
                    log.info("成功加载外部 IP2Location V4 库: {}", ipV4Path);
                } else {
                    // 直接从 DataCacheUtil 获取解析好的临时文件物理路径
                    String tempPath = DataCacheUtil.getIpV4File();
                    if (tempPath != null) {
                        locIpV4.Open(tempPath, true);
                        log.info("成功加载内置 IP2Location V4 库: {}", tempPath);
                    } else {
                        log.warn("未找到 IP2Location V4 库文件，跳过 IP 解析");
                    }
                }
            } catch (Exception e) {
                log.error("IP2Location V4 初始化失败", e);
            }
        }

        if (locIpV6 == null) {
            locIpV6 = new IP2Location();
            try {
                String ipV6Path = System.getProperty("ip2location.v6.path");
                if (ipV6Path != null && !ipV6Path.isEmpty()) {
                    locIpV6.Open(ipV6Path, true);
                } else {
                    // 直接从 DataCacheUtil 获取解析好的临时文件物理路径
                    String tempPath = DataCacheUtil.getIpV6File();
                    if (tempPath != null) {
                        locIpV6.Open(tempPath, true);
                    }
                }
            } catch (Exception e) {
                log.error("IP2Location V6 初始化失败", e);
            }
        }
    }

    /**
     * 核心清洗与加工逻辑
     */
    public JSONObject process(JSONObject json) {
        // 1. 兼容 Filebeat 直接打入 Kafka 的 Nginx 原始文本日志
        if (json.containsKey("message") && json.getString("logType") == null) {
            String nginxRawMsg = json.getString("message");
            json.put("logType", "AccessLog");
            json.put("remoteAddr", extractIpFromNginxLog(nginxRawMsg));
            json.put("requestUri", extractUriFromNginxLog(nginxRawMsg));
            json.put("status", extractStatusFromNginxLog(nginxRawMsg));

            if (!json.containsKey("logId")) {
                json.put("logId", UUID.randomUUID().toString().replace("-", ""));
            }
            json.remove("message");
        }

        // 2. 统一时间戳格式，确保 startTime 是长整型毫秒数
        long startTime = System.currentTimeMillis();
        if (json.containsKey("startTime") && json.getLong("startTime") != null) {
            startTime = json.getLong("startTime");
        } else {
            json.put("startTime", startTime);
        }

        // 3. 时间维度拆分 (方便 ClickHouse 聚合查询)
        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat hourFmt = new SimpleDateFormat("HH");
        SimpleDateFormat minFmt = new SimpleDateFormat("mm");
        Date date = new Date(startTime);

        json.put("statDate", Long.valueOf(dateFmt.format(date)));
        json.put("statHour", Long.valueOf(hourFmt.format(date)));
        json.put("statMin", Long.valueOf(minFmt.format(date)));

        // 4. User-Agent 解析 (提取浏览器、操作系统、设备型号等)
        String userAgentStr = json.getString("httpUserAgent");
        if (userAgentStr != null && !userAgentStr.isEmpty()) {
            UserAgent userAgent = getUserAgentAnalyzer().parse(userAgentStr);

            // 浏览器名称
            AgentField browserField = userAgent.get(UserAgent.AGENT_NAME);
            if (!browserField.isDefaultValue()) {
                json.put("browser", browserField.getValue());
            }

            // 浏览器版本
            AgentField browserVersionField = userAgent.get(UserAgent.AGENT_NAME_VERSION);
            if (!browserVersionField.isDefaultValue()) {
                json.put("browserVersion", browserVersionField.getValue());
            }

            // 设备型号
            AgentField deviceName = userAgent.get(UserAgent.DEVICE_NAME);
            if (!deviceName.isDefaultValue()) {
                json.put("model", deviceName.getValue());
            }

            // 品牌/制造商
            AgentField deviceBrand = userAgent.get(UserAgent.DEVICE_BRAND);
            if (!deviceBrand.isDefaultValue()) {
                json.put("brand", deviceBrand.getValue());
                json.put("manufacturer", deviceBrand.getValue());
            }

            // 代理分类 (例如: Browser, Robot/Spider, In-app browser)
            AgentField agentClass = userAgent.get(UserAgent.AGENT_CLASS);
            if (!agentClass.isDefaultValue()) {
                json.put("agentClass", agentClass.getValue());
            }
        }

        // 5. IP 地理位置解析 (通过 IP2Location)
        String remoteAddr = json.getString("remoteAddr");
        if (remoteAddr != null && !remoteAddr.isEmpty() && !json.containsKey("province")) {
            initIp2Location();
            IPResult rec = null;
            try {
                if (validator.isValidInet4Address(remoteAddr)) {
                    // 如果加载了 bin 文件，这里会返回真实结果
                    rec = locIpV4.IPQuery(remoteAddr);
                } else if (validator.isValidInet6Address(remoteAddr)) {
                    rec = locIpV6.IPQuery(remoteAddr);
                }
            } catch (Exception e) {
                log.error("query ip error ", e);
            }

            if (rec != null && "OK".equalsIgnoreCase(rec.getStatus())) {
                String country = rec.getCountryShort().toLowerCase(Locale.ROOT);
                String province = rec.getRegion().toLowerCase(Locale.ROOT);
                String city = rec.getCity().toLowerCase(Locale.ROOT);

                if ("-".equalsIgnoreCase(country)) country = "";
                if ("-".equalsIgnoreCase(province)) province = "";
                if ("-".equalsIgnoreCase(city)) city = "";

                // 简单的台湾/港澳转换逻辑
                if (!country.isEmpty()) {
                    if (country.equalsIgnoreCase("tw")) {
                        country = "cn";
                        province = "taiwan";
                    } else if (country.equalsIgnoreCase("hk")) {
                        country = "cn";
                        province = "hongkong";
                        city = "hongkong";
                    } else if (country.equalsIgnoreCase("mo")) {
                        country = "cn";
                        province = "macau";
                        city = "macau";
                    }
                }

                json.put("country", country);
                json.put("province", province);
                json.put("city", city);
            }
        }

        // 6. 数据安全校验：移除会干扰 Mybatis-Plus/ClickHouse 插入的自动填充字段
        json.remove("creator");
        json.remove("updater");
        json.remove("deleted");
        json.remove("createTime");
        json.remove("updateTime");

        return json;
    }

    // --- 模拟的 Nginx 正则提取函数 ---
    private String extractIpFromNginxLog(String log) {
        return "127.0.0.1";
    }

    private String extractUriFromNginxLog(String log) {
        return "/api/test";
    }

    private String extractStatusFromNginxLog(String log) {
        return "200";
    }
}