package net.uint32.validation.validator;

import net.uint32.validation.config.ValiCtx;
import net.uint32.validation.model.ValidationNamespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * desc
 *
 * @author tangmingyou
 * @date 2021-09-29 10:00
 */
public class DefaultValidator implements StatefulValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultValidator.class);

    @Override
    public Object[] parseValidatorArgs(String[] valiArgs) {
        if (valiArgs == null || valiArgs.length == 0) {
            LOGGER.warn("默认值校验器，必须要对应一个参数");
            return null;
        }
        return valiArgs;
    }

    @Override
    public Object isValid(ValiCtx ctx, Object value, Object[] valiArgs, ValidationNamespace.ValidationParam valiParam) {
        if (value != null || valiArgs == null) {
            return NO_NEW_VALUE;
        }
        Class<?> type = valiParam.getType();
        return caseToTypeValue((String) valiArgs[0], type);
    }

    @Override
    public String message(String name, Object value, Object[] valiArgs) {
        return null;
    }

    private Object caseToTypeValue(String def, Class<?> clazz) {
        // clazz == null, 父级是 HashMap
        if (clazz == null || def == null || clazz == String.class) {
            return def;
        }

        if (Number.class.isAssignableFrom(clazz)) {
            if (clazz == Integer.class) {
                return Integer.valueOf(def);
            } else if (clazz == Long.class) {
                return Long.valueOf(def);
            } else if (clazz == Double.class) {
                return Double.valueOf(def);
            } else if (clazz == Float.class) {
                return Float.valueOf(def);
            } else if (clazz == Short.class) {
                return Short.valueOf(def);
            } else if (clazz == Byte.class) {
                return Byte.valueOf(def);
            }
        }

        if (Boolean.class.isAssignableFrom(clazz)) {
            return Boolean.valueOf(def);
        }
        return NO_NEW_VALUE;
    }

}
