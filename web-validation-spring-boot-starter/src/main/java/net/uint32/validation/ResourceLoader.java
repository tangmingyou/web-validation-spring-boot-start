package net.uint32.validation;

import net.uint32.validation.model.ValidationConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 加载配置器配置文件
 *
 * @author tangmingyou
 * @date 2021-09-24 14:39
 */
public class ResourceLoader {

    public static List<ValidationConfig> loadConfig(String ... locationPatterns) throws IOException {
        List<ValidationConfig> validationConfigs = new LinkedList<>();
        for (String location : locationPatterns) {
            List<ValidationConfig> configs = loadResources(location);
            validationConfigs.addAll(configs);
        }
        return validationConfigs;
    }

    public static void main(String[] args) throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("validation/**/*.yml");
        Resource[] resources2 = resolver.getResources("validation/**/*.yaml");
        System.out.println(Arrays.toString(resources));
        System.out.println(Arrays.toString(resources2));

        System.out.println(loadResources(resources));
        System.out.println(loadResources(resources2));

    }

    public static List<ValidationConfig> loadResources(String location) throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("validation/**/*.yml");
        return loadResources(resources);
    }

    public static List<ValidationConfig> loadResources(Resource[] resources) throws IOException {
        List<ValidationConfig> configs = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        char[] buf = new char[1024];
        for (Resource resource : resources) {
            try (
                InputStream is = resource.getInputStream();
                InputStreamReader reader = new InputStreamReader(is);
            ) {
                int len = 0;
                while (0 < (len = reader.read(buf))) {
                    builder.append(buf, 0, len);
                }
            }
            Yaml yaml = new Yaml();
            ValidationConfig config = yaml.loadAs(builder.toString(), ValidationConfig.class);
            configs.add(config);
            builder.delete(0, builder.length());
        }
        return configs;
    }
}
