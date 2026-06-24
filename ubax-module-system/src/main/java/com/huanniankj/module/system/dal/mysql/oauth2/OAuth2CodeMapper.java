package com.huanniankj.module.system.dal.mysql.oauth2;

import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.module.system.dal.dataobject.oauth2.OAuth2CodeDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * OAuth2 授权码 DO Mapper
 *
 * @author zhaoff
 */
@Mapper
public interface OAuth2CodeMapper extends BaseMapperX<OAuth2CodeDO> {

    default OAuth2CodeDO selectByCode(String code) {
        return selectOne(OAuth2CodeDO::getCode, code);
    }

}
