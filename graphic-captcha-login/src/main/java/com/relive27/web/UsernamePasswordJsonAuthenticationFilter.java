package com.relive27.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

/**
 * @author: ReLive27
 * @date: 2023/9/13 11:14
 */
@Slf4j
public class UsernamePasswordJsonAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final JsonParser jsonParser = JsonParserFactory.getJsonParser();

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String username;
        String password;
        if (HttpMethod.POST.equals(HttpMethod.resolve(request.getMethod()))) {
            try (BufferedReader reader = request.getReader()) {
                String tmp;
                StringBuilder body = new StringBuilder();
                while ((tmp = reader.readLine()) != null) {
                    body.append(tmp);
                }

                Map<String, Object> bodyMap = jsonParser.parseMap(body.toString());
                username = (String) bodyMap.get(this.getUsernameParameter());
                password = (String) bodyMap.get(this.getPasswordParameter());
            } catch (IOException e) {
                log.error("Failed to parse login request body", e);
                return null;
            }
        } else {
            username = this.obtainUsername(request);
            username = username != null ? username.trim() : "";
            password = this.obtainPassword(request);
            password = password != null ? password : "";
        }
        UsernamePasswordAuthenticationToken authRequest = UsernamePasswordAuthenticationToken.unauthenticated(username, password);
        this.setDetails(request, authRequest);
        return this.getAuthenticationManager().authenticate(authRequest);
    }
}
