package com.relive27.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HomeController 类，处理应用根路径的请求。
 * 该类定义了一个 GET 请求的处理方法，返回当前已认证用户的用户名。
 *
 * @author: ReLive27
 * @date: 2024/5/5 16:21
 */
@RestController
public class HomeController {

    /**
     * 处理根路径 ("/") 的 GET 请求，返回当前认证用户的用户名。
     *
     * @param authentication 当前的身份验证信息，包含已认证用户的信息
     * @return 当前认证用户的用户名
     */
    @GetMapping("/")
    public String home(Authentication authentication) {
        // 返回当前认证用户的用户名
        return authentication.getName();
    }
}

