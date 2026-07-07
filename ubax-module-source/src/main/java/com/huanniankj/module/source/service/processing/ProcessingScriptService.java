package com.huanniankj.module.source.service.processing;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.source.controller.processing.vo.ProcessingScriptExecuteReqVO;
import com.huanniankj.module.source.controller.processing.vo.ProcessingScriptExecuteRespVO;
import com.huanniankj.module.source.controller.processing.vo.ProcessingScriptLogPageReqVO;
import com.huanniankj.module.source.controller.processing.vo.ProcessingScriptLogRespVO;
import com.huanniankj.module.source.controller.processing.vo.ProcessingScriptPageReqVO;
import com.huanniankj.module.source.controller.processing.vo.ProcessingScriptRespVO;
import com.huanniankj.module.source.controller.processing.vo.ProcessingScriptSaveReqVO;

/**
 * 处理脚本服务接口
 *
 * @author zhaoff
 */
public interface ProcessingScriptService {

    /**
     * 创建处理脚本
     *
     * @param saveReqVO 处理脚本
     * @return 处理脚本ID
     */
    Long createScript(ProcessingScriptSaveReqVO saveReqVO);

    /**
     * 更新处理脚本
     *
     * @param saveReqVO 处理脚本
     */
    void updateScript(ProcessingScriptSaveReqVO saveReqVO);

    /**
     * 删除处理脚本
     *
     * @param id 处理脚本ID
     */
    void deleteScript(Long id);

    /**
     * 根据ID查询处理脚本
     *
     * @param id 处理脚本ID
     * @return 处理脚本
     */
    ProcessingScriptRespVO getScript(Long id);

    /**
     * 分页查询处理脚本
     *
     * @param pageReqVO 分页请求
     * @return 处理脚本分页
     */
    PageResult<ProcessingScriptRespVO> getScriptPage(ProcessingScriptPageReqVO pageReqVO);

    /**
     * 执行处理脚本
     *
     * @param reqVO 执行请求
     * @return 执行结果
     */
    ProcessingScriptExecuteRespVO executeScript(ProcessingScriptExecuteReqVO reqVO);

    PageResult<ProcessingScriptLogRespVO> getScriptLogPage(ProcessingScriptLogPageReqVO pageReqVO);

    ProcessingScriptLogRespVO getScriptLog(Long id);

    void deleteScriptLog(Long id);

}
