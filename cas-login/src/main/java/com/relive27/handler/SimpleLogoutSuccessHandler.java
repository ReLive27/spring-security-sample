package com.relive27.handler;

import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.util.Assert;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * 退出成功处理器，在用户注销成功后返回 JSON 格式的重定向 URL。
 *
 * @author: ReLive27
 * @date: 2025/2/6 16:17
 */
public class SimpleLogoutSuccessHandler implements LogoutSuccessHandler {

    private final HttpMessageConverter<Object> messageConverter = new MappingJackson2HttpMessageConverter();

    /**
     * 退出成功后的重定向目标 URL。
     */
    private final String targetUrl;

    /**
     * 构造方法，初始化目标 URL。
     *
     * @param targetUrl 退出成功后跳转的目标地址
     * @throws IllegalArgumentException 如果 {@code targetUrl} 为空
     */
    public SimpleLogoutSuccessHandler(String targetUrl) {
        Assert.hasText(targetUrl, "targetUrl cannot be empty");
        this.targetUrl = targetUrl;
    }

    /**
     * 处理用户注销成功逻辑，以 JSON 形式返回重定向地址。
     *
     * @param request        HTTP 请求
     * @param response       HTTP 响应
     * @param authentication 用户认证信息（可能为 null）
     * @throws IOException      如果写入响应时发生 I/O 错误
     * @throws ServletException 如果发生 Servlet 相关异常
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 清除 session 以确保彻底退出
        request.getSession().invalidate();

        // 返回 JSON 格式的重定向 URL
        ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
        messageConverter.write(Collections.singletonMap("redirectUrl", targetUrl), null, httpResponse);
    }

    /**
     * 获取退出后重定向的目标 URL。
     *
     * @return 目标 URL
     */
    public String getTargetUrl() {
        return targetUrl;
    }
}
