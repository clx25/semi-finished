package com.semifinished.excel.config;

import java.util.Map;

public interface ExcelConfigurer {
    void addHeaderMap(Map<String, Map<String, String>> headerMap);
}
