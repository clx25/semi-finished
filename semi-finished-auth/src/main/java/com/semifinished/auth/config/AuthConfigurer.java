package com.semifinished.auth.config;

import java.util.Map;

public interface AuthConfigurer {
    void skipApi(Map<String, String> skipApi);
}
