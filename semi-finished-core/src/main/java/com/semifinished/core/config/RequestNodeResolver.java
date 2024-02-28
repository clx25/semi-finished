package com.semifinished.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.annontation.RequestParamNode;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.utils.Assert;
import lombok.AllArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.AbstractNamedValueMethodArgumentResolver;

import java.util.Map;

@AllArgsConstructor
public class RequestNodeResolver extends AbstractNamedValueMethodArgumentResolver {

    private final ObjectMapper objectMapper;

    @Override
    @NonNull
    protected NamedValueInfo createNamedValueInfo(MethodParameter methodParameter) {
        RequestParamNode requestParamNode = methodParameter.getParameterAnnotation(RequestParamNode.class);
        if (requestParamNode == null) {
            return new NamedValueInfo("", false, ValueConstants.DEFAULT_NONE);
        }
        return new NamedValueInfo(requestParamNode.name(), requestParamNode.required(), requestParamNode.defaultValue());
    }

    @Override
    protected Object resolveName(@NonNull String name, @NonNull MethodParameter methodParameter, NativeWebRequest nativeWebRequest) throws Exception {
        Map<String, String[]> parameterMap = nativeWebRequest.getParameterMap();
        if (parameterMap.isEmpty()) {
            return null;
        }
        Map<String, Object> result = CollectionUtils.newLinkedHashMap(parameterMap.size());
        parameterMap.forEach((key, values) -> {
            if (values.length == 1) {
                result.put(key, values[0]);
                return;
            }
            result.put(key, values);
        });

        Class<?> type = methodParameter.getParameter().getType();


        Assert.isFalse(ObjectNode.class.isAssignableFrom(type), () -> new ParamsException(name + "格式错误"));
        try {

            return objectMapper.convertValue(result, type);
        } catch (Exception e) {
            throw new ParamsException(name + "格式错误", e);
        }

    }


    /**
     * 如果{@link #createNamedValueInfo}中返回的required为true,
     * 但是{@link #resolveName}返回null，那么就会调用该方法，抛出一个异常
     */
    @Override
    protected void handleMissingValue(@NonNull String name, @NonNull MethodParameter parameter, @NonNull NativeWebRequest request) {
        throw new ParamsException("缺少参数");
    }

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.hasParameterAnnotation(RequestParamNode.class);
    }
}
