package net.uint32.validation.config;

import org.objectweb.asm.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * desc
 *
 * @author tangmingyou
 * @date 2021-09-24 13:47
 */
public class ReflectUtils {

    /**
     * 获取指定类指定方法的参数名
     * https://blog.csdn.net/mhmyqn/article/details/47294485
     *
     * @param clazz  要获取参数名的方法所属的类
     * @param method 要获取参数名的方法
     * @return 按参数顺序排列的参数名列表，如果没有参数，则返回null
     */
    public static String[] getMethodParameterNamesByAsm4(Class<?> clazz, final Method method) throws IOException {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 0) {
            return null;
        }
        final Type[] types = new Type[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            types[i] = Type.getType(parameterTypes[i]);
        }
        final String[] parameterNames = new String[parameterTypes.length];
        String className = clazz.getName();
        int lastDotIndex = className.lastIndexOf(".");
        className = className.substring(lastDotIndex + 1) + ".class";
        InputStream is = clazz.getResourceAsStream(className);
        ClassReader classReader = new ClassReader(is);
        classReader.accept(new ClassVisitor(Opcodes.ASM4) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                // 只处理指定的方法
                Type[] argumentTypes = Type.getArgumentTypes(desc);
                if (!method.getName().equals(name) || !Arrays.equals(argumentTypes, types)) {
                    return null;
                }
                return new MethodVisitor(Opcodes.ASM5) {
                    @Override
                    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
                        // 静态方法第一个参数就是方法的参数，如果是实例方法，第一个参数是this
                        if (Modifier.isStatic(method.getModifiers())) {
                            parameterNames[index] = name;
                        } else if (index > 0) {
                            if (index > parameterNames.length) {return;}
                            parameterNames[index - 1] = name;
                        }
                    }
                };

            }
        }, 0);
        return parameterNames;
    }

    public static Field getField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            try {
                return clazz.getField(fieldName);
            } catch (NoSuchFieldException ex) {
                return null;
            }
        }
    }

    public static final Object GETTER_FAIL = new Object();

    private String FAILED_FIELD;

    public static Object getValue(Object obj, Field field) {
        if (obj instanceof Map) {
            return ((Map<?,?>)obj).get(field);
        }
        // 集合获取不了
        else if (obj instanceof Collection) {
            return GETTER_FAIL;
        }
        try {
            field.setAccessible(true);
            return field.get(obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object getFieldValue(Object obj, String fieldName) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Map) {
            return ((Map<?,?>)obj).get(fieldName);
        }
        return getValue(obj, getField(obj.getClass(), fieldName));
    }

    public static Object newInstance(Class<?> clazz) throws ReflectiveOperationException {
        if (Map.class.isAssignableFrom(clazz)) {
            return new HashMap<>();
        }
        return clazz.getDeclaredConstructor().newInstance();
    }

    public static void setValue(Object obj, String fieldName, Object value) {
        if (obj == null) {
            return;
        }
        if (obj instanceof Map) {
            ((Map)obj).put(fieldName, value);
        }
        Field field = getField(obj.getClass(), fieldName);
        if (field == null) {
            return;
        }
        try {
            field.setAccessible(true);
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(fieldName + " 字段赋默认值失败", e);
        }
    }

    public static Class<?> getFieldType(Class<?> objType, String fieldName) {
        Field field = getField(objType, fieldName);
        return field == null ? null : field.getType();
    }

//    public static void main(String[] args) throws NoSuchMethodException, IOException {
//        System.out.println(getField(ReflectUtils.class, "FAILED_FIELD"));
//    }

}
