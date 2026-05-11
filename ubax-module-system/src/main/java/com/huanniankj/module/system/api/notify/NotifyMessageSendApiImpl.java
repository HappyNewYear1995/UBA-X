package com.huanniankj.module.system.api.notify;

import com.huanniankj.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import com.huanniankj.module.system.service.notify.NotifySendService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 站内信发送 API 实现类
 *
 * @author zhaoff
 */
@Service
public class NotifyMessageSendApiImpl implements NotifyMessageSendApi {

    @Resource
    private NotifySendService notifySendService;

    @Override
    public Long sendSingleMessageToAdmin(NotifySendSingleToUserReqDTO reqDTO) {
        return notifySendService.sendSingleNotifyToAdmin(reqDTO.getUserId(),
                reqDTO.getTemplateCode(), reqDTO.getTemplateParams());
    }

    @Override
    public Long sendSingleMessageToMember(NotifySendSingleToUserReqDTO reqDTO) {
        return notifySendService.sendSingleNotifyToMember(reqDTO.getUserId(),
                reqDTO.getTemplateCode(), reqDTO.getTemplateParams());
    }

}
