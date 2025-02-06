package com.relive27.handler;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.util.Assert;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;


/**
 * 处理JWT登录成功的认证逻辑。
 * <p>
 * 该类实现了AuthenticationSuccessHandler接口，用于在用户认证成功后生成JWT令牌，并返回给客户端。
 * 使用Nimbus JOSE库生成和签名JWT令牌。
 * </p>
 *
 * @author: ReLive27
 * @date: 2025/2/5 21:32
 */
@Slf4j
public class NimbusJwtLoginAuthenticationHandler implements AuthenticationSuccessHandler {

    /**
     * 用于将生成的消息转换为HTTP响应的消息转换器。
     */
    private final HttpMessageConverter<Object> messageConverter = new MappingJackson2HttpMessageConverter();

    /**
     * JWK（JSON Web Key）源，用于获取签名公钥。
     */
    private final JWKSource<SecurityContext> jwkSource;

    /**
     * 构造器，注入JWK源。
     *
     * @param jwkSource JWK源，提供公钥用于签名JWT
     * @throws IllegalArgumentException 如果jwkSource为null
     */
    public NimbusJwtLoginAuthenticationHandler(JWKSource<SecurityContext> jwkSource) {
        Assert.notNull(jwkSource, "jwkSource cannot be null");
        this.jwkSource = jwkSource;
    }

    /**
     * 认证成功后的处理方法，生成JWT并返回给客户端。
     *
     * @param request        HTTP请求
     * @param response       HTTP响应
     * @param authentication 认证对象，包含认证信息
     * @throws IOException      如果写入响应时发生I/O错误
     * @throws ServletException 如果处理请求时发生Servlet异常
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
        String token;

        try {
            // 创建JWT头部信息，指定算法为RS256
            JWSHeader headers = new JWSHeader.Builder(JWSAlgorithm.RS256).build();

            // 根据JWT头部创建JWK选择器
            JWKSelector jwkSelector = new JWKSelector(createJwkMatcher(headers));
            // 获取公钥
            JWK jwk = this.jwkSource.get(jwkSelector, null).get(0);

            // 创建JWT签名器，使用RSA签名算法
            JWSSigner signer = new RSASSASigner(jwk.toRSAKey());

            // 构建JWT声明集，包含认证信息和过期时间
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(authentication.getName()) // 设置认证用户的用户名
                    .issuer("http://localhost:8080")    // 设置JWT的发行者
                    .expirationTime(new Date(new Date().getTime() + 60 * 1000)) // 设置过期时间为1分钟
                    .build();

            // 使用签名器签署JWT
            SignedJWT signedJWT = new SignedJWT(headers, claimsSet);
            signedJWT.sign(signer);
            // 获取生成的JWT令牌
            token = signedJWT.serialize();
        } catch (JOSEException e) {
            // 生成JWT失败，返回错误信息
            log.error("生成Token失败", e);
            this.messageConverter.write(Collections.singletonMap("error", "登录失败"), null, httpResponse);
            return;
        }

        // 返回生成的JWT令牌给客户端
        this.messageConverter.write(Collections.singletonMap("token", token), null, httpResponse);
    }

    /**
     * 创建JWK匹配器，用于选择适合的JWK进行签名。
     *
     * @param headers JWT头部信息
     * @return JWKMatcher 用于匹配适合的JWK
     */
    private static JWKMatcher createJwkMatcher(JWSHeader headers) {
        JWSAlgorithm jwsAlgorithm = JWSAlgorithm.parse(headers.getAlgorithm().getName());
        return new JWKMatcher.Builder()
                .keyType(KeyType.forAlgorithm(jwsAlgorithm)) // 设置密钥类型
                .keyID(headers.getKeyID()) // 设置密钥ID
                .keyUses(KeyUse.SIGNATURE, null) // 设置密钥用途为签名
                .algorithms(jwsAlgorithm, null) // 设置算法
                .x509CertSHA256Thumbprint(headers.getX509CertSHA256Thumbprint()) // 设置证书指纹
                .build();
    }
}
