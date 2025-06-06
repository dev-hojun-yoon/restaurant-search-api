package kr.hhplus.be.server.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
//@EnableConfigurationProperties(NaverApiProperties.class)
public class NaverApiConfig {

    @Bean
    public WebClient webClient(NaverApiProperties properties) {
        System.out.println(properties.getBaseUrl());
        System.out.println(properties.getClientId());
        System.out.println(properties.getClientSecret());

        return WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }
}
