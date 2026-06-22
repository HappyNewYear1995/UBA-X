package com.huanniankj.module.analysis.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huanniankj.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

import java.time.LocalDate;

/**
 * 留存分析结果 DO
 *
 * @author zhaoff
 */
@TableName("analysis_retention_result")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetentionResultDO extends BaseDO {

    /**
     * 结果 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 留存配置 ID
     */
    private Long configId;

    /**
     * 统计日期
     */
    private LocalDate statDate;

    /**
     * 新增用户数
     */
    private Long newUsers;

    /**
     * 各天留存用户数（JSON 格式，如 {"1":1200,"2":980,"3":850,"7":600}）
     */
    private String retentionUsers;

    /**
     * 各天留存率（JSON 格式，如 {"1":45.0,"2":38.0,"3":32.0,"7":20.0}）
     */
    private String retentionRates;

}
