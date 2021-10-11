package net.uint32.validation.validator;

public interface Validator {

    /**
     * 解析配置的校验器参数，会缓存到配置对象校验时传递，避免多次解析
     * @param valiArgs 配置的校验器参数 , 分隔
     * @return 校验器需要的参数
     */
    Object[] parseValidatorArgs(String[] valiArgs);

    String message(String name, Object value, Object[] valiArgs);

}
