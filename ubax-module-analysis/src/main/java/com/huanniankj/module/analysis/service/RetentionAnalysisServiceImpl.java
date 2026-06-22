package com.huanniankj.module.analysis.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.huanniankj.module.analysis.controller.vo.RetentionAnalysisReqVO;
import com.huanniankj.module.analysis.controller.vo.RetentionAnalysisRespVO;
import com.huanniankj.module.analysis.dal.clickhouse.EventAnalysisMapper;
import com.huanniankj.module.analysis.dal.dataobject.EventAnalysisDO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 留存分析服务实现
 * <p>
 * 基于用户行为日志，按日期统计新用户数和后续每天的留存用户数，
 * 计算留存率并生成趋势图和表格数据。
 *
 * @author zhaoff
 */
@Service
@Slf4j
public class RetentionAnalysisServiceImpl implements RetentionAnalysisService {

    @Resource
    private EventAnalysisMapper eventAnalysisMapper;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM-dd");
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public RetentionAnalysisRespVO analyzeRetention(RetentionAnalysisReqVO reqVO) {
        LocalDateTime startTime = reqVO.getStartTime();
        LocalDateTime endTime = reqVO.getEndTime();

        // 1. 查询时间范围内每天的新用户（首次出现的设备）
        List<RetentionAnalysisRespVO.RetentionRow> tableRows = new ArrayList<>();
        List<RetentionAnalysisRespVO.TrendItem> trendItems = new ArrayList<>();

        // 获取所有日期范围内首次出现的设备ID，按天分组
        LocalDate startDate = startTime.toLocalDate();
        LocalDate endDate = endTime.toLocalDate();

        // 查询全局最早出现时间，用于判断"新用户"
        // 简化实现：按天统计每天首次出现的设备数作为新用户
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(LocalTime.MAX);

            // 查询当天活跃的去重设备数
            QueryWrapper<EventAnalysisDO> activeWrapper = new QueryWrapper<>();
            activeWrapper.select("uniqExact(device_id) AS uv");
            activeWrapper.ge("start_time", dayStart);
            activeWrapper.le("start_time", dayEnd);
            List<Map<String, Object>> activeRows = eventAnalysisMapper.selectMaps(activeWrapper);
            long newUsers = 0;
            if (activeRows != null && !activeRows.isEmpty()) {
                Object uvObj = activeRows.get(0).get("uv");
                newUsers = uvObj != null ? ((Number) uvObj).longValue() : 0;
            }

            // 查询后续每天的留存用户数
            Map<Integer, Double> retentionRates = new LinkedHashMap<>();
            int maxDay = "30_days".equals(reqVO.getRetentionType()) ? 30
                    : "7_days".equals(reqVO.getRetentionType()) ? 7 : 7;

            for (int day = 1; day <= maxDay; day++) {
                LocalDate retentionDate = date.plusDays(day);
                if (retentionDate.isAfter(endDate)) break;

                LocalDateTime retDayStart = retentionDate.atStartOfDay();
                LocalDateTime retDayEnd = retentionDate.atTime(LocalTime.MAX);

                // 查询当天出现且在基准日也出现的设备数
                QueryWrapper<EventAnalysisDO> retWrapper = new QueryWrapper<>();
                retWrapper.select("uniqExact(device_id) AS uv");
                retWrapper.ge("start_time", retDayStart);
                retWrapper.le("start_time", retDayEnd);
                retWrapper.inSql("device_id",
                        "SELECT device_id FROM ubax_user_behavior_log " +
                                "WHERE start_time >= '" + dayStart.format(DATE_TIME_FMT) + "' " +
                                "AND start_time <= '" + dayEnd.format(DATE_TIME_FMT) + "' " +
                                "GROUP BY device_id");
                List<Map<String, Object>> retRows = eventAnalysisMapper.selectMaps(retWrapper);

                long retainedUsers = 0;
                if (retRows != null && !retRows.isEmpty()) {
                    Object uvObj = retRows.get(0).get("uv");
                    retainedUsers = uvObj != null ? ((Number) uvObj).longValue() : 0;
                }

                double rate = newUsers > 0 ? Math.round(retainedUsers * 10000.0 / newUsers) / 100.0 : 0.0;
                retentionRates.put(day, rate);
            }

            tableRows.add(RetentionAnalysisRespVO.RetentionRow.builder()
                    .date(date.format(DATE_FMT))
                    .newUsers(newUsers)
                    .retentionRates(retentionRates)
                    .build());

            // 趋势数据
            Double nextDayRate = retentionRates.getOrDefault(1, 0.0);
            Double day7Rate = retentionRates.getOrDefault(7, 0.0);
            Double day30Rate = retentionRates.getOrDefault(30, 0.0);

            trendItems.add(RetentionAnalysisRespVO.TrendItem.builder()
                    .date(date.format(DATE_FMT))
                    .nextDayRate(nextDayRate)
                    .day7Rate(day7Rate)
                    .day30Rate(day30Rate)
                    .build());
        }

        return RetentionAnalysisRespVO.builder()
                .trend(trendItems)
                .table(tableRows)
                .build();
    }
}
