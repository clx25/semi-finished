package com.semifinished.exception;

/**
 * 在项目启动时会检查配置，如果配置有问题抛出该异常，如格式错误，缺少必要配置
 */
public class ConfigException extends ProjectRuntimeException {
    public ConfigException(String message) {
        super(message);
    }
}
