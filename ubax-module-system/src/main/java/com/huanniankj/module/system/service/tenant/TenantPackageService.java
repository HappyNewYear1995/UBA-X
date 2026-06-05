package com.huanniankj.module.system.service.tenant;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.system.controller.tenant.vo.packages.TenantPackagePageReqVO;
import com.huanniankj.module.system.controller.tenant.vo.packages.TenantPackageSaveReqVO;
import com.huanniankj.module.system.dal.dataobject.tenant.TenantPackageDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 租户套餐 Service 接口
 *
 * @author zhaoff
 */
public interface TenantPackageService {

    /**
     * 创建租户套餐
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTenantPackage(@Valid TenantPackageSaveReqVO createReqVO);

    /**
     * 更新租户套餐
     *
     * @param updateReqVO 更新信息
     */
    void updateTenantPackage(@Valid TenantPackageSaveReqVO updateReqVO);

    /**
     * 删除租户套餐
     *
     * @param id 编号
     */
    void deleteTenantPackage(Long id);

    /**
     * 批量删除租户套餐
     *
     * @param ids 编号数组
     */
    void deleteTenantPackageList(List<Long> ids);

    /**
     * 获得租户套餐
     *
     * @param id 编号
     * @return 租户套餐
     */
    TenantPackageDO getTenantPackage(Long id);

    /**
     * 获得租户套餐分页
     *
     * @param pageReqVO 分页查询
     * @return 租户套餐分页
     */
    PageResult<TenantPackageDO> getTenantPackagePage(TenantPackagePageReqVO pageReqVO);

    /**
     * 校验租户套餐
     *
     * @param id 编号
     * @return 租户套餐
     */
    TenantPackageDO validTenantPackage(Long id);

    /**
     * 获得指定状态的租户套餐列表
     *
     * @param status 状态
     * @return 租户套餐
     */
    List<TenantPackageDO> getTenantPackageListByStatus(Integer status);

}
