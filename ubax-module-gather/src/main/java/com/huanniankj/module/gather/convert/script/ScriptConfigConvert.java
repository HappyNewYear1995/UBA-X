package com.huanniankj.module.gather.convert.script;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.gather.controller.database.vo.DatabaseScriptRespVO;
import com.huanniankj.module.gather.controller.database.vo.DatabaseScriptSaveReqVO;
import com.huanniankj.module.gather.controller.database.vo.DatabaseScriptLogRespVO;
import com.huanniankj.module.gather.dal.dataobject.database.DatabaseScriptDO;
import com.huanniankj.module.gather.dal.dataobject.database.DatabaseScriptLogDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 脚本 Convert
 *
 * @author zhaoff
 */
@Mapper
public interface ScriptConfigConvert {

    ScriptConfigConvert INSTANCE = Mappers.getMapper(ScriptConfigConvert.class);

    DatabaseScriptRespVO convert(DatabaseScriptDO bean);

    List<DatabaseScriptRespVO> convertList(List<DatabaseScriptDO> list);

    PageResult<DatabaseScriptRespVO> convertPage(PageResult<DatabaseScriptDO> page);

    DatabaseScriptDO convert(DatabaseScriptSaveReqVO reqVO);

    DatabaseScriptLogRespVO convertLog(DatabaseScriptLogDO bean);

    List<DatabaseScriptLogRespVO> convertLogList(List<DatabaseScriptLogDO> list);

    PageResult<DatabaseScriptLogRespVO> convertLogPage(PageResult<DatabaseScriptLogDO> page);

}
