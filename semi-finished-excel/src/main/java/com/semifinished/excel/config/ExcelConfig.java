package com.semifinished.excel.config;

import com.semifinished.core.config.CoreConfigurer;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ExcelConfig implements CoreConfigurer {

    @Override
    public void addJsonConfig(Map<String, String> jsonConfig) {
        jsonConfig.put("SEMI-JSON-API-EXCEL", "EXCEL");
    }
}
