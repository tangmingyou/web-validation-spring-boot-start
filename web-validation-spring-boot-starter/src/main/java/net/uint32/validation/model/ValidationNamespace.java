package net.uint32.validation.model;

import javafx.util.Pair;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.uint32.validation.config.ValidatorEnum;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * desc
 *
 * @author tangmingyou
 * @date 2021-09-24 18:21
 */
@Data
public class ValidationNamespace {

    private Class<?> clazz;

    private Map<String, ValidationMethod> methods;

    @Data
    public static class ValidationMethod {

        private String methodName;

        private Map<String, ValidationParam> arguments;

        private ValidationParam[] valiParams;

        private String[] argNames;

        private Class<?>[] parameterTypes;
    }

    @Getter
    @Setter
    public static class ValidationParam {

        private String key;

        private String name;

        /** 校验器，校验器参数 */
        private List<Pair<ValidatorEnum, Object[]>> validators;

        private Map<String, ValidationParam> fields;

        private Class<?> type;

        /** 父对象 */
        private ValidationParam parent;

        private Function<Object, Object> getter;

        private Function<Object, Object> setter;

    }


}
