package com.relive.authorization;

import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author: ReLive
 * @date: 2023/5/5 20:25
 */
public class TireTreeRequestMatcher implements RequestMatcher {
    private final TreePathMatcher matcher;

    public TireTreeRequestMatcher(TreePathMatcher matcher) {
        Assert.notNull(matcher, "TreePathMatcher cannot be null");
        this.matcher = matcher;
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        String path = this.getRequestPath(request) + request.getMethod();
        return this.matcher.matches(path);
    }

    @Override
    public MatchResult matcher(HttpServletRequest request) {
        String path = this.getRequestPath(request) + "==" + request.getMethod();
        Map<String, String> extractRole = this.matcher.extractRole(path);
        return CollectionUtils.isEmpty(extractRole) ? MatchResult.notMatch() : MatchResult.match(extractRole);

    }

    private String getRequestPath(HttpServletRequest request) {
        String url = request.getServletPath();
        String pathInfo = request.getPathInfo();
        if (pathInfo != null) {
            url = StringUtils.hasLength(url) ? url + pathInfo : pathInfo;
        }

        return url;
    }
}
