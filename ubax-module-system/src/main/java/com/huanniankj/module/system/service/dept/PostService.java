package com.huanniankj.module.system.service.dept;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.system.controller.dept.vo.post.PostPageReqVO;
import com.huanniankj.module.system.controller.dept.vo.post.PostSaveReqVO;
import com.huanniankj.module.system.dal.dataobject.dept.PostDO;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * 岗位服务接口
 *
 * @author zhaoff
 */
public interface PostService {

    /**
     * 创建岗位
     *
     * @param createReqVO 岗位信息
     * @return 岗位编号
     */
    Long createPost(PostSaveReqVO createReqVO);

    /**
     * 更新岗位
     *
     * @param updateReqVO 岗位信息
     */
    void updatePost(PostSaveReqVO updateReqVO);

    /**
     * 删除岗位信息
     *
     * @param id 岗位编号
     */
    void deletePost(Long id);

    /**
     * 批量删除岗位信息
     *
     * @param ids 岗位编号数组
     */
    void deletePostList(List<Long> ids);

    /**
     * 获得岗位列表
     *
     * @param ids 岗位编号数组
     * @return 部门列表
     */
    List<PostDO> getPostList(@Nullable Collection<Long> ids);

    /**
     * 获得符合条件的岗位列表
     *
     * @param ids      岗位编号数组。如果为空，不进行筛选
     * @param statuses 状态数组。如果为空，不进行筛选
     * @return 部门列表
     */
    List<PostDO> getPostList(@Nullable Collection<Long> ids,
                             @Nullable Collection<Integer> statuses);

    /**
     * 获得岗位分页列表
     *
     * @param reqVO 分页条件
     * @return 部门分页列表
     */
    PageResult<PostDO> getPostPage(PostPageReqVO reqVO);

    /**
     * 获得岗位信息
     *
     * @param id 岗位编号
     * @return 岗位信息
     */
    PostDO getPost(Long id);

    /**
     * 校验岗位们是否有效。如下情况，视为无效：
     * 1. 岗位编号不存在
     * 2. 岗位被禁用
     *
     * @param ids 岗位编号数组
     */
    void validatePostList(Collection<Long> ids);

}
