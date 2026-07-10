package com.team6.server.episode.infrastructure.openai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(OpenAiProperties.class)
@ConditionalOnProperty(prefix = "openai.title-suggestion", name = "enabled", havingValue = "true")
public class OpenAiConfig {
    @Bean
    RestClient openAiRestClient(OpenAiProperties properties, RestClient.Builder builder) {
        properties.validateForEnabledProvider();
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeout());
        requestFactory.setReadTimeout(properties.readTimeout());
        return builder
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                .requestFactory(requestFactory)
                .build();
    }
}
