package com.relive27.cachedrequest;

import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.AbstractRequestMatcherRegistry;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author: ReLive27
 * @date: 2023/9/12 14:34
 */
public class ContentCachingConfigurer<H extends HttpSecurityBuilder<H>> extends AbstractHttpConfigurer<ContentCachingConfigurer<H>, H> {
    private RequestMatcher requireContentCachingMatcher;
    private List<RequestMatcher> ignoredContentCachingMatchers;

    private final ApplicationContext context;

    public ContentCachingConfigurer(ApplicationContext context) {
        this.requireContentCachingMatcher = ContentCachingFilter.DEFAULT_CONTENT_CACHE_MATCHER;
        this.ignoredContentCachingMatchers = new ArrayList();
        this.context = context;
    }

    public ContentCachingConfigurer<H> requireContentCachingMatcher(RequestMatcher requireContentCachingMatcher) {
        Assert.notNull(requireContentCachingMatcher, "requireContentCachingMatcher cannot be null");
        this.requireContentCachingMatcher = requireContentCachingMatcher;
        return this;
    }

    public ContentCachingConfigurer<H> ignoringAntMatchers(String... antPatterns) {
        return (new IgnoreContentCachingRegistry(this.context).antMatchers(antPatterns)).and();
    }

    public ContentCachingConfigurer<H> ignoringRequestMatchers(RequestMatcher... requestMatchers) {
        return (new IgnoreContentCachingRegistry(this.context).requestMatchers(requestMatchers)).and();
    }

    @Override
    public void configure(H http) throws Exception {
        ContentCachingFilter filter = new ContentCachingFilter();
        RequestMatcher requireCsrfProtectionMatcher = this.getRequireCsrfProtectionMatcher();
        if (requireCsrfProtectionMatcher != null) {
            filter.setRequireContentCacheMatcher(requireCsrfProtectionMatcher);
        }
        filter = this.postProcess(filter);
        http.addFilterAfter(filter, SecurityContextHolderFilter.class);
    }

    private RequestMatcher getRequireCsrfProtectionMatcher() {
        return (this.ignoredContentCachingMatchers.isEmpty() ? this.requireContentCachingMatcher : new AndRequestMatcher(this.requireContentCachingMatcher, new NegatedRequestMatcher(new OrRequestMatcher(this.ignoredContentCachingMatchers))));
    }

    private class IgnoreContentCachingRegistry extends AbstractRequestMatcherRegistry<ContentCachingConfigurer<H>.IgnoreContentCachingRegistry> {
        IgnoreContentCachingRegistry(ApplicationContext context) {
            this.setApplicationContext(context);
        }

        public ContentCachingConfigurer<H>.MvcMatchersIgnoreContentCachingRegistry mvcMatchers(HttpMethod method, String... mvcPatterns) {
            List<MvcRequestMatcher> mvcMatchers = this.createMvcMatchers(method, mvcPatterns);
            ContentCachingConfigurer.this.ignoredContentCachingMatchers.addAll(mvcMatchers);
            return ContentCachingConfigurer.this.new MvcMatchersIgnoreContentCachingRegistry(this.getApplicationContext(), mvcMatchers);
        }

        public ContentCachingConfigurer<H>.MvcMatchersIgnoreContentCachingRegistry mvcMatchers(String... mvcPatterns) {
            return this.mvcMatchers(null, mvcPatterns);
        }

        ContentCachingConfigurer<H> and() {
            return ContentCachingConfigurer.this;
        }

        protected ContentCachingConfigurer<H>.IgnoreContentCachingRegistry chainRequestMatchers(List<RequestMatcher> requestMatchers) {
            ContentCachingConfigurer.this.ignoredContentCachingMatchers.addAll(requestMatchers);
            return this;
        }
    }

    private final class MvcMatchersIgnoreContentCachingRegistry extends ContentCachingConfigurer<H>.IgnoreContentCachingRegistry {
        private final List<MvcRequestMatcher> mvcMatchers;

        private MvcMatchersIgnoreContentCachingRegistry(ApplicationContext context, List<MvcRequestMatcher> mvcMatchers) {
            super(context);
            this.mvcMatchers = mvcMatchers;
        }

        ContentCachingConfigurer<H>.IgnoreContentCachingRegistry servletPath(String servletPath) {
            Iterator iterator = this.mvcMatchers.iterator();

            while (iterator.hasNext()) {
                MvcRequestMatcher matcher = (MvcRequestMatcher) iterator.next();
                matcher.setServletPath(servletPath);
            }

            return this;
        }
    }
}
