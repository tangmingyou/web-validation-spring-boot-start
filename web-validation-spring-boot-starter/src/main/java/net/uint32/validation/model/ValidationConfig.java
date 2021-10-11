package net.uint32.validation.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * desc
 *
 * @author tangmingyou
 * @date 2021-09-24 14:53
 */
@Data
public class ValidationConfig {

    private ScopeConfig global;

    private ScopeConfig scope;

    private String namespace;

    private Class<?> clazz;

    private List<ValidateMethodConfig> methods;

    private Map<String, ValidateMethodConfig> methodMap;

    @Data
    public static class ValidateMethodConfig {
        private String method;

        private Map<String, Map<String, Object>> arguments;
    }

    @Data
    public static class ScopeConfig {
        /** 校验失败的消费者 */
        private String consumer;

        private Map<String, List<String>> validations;
    }

}
