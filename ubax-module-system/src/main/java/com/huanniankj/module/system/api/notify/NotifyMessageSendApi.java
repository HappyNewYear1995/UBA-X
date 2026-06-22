package com.huanniankj.module.system.api.notify;

import com.huanniankj.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import jakarta.validation.Valid;

/**
 * 站内信发送 API 接口
 *
 * @author zhaoff
 */
public interface NotifyMessageSendApi {

    /**
     * 发送单条站内信给 Admin 用户
     *
     * @param reqDTO 发送请求
     * @return 发送消息 ID
     */
    Long sendSingleMessageToAdmin(@Valid NotifySendSingleToUserReqDTO reqDTO);

}
