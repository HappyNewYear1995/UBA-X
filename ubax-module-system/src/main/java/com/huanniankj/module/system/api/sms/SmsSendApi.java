package com.huanniankj.module.system.api.sms;

import com.huanniankj.module.system.api.sms.dto.send.SmsSendSingleToUserReqDTO;
import jakarta.validation.Valid;

/**
 * 短信发送 API 接口
 *
 * @author zhaoff
 */
public interface SmsSendApi {

    /**
     * 发送单条短信给 Admin 用户
     * <p>
     * 在 mobile 为空时，使用 userId 加载对应 Admin 的手机号
     *
     * @param reqDTO 发送请求
     * @return 发送日志编号
     */
    Long sendSingleSmsToAdmin(@Valid SmsSendSingleToUserReqDTO reqDTO);

}
