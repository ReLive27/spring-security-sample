package com.relive.config;

import com.relive.authorization.TireTreeAuthorizationManager;
import com.relive.authorization.TireTreeRequestMatcher;
import com.relive.authorization.TreePathMatcher;
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
 * @author: ReLive
 * @date: 2023/5/5 20:53
 */
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, TireTreeRequestMatcher matcher) throws Exception {
        http.authorizeHttpRequests(authorizeHttpRequest -> {
            authorizeHttpRequest.regexMatchers("/actuator/health").permitAll()
                    .requestMatchers(matcher).access(new TireTreeAuthorizationManager())
                    .anyRequest().authenticated();
        })
                .httpBasic(Customizer.withDefaults())
                .csrf().disable();

        return http.build();
    }

    @Bean
    TireTreeRequestMatcher tireTreeRequestMatcher(TreePathMatcher matcher) {
        return new TireTreeRequestMatcher(matcher);
    }

    @Bean
    UserDetailsService userDetailsService() {
        UserDetails admin = User.withUsername("admin")
                .password("{noop}password")
                .roles("ADMIN")
                .build();
        UserDetails user = User.withUsername("user")
                .password("{noop}password")
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(admin, user);
    }
}
