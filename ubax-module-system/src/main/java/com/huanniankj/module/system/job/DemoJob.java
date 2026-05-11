package com.huanniankj.module.system.job;

import com.huanniankj.framework.quartz.core.handler.JobHandler;
import com.huanniankj.framework.tenant.core.context.TenantContextHolder;
import com.huanniankj.framework.tenant.core.job.TenantJob;
import com.huanniankj.module.system.dal.dataobject.user.AdminUserDO;
import com.huanniankj.module.system.dal.mysql.user.AdminUserMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DemoJob implements JobHandler {

    @Resource
    private AdminUserMapper adminUserMapper;

    @Override
    @TenantJob // 标记多租户
    public String execute(String param) {
        System.out.println("当前租户：" + TenantContextHolder.getTenantId());
        List<AdminUserDO> users = adminUserMapper.selectList();
        return "用户数量：" + users.size();
    }

}
