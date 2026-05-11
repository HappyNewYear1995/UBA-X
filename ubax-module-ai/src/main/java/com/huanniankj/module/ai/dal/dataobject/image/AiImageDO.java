package com.huanniankj.module.ai.dal.dataobject.image;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.huanniankj.framework.mybatis.core.dataobject.BaseDO;
import com.huanniankj.module.ai.dal.dataobject.model.AiModelDO;
import com.huanniankj.module.ai.enums.image.AiImageStatusEnum;
import com.huanniankj.module.ai.enums.model.AiPlatformEnum;
import com.huanniankj.module.ai.framework.ai.core.model.midjourney.api.MidjourneyApi;
import com.huanniankj.module.system.api.user.dto.AdminUserRespDTO;
import lombok.Data;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.stabilityai.api.StabilityAiImageOptions;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI 绘画 DO
 *
 * @author zhaoff
 */
@TableName(value = "ai_image", autoResultMap = true)
@Data
public class AiImageDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 用户编号
     * <p>
     * 关联 {@link AdminUserRespDTO#getId()}
     */
    private Long userId;

    /**
     * 提示词
     */
    private String prompt;

    /**
     * 平台
     * <p>
     * 枚举 {@link AiPlatformEnum}
     */
    private String platform;

    /**
     * 模型编号
     * <p>
     * 关联 {@link AiModelDO#getId()}
     */
    private Long modelId;

    /**
     * 模型标识
     * <p>
     * 冗余 {@link AiModelDO#getModel()}
     */
    private String model;

    /**
     * 图片宽度
     */
    private Integer width;

    /**
     * 图片高度
     */
    private Integer height;

    /**
     * 生成状态
     * <p>
     * 枚举 {@link AiImageStatusEnum}
     */
    private Integer status;

    /**
     * 完成时间
     */
    private LocalDateTime finishTime;

    /**
     * 绘画错误信息
     */
    private String errorMessage;

    /**
     * 图片地址
     */
    private String picUrl;

    /**
     * 是否公开
     */
    private Boolean publicStatus;

    /**
     * 绘制参数，不同 platform 的不同参数
     * <p>
     * 1. {@link OpenAiImageOptions}
     * 2. {@link StabilityAiImageOptions}
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> options;

    /**
     * mj buttons 按钮
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<MidjourneyApi.Button> buttons;

    /**
     * 任务编号
     * <p>
     * 1. midjourney proxy：关联的 task id
     */
    private String taskId;

}

