package net.uint32.validation.validator;

import net.uint32.validation.config.ValiCtx;
import net.uint32.validation.model.ValidationNamespace;

/**
 * desc
 *
 * @author tangmingyou
 * @date 2021-09-26 20:10
 */
public interface StatefulValidator extends Validator {

    /** == 判断校验失败 */
    public static final Object FAIL = new Object();

    public static final Object NO_NEW_VALUE = new Object();

    /**
     * 返回新值，校验失败返回 {@link #FAIL}
     */
    Object isValid(ValiCtx ctx, Object value, Object[] valiArgs, ValidationNamespace.ValidationParam valiParam);

}
