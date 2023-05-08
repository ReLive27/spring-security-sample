package com.relive.authorization;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.cloud.consul.config.ConsulConfigProperties;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.springframework.cloud.consul.config.ConsulConfigProperties.Format.FILES;

/**
 * @author: ReLive
 * @date: 2023/5/6 19:03
 */
@Slf4j
public class ConsulConfigTreePathProvider implements TreePathProvider {
    private static final String API_AUTHORIZATION_KEY = "apiAuthorization";

    private final JsonParser jsonParser = JsonParserFactory.getJsonParser();

    private final ConsulConfigProperties properties;

    private final ConsulClient consul;

    private boolean firstTime = true;

    private LinkedHashMap<String, Long> consulIndexes;

    public ConsulConfigTreePathProvider(ConsulConfigProperties properties, ConsulClient consul,
                                        LinkedHashMap<String, Long> initialIndexes) {
        this.properties = properties;
        this.consul = consul;
        String name = this.properties.getPrefix() + "/" + this.properties.getName() + "/";
        Assert.notNull(initialIndexes.get(name), "ConsulConfigIndexes did not find the key as " + name);
        this.consulIndexes = new LinkedHashMap<>();
        Long initialIndex = initialIndexes.get(name);
        this.consulIndexes.put("/" + name + this.properties.getDataKey(), initialIndex);
    }

    @Override
    public Set<String> providePathData() {
        Set<String> resources = new HashSet<>();

        for (String context : this.consulIndexes.keySet()) {
            if (this.properties.getFormat() != FILES && context.endsWith("/")) {
                context = StringUtils.trimTrailingCharacter(context, '/');
            }

            try {
                Long currentIndex = this.consulIndexes.get(context);
                if (currentIndex == null) {
                    currentIndex = -1L;
                }

                if (log.isTraceEnabled()) {
                    log.trace("watching consul for context '" + context + "' with index " + currentIndex);
                }

                String aclToken = this.properties.getAclToken();
                if (!StringUtils.hasText(aclToken)) {
                    aclToken = null;
                }

                if (this.firstTime) {
                    Response<GetValue> response = consul.getKVValue(context, aclToken);
                    if (response.getValue() != null) {
                        this.fullResources(resources, response);
                    }
                } else {

                    Response<GetValue> response = this.consul.getKVValue(context, aclToken,
                            new QueryParams(this.properties.getWatch().getWaitTime(), currentIndex));

                    if (response.getValue() != null) {
                        Long newIndex = response.getConsulIndex();

                        if (newIndex != null && !newIndex.equals(currentIndex)) {
                            if (!this.consulIndexes.containsValue(newIndex) && !currentIndex.equals(-1L)) {
                                if (log.isTraceEnabled()) {
                                    log.trace("Context " + context + " has new index " + newIndex);
                                }

                                this.fullResources(resources, response);
                            }
                            this.consulIndexes.put(context, newIndex);
                        }
                    }
                }
            } catch (Exception e) {
                if (log.isWarnEnabled()) {
                    log.warn("Error querying consul Key/Values for context '" + context + "'. Message: "
                            + e.getMessage());

                }
            }
        }
        this.firstTime = false;
        return resources;
    }

    private void fullResources(Set<String> resources, Response<GetValue> response) {
        String decodedValue = response.getValue().getDecodedValue(StandardCharsets.UTF_8);
        if (StringUtils.hasText(decodedValue)) {
            Map<String, Object> map = this.jsonParser.parseMap(decodedValue);
            List<String> api = (List<String>) map.get(API_AUTHORIZATION_KEY);
            if (api != null) {
                resources.addAll(api);
            }
        }
    }
}
