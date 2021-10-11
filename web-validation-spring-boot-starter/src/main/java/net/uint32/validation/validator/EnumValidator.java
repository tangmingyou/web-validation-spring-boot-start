package net.uint32.validation.validator;

import net.uint32.validation.config.ValiCtx;

import java.util.Arrays;
import java.util.Objects;

/**
 * 枚举校验器，校验值范围
 *
 * @author tangmingyou
 * @date 2021-09-30 9:46
 */
public class EnumValidator implements StatelessValidator {

    @Override
    public boolean isValid(ValiCtx ctx, Object value, Object[] valiArgs) {
        for (Object constant : valiArgs) {
            if (Objects.equals(constant, value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object[] parseValidatorArgs(String[] valiArgs) {
        if (valiArgs == null || valiArgs.length == 0) {
            throw new IllegalArgumentException("ENUM校验器参数不能为空");
        }
        return valiArgs;
    }

    @Override
    public String message(String name, Object value, Object[] valiArgs) {
        return String.format("%s 值在 %s 范围内", name, Arrays.toString(valiArgs));
    }
}
