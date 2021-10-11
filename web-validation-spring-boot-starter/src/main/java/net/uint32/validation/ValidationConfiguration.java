package net.uint32.validation;

import javafx.util.Pair;
import net.uint32.validation.config.*;
import net.uint32.validation.model.ValidationConfig;
import net.uint32.validation.model.ValidationNamespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.NoOp;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

/**
 * https://blog.csdn.net/wqztmx4/article/details/105629861
 *
 * @author tangmingyou
 * @date 2021-09-24 12:27
 */
@Configuration
public class ValidationConfiguration implements BeanPostProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationConfiguration.class);

    final Map<Class<?>, ValidationNamespace> namespaceMap;

    public ValidationConfiguration() throws IOException, ClassNotFoundException {
        List<ValidationConfig> validationConfigs = ResourceLoader.loadConfig(
                        "validation/**/*.yml",
                        "validation/**/*.yaml");

        this.namespaceMap = processConfiguration(validationConfigs);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        ValidationNamespace namespace;
        Class<?> beanClass = bean.getClass();
        if (null == (namespace = namespaceMap.get(beanClass))) {
            return bean;
        }
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(beanClass);
        // 设置代理方法
        enhancer.setCallbacks(new Callback[]{NoOp.INSTANCE, new EnhanceMethodInterceptor(namespace)});
        // 设置代理方法过滤器
        enhancer.setCallbackFilter(new ValidationEnhanceCallbackFilter(namespace.getMethods().keySet()));
        return enhancer.create();
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    private Map<Class<?>, ValidationNamespace> processConfiguration(List<ValidationConfig> validationConfigs) throws ClassNotFoundException, IOException {
        Map<Class<?>, ValidationNamespace> namespaceMap = new HashMap<>();

        for (ValidationConfig config : validationConfigs) {
            ValidationNamespace namespace = new ValidationNamespace();
            Class<?> clazz = Class.forName(config.getNamespace());
            namespace.setClazz(clazz);
            // 方法配置映射为 map
            Map<String, ValidationNamespace.ValidationMethod> methodMap = new HashMap<>();
            namespace.setMethods(methodMap);
            namespaceMap.put(clazz, namespace);

            for (ValidationConfig.ValidateMethodConfig methodConfig : config.getMethods()) {
                ValidationNamespace.ValidationMethod method = new ValidationNamespace.ValidationMethod();
                method.setMethodName(methodConfig.getMethod());

                // 获取要拦截校验方法
                Method clazzMethod = getDeclaredMethodByName(clazz, methodConfig.getMethod());
                if (clazzMethod == null) {
                    throw new IllegalStateException(clazz + " 中方法 " + methodConfig.getMethod() + " 不存在");
                }
                // 获取方法参数类型列表
                Class<?>[] parameterTypes = clazzMethod.getParameterTypes();
                method.setParameterTypes(parameterTypes);
                // 获取方法名列表
                String[] methodParameterNames = ReflectUtils.getMethodParameterNamesByAsm4(clazz, clazzMethod);
                final String [] argNames = methodParameterNames == null ? new String[0] : methodParameterNames;
                for (String argName : argNames) {
                    if (argName == null) {
                        throw new IllegalStateException(clazz + " 方法 " + methodConfig.getMethod() + " 方法参数名无法获取");
                    }
                }
                // 缓存参数名
                method.setArgNames(argNames);

                // 递归处理参数校验配置
                Map<String, Map<String, Object>> arguments = methodConfig.getArguments();

                Map<String, ValidationNamespace.ValidationParam> paramMap = parseChildParam((Map) arguments, null, null, field -> {
                    for (int i = 0; i < argNames.length; i++) {
                        if (field.equals(argNames[i])) {
                            return parameterTypes[i];
                        }
                    }
                    throw new IllegalArgumentException(clazz + "." + method.getMethodName() + ", " + field + " 参数不存在");
                });

                // 根据参数顺序一一对应校验配置
                ValidationNamespace.ValidationParam[] validateArr = new ValidationNamespace.ValidationParam[parameterTypes.length];
                for (int i = 0; i < argNames.length; i++) {
                    validateArr[i] = paramMap.get(argNames[i]);
                }
                method.setValiParams(validateArr);
                method.setArguments(paramMap);

                methodMap.put(method.getMethodName(), method);
            }
            // TODO contains 校验是否重复
//            config.setMethodMap(methodMap);
//            // TODO contains 校验是否重复
//            config.setClazz(clazz);
        }
        return namespaceMap;
    }

    /** 递归处理方法参数 */
    private Map<String, ValidationNamespace.ValidationParam> parseChildParam(Map<String, Object> args, ValidationNamespace.ValidationParam parent, Class<?> parentClazz, Function<String, Class<?>> typeGetter) {
        Map<String, ValidationNamespace.ValidationParam> paramMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : args.entrySet()) {
            ValidationNamespace.ValidationParam param = new ValidationNamespace.ValidationParam();
            String key = entry.getKey();
            Object value = entry.getValue();

            param.setParent(parent);
            param.setKey(key);
            paramMap.put(key, param);

            if (value instanceof String && "name".equals(key)) {
                parent.setName((String) value);
                continue;
            }
            if (!(value instanceof Map)) {
                System.out.println("unknow kv:" + key + "," + value);
                continue;
            }

            Class<?> clazz = typeGetter != null
                    // 方法参数
                    ? typeGetter.apply(key)
                    // 子对象
                    : Map.class.isAssignableFrom(parentClazz)
                    ? HashMap.class : ReflectUtils.getFieldType(parentClazz, key);
            if (clazz == null) {
                throw new IllegalArgumentException((parent == null ? "" : (parent.getKey() + " 中 ")) + key + " 字段不存在");
            }
            param.setType(clazz);

            // TODO 逗号分隔的多个 key 的校验器放到一个, isPublish,isTop,topic
            Map<String, Object> mapValue = (Map)value;
            if (mapValue.containsKey("validators")) {
                // 父对象是 Map, field 没有类型
                if (parent != null && Map.class.isAssignableFrom(parent.getType())) {
                    param.setType(null);
                }
                Object name = mapValue.get("name");
                if (name instanceof String) {
                    param.setName((String) name);
                }
                // validators 有值非法
                Object validators = mapValue.get("validators");
                if (!(validators instanceof List)) {
                    param.setValidators(Collections.emptyList());
                    LOGGER.warn("{} 的 validators 配置值有误，请检查", param.getKey());
                    continue;
                }
                // enum validator process
                List<Pair<ValidatorEnum, Object[]>> validatorEnums = new ArrayList<>();
                for (String validator : (List<String>)validators) {
                    // 校验器名,参数解析
                    String[] valiMethod = ValiUtils.parseMethodStr(validator, '(', ')');

                    List<String> valiParams = valiMethod[1] == null ? null
                            : ValiUtils.parseParameters(valiMethod[1]);
                    String[] valiArgs = valiParams == null || valiParams.size() == 0
                            ? new String[0]
                            : valiParams.toArray(new String[0]);

                     ValidatorEnum valiType = ValidatorEnum.nonStrictValueOf(valiMethod[0]);
                     if (valiType == null) {
                         throw new IllegalArgumentException("未知的校验器:" + valiMethod[0]);
                     }
                     validatorEnums.add(new Pair<>(valiType, valiType.validator() == null ? null : valiType.validator().parseValidatorArgs(valiArgs)));
                }
                param.setValidators(validatorEnums);
            } else {
                Map<String, ValidationNamespace.ValidationParam> childParam = parseChildParam(mapValue, param, clazz, null);
                param.setFields(childParam);
            }
        }
        return paramMap;
    }

    private static Method getDeclaredMethodByName(Class<?> clazz, String methodName) {
        // 只支持当前类中声明的方法，父级的获取不到方法名...
        Method[] methods = clazz.getDeclaredMethods();
        Method m = null;
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                if (m != null) {
                    throw new IllegalStateException(clazz + " 重复的方法名:" + methodName);
                }
                m = method;
            }
        }
        return m;
    }

    public static void main(String[] args) throws IOException, NoSuchMethodException {
        System.out.println(Map.class.isAssignableFrom(HashMap.class));
        System.out.println(HashMap.class.isAssignableFrom(Map.class));

    }

}
