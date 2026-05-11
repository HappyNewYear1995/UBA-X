package com.huanniankj.module.system.dal.dataobject.notice;

import com.baomidou.mybatisplus.annotation.TableName;
import com.huanniankj.framework.common.enums.CommonStatusEnum;
import com.huanniankj.framework.mybatis.core.dataobject.BaseDO;
import com.huanniankj.module.system.enums.notice.NoticeTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知公告表
 *
 * @author zhaoff
 */
@TableName("system_notice")
@Data
@EqualsAndHashCode(callSuper = true)
public class NoticeDO extends BaseDO {

    /**
     * 公告ID
     */
    private Long id;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告类型
     * <p>
     * 枚举 {@link NoticeTypeEnum}
     */
    private Integer type;

    /**
     * 公告内容
     */
    private String content;

    /**
     * 公告状态
     * <p>
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
