package com.huanniankj.module.system.service.notice;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.system.controller.notice.vo.NoticePageReqVO;
import com.huanniankj.module.system.controller.notice.vo.NoticeSaveReqVO;
import com.huanniankj.module.system.dal.dataobject.notice.NoticeDO;

import java.util.List;

/**
 * 通知公告 Service 接口
 *
 * @author zhaoff
 */
public interface NoticeService {

    /**
     * 创建通知公告
     *
     * @param createReqVO 通知公告
     * @return 编号
     */
    Long createNotice(NoticeSaveReqVO createReqVO);

    /**
     * 更新通知公告
     *
     * @param reqVO 通知公告
     */
    void updateNotice(NoticeSaveReqVO reqVO);

    /**
     * 删除通知公告
     *
     * @param id 编号
     */
    void deleteNotice(Long id);

    /**
     * 批量删除通知公告
     *
     * @param ids 编号列表
     */
    void deleteNoticeList(List<Long> ids);

    /**
     * 获得通知公告分页列表
     *
     * @param reqVO 分页条件
     * @return 部门分页列表
     */
    PageResult<NoticeDO> getNoticePage(NoticePageReqVO reqVO);

    /**
     * 获得通知公告
     *
     * @param id 编号
     * @return 通知公告
     */
    NoticeDO getNotice(Long id);

}
