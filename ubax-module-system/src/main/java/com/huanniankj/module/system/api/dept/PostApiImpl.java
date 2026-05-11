package com.huanniankj.module.system.api.dept;

import com.huanniankj.framework.common.util.object.BeanUtils;
import com.huanniankj.module.system.api.dept.dto.PostRespDTO;
import com.huanniankj.module.system.dal.dataobject.dept.PostDO;
import com.huanniankj.module.system.service.dept.PostService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 岗位 API 实现类
 *
 * @author zhaoff
 */
@Service
public class PostApiImpl implements PostApi {

    @Resource
    private PostService postService;

    @Override
    public void validPostList(Collection<Long> ids) {
        postService.validatePostList(ids);
    }

    @Override
    public List<PostRespDTO> getPostList(Collection<Long> ids) {
        List<PostDO> list = postService.getPostList(ids);
        return BeanUtils.toBean(list, PostRespDTO.class);
    }

}
