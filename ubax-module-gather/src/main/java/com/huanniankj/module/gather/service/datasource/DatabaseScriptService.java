package com.huanniankj.module.gather.service.datasource;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.gather.controller.database.vo.*;

/**
 * 数据库脚本服务接口
 *
 * @author zhaoff
 */
public interface DatabaseScriptService {

    /**
     * 创建脚本
     *
     * @param saveReqVO 创建请求
     * @return 脚本ID
     */
    Long createScript(DatabaseScriptSaveReqVO saveReqVO);

    /**
     * 更新脚本
     *
     * @param saveReqVO 更新请求
     */
    void updateScript(DatabaseScriptSaveReqVO saveReqVO);

    /**
     * 删除脚本
     *
     * @param id 脚本ID
     */
    void deleteScript(Long id);

    /**
     * 获取脚本详情
     *
     * @param id 脚本ID
     * @return 脚本详情
     */
    DatabaseScriptRespVO getScript(Long id);

    /**
     * 获取脚本分页
     *
     * @param pageReqVO 分页请求
     * @return 脚本分页
     */
    PageResult<DatabaseScriptRespVO> getScriptPage(DatabaseScriptPageReqVO pageReqVO);

    /**
     * 执行脚本
     *
     * @param reqVO 执行请求
     * @return 执行结果
     */
    DatabaseScriptExecuteRespVO executeScript(DatabaseScriptExecuteReqVO reqVO);

    /**
     * 获取执行日志分页
     *
     * @param pageReqVO 分页请求
     * @return 行日志分页
     */
    PageResult<DatabaseScriptLogRespVO> getScriptLogPage(DatabaseScriptLogPageReqVO pageReqVO);

    /**
     * 获取执行日志详情
     *
     * @param id 脚本ID
     * @return 执行日志详情
     */
    DatabaseScriptLogRespVO getScriptLog(Long id);

    /**
     * 删除执行日志
     *
     * @param id 脚本ID
     */
    void deleteScriptLog(Long id);

}
