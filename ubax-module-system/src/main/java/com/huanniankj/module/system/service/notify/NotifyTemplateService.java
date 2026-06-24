package com.huanniankj.module.system.service.notify;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.system.controller.notify.vo.template.NotifyTemplatePageReqVO;
import com.huanniankj.module.system.controller.notify.vo.template.NotifyTemplateSaveReqVO;
import com.huanniankj.module.system.dal.dataobject.notify.NotifyTemplateDO;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

/**
 * 站内信模版服务接口
 *
 * @author zhaoff
 */
public interface NotifyTemplateService {

    /**
     * 创建站内信模版
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createNotifyTemplate(@Valid NotifyTemplateSaveReqVO createReqVO);

    /**
     * 更新站内信模版
     *
     * @param updateReqVO 更新信息
     */
    void updateNotifyTemplate(@Valid NotifyTemplateSaveReqVO updateReqVO);

    /**
     * 删除站内信模版
     *
     * @param id 编号
     */
    void deleteNotifyTemplate(Long id);

    /**
     * 批量删除站内信模版
     *
     * @param ids 编号列表
     */
    void deleteNotifyTemplateList(List<Long> ids);

    /**
     * 获得站内信模版
     *
     * @param id 编号
     * @return 站内信模版
     */
    NotifyTemplateDO getNotifyTemplate(Long id);

    /**
     * 获得站内信模板，从缓存中
     *
     * @param code 模板编码
     * @return 站内信模板
     */
    NotifyTemplateDO getNotifyTemplateByCodeFromCache(String code);

    /**
     * 获得站内信模版分页
     *
     * @param pageReqVO 分页查询
     * @return 站内信模版分页
     */
    PageResult<NotifyTemplateDO> getNotifyTemplatePage(NotifyTemplatePageReqVO pageReqVO);

    /**
     * 格式化站内信内容
     *
     * @param content 站内信模板的内容
     * @param params  站内信内容的参数
     * @return 格式化后的内容
     */
    String formatNotifyTemplateContent(String content, Map<String, Object> params);

}
