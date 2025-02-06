package com.relive27.cas.client;

import org.jasig.cas.client.util.CommonUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;


/**
 * CAS（Central Authentication Service）认证入口点，处理未认证请求并重定向至CAS登录页面。
 * <p>
 * 该类实现了 {@link AuthenticationEntryPoint}，用于处理身份认证异常，并引导用户进行CAS登录。
 * 当 {@code httpStatus} 设置为 {@link HttpStatus#OK} 时，返回 JSON 格式的重定向 URL；
 * 否则，执行 302 重定向。
 * </p>
 *
 * @author: ReLive27
 * @date: 2025/2/6 13:25
 */
public class CasJsonAuthenticationEntryPoint implements AuthenticationEntryPoint, InitializingBean {

    /**
     * CAS 服务相关配置，包含服务地址和参数。
     */
    private ServiceProperties serviceProperties;

    /**
     * CAS 登录 URL，用户未认证时会被重定向到该地址。
     */
    private String loginUrl;

    /**
     * 是否在服务 URL 中包含会话 ID，默认为 {@code true}。
     */
    private boolean encodeServiceUrlWithSessionId = true;

    /**
     * 认证失败时返回的 HTTP 状态码，默认为 {@code FOUND (302)}。
     * 若设置为 {@code OK (200)}，则返回 JSON 格式的重定向 URL。
     */
    private HttpStatus httpStatus = HttpStatus.FOUND;

    /**
     * JSON 消息转换器，用于返回 JSON 格式的重定向 URL。
     */
    private final HttpMessageConverter<Object> messageConverter = new MappingJackson2HttpMessageConverter();

    /**
     * 校验必要属性，确保 {@code loginUrl} 和 {@code serviceProperties} 不能为空。
     *
     * @throws IllegalArgumentException 如果 `loginUrl` 或 `serviceProperties` 为空
     */
    @Override
    public void afterPropertiesSet() {
        Assert.hasLength(this.loginUrl, "loginUrl must be specified");
        Assert.notNull(this.serviceProperties, "serviceProperties must be specified");
        Assert.notNull(this.serviceProperties.getService(), "serviceProperties.getService() cannot be null.");
    }

    /**
     * 处理未认证请求，生成 CAS 认证重定向 URL，并根据 {@code httpStatus} 选择返回 JSON 或重定向。
     *
     * @param request       HTTP 请求
     * @param response      HTTP 响应
     * @param authException 认证异常
     * @throws IOException 如果写入响应时发生 I/O 错误
     */
    @Override
    public final void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        // 生成 CAS 服务 URL
        String urlEncodedService = createServiceUrl(request, response);
        // 生成 CAS 重定向 URL
        String redirectUrl = createRedirectUrl(urlEncodedService);
        // 预处理（可由子类实现）
        preCommence(request, response);

        if (HttpStatus.OK.equals(httpStatus)) {
            // 以 JSON 格式返回重定向 URL
            ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
            messageConverter.write(Collections.singletonMap("redirectUrl", redirectUrl), null, httpResponse);
        } else {
            // 302 重定向至 CAS 服务器
            response.sendRedirect(redirectUrl);
        }
    }

    /**
     * 生成 CAS 服务 URL。
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @return 生成的服务 URL
     */
    protected String createServiceUrl(HttpServletRequest request, HttpServletResponse response) {
        return CommonUtils.constructServiceUrl(null, response,
                this.serviceProperties.getService(), null,
                this.serviceProperties.getArtifactParameter(),
                this.encodeServiceUrlWithSessionId);
    }

    /**
     * 生成 CAS 登录重定向 URL。
     *
     * @param serviceUrl 生成的服务 URL
     * @return CAS 登录重定向 URL
     */
    protected String createRedirectUrl(String serviceUrl) {
        return CommonUtils.constructRedirectUrl(
                this.loginUrl,
                this.serviceProperties.getServiceParameter(),
                serviceUrl,
                this.serviceProperties.isSendRenew(),
                false);
    }

    /**
     * 预处理方法，供子类扩展，默认不做任何处理。
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     */
    protected void preCommence(HttpServletRequest request, HttpServletResponse response) {
        // 允许子类进行扩展
    }

    // -------------------- Getter 和 Setter 方法 --------------------

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getLoginUrl() {
        return this.loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public ServiceProperties getServiceProperties() {
        return this.serviceProperties;
    }

    public void setServiceProperties(ServiceProperties serviceProperties) {
        this.serviceProperties = serviceProperties;
    }

    public void setEncodeServiceUrlWithSessionId(boolean encodeServiceUrlWithSessionId) {
        this.encodeServiceUrlWithSessionId = encodeServiceUrlWithSessionId;
    }

    protected boolean getEncodeServiceUrlWithSessionId() {
        return this.encodeServiceUrlWithSessionId;
    }
}
