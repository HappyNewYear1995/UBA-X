package com.huanniankj.module.system.controller.sms.vo.log;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.huanniankj.framework.excel.core.annotations.DictFormat;
import com.huanniankj.framework.excel.core.convert.DictConvert;
import com.huanniankj.framework.excel.core.convert.JsonConvert;
import com.huanniankj.module.system.enums.DictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 短信日志 Response VO
 *
 * @author zhaoff
 */
@Schema(description = "短信日志 Response VO")
@Data
@ExcelIgnoreUnannotated
public class SmsLogRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "短信渠道编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @ExcelProperty("短信渠道编号")
    private Long channelId;

    @Schema(description = "短信渠道编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "ALIYUN")
    @ExcelProperty("短信渠道编码")
    private String channelCode;

    @Schema(description = "模板编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    @ExcelProperty("模板编号")
    private Long templateId;

    @Schema(description = "模板编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "test-01")
    @ExcelProperty("模板编码")
    private String templateCode;

    @Schema(description = "短信类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty(value = "短信类型", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.SMS_TEMPLATE_TYPE)
    private Integer templateType;

    @Schema(description = "短信内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "你好，你的验证码是 1024")
    @ExcelProperty("短信内容")
    private String templateContent;

    @Schema(description = "短信参数", requiredMode = Schema.RequiredMode.REQUIRED, example = "name,code")
    @ExcelProperty(value = "短信参数", converter = JsonConvert.class)
    private Map<String, Object> templateParams;

    @Schema(description = "短信 API 的模板编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "SMS_1234")
    @ExcelProperty("短信 API 的模板编号")
    private String apiTemplateId;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "12312341234")
    @ExcelProperty("手机号")
    private String mobile;

    @Schema(description = "用户编号", example = "10")
    @ExcelProperty("用户编号")
    private Long userId;

    @Schema(description = "用户类型", example = "1")
    @ExcelProperty(value = "用户类型", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.USER_TYPE)
    private Integer userType;

    @Schema(description = "发送状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty(value = "发送状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.SMS_SEND_STATUS)
    private Integer sendStatus;

    @Schema(description = "发送时间")
    @ExcelProperty("发送时间")
    private LocalDateTime sendTime;

    @Schema(description = "短信 API 发送结果的编码", example = "SUCCESS")
    @ExcelProperty("短信 API 发送结果的编码")
    private String apiSendCode;

    @Schema(description = "短信 API 发送失败的提示", example = "成功")
    @ExcelProperty("短信 API 发送失败的提示")
    private String apiSendMsg;

    @Schema(description = "短信 API 发送返回的唯一请求 ID", example = "ubax")
    @ExcelProperty("短信 API 发送返回的唯一请求 ID")
    private String apiRequestId;

    @Schema(description = "短信 API 发送返回的序号", example = "1")
    @ExcelProperty("短信 API 发送返回的序号")
    private String apiSerialNo;

    @Schema(description = "接收状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty(value = "接收状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.SMS_RECEIVE_STATUS)
    private Integer receiveStatus;

    @Schema(description = "接收时间")
    @ExcelProperty("接收时间")
    private LocalDateTime receiveTime;

    @Schema(description = "API 接收结果的编码", example = "DELIVRD")
    @ExcelProperty("API 接收结果的编码")
    private String apiReceiveCode;

    @Schema(description = "API 接收结果的说明", example = "用户接收成功")
    @ExcelProperty("API 接收结果的说明")
    private String apiReceiveMsg;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
