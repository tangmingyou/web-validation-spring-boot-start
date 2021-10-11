package net.uint32.validation.validator;


import net.uint32.validation.config.ValiCtx;

/**
 * desc
 *
 * @author tangmingyou
 * @date 2021-09-26 20:10
 */
public interface StatelessValidator extends Validator {

    boolean isValid(ValiCtx ctx, Object value, Object[] valiArgs);

}
