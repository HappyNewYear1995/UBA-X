package com.huanniankj.module.source.convert.datasource;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.source.controller.database.vo.DatabaseRespVO;
import com.huanniankj.module.source.controller.database.vo.DatabaseSaveReqVO;
import com.huanniankj.module.source.dal.dataobject.database.DatabaseDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 数据库数据源 Convert
 *
 * @author zhaoff
 */
@Mapper
public interface DatabaseSourceConvert {

    DatabaseSourceConvert INSTANCE = Mappers.getMapper(DatabaseSourceConvert.class);

    /**
     * DO 转 RespVO
     *
     * @param bean DO 对象
     * @return RespVO 对象
     */
    // @Mapping(source = "password", target = "password")
    DatabaseRespVO convert(DatabaseDO bean);

    /**
     * DO 列表转 RespVO 列表
     *
     * @param list DO 列表
     * @return RespVO 列表
     */
    List<DatabaseRespVO> convertList(List<DatabaseDO> list);

    /**
     * DO 分页转 RespVO 分页
     *
     * @param page DO 分页
     * @return RespVO 分页
     */
    PageResult<DatabaseRespVO> convertPage(PageResult<DatabaseDO> page);

    /**
     * SaveReqVO 转 DO
     *
     * @param reqVO 保存请求
     * @return DO 对象
     */
    DatabaseDO convert(DatabaseSaveReqVO reqVO);

}
