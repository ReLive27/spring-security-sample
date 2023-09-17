package com.relive27;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.relive27.cachedrequest.ContentCachingConfigurer;
import com.relive27.captcha.CaptchaAuthenticationConfigurer;
import com.relive27.web.FormJsonLoginConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationEntryPointFailureHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import java.io.Writer;
import java.util.Collections;
import java.util.UUID;

/**
 * @author: ReLive27
 * @date: 2023/8/28 11:12
 */
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorizeRequest ->
                authorizeRequest.antMatchers("/captcha.jpg").permitAll()
                        .anyRequest().authenticated())
                .apply(new FormJsonLoginConfigurer<>())
                .successHandler((request, response, authentication) -> {
                    try (Writer writer = response.getWriter()) {
                        //This is just a simple simulation token response, please do not imitate it in the production environment.
                        writer.write(new ObjectMapper().writeValueAsString(Collections.singletonMap("token", UUID.randomUUID().toString())));
                    }
                })
                .failureHandler(new AuthenticationEntryPointFailureHandler(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
                )).and()
                .apply(new CaptchaAuthenticationConfigurer())
                .and()
                .apply(new ContentCachingConfigurer<>(http.getSharedObject(ApplicationContext.class)))
                .and()
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    UserDetailsService userDetailsService() {
        UserDetails user = User.withUsername("admin")
                .password("{noop}password")
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}
