package com.relive27.cachedrequest;

import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

/**
 * @author: ReLive27
 * @date: 2023/9/12 14:24
 */
public class ContentCachingFilter extends OncePerRequestFilter {
    public static final RequestMatcher DEFAULT_CONTENT_CACHE_MATCHER = new ContentCachingFilter.DefaultRequiresContentCacheMatcher();
    private RequestMatcher requireContentCacheMatcher = DEFAULT_CONTENT_CACHE_MATCHER;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!this.requireContentCacheMatcher.matches(request)) {
            filterChain.doFilter(request, response);
        } else {
            CachedBodyHttpServletRequest cachedBodyHttpServletRequest = new CachedBodyHttpServletRequest(request);
            filterChain.doFilter(cachedBodyHttpServletRequest, response);
        }
    }

    public void setRequireContentCacheMatcher(RequestMatcher requireContentCacheMatcher) {
        Assert.notNull(requireContentCacheMatcher, "requireContentCacheMatcher cannot be null");
        this.requireContentCacheMatcher = requireContentCacheMatcher;
    }

    private static final class DefaultRequiresContentCacheMatcher implements RequestMatcher {
        private final HashSet<String> allowedMethods;

        private DefaultRequiresContentCacheMatcher() {
            this.allowedMethods = new HashSet(Arrays.asList("GET", "POST", "DELETE", "PUT"));
        }

        public boolean matches(HttpServletRequest request) {
            return this.allowedMethods.contains(request.getMethod());
        }
    }
}
