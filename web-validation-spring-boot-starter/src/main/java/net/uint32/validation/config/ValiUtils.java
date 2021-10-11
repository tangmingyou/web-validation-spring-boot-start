package net.uint32.validation.config;

import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Pattern;

public class ValiUtils {

    /**
     * 开头和结尾空字符(制表符、换行符、换页符、回车符和空格)的匹配
     */
    private static final Pattern EMPTY_PATTERN = Pattern.compile("(^[ \\t\\n\\f\\r]+|[ \\t\\n\\f\\r]+$)");

    private static final Pattern EMPTY_LEFT_PATTERN = Pattern.compile("^[ \\t\\n\\f\\r]+");

    private static final Pattern EMPTY_RIGHT_PATTERN = Pattern.compile("[ \\t\\n\\f\\r]+$");

    /**
     * 去掉字符串两边的制表符、换行符、换页符、回车符和空格
     *
     * @return 去掉两端后的字符串
     */
    public static String trim(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return EMPTY_PATTERN.matcher(str).replaceAll("");
    }

    /**
     * 去掉字符串右边的制表符、换行符、换页符、回车符和空格
     *
     * @return 去掉两端后的字符串
     */
    public static String trimRight(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return EMPTY_RIGHT_PATTERN.matcher(str).replaceAll("");
    }

    /**
     * 去掉字符串左边的制表符、换行符、换页符、回车符和空格
     *
     * @return 去掉两端后的字符串
     */
    public static String trimLeft(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return EMPTY_LEFT_PATTERN.matcher(str).replaceAll("");
    }

    /**
     * 将 hello(params) 解析成 hello 和 params 两部分
     * @return [0] = hello，[1] = params，若没有 params 则[1] = null
     */
    public static String[] parseMethodStr(String template, char open, char close) {
        String[] parts = new String[2];
        int idx1 = template.indexOf(open);
        if (idx1 == -1) {
            parts[0] = template;
            return parts;
        }
        int idx2 = template.lastIndexOf(close);
        if (idx2 == -1 || idx1 >= idx2) {
            parts[0] = template;
            return parts;
        }
        // 参数
        String paramsStr = template.substring(idx1 + 1, idx2);
        // 名
        String method = trim(template.substring(0, idx1));
        parts[0] = method;
        parts[1] = paramsStr;
        return parts;
    }

    /**
     * 将 "1,a",2,3 字符串转换成 ["1,a", "2", "3"]
     * @param paramsStr k,v 字符串
     * @return 转换后的 map 对象
     */
    public static Map<String, String> parseKvStr(String paramsStr, int aboutSize) {
        if (StringUtils.isEmpty(paramsStr)) {
            return Collections.emptyMap();
        }
        // 解析验证器参数
        Map<String, String> params = new HashMap<>(aboutSize);
        String param = "";
        for (int last = 0, idx = 0, i = 0; i != -1;) {
            i = paramsStr.indexOf(",", last);
            idx = i == -1 ? paramsStr.length() : i;
            // System.out.println("----" + last + "," + idx + "," + i + ": " + (i == -1 ? null:paramsStr.substring(i)));
            if (last >= idx) {
                break;
            }
            param = paramsStr.substring(last, idx);
            int j = param.indexOf("=");
            if (j == -1) {
                throw new IllegalArgumentException("validator param parse fail, parse use \"k1=v1,k2=v2\" pattern at : " + paramsStr);
            }
            String paramKey = trim(param.substring(0, j));
            String paramValue = trim(param.substring(j + 1));
            // 判断参数值是不是字符串
            boolean isStr = false;
            if (!StringUtils.isEmpty(paramValue)) {
                char firstChar = paramValue.charAt(0);
                if (firstChar == '\'' || firstChar == '"') {
                    isStr = true;
                    char closeChar = paramValue.charAt(paramValue.length() - 1);
                    // 如果 value 是字符串且里面包含 ,
                    if (closeChar != firstChar) {
                        int closeCharIdx = paramsStr.indexOf(firstChar, idx);
                        if (closeCharIdx == -1) {
                            throw new IllegalArgumentException("validator param " + paramKey + " no string close char with " + firstChar);
                        }
                        // 去掉字符串首 ' "
                        paramValue = paramValue.substring(1) + trimRight(paramsStr.substring(idx, closeCharIdx));
                        // 更新扫描位置
                        i = idx = closeCharIdx + 1;
                    } else {
                        // 去掉字符串首尾 ' "
                        paramValue = paramValue.substring(1, paramValue.length() - 1);
                    }
                }
            }
            // value 类型转换 true false number
            params.put(paramKey, !isStr && "null".equals(paramValue) ? null : paramValue);
            last = idx + 1;
        }
        return params;
    }


    /**
     *  将 length(1,2) 中的 1,2 解析成 [1, 2] 两部分
     * @param paramsStr , 分隔的参数字符串
     * @return 解析后的list 对象
     */
    public static List<String> parseParameters(String paramsStr) {
        if (StringUtils.isEmpty(paramsStr)) {
            return Collections.emptyList();
        }
        // 解析验证器参数
        List<String> parameters = new LinkedList<>();
        String param = "";
        for (int open = 0, close = 0, i = 0; i != -1;) {
            // 去掉这个参数前面的空白字符
            for (;open < paramsStr.length();open++) {
                if (!Character.isWhitespace(paramsStr.charAt(open))) {
                    break;
                }
            }
            char openChar = paramsStr.charAt(open);
            boolean isStr = false;
            // 是 '' 或 "" 字符串包裹符开头的
            if (openChar == '\'' || openChar == '"') {
                close = open;
                do {
                    close = close + 1;
                    if (close >= paramsStr.length()
                        || -1 == (close = paramsStr.indexOf(openChar, close))) {
                        throw new IllegalArgumentException(paramsStr + "" + openChar + " 包裹的字符串参数没有结尾");
                    }
                } while(paramsStr.charAt(close - 1) == '\\');
                 close = close + 1;
                isStr = true;
            } else {
                i = paramsStr.indexOf(',', open);
                close = i == -1 ? paramsStr.length() : i;
            }
            param = trimRight(paramsStr.substring(open, close));
            // 不保留 '' "" 字符串包裹符
            param = isStr ? param.substring(1, param.length() - 1) : param;
            // 字符串包裹符未包裹的 null 字符串，转换成 null 值
            parameters.add(!isStr && "null".equals(param) ? null : param);

            open = isStr ? paramsStr.indexOf(',', close) : close + 1;
            if (open == -1) {
                if (!trim(paramsStr.substring(close)).isEmpty()) {
                    throw new IllegalArgumentException(paramsStr + " 中无法识别的参数: " + paramsStr.substring(close));
                } else {
                    break;
                }
            } else if (open >= paramsStr.length()) {
                break;
            }
            open = isStr ? open + 1 : open;
        }
        return parameters;
    }

    public static <T> T isNullOr(T val, T other) {
        return val == null ? other : val;
    }

    public static void main(String[] args) {

        System.out.println(Arrays.toString(parseMethodStr("hello", '(', ')')));
        System.out.println(parseParameters("  'a\\\\'az',2  ,  3, 45, '  22,3  ', \"'55,,6'\""));

    }
}
