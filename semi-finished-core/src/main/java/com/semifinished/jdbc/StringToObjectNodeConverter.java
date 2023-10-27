package com.semifinished.jdbc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.exception.ProjectRuntimeException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 查询数据库的数据类型转换器，把String类型转为ObjectNode类型
 */
@Component
@AllArgsConstructor
public class StringToObjectNodeConverter implements Converter<String, ObjectNode>, InitializingBean {
    private final ObjectMapper objectMapper;

    @Override
    public ObjectNode convert(String row) {
        if (!StringUtils.hasText(row)) {
            return null;
        }
        try {
            return objectMapper.readValue(row, ObjectNode.class);
        } catch (JsonProcessingException e) {
            throw new ProjectRuntimeException("内容无法转为json格式");
        }
    }


    @Override
    public void afterPropertiesSet() {
        ConversionService sharedInstance = DefaultConversionService.getSharedInstance();
        ((GenericConversionService) sharedInstance).addConverter(this);
    }
}
