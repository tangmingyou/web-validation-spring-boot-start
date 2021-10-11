package net.uint32.validation.validator;

import net.uint32.validation.config.ValiCtx;

import java.util.Arrays;

/**
 * desc
 *
 * @author tangmingyou
 * @date 2021-09-30 10:03
 */
public class RangeValidator implements StatelessValidator {
    @Override
    public boolean isValid(ValiCtx ctx, Object value, Object[] valiArgs) {
        int min = (int)valiArgs[0];
        int max = (int)valiArgs[1];
        int intValue = Integer.parseInt(String.valueOf(value));
        return min <= intValue && intValue <= max;
    }

    @Override
    public Object[] parseValidatorArgs(String[] valiArgs) {
        if (valiArgs == null || valiArgs.length != 2) {
            throw new IllegalArgumentException("Range校验器参数有误 " + Arrays.toString(valiArgs));
        }
        try {
            return new Integer[]{Integer.valueOf(valiArgs[0]), Integer.valueOf(valiArgs[1])};
        }catch (Exception e) {
            throw new IllegalArgumentException("Range校验器参数有误 " + Arrays.toString(valiArgs));
        }
    }

    @Override
    public String message(String name, Object value, Object[] valiArgs) {
        return String.format("%s值在[%d,%d] 范围内", name, (Integer)valiArgs[0], (Integer)valiArgs[1]);
    }
}
