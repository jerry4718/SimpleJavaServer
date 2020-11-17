package space.mmty.module;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HttpMethodInvokeOption {
    private final Method method;
    private final Object handler;

    public HttpMethodInvokeOption(Method method, Object handler) {
        this.method = method;
        this.handler = handler;
    }

    public Method getMethod() {
        return method;
    }

    public Object getHandler() {
        return handler;
    }

    public Object invoke(Object... args) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(handler, args);
    }
}
