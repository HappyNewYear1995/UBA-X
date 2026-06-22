package org.springframework.messaging.handler.invocation;

import com.huanniankj.framework.tenant.core.context.TenantContextHolder;
import com.huanniankj.framework.tenant.core.util.TenantUtils;
import lombok.Setter;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.HandlerMethod;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;

import static com.huanniankj.framework.web.core.util.WebFrameworkUtils.HEADER_TENANT_ID;

/**
 * 针对 rabbitmq-spring 和 kafka-spring，不存在合适的拓展点，可以实现 Consumer 消费前，读取 Header 中的 tenant-id 设置到 {@link TenantContextHolder} 中
 *
 * @author zahoff
 */
public class InvocableHandlerMethod extends HandlerMethod {

    private static final Object[] EMPTY_ARGS = new Object[0];

    private HandlerMethodArgumentResolverComposite resolvers = new HandlerMethodArgumentResolverComposite();

    @Setter
    private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public InvocableHandlerMethod(HandlerMethod handlerMethod) {
        super(handlerMethod);
    }

    public InvocableHandlerMethod(Object bean, Method method) {
        super(bean, method);
    }

    public InvocableHandlerMethod(Object bean, String methodName, Class<?>... parameterTypes)
            throws NoSuchMethodException {

        super(bean, methodName, parameterTypes);
    }

    public void setMessageMethodArgumentResolvers(HandlerMethodArgumentResolverComposite argumentResolvers) {
        this.resolvers = argumentResolvers;
    }

    @Nullable
    public Object invoke(Message<?> message, Object... providedArgs) throws Exception {
        Object[] args = getMethodArgumentValues(message, providedArgs);
        if (logger.isTraceEnabled()) {
            logger.trace("Arguments: " + Arrays.toString(args));
        }
        // 注意：如下是本类的改动点！！！
        // 情况一：无租户编号的情况
        Long tenantId = parseTenantId(message);
        if (tenantId == null) {
            return doInvoke(args);
        }
        // 情况二：有租户的情况下
        return TenantUtils.execute(tenantId, () -> doInvoke(args));
    }

    private Long parseTenantId(Message<?> message) {
        Object tenantId = message.getHeaders().get(HEADER_TENANT_ID);
        if (tenantId == null) {
            return null;
        }
        if (tenantId instanceof Long) {
            return (Long) tenantId;
        }
        if (tenantId instanceof Number) {
            return ((Number) tenantId).longValue();
        }
        if (tenantId instanceof String) {
            return Long.parseLong((String) tenantId);
        }
        if (tenantId instanceof byte[]) {
            return Long.parseLong(new String((byte[]) tenantId));
        }
        throw new IllegalArgumentException("未知的数据类型：" + tenantId);
    }

    protected Object[] getMethodArgumentValues(Message<?> message, Object... providedArgs) throws Exception {
        MethodParameter[] parameters = getMethodParameters();
        if (ObjectUtils.isEmpty(parameters)) {
            return EMPTY_ARGS;
        }

        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            MethodParameter parameter = parameters[i];
            parameter.initParameterNameDiscovery(this.parameterNameDiscoverer);
            args[i] = findProvidedArgument(parameter, providedArgs);
            if (args[i] != null) {
                continue;
            }
            if (!this.resolvers.supportsParameter(parameter)) {
                throw new MethodArgumentResolutionException(
                        message, parameter, formatArgumentError(parameter, "No suitable resolver"));
            }
            try {
                args[i] = this.resolvers.resolveArgument(parameter, message);
            } catch (Exception ex) {
                // Leave stack trace for later, exception may actually be resolved and handled...
                if (logger.isDebugEnabled()) {
                    String exMsg = ex.getMessage();
                    if (exMsg != null && !exMsg.contains(parameter.getExecutable().toGenericString())) {
                        logger.debug(formatArgumentError(parameter, exMsg));
                    }
                }
                throw ex;
            }
        }
        return args;
    }

    @Nullable
    protected Object doInvoke(Object... args) throws Exception {
        try {
            return getBridgedMethod().invoke(getBean(), args);
        } catch (IllegalArgumentException ex) {
            assertTargetBean(getBridgedMethod(), getBean(), args);
            String text = (ex.getMessage() == null || ex.getCause() instanceof NullPointerException) ?
                    "Illegal argument" : ex.getMessage();
            throw new IllegalStateException(formatInvokeError(text, args), ex);
        } catch (InvocationTargetException ex) {
            // Unwrap for HandlerExceptionResolvers ...
            Throwable targetException = ex.getTargetException();
            if (targetException instanceof RuntimeException runtimeException) {
                throw runtimeException;
            } else if (targetException instanceof Error error) {
                throw error;
            } else if (targetException instanceof Exception exception) {
                throw exception;
            } else {
                throw new IllegalStateException(formatInvokeError("Invocation failure", args), targetException);
            }
        }
    }

    MethodParameter getAsyncReturnValueType(@Nullable Object returnValue) {
        return new AsyncResultMethodParameter(returnValue);
    }


    private class AsyncResultMethodParameter extends AnnotatedMethodParameter {

        @Nullable
        private final Object returnValue;

        private final ResolvableType returnType;

        public AsyncResultMethodParameter(@Nullable Object returnValue) {
            super(-1);
            this.returnValue = returnValue;
            this.returnType = ResolvableType.forType(super.getGenericParameterType()).getGeneric();
        }

        protected AsyncResultMethodParameter(AsyncResultMethodParameter original) {
            super(original);
            this.returnValue = original.returnValue;
            this.returnType = original.returnType;
        }

        @Override
        public Class<?> getParameterType() {
            if (this.returnValue != null) {
                return this.returnValue.getClass();
            }
            if (!ResolvableType.NONE.equals(this.returnType)) {
                return this.returnType.toClass();
            }
            return super.getParameterType();
        }

        @Override
        public Type getGenericParameterType() {
            return this.returnType.getType();
        }

        @Override
        public AsyncResultMethodParameter clone() {
            return new AsyncResultMethodParameter(this);
        }
    }

}
