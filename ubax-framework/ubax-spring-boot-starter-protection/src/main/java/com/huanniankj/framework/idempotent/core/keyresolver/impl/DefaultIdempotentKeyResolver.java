package com.huanniankj.framework.idempotent.core.keyresolver.impl;

import cn.hutool.crypto.SecureUtil;
import com.huanniankj.framework.common.util.string.StrUtils;
import com.huanniankj.framework.idempotent.core.annotation.Idempotent;
import com.huanniankj.framework.idempotent.core.keyresolver.IdempotentKeyResolver;
import org.aspectj.lang.JoinPoint;

/**
 * 默认（全局级别）幂等 Key 解析器，使用方法名 + 方法参数，组装成一个 Key
 * <p>
 * 为了避免 Key 过长，使用 MD5 进行“压缩”
 *
 * @author zhaoff
 */
public class DefaultIdempotentKeyResolver implements IdempotentKeyResolver {

    @Override
    public String resolver(JoinPoint joinPoint, Idempotent idempotent) {
        String methodName = joinPoint.getSignature().toString();
        String argsStr = StrUtils.joinMethodArgs(joinPoint);
        return SecureUtil.md5(methodName + argsStr);
    }

}
