package net.uint32.validation.validator;

import net.uint32.validation.config.ValiCtx;

import java.util.Objects;

/**
 * desc
 *
 * @author tangmingyou
 * @date 2021-09-30 10:04
 */
public class NotBlankValidator implements StatelessValidator {

    @Override
    public boolean isValid(ValiCtx ctx, Object value, Object[] valiArgs) {
        String strValue = Objects.toString(value, null);
        if (strValue == null || strValue.isEmpty()) {
            return false;
        }
        for (char c : strValue.toCharArray()) {
            if (!Character.isWhitespace(c)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object[] parseValidatorArgs(String[] valiArgs) {
        return new Object[0];
    }

    @Override
    public String message(String name, Object value, Object[] valiArgs) {
        return String.format("%s 不能为空且不能为空白字符", name);
    }
}
