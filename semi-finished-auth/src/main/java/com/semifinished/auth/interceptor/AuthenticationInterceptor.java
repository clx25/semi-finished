package com.semifinished.auth.interceptor;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.semifinished.auth.cache.AuthCacheKey;
import com.semifinished.auth.config.AuthProperties;
import com.semifinished.auth.config.AuthResultInfo;
import com.semifinished.auth.exception.AuthException;
import com.semifinished.auth.utils.JwtUtils;
import com.semifinished.core.cache.SemiCache;
import com.semifinished.core.config.ConfigProperties;
import lombok.AllArgsConstructor;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Map;

/**
 * 登录认证拦截器
 */

@AllArgsConstructor
public class AuthenticationInterceptor implements HandlerInterceptor {
    private final PathMatcher pathMatcher = new AntPathMatcher();
    private final ConfigProperties configProperties;
    private final AuthProperties authProperties;
    private final SemiCache semiCache;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //测试使用
//        request.setAttribute("id", "1");

        //如果关闭了验证，直接跳过验证
        if (!authProperties.isAuthEnable()) {
            return true;
        }

        //判断该请求是否匹配跳过规则
        if (skipAuth(request)) {
            return true;
        }

        //获取token并验证
        String token = request.getHeader(authProperties.getTokenKey());
        if (!StringUtils.hasText(token)) {
            throw new AuthException(AuthResultInfo.UNAUTHORIZED);
        }

        try {
            //获取token信息放入attribute中方便在其他地方使用
            DecodedJWT decodedJWT = JwtUtils.parseToken(token);

            request.setAttribute("username", decodedJWT.getSubject());
            String idKey = configProperties.getIdKey();
            request.setAttribute(idKey, decodedJWT.getClaim(idKey).asString());
            request.setAttribute("roleId", decodedJWT.getClaim("roleId").asString());
            request.setAttribute("deptId", decodedJWT.getClaim("deptId").asString());

            //刷新token，放入响应头
            String newToken = JwtUtils.refreshToken(configProperties, token, decodedJWT);
            response.setHeader(authProperties.getTokenKey(), newToken);

        } catch (Exception e) {
            throw new AuthException(AuthResultInfo.UNAUTHORIZED);
        }
        return true;
    }


    /**
     * 判断当前请求是否跳过认证
     *
     * @param req request
     * @return 是否跳过认证
     */
    private boolean skipAuth(HttpServletRequest req) {

        Map<String, String> skipAuth = semiCache.getValue(AuthCacheKey.SKIP_AUTH.getKey());

        if (CollectionUtils.isEmpty(skipAuth)) {
            return false;
        }

        String servletPath = req.getServletPath();
        String method = req.getMethod();

        for (Map.Entry<String, String> entry : skipAuth.entrySet()) {

            String methods = entry.getValue();
            if (!StringUtils.hasText(methods)) {
                continue;
            }
            if (!"*".equals(methods.trim())) {
                String[] matchMethods = methods.split(",");
                //请求方式是否匹配
                if (Arrays.stream(matchMethods).noneMatch(method::equalsIgnoreCase)) {
                    continue;
                }
            }

            String pattern = entry.getKey().trim();
            if (!pattern.startsWith("/")) {
                pattern = "/" + pattern;
            }
            if (pathMatcher.match(pattern, servletPath)) {
                return true;
            }
        }


        return false;
    }


}
