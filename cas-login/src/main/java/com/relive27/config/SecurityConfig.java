package com.relive27.config;

import com.relive27.cas.client.CASAuthorizationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 安全配置类，配置应用的 Spring Security 设置。
 * 该类定义了一个用于身份验证的过滤链和一个用户详细信息服务。
 *
 * @author: ReLive27
 * @date: 2022/11/30 21:58
 */
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    /**
     * 配置安全过滤链，定义 HTTP 请求的授权和身份验证行为。
     *
     * @param http HttpSecurity 对象，用于配置 HTTP 安全设置
     * @return 配置好的 SecurityFilterChain 实例
     * @throws Exception 配置过程中可能抛出的异常
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 配置请求授权规则，要求所有请求都必须认证
        http.authorizeHttpRequests((authorizeRequest) ->
                authorizeRequest.anyRequest().authenticated())
                // 配置表单登录，使用默认设置
                .formLogin(Customizer.withDefaults())
                // 应用自定义的 CAS 认证配置
                .apply(new CASAuthorizationConfiguration());

        // 配置异常处理，指定当用户未认证时，跳转到 CAS 的认证入口
        http.exceptionHandling(e ->
                e.authenticationEntryPoint(CASAuthorizationConfiguration.getCasAuthenticationEntryPoint(http)));

        // 构建并返回配置好的 SecurityFilterChain
        return http.build();
    }

    /**
     * 配置用户详细信息服务，定义一个内存中的用户 casuser。
     *
     * @return 一个 UserDetailsService 实例，返回一个内存中的用户管理器
     */
    @Bean
    UserDetailsService users() {
        // 创建一个名为 "casuser" 的用户，密码是 "password"（使用 {noop} 代表明文密码）
        UserDetails user = User.withUsername("casuser")
                .password("{noop}password")
                .roles("USER")
                .build();
        // 返回一个内存中的用户详细信息管理器，包含一个用户
        return new InMemoryUserDetailsManager(user);
    }
}
