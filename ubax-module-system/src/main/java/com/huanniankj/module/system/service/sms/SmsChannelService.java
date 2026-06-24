package com.huanniankj.module.system.service.sms;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.system.controller.sms.vo.channel.SmsChannelPageReqVO;
import com.huanniankj.module.system.controller.sms.vo.channel.SmsChannelSaveReqVO;
import com.huanniankj.module.system.dal.dataobject.sms.SmsChannelDO;
import com.huanniankj.module.system.framework.sms.core.client.SmsClient;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 短信渠道服务接口
 *
 * @author zhaoff
 */
public interface SmsChannelService {

    /**
     * 创建短信渠道
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSmsChannel(@Valid SmsChannelSaveReqVO createReqVO);

    /**
     * 更新短信渠道
     *
     * @param updateReqVO 更新信息
     */
    void updateSmsChannel(@Valid SmsChannelSaveReqVO updateReqVO);

    /**
     * 删除短信渠道
     *
     * @param id 编号
     */
    void deleteSmsChannel(Long id);

    /**
     * 批量删除短信渠道
     *
     * @param ids 编号数组
     */
    void deleteSmsChannelList(List<Long> ids);

    /**
     * 获得短信渠道
     *
     * @param id 编号
     * @return 短信渠道
     */
    SmsChannelDO getSmsChannel(Long id);

    /**
     * 获得所有短信渠道列表
     *
     * @return 短信渠道列表
     */
    List<SmsChannelDO> getSmsChannelList();

    /**
     * 获得短信渠道分页
     *
     * @param pageReqVO 分页查询
     * @return 短信渠道分页
     */
    PageResult<SmsChannelDO> getSmsChannelPage(SmsChannelPageReqVO pageReqVO);

    /**
     * 获得短信客户端
     *
     * @param id 编号
     * @return 短信客户端
     */
    SmsClient getSmsClient(Long id);

    /**
     * 获得短信客户端
     *
     * @param code 编码
     * @return 短信客户端
     */
    SmsClient getSmsClient(String code);

}
