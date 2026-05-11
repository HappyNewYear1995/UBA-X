package com.huanniankj.module.system.api.sms;

import com.huanniankj.module.system.api.sms.dto.code.SmsCodeSendReqDTO;
import com.huanniankj.module.system.api.sms.dto.code.SmsCodeUseReqDTO;
import com.huanniankj.module.system.api.sms.dto.code.SmsCodeValidateReqDTO;
import com.huanniankj.module.system.service.sms.SmsCodeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 短信验证码 API 实现类
 *
 * @author zhaoff
 */
@Service
@Validated
public class SmsCodeApiImpl implements SmsCodeApi {

    @Resource
    private SmsCodeService smsCodeService;

    @Override
    public void sendSmsCode(SmsCodeSendReqDTO reqDTO) {
        smsCodeService.sendSmsCode(reqDTO);
    }

    @Override
    public void useSmsCode(SmsCodeUseReqDTO reqDTO) {
        smsCodeService.useSmsCode(reqDTO);
    }

    @Override
    public void validateSmsCode(SmsCodeValidateReqDTO reqDTO) {
        smsCodeService.validateSmsCode(reqDTO);
    }

}
