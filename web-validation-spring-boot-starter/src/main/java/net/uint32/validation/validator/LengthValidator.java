package net.uint32.validation.validator;

import net.uint32.validation.config.ValiCtx;
import org.springframework.util.StringUtils;

import java.util.Collection;

/**
 * 长度校验器
 */
public class LengthValidator implements StatelessValidator {

    @Override
    public Object[] parseValidatorArgs(String[] valiArgs) {
        if (valiArgs == null || valiArgs.length != 2) {
            throw new IllegalArgumentException("长度校验器需要最少2个参数，例LENGTH(min,max)");
        }
        String minStr = valiArgs[0], maxStr = valiArgs[1];
        Integer min, max;
        try {
            min = StringUtils.isEmpty(minStr) ? null : Integer.valueOf(minStr);
            max = StringUtils.isEmpty(maxStr) ? null : Integer.valueOf(maxStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("长度校验器(LENGTH)非法参数，" + minStr + "," + maxStr);
        }
        if (min == null && max == null) {
            throw new IllegalArgumentException("长度校验器需要最少1个参数，例LENGTH(min,max)");
        }
        return new Object[]{min, max};
    }

    @Override
    public String message(String name, Object value, Object[] valiArgs) {
        return name + "长度需在" + valiArgs[0] + "," + valiArgs[1] + "之间";
    }

    @Override
    public boolean isValid(ValiCtx ctx, Object value, Object[] valiArgs) {
        if (value == null) {
            return true;
        }
        Integer min = (Integer)valiArgs[0], max = (Integer)valiArgs[1];
        int len = -1;
        if (value instanceof CharSequence) {
            len = ((CharSequence)value).length();
        } else if (value instanceof Collection) {
            len = ((Collection<?>)value).size();
        } else {
            len = String.valueOf(value).length();
        }
        return (min == null || len >= min) && (max == null || len <= max);
    }

}
