package com.relive27.web;

import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.web.authentication.ForwardAuthenticationFailureHandler;
import org.springframework.security.web.authentication.ForwardAuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * @author: ReLive27
 * @date: 2023/9/13 11:19
 */
public class FormJsonLoginConfigurer<H extends HttpSecurityBuilder<H>> extends AbstractAuthenticationFilterConfigurer<H, FormJsonLoginConfigurer<H>, UsernamePasswordJsonAuthenticationFilter> {
    public FormJsonLoginConfigurer() {
        super(new UsernamePasswordJsonAuthenticationFilter(), null);
        this.usernameParameter("username");
        this.passwordParameter("password");
    }

    public FormJsonLoginConfigurer<H> loginPage(String loginPage) {
        return super.loginPage(loginPage);
    }

    public FormJsonLoginConfigurer<H> usernameParameter(String usernameParameter) {
        (this.getAuthenticationFilter()).setUsernameParameter(usernameParameter);
        return this;
    }

    public FormJsonLoginConfigurer<H> passwordParameter(String passwordParameter) {
        (this.getAuthenticationFilter()).setPasswordParameter(passwordParameter);
        return this;
    }

    public FormJsonLoginConfigurer<H> failureForwardUrl(String forwardUrl) {
        this.failureHandler(new ForwardAuthenticationFailureHandler(forwardUrl));
        return this;
    }

    public FormJsonLoginConfigurer<H> successForwardUrl(String forwardUrl) {
        this.successHandler(new ForwardAuthenticationSuccessHandler(forwardUrl));
        return this;
    }

    public void init(H http) throws Exception {
        super.init(http);
    }

    protected RequestMatcher createLoginProcessingUrlMatcher(String loginProcessingUrl) {
        return new AntPathRequestMatcher(loginProcessingUrl);
    }

    private String getUsernameParameter() {
        return (this.getAuthenticationFilter()).getUsernameParameter();
    }

    private String getPasswordParameter() {
        return (this.getAuthenticationFilter()).getPasswordParameter();
    }

}
