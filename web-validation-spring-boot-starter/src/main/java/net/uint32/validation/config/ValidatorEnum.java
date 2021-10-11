package net.uint32.validation.config;

import net.uint32.validation.validator.DefaultValidator;
import net.uint32.validation.validator.LengthValidator;
import net.uint32.validation.validator.RangeValidator;
import net.uint32.validation.validator.Validator;

/**
 * desc
 *
 * @author tangmingyou
 * @date 2021-09-24 18:12
 */
public enum ValidatorEnum {

    LENGTH(new LengthValidator()),
    RANGE(new RangeValidator()),
    REQUIRED(null),
    DEFAULT(new DefaultValidator());


    final Validator validator;

    ValidatorEnum(Validator validator) {
        this.validator = validator;
    }

    public Validator validator() {
        return validator;
    }

    public static ValidatorEnum nonStrictValueOf(String key) {
        for (ValidatorEnum value : values()) {
            if (value.name().equalsIgnoreCase(key)) {
                return value;
            }
        }
        return null;
    }

}
