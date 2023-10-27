package com.semifinished.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.semifinished.annontation.RequestBodyValue;
import com.semifinished.exception.ParamsException;
import lombok.AllArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.AbstractNamedValueMethodArgumentResolver;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * {@link RequestBodyValue}的解析器
 */
@AllArgsConstructor
public class RequestValueResolver extends AbstractNamedValueMethodArgumentResolver {

    private final ObjectMapper objectMapper;

    /**
     * 构建注解信息，匹配的name，是否必需，默认值
     * 该方法有缓存，只会调用一次
     *
     * @param parameter spring封装的方法参数描述类
     * @return 注解信息
     */
    @Override
    @NonNull
    protected NamedValueInfo createNamedValueInfo(MethodParameter parameter) {
        RequestBodyValue requestBodyValue = parameter.getParameterAnnotation(RequestBodyValue.class);
        if (requestBodyValue == null) {
            return new NamedValueInfo("", false, ValueConstants.DEFAULT_NONE);
        }
        return new NamedValueInfo(requestBodyValue.name(), requestBodyValue.required(), requestBodyValue.defaultValue());
    }

    /**
     * 具体的请求解析方法，spring会将返回值赋值给入参
     */
    @Override
    protected Object resolveName(@NonNull String name, @NonNull MethodParameter parameter, NativeWebRequest request) throws IOException {
        HttpServletRequest servletRequest = request.getNativeRequest(HttpServletRequest.class);
        if (servletRequest == null) {
            return null;
        }

        String bodyCache = (String) servletRequest.getAttribute("body");
        if (bodyCache == null) {
            String str;
            StringBuilder body = new StringBuilder();
            BufferedReader br = servletRequest.getReader();
            while ((str = br.readLine()) != null) {
                body.append(str);
            }
            bodyCache = body.toString();
            servletRequest.setAttribute("body", bodyCache);
        }

        JsonNode jsonNode = objectMapper.readTree(bodyCache);

        if (!jsonNode.isObject()) {
            throw new ParamsException("参数类型错误");
        }
        return jsonNode.path(name).asText(null);
    }

    /**
     * 如果{@link #createNamedValueInfo}中返回的required为true,
     * 但是{@link #resolveName}返回null，那么就会调用该方法，抛出一个异常
     */
    @Override
    protected void handleMissingValue(@NonNull String name, @NonNull MethodParameter parameter, @NonNull NativeWebRequest request) {
        throw new ParamsException("缺少参数:" + name);
    }


    /**
     * 判断该resolver是否支持
     * 该方法有缓存，只会调用一次
     *
     * @param parameter spring封装的方法参数描述类，可以从中获取将要被调用的controller方法的入参，每个入参，调用一次该方法
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        //判断是否有该注解
        return parameter.hasParameterAnnotation(RequestBodyValue.class);
    }
}
