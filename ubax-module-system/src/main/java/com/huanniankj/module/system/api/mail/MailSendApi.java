package com.huanniankj.module.system.api.mail;

import com.huanniankj.module.system.api.mail.dto.MailSendSingleToUserReqDTO;
import jakarta.validation.Valid;

/**
 * 邮箱发送 API 接口
 *
 * @author zhaoff
 */
public interface MailSendApi {

    /**
     * 发送单条邮箱给 Admin 用户
     * <p>
     * 在 mail 为空时，使用 userId 加载对应 Admin 的邮箱
     *
     * @param reqDTO 发送请求
     * @return 发送日志编号
     */
    Long sendSingleMailToAdmin(@Valid MailSendSingleToUserReqDTO reqDTO);

}
