package com.huanniankj.module.app.service.dashboard;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.common.util.object.BeanUtils;
import com.huanniankj.module.app.controller.dashboard.vo.DashboardPageReqVO;
import com.huanniankj.module.app.controller.dashboard.vo.DashboardSaveReqVO;
import com.huanniankj.module.app.dal.dataobject.dashboard.DashboardDO;
import com.huanniankj.module.app.dal.mysql.dashboard.DashboardMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static com.huanniankj.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.huanniankj.module.app.enums.ErrorCodeConstants.DASHBOARD_NAME_DUPLICATE;
import static com.huanniankj.module.app.enums.ErrorCodeConstants.DASHBOARD_NOT_EXISTS;

/**
 * 数据看板 Service 实现类
 *
 * @author zhaoff
 */
@Service
@Validated
public class DashboardServiceImpl implements DashboardService {

    @Resource
    private DashboardMapper dashboardMapper;

    @Override
    public Long createDashboard(DashboardSaveReqVO createReqVO) {
        // 校验名称唯一
        validateNameUnique(null, createReqVO.getName());
        // 插入
        DashboardDO dashboard = BeanUtils.toBean(createReqVO, DashboardDO.class);
        dashboardMapper.insert(dashboard);
        return dashboard.getId();
    }

    @Override
    public void updateDashboard(DashboardSaveReqVO updateReqVO) {
        // 校验存在
        validateDashboardExists(updateReqVO.getId());
        // 校验名称唯一
        validateNameUnique(updateReqVO.getId(), updateReqVO.getName());
        // 更新
        DashboardDO updateObj = BeanUtils.toBean(updateReqVO, DashboardDO.class);
        dashboardMapper.updateById(updateObj);
    }

    @Override
    public void deleteDashboard(Long id) {
        // 校验存在
        validateDashboardExists(id);
        // 删除
        dashboardMapper.deleteById(id);
    }

    @Override
    public DashboardDO getDashboard(Long id) {
        return dashboardMapper.selectById(id);
    }

    @Override
    public PageResult<DashboardDO> getDashboardPage(DashboardPageReqVO pageReqVO) {
        return dashboardMapper.selectPage(pageReqVO, pageReqVO.getName(), pageReqVO.getEnabled());
    }

    private void validateDashboardExists(Long id) {
        if (dashboardMapper.selectById(id) == null) {
            throw exception(DASHBOARD_NOT_EXISTS);
        }
    }

    private void validateNameUnique(Long id, String name) {
        DashboardDO dashboard = dashboardMapper.selectByName(name).stream().findFirst().orElse(null);
        if (dashboard == null) {
            return;
        }
        if (id == null) {
            throw exception(DASHBOARD_NAME_DUPLICATE);
        }
        if (!dashboard.getId().equals(id)) {
            throw exception(DASHBOARD_NAME_DUPLICATE);
        }
    }

}
