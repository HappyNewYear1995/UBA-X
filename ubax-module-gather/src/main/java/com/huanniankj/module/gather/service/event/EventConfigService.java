package com.huanniankj.module.gather.service.event;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.gather.controller.event.vo.EventConfigPageReqVO;
import com.huanniankj.module.gather.controller.event.vo.EventConfigRespVO;
import com.huanniankj.module.gather.controller.event.vo.EventConfigSaveReqVO;

import java.util.List;

/**
 * 事件配置服务接口
 *
 * @author zhaoff
 */
public interface EventConfigService {

    /**
     * 创建事件配置
     *
     * @param saveReqVO 创建事件配置请求
     * @return 配置 ID
     */
    Long createEventConfig(EventConfigSaveReqVO saveReqVO);

    /**
     * 更新事件配置
     *
     * @param saveReqVO 更新事件配置请求
     */
    void updateEventConfig(EventConfigSaveReqVO saveReqVO);

    /**
     * 删除事件配置
     *
     * @param id 配置 ID
     */
    void deleteEventConfig(Long id);

    /**
     * 获取事件配置详情
     *
     * @param id 配置 ID
     * @return 配置详情
     */
    EventConfigRespVO getEventConfig(Long id);

    /**
     * 获取事件配置分页列表
     *
     * @param pageReqVO 分页查询
     * @return 配置分页结果
     */
    PageResult<EventConfigRespVO> getEventConfigPage(EventConfigPageReqVO pageReqVO);

    /**
     * 获取所有启用的事件配置列表
     *
     * @return 启用的配置列表
     */
    List<EventConfigRespVO> getEnabledEventConfigList();

    /**
     * 根据匹配位置获取启用的事件配置列表
     *
     * @param matchPosition 匹配位置
     * @return 启用的配置列表
     */
    List<EventConfigRespVO> getEnabledEventConfigListByPosition(String matchPosition);

    /**
     * 匹配事件
     * <p>
     * 根据匹配规则匹配 Agent 上传的事件数据
     *
     * @param position 匹配位置
     * @param content  匹配内容
     * @return 匹配到的配置列表
     */
    List<EventConfigRespVO> matchEvent(String position, String content);

}
