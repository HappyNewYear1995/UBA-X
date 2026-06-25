package com.huanniankj.module.analysis.service.path;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.huanniankj.module.analysis.controller.path.vo.PathAnalysisReqVO;
import com.huanniankj.module.analysis.controller.path.vo.PathAnalysisRespVO;
import com.huanniankj.module.analysis.dal.clickhouse.EventAnalysisMapper;
import com.huanniankj.module.analysis.dal.dataobject.base.EventAnalysisDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 路径分析服务实现
 * <p>
 * 基于用户行为日志，统计用户从一个事件到下一个事件的流转路径，
 * 生成桑基图数据（节点+链接）和路径统计表格。
 *
 * @author zhaoff
 */
@Service
@Slf4j
public class PathAnalysisServiceImpl implements PathAnalysisService {

    @Autowired(required = false)
    private EventAnalysisMapper eventAnalysisMapper;

    @Override
    public PathAnalysisRespVO analyzePath(PathAnalysisReqVO reqVO) {
        if (eventAnalysisMapper == null) {
            log.warn("ClickHouse 数据源未配置，路径分析不可用");
            return PathAnalysisRespVO.builder()
                    .nodes(Collections.emptyList())
                    .links(Collections.emptyList())
                    .pathStats(Collections.emptyList())
                    .build();
        }

        int maxDepth = reqVO.getMaxDepth() != null ? reqVO.getMaxDepth() : 7;

        // 查询时间范围内按设备ID和开始时间排序的事件序列
        QueryWrapper<EventAnalysisDO> wrapper = new QueryWrapper<>();
        if (reqVO.getStartTime() != null) {
            wrapper.ge("start_time", reqVO.getStartTime());
        }
        if (reqVO.getEndTime() != null) {
            wrapper.le("start_time", reqVO.getEndTime());
        }
        if (reqVO.getStartEvent() != null && !reqVO.getStartEvent().isEmpty()) {
            wrapper.eq("request_uri", reqVO.getStartEvent());
        }

        // 统计事件之间的流转关系：按相邻事件对统计用户数
        // 使用 SQL 查询相邻事件对
        wrapper.select(
                "request_uri AS source_event",
                "count(1) AS event_count",
                "uniqExact(device_id) AS uv"
        );
        wrapper.groupBy("request_uri");
        wrapper.orderByDesc("uv");

        List<Map<String, Object>> eventStats = eventAnalysisMapper.selectMaps(wrapper);

        // 查询事件流转对：使用子查询获取每个设备的下一个事件
        // 简化实现：统计所有事件的出现次数，然后基于高频事件构建路径
        QueryWrapper<EventAnalysisDO> pairWrapper = new QueryWrapper<>();
        if (reqVO.getStartTime() != null) {
            pairWrapper.ge("start_time", reqVO.getStartTime());
        }
        if (reqVO.getEndTime() != null) {
            pairWrapper.le("start_time", reqVO.getEndTime());
        }

        // 查询事件流转对（相邻事件）
        String pairSql = "SELECT " +
                "a.request_uri AS source_event, " +
                "b.request_uri AS target_event, " +
                "uniqExact(a.device_id) AS uv, " +
                "avg(b.request_time) AS avg_time " +
                "FROM ubax_user_behavior_log a " +
                "INNER JOIN ubax_user_behavior_log b " +
                "ON a.device_id = b.device_id " +
                "AND b.start_time > a.start_time " +
                "AND b.start_time <= a.start_time + INTERVAL 30 MINUTE ";

        if (reqVO.getStartTime() != null) {
            pairSql += "AND a.start_time >= '" + reqVO.getStartTime() + "' ";
        }
        if (reqVO.getEndTime() != null) {
            pairSql += "AND a.start_time <= '" + reqVO.getEndTime() + "' ";
        }
        if (reqVO.getStartEvent() != null && !reqVO.getStartEvent().isEmpty()) {
            pairSql += "AND a.request_uri = '" + reqVO.getStartEvent() + "' ";
        }

        pairSql += "GROUP BY a.request_uri, b.request_uri " +
                "ORDER BY uv DESC " +
                "LIMIT 50";

        // 使用原生 SQL 查询
        List<Map<String, Object>> pairStats = eventAnalysisMapper.selectMaps(
                new QueryWrapper<EventAnalysisDO>().apply(pairSql)
        );

        // 构建桑基图数据
        Set<String> nodeNames = new LinkedHashSet<>();
        List<PathAnalysisRespVO.SankeyLink> links = new ArrayList<>();

        for (Map<String, Object> pair : pairStats) {
            String source = String.valueOf(pair.get("source_event"));
            String target = String.valueOf(pair.get("target_event"));
            long uv = pair.get("uv") != null ? ((Number) pair.get("uv")).longValue() : 0;

            if (source.equals(target)) continue;

            nodeNames.add(source);
            nodeNames.add(target);

            links.add(PathAnalysisRespVO.SankeyLink.builder()
                    .source(source)
                    .target(target)
                    .value(uv)
                    .build());
        }

        List<PathAnalysisRespVO.SankeyNode> nodes = nodeNames.stream()
                .map(name -> PathAnalysisRespVO.SankeyNode.builder().name(name).build())
                .collect(Collectors.toList());

        // 构建路径统计
        // 基于桑基图链接，汇总主要路径
        Map<String, PathAnalysisRespVO.PathStat> pathMap = new LinkedHashMap<>();
        long totalPathUsers = links.stream().mapToLong(PathAnalysisRespVO.SankeyLink::getValue).sum();

        // 从起始事件开始，追踪路径
        if (reqVO.getStartEvent() != null && !reqVO.getStartEvent().isEmpty()) {
            buildPaths(reqVO.getStartEvent(), links, pathMap, "", 0, maxDepth);
        } else if (!nodes.isEmpty()) {
            buildPaths(nodes.get(0).getName(), links, pathMap, "", 0, maxDepth);
        }

        List<PathAnalysisRespVO.PathStat> pathStats = pathMap.values().stream()
                .sorted((a, b) -> Long.compare(b.getUsers(), a.getUsers()))
                .limit(20)
                .collect(Collectors.toList());

        // 计算占比
        long pathTotalUsers = pathStats.stream().mapToLong(PathAnalysisRespVO.PathStat::getUsers).sum();
        for (PathAnalysisRespVO.PathStat stat : pathStats) {
            if (pathTotalUsers > 0) {
                stat.setPercentage(Math.round(stat.getUsers() * 10000.0 / pathTotalUsers) / 100.0);
            }
        }

        return PathAnalysisRespVO.builder()
                .nodes(nodes)
                .links(links)
                .pathStats(pathStats)
                .build();
    }

    /**
     * 递归构建路径
     */
    private void buildPaths(String currentNode, List<PathAnalysisRespVO.SankeyLink> links,
                            Map<String, PathAnalysisRespVO.PathStat> pathMap,
                            String currentPath, int depth, int maxDepth) {
        if (depth >= maxDepth) return;

        String newPath = currentPath.isEmpty() ? currentNode : currentPath + " → " + currentNode;

        // 查找当前节点的所有出边
        List<PathAnalysisRespVO.SankeyLink> outLinks = links.stream()
                .filter(l -> l.getSource().equals(currentNode))
                .collect(Collectors.toList());

        if (outLinks.isEmpty()) {
            // 叶子节点，记录路径
            long users = links.stream()
                    .filter(l -> l.getTarget().equals(currentNode))
                    .mapToLong(PathAnalysisRespVO.SankeyLink::getValue)
                    .max().orElse(0L);
            if (users > 0) {
                pathMap.put(newPath, PathAnalysisRespVO.PathStat.builder()
                        .path(newPath)
                        .users(users)
                        .percentage(0.0)
                        .avgDuration(0.0)
                        .build());
            }
            return;
        }

        for (PathAnalysisRespVO.SankeyLink link : outLinks) {
            buildPaths(link.getTarget(), links, pathMap, newPath, depth + 1, maxDepth);
        }
    }
}
