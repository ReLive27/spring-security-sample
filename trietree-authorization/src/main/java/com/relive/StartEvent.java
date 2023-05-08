package com.relive;

import com.ecwid.consul.v1.ConsulClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author: ReLive
 * @date: 2023/5/6 20:58
 */
@Component
@RequiredArgsConstructor
public class StartEvent implements ApplicationListener<ApplicationReadyEvent> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConsulClient consulClient;

    @SneakyThrows
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        //Mock data for testing
        List<String> api = new ArrayList<>();
        api.add("/testA==GET==ROLE_ADMIN");
        api.add("/testB==POST==ROLE_USER");
        consulClient.setKVValue("/config/app/data", this.objectMapper.writeValueAsString(Collections.singletonMap("apiAuthorization", api)));
    }
}
