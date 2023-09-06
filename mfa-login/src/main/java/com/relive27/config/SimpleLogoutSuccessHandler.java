package com.relive27.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author: ReLive27
 * @date: 2023/2/23 19:45
 */
public class SimpleLogoutSuccessHandler implements LogoutSuccessHandler {
    private static ObjectMapper objectMapper = new ObjectMapper();
    private final String contentType = "application/json;charset=UTF-8";

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(contentType);
        Map<String, Object> responseClaims = new LinkedHashMap<>();
        responseClaims.put("code", HttpStatus.OK.value());
        responseClaims.put("message", "success");
        try (Writer writer = response.getWriter()) {
            writer.write(objectMapper.writeValueAsString(responseClaims));
        }
    }
}
