package com.relive27.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: ReLive27
 * @date: 2024/5/5 16:21
 */
@RestController
public class UserController {

    /**
     * 获取当前认证用户的用户名。
     *
     * @param authentication 当前的身份验证信息，包含已认证用户的信息
     * @return 当前认证用户的用户名
     */
    @GetMapping("/user/info")
    public Map<String, Object> home(Authentication authentication) {
        // 返回当前认证用户的用户名
        Map<String, Object> data = new HashMap<>();
        data.put("name", authentication.getName());
        return Collections.singletonMap("data", data);
    }
}

