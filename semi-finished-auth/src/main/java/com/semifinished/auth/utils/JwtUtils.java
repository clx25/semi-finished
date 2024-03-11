package com.semifinished.auth.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.semifinished.core.config.ConfigProperties;
import com.semifinished.core.exception.CodeException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * jwt token的创建与校验
 */
public class JwtUtils {
    public static final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
    private final static Algorithm algorithm = Algorithm.HMAC256("jfap84jt&f9fr4wtsWS3");

    public static String createToken(ConfigProperties configProperties, String subject, String id, String roleId, String deptId, long tokenDuration) {
        long tokenDate = new Date().getTime() + tokenDuration * 1000;

        return JWT.create()
                .withSubject(subject)
                .withClaim(configProperties.getIdKey(), id)
                .withClaim("roleId", roleId)
                .withClaim("deptId", deptId)
                .withClaim("efficient", tokenDuration)
                .withExpiresAt(new Date(tokenDate))
                .sign(algorithm);
    }

    public static DecodedJWT parseToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new CodeException("传入的token为null");
        }
        return JWT.require(algorithm).build().verify(token);
    }

    public static String refreshToken(ConfigProperties configProperties, String token, DecodedJWT jwt) {
        Date expiresAt = jwt.getExpiresAt();

        if (expiresAt.getTime() - new Date().getTime() < 3 * 24 * 60 * 60 * 1000) {
            Long efficient = jwt.getClaim("efficient").asLong();
            return createToken(configProperties, jwt.getSubject(), jwt.getClaim("id").asString(), jwt.getClaim("roleId").asString(), jwt.getClaim("deptId").asString(), efficient);
        }
        return token;
    }


}
