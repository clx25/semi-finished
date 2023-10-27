package com.semifinished;

import com.semifinished.config.CoreConfigurer;
import com.semifinished.pojo.Desensitization;
import com.semifinished.util.MapUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class TestCoreConfigurer implements CoreConfigurer {
    @Override
    public void addExcludeColumn(Map<String, List<String>> excludeColumns) {
        excludeColumns.put("info", Arrays.asList("seen", "date"));
    }

    @Override
    public void addColumnMapping(Map<String, Map<String, String>> columnMapping) {
        columnMapping.put("info", MapUtils.of("title", "tt"));
    }

    @Override
    public void addTableMapping(Map<String, String> tableMappings) {
        tableMappings.put("actors", "act");
    }

    @Override
    public void addDesensitize(List<Desensitization> desensitize) {
        Desensitization build = Desensitization.builder().table("info")
                .column("title")
                .desensitize((title) -> "a" + title + "c")
                .build();
        desensitize.add(build);
    }

    @Override
    public String chooseDb() {
        return "master";
    }
}
