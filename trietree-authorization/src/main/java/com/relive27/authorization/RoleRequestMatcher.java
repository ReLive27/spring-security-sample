package com.relive27.authorization;

import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author: ReLive27
 * @date: 2023/5/5 20:25
 */
public class RoleRequestMatcher implements RequestMatcher {
    private final PolicyEnforcer enforcer;

    /**
     * 构造方法，初始化 RoleRequestMatcher
     *
     * @param enforcer 策略执行器，用于判断是否有权限访问指定路径
     */
    public RoleRequestMatcher(PolicyEnforcer enforcer) {
        Assert.notNull(enforcer, "enforcer 不能为空");
        this.enforcer = enforcer;
    }

    /**
     * 判断请求是否匹配某个策略规则
     *
     * @param request HTTP 请求对象
     * @return 如果请求路径和方法匹配策略规则，返回 true，否则返回 false
     */
    @Override
    public boolean matches(HttpServletRequest request) {
        // 构造请求路径：URL路径 + HTTP方法
        String path = this.getRequestPath(request) + request.getMethod();
        // 判断策略执行器是否匹配
        return this.enforcer.hasAccess(path);
    }

    /**
     * 返回请求的匹配结果
     *
     * @param request HTTP 请求对象
     * @return 匹配结果，包含授权信息（如果有的话）
     */
    @Override
    public MatchResult matcher(HttpServletRequest request) {
        // 构造请求路径：URL路径 + "==" + HTTP方法
        String path = this.getRequestPath(request) + "==" + request.getMethod();
        // 使用策略执行器提取授权信息
        Map<String, String> extractRole = this.enforcer.fetchAuthorize(path);

        // 如果没有匹配的角色信息，则返回未匹配结果；否则返回匹配结果
        return CollectionUtils.isEmpty(extractRole) ? MatchResult.notMatch() : MatchResult.match(extractRole);
    }

    /**
     * 获取 HTTP 请求的路径
     *
     * @param request HTTP 请求对象
     * @return 拼接后的请求路径
     */
    private String getRequestPath(HttpServletRequest request) {
        // 获取基础路径
        String url = request.getServletPath();
        // 获取额外的路径信息（如果有）
        String pathInfo = request.getPathInfo();

        // 拼接路径信息
        if (pathInfo != null) {
            url = StringUtils.hasLength(url) ? url + pathInfo : pathInfo;
        }

        return url;
    }
}
