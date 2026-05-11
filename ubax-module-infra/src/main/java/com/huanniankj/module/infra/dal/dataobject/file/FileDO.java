package com.huanniankj.module.infra.dal.dataobject.file;

import com.baomidou.mybatisplus.annotation.TableName;
import com.huanniankj.framework.mybatis.core.dataobject.BaseDO;
import com.huanniankj.framework.tenant.core.aop.TenantIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 文件表
 * 每次文件上传，都会记录一条记录到该表中
 *
 * @author zhaoff
 */
@TableName("infra_file")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TenantIgnore
public class FileDO extends BaseDO {

    /**
     * 编号，数据库自增
     */
    private Long id;

    /**
     * 配置编号
     * <p>
     * 关联 {@link FileConfigDO#getId()}
     */
    private Long configId;

    /**
     * 原文件名
     */
    private String name;

    /**
     * 路径，即文件名
     */
    private String path;

    /**
     * 访问地址
     */
    private String url;

    /**
     * 文件的 MIME 类型，例如 "application/octet-stream"
     */
    private String type;

    /**
     * 文件大小
     */
    private Long size;

}
