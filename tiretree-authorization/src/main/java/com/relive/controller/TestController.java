package com.relive.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: ReLive
 * @date: 2023/5/5 20:55
 */
@RestController
public class TestController {

    @GetMapping("/testA")
    public String getA() {
        return "Get Request Success";
    }

    @PostMapping("/testB")
    public String postB() {
        return "Post Request Success";
    }
}
