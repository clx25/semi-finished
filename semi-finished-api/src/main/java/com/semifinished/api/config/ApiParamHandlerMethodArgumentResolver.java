//package com.semifinished.api.config;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import com.semifinished.api.annotation.ApiGroup;
//import com.semifinished.api.listener.JsonApiInit;
//import lombok.AllArgsConstructor;
//import org.springframework.core.MethodParameter;
//import org.springframework.core.annotation.AnnotatedElementUtils;
//import org.springframework.http.server.ServletServerHttpRequest;
//import org.springframework.util.Assert;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.support.WebDataBinderFactory;
//import org.springframework.web.context.request.NativeWebRequest;
//import org.springframework.web.method.support.HandlerMethodArgumentResolver;
//import org.springframework.web.method.support.ModelAndViewContainer;
//
//import javax.servlet.http.HttpServletRequest;
//import java.lang.reflect.Method;
//import java.util.Map;
//
//@AllArgsConstructor
//public class ApiParamHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {
//
//    private final ObjectMapper objectMapper;
//    private final ApiProperties apiProperties;
//    private final ParamsReplacer paramsReplacer;
//
//    @Override
//    public boolean supportsParameter(MethodParameter parameter) {
//        Map<String, String> apiRequestNameGroupMapping = JsonApiInit.apiRequestNameGroupMapping;
//        Method method = parameter.getMethod();
//        boolean support = false;
//        if (method != null) {
//            RequestMapping requestMapping = AnnotatedElementUtils.getMergedAnnotation(method, RequestMapping.class);
//            if (requestMapping != null) {
//                String name = requestMapping.name();
//                support = apiRequestNameGroupMapping.containsKey(name);
//            }
//        }
//
//        return !apiProperties.isCommonApiEnable() && (support ||
//                (parameter.hasMethodAnnotation(ApiGroup.class) &&
//                        JsonNode.class.isAssignableFrom(parameter.getParameterType())));
//    }
//
//    @Override
//    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
//        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
//        Assert.state(servletRequest != null, "No HttpServletRequest");
//        ServletServerHttpRequest inputMessage = new ServletServerHttpRequest(servletRequest);
//        ObjectNode objectNode = this.objectMapper.readValue(inputMessage.getBody(), ObjectNode.class);
//        return paramsReplacer.replace(objectNode);
//    }
//
//}
