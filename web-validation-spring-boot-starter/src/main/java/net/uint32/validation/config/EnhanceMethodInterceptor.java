package net.uint32.validation.config;

import javafx.util.Pair;
import net.uint32.validation.model.ValidationNamespace;
import net.uint32.validation.validator.StatefulValidator;
import net.uint32.validation.validator.StatelessValidator;
import net.uint32.validation.validator.Validator;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

/**
 * desc
 *
 * @author tangmingyou
 * @date 2021-09-24 16:17
 */
public class EnhanceMethodInterceptor implements MethodInterceptor {

    private final ValidationNamespace namespace;

    public EnhanceMethodInterceptor(ValidationNamespace namespace) {
        this.namespace = namespace;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        ValidationNamespace.ValidationMethod validationMethod = namespace.getMethods().get(method.getName());

        if (validationMethod == null) {
            return methodProxy.invokeSuper(obj, args);
        }

        ValiCtx ctx = doValidation(args, validationMethod);
        if (!ctx.getResults().isEmpty()) {
            // TODO 校验失败。。。
            System.out.println("校验失败:" + ctx.getResults());
        }
        return methodProxy.invokeSuper(obj, args);
    }

    private ValiCtx doValidation(Object[] args,
                              ValidationNamespace.ValidationMethod vali) {
        ValiCtx ctx = new ValiCtx();
        Class<?>[] parameterTypes = vali.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Object value = args[i];
            ValidationNamespace.ValidationParam valiParam = vali.getValiParams()[i];
            Object newValue = doValidateFields(ctx, args[i], valiParam);
            // 设置新的值
            if (StatefulValidator.NO_NEW_VALUE != newValue && !Objects.equals(args[i], newValue)) {
                args[i] = newValue;
            }
        }
        return ctx;
    }

    private Object doValidateFields(ValiCtx ctx,
                                  Object value,
                                  ValidationNamespace.ValidationParam valiParam) {
        Object oldValue = value;
        // 递归校验对象字段
        if (valiParam.getFields() != null) {
            for (Map.Entry<String, ValidationNamespace.ValidationParam> field : valiParam.getFields().entrySet()) {
                // TODO value getter value field.getKey()
                Object fieldValue = ReflectUtils.getFieldValue(value, field.getKey());
                Object newFieldValue = doValidateFields(ctx, fieldValue, field.getValue());
                if (valiParam.getType() != null) {
                    // 设置新值
                    if (StatefulValidator.NO_NEW_VALUE != newFieldValue && !Objects.equals(newFieldValue, oldValue)) {
                        try {
                            value = value != null ? value : ReflectUtils.newInstance(valiParam.getType());
                        } catch (ReflectiveOperationException e) {
                            throw new RuntimeException(valiParam.getKey() + " 实例化失败", e);
                        }
                        ReflectUtils.setValue(value, field.getKey(), newFieldValue);
                    }
                }
            }
            return value != oldValue ? value : StatefulValidator.NO_NEW_VALUE;
        }
        // 获取校验器进行参数校验
        if (valiParam.getValidators() != null) {
            for (Pair<ValidatorEnum, Object[]> validatorPair : valiParam.getValidators()) {
                Validator validator = validatorPair.getKey().validator();
                boolean isValid;
                boolean stateful = false;
                Object newValue = null;
                if (validator instanceof StatelessValidator) {
                    // TODO try catch
                    isValid = ((StatelessValidator) validator)
                            .isValid(ctx, value, validatorPair.getValue());
                } else if ((stateful = validator instanceof StatefulValidator)) {
                    newValue = ((StatefulValidator) validator)
                            .isValid(ctx, value, validatorPair.getValue(), valiParam);
                    isValid = newValue != StatefulValidator.FAIL;
                } else {
                    throw new IllegalStateException("校验器必须实现" + StatelessValidator.class + " 或 "
                            + StatefulValidator.class + " 接口");
                }
                // 校验失败，获取消息
                if (!isValid) {
                    String message = validator.message(
                            ValiUtils.isNullOr(valiParam.getName(), valiParam.getKey()),
                            value,
                            validatorPair.getValue());
                    if (message != null && !message.isEmpty()) {
                        ctx.putResult(message);
                    }
                    // TODO 校验失败处理 failConsumer
                    return StatefulValidator.NO_NEW_VALUE;
                } else if (stateful && !Objects.equals(value, newValue)) {
                    //  校验成功, 校验新值
                    value = newValue;
                }
            }
            return value != oldValue ? value : StatefulValidator.NO_NEW_VALUE;
        }

        return StatefulValidator.NO_NEW_VALUE;
    }

}
