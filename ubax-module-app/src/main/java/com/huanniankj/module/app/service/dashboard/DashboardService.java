package com.huanniankj.module.app.service.dashboard;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.app.controller.dashboard.vo.DashboardPageReqVO;
import com.huanniankj.module.app.controller.dashboard.vo.DashboardSaveReqVO;
import com.huanniankj.module.app.dal.dataobject.dashboard.DashboardDO;

/**
 * 数据看板 Service 接口
 *
 * @author zhaoff
 */
public interface DashboardService {

    /**
     * 创建数据看板
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDashboard(DashboardSaveReqVO createReqVO);

    /**
     * 更新数据看板
     *
     * @param updateReqVO 更新信息
     */
    void updateDashboard(DashboardSaveReqVO updateReqVO);

    /**
     * 删除数据看板
     *
     * @param id 编号
     */
    void deleteDashboard(Long id);

    /**
     * 获得数据看板
     *
     * @param id 编号
     * @return 数据看板
     */
    DashboardDO getDashboard(Long id);

    /**
     * 获得数据看板分页
     *
     * @param pageReqVO 分页查询
     * @return 数据看板分页
     */
    PageResult<DashboardDO> getDashboardPage(DashboardPageReqVO pageReqVO);

}
