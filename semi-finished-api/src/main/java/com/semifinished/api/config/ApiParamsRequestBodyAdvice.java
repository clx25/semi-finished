//package com.semifinished.api.config;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import com.semifinished.api.listener.JsonApiInit;
//import lombok.AllArgsConstructor;
//import org.springframework.core.MethodParameter;
//import org.springframework.http.HttpInputMessage;
//import org.springframework.http.converter.HttpMessageConverter;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;
//
//import java.io.IOException;
//import java.lang.reflect.Type;
//
///**
// * 替换通用接口参数
// */
////@ControllerAdvice
//@AllArgsConstructor
//public class ApiParamsRequestBodyAdvice implements RequestBodyAdvice {
//    private final ParamsReplacer paramsReplacer;
//
//    @Override
//    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
//
//        if (!JsonNode.class.isAssignableFrom(methodParameter.getParameterType())) {
//            return false;
//        }
//        RequestMapping methodAnnotation = methodParameter.getMethodAnnotation(RequestMapping.class);
//
//        if (methodAnnotation != null) {
//            return JsonApiInit.apiRequestNameGroupMapping.containsKey(methodAnnotation.name());
//        }
//        return false;
//    }
//
//    @Override
//    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
//        return inputMessage;
//    }
//
//    @Override
//    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
//        return paramsReplacer.replace((ObjectNode) body);
//    }
//
//    @Override
//    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
//        return paramsReplacer.replace((ObjectNode) body);
//    }
//
//
//}
