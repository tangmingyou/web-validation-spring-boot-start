package net.uint32.validation.config;

import org.springframework.cglib.proxy.CallbackFilter;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * desc
 *
 * @author tangmingyou
 * @date 2021-09-24 16:13
 */
public class ValidationEnhanceCallbackFilter implements CallbackFilter {

    private final Set<String> methodNameSet;

    public ValidationEnhanceCallbackFilter(Set<String> methodNameSet) {
        this.methodNameSet = methodNameSet;
    }

    @Override
    public int accept(Method method) {
        if (methodNameSet.contains(method.getName())) {
            return 1;
        }
        return 0;
    }
}
