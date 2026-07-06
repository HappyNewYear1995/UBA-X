package com.huanniankj.module.source.service.processing;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.source.controller.processing.vo.*;

public interface ProcessingScriptService {

    Long createScript(ProcessingScriptSaveReqVO saveReqVO);

    void updateScript(ProcessingScriptSaveReqVO saveReqVO);

    void deleteScript(Long id);

    ProcessingScriptRespVO getScript(Long id);

    PageResult<ProcessingScriptRespVO> getScriptPage(ProcessingScriptPageReqVO pageReqVO);

    ProcessingScriptExecuteRespVO executeScript(ProcessingScriptExecuteReqVO reqVO);

}
