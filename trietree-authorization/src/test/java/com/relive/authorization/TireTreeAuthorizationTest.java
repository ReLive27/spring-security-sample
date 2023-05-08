package com.relive.authorization;

import com.ecwid.consul.v1.ConsulClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.relive.autoconfig.ConsulConfigTreePathConfiguration;
import com.relive.autoconfig.TreePathAuthorizationConfiguration;
import com.relive.config.SecurityConfig;
import com.relive.controller.TestController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author: ReLive
 * @date: 2023/5/6 21:53
 */
@Import({SecurityConfig.class, ConsulConfigTreePathConfiguration.class, TreePathAuthorizationConfiguration.class})
@WebMvcTest(TestController.class)
public class TireTreeAuthorizationTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ConsulClient consulClient;

    @BeforeEach
    public void initializePermissionInformationToConsulKVStore() throws JsonProcessingException {
        List<String> api = new ArrayList<>();
        api.add("/testA==GET==ROLE_ADMIN");
        api.add("/testB==POST==ROLE_USER");
        consulClient.setKVValue("/config/app/data", this.objectMapper.writeValueAsString(Collections.singletonMap("apiAuthorization", api)));
    }

    @Test
    void whenUsingUserLoginAccessTestAToReturnForbidden() throws Exception {
        this.mockMvc.perform(get("/testA")
                .with(httpBasic("user", "password")))
                .andExpect(status().isForbidden());
    }

    @Test
    void whenUsingAdminLoginAccessTestAToReturnSuccess() throws Exception {
        this.mockMvc.perform(get("/testA")
                .with(httpBasic("admin", "password")))
                .andExpect(status().isOk());
    }

    @Test
    void whenUsingAdminLoginAccessTestBToReturnForbidden() throws Exception {
        this.mockMvc.perform(post("/testB")
                .with(httpBasic("admin", "password")))
                .andExpect(status().isForbidden());
    }

    @Test
    void whenUsingUserLoginAccessTestBToReturnSuccess() throws Exception {
        this.mockMvc.perform(post("/testB")
                .with(httpBasic("user", "password")))
                .andExpect(status().isOk());
    }
}
