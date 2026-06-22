package com.huanniankj.module.analysis.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.huanniankj.module.analysis.controller.vo.FunnelAnalysisReqVO;
import com.huanniankj.module.analysis.controller.vo.FunnelAnalysisRespVO;
import com.huanniankj.module.analysis.dal.clickhouse.EventAnalysisMapper;
import com.huanniankj.module.analysis.dal.dataobject.EventAnalysisDO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 漏斗分析服务实现
 * <p>
 * 基于用户行为日志，按步骤统计每个步骤的用户数，
 * 计算步骤转化率、总转化率、流失率等指标。
 *
 * @author zhaoff
 */
@Service
@Slf4j
public class FunnelAnalysisServiceImpl implements FunnelAnalysisService {

    @Resource
    private EventAnalysisMapper eventAnalysisMapper;

    @Override
    public FunnelAnalysisRespVO analyzeFunnel(FunnelAnalysisReqVO reqVO) {
        List<String> steps = reqVO.getSteps();
        if (steps == null || steps.isEmpty()) {
            return FunnelAnalysisRespVO.builder()
                    .steps(Collections.emptyList())
                    .totalUsers(0L)
                    .finalConversionRate(0.0)
                    .avgConversionRate(0.0)
                    .avgDuration(0.0)
                    .build();
        }

        // 查询每个步骤的用户数
        List<FunnelAnalysisRespVO.FunnelStep> stepResults = new ArrayList<>();
        Long firstStepUsers = null;

        for (int i = 0; i < steps.size(); i++) {
            String stepEvent = steps.get(i);

            QueryWrapper<EventAnalysisDO> wrapper = new QueryWrapper<>();
            wrapper.eq("request_uri", stepEvent);

            if (reqVO.getStartTime() != null) {
                wrapper.ge("start_time", reqVO.getStartTime());
            }
            if (reqVO.getEndTime() != null) {
                wrapper.le("start_time", reqVO.getEndTime());
            }

            // 统计该步骤的去重用户数和平均耗时
            wrapper.select(
                    "uniqExact(device_id) AS uv",
                    "avg(request_time) AS avgReqTime"
            );

            List<Map<String, Object>> rows = eventAnalysisMapper.selectMaps(wrapper);
            long users = 0;
            double avgTime = 0.0;

            if (rows != null && !rows.isEmpty()) {
                Map<String, Object> row = rows.get(0);
                Object uvObj = row.get("uv");
                users = uvObj != null ? ((Number) uvObj).longValue() : 0;
                Object avgObj = row.get("avgReqTime");
                avgTime = avgObj != null ? ((Number) avgObj).doubleValue() : 0.0;
            }

            if (i == 0) {
                firstStepUsers = users;
            }

            // 计算转化率
            double conversionRate = 100.0;
            double overallRate = firstStepUsers > 0 ? (users * 100.0 / firstStepUsers) : 0.0;
            double lossRate = 0.0;

            if (i > 0 && stepResults.get(i - 1).getUsers() > 0) {
                conversionRate = users * 100.0 / stepResults.get(i - 1).getUsers();
                lossRate = 100.0 - conversionRate;
            } else if (i > 0) {
                conversionRate = 0.0;
                lossRate = 100.0;
            }

            stepResults.add(FunnelAnalysisRespVO.FunnelStep.builder()
                    .stepName(stepEvent)
                    .users(users)
                    .conversionRate(Math.round(conversionRate * 100.0) / 100.0)
                    .overallRate(Math.round(overallRate * 100.0) / 100.0)
                    .lossRate(Math.round(lossRate * 100.0) / 100.0)
                    .avgTime(Math.round(avgTime * 100.0) / 100.0)
                    .build());
        }

        // 计算汇总指标
        long totalUsers = firstStepUsers != null ? firstStepUsers : 0L;
        double finalConversionRate = stepResults.isEmpty() ? 0.0 : stepResults.get(stepResults.size() - 1).getOverallRate();

        double avgConversionRate = 0.0;
        if (stepResults.size() > 1) {
            List<Double> rates = stepResults.subList(1, stepResults.size())
                    .stream().map(FunnelAnalysisRespVO.FunnelStep::getConversionRate)
                    .collect(Collectors.toList());
            avgConversionRate = Math.round(rates.stream().mapToDouble(d -> d).average().orElse(0.0) * 100.0) / 100.0;
        }

        double avgDuration = stepResults.stream()
                .filter(s -> s.getAvgTime() != null && s.getAvgTime() > 0)
                .mapToDouble(FunnelAnalysisRespVO.FunnelStep::getAvgTime)
                .average().orElse(0.0);

        return FunnelAnalysisRespVO.builder()
                .steps(stepResults)
                .totalUsers(totalUsers)
                .finalConversionRate(Math.round(finalConversionRate * 100.0) / 100.0)
                .avgConversionRate(avgConversionRate)
                .avgDuration(Math.round(avgDuration * 100.0) / 100.0)
                .build();
    }
}
