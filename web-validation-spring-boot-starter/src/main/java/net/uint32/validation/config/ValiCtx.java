package net.uint32.validation.config;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * 验证器上下文
 */
@Data
public class ValiCtx {

    private Set<String> results;

    public void putResult(String result) {
        if (results == null) {
            results = new HashSet<>();
        }
        results.add(result);
    }

    public Set<String> getResults() {
        if (results == null) {
            results = new HashSet<>();
        }
        return results;
    }

}
