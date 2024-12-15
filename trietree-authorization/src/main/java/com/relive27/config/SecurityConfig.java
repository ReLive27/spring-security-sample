package com.relive27.config;

import com.relive27.authorization.RoleAuthorizationManager;
import com.relive27.authorization.SecurityUtils;
import com.relive27.authorization.adapter.Adapter;
import com.relive27.authorization.adapter.RedisAdapter;
import com.relive27.authorization.watcher.RedisWatcher;
import com.relive27.authorization.watcher.Watcher;
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
 * @author: ReLive27
 * @date: 2023/5/5 20:53
 */
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    /**
     * 配置 HTTP 安全策略，包括授权请求、基本认证、禁用 CSRF 等。
     *
     * @param http HttpSecurity 对象，用于配置 web 安全。
     * @return 返回一个配置好的 SecurityFilterChain 实例。
     * @throws Exception 配置可能抛出的异常。
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 配置请求授权规则
        http.authorizeHttpRequests(authorizeHttpRequest -> {
            authorizeHttpRequest
                    // 配置角色请求匹配器，指定角色授权管理器
                    .requestMatchers(SecurityUtils.getRoleRequestMatcher(http)).access(new RoleAuthorizationManager())
                    // 配置任何其他请求都必须认证
                    .anyRequest().authenticated();
        })
                // 启用基本认证
                .httpBasic(Customizer.withDefaults())
                // 禁用 CSRF 防护
                .csrf().disable();

        return http.build();
    }

    /**
     * 配置 Redis 适配器，用于将策略规则保存在 Redis 中。
     *
     * @return 返回一个 RedisAdapter 实例，用于与 Redis 进行交互。
     */
    @Bean
    Adapter redisAdapter() {
        // 创建并返回一个 RedisAdapter 实例
        return new RedisAdapter("127.0.0.1", 6879, "123456");
    }

    /**
     * 配置 Redis 观察者，用于监听策略的变更并触发更新。
     *
     * @return 返回一个 RedisWatcher 实例，用于监听 Redis 中的策略变化。
     */
    @Bean
    Watcher redisWatcher() {
        // 创建并返回一个 RedisWatcher 实例，监听 "policyWatch" 通道
        return new RedisWatcher("127.0.0.1", 6879, "policyWatch", 1800, "123456");
    }

    /**
     * 配置内存中的用户详情服务，定义两个用户（admin 和 user）。
     *
     * @return 返回一个 InMemoryUserDetailsManager 实例，提供内存中的用户身份信息。
     */
    @Bean
    UserDetailsService userDetailsService() {
        // 创建两个用户：admin 和 user
        UserDetails admin = User.withUsername("admin")
                .password("{noop}password") // 使用无加密密码
                .roles("ADMIN") // 设置角色为 ADMIN
                .build();
        UserDetails user = User.withUsername("user")
                .password("{noop}password") // 使用无加密密码
                .roles("USER") // 设置角色为 USER
                .build();

        // 返回一个 InMemoryUserDetailsManager 实例，包含这两个用户
        return new InMemoryUserDetailsManager(admin, user);
    }
}
