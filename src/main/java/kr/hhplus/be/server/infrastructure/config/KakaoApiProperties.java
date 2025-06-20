package kr.hhplus.be.server.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties(prefix = "kakao.api")
public class KakaoApiProperties implements ApiProperties{
    private String baseUrl;
    private String restApiKey;

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }
    public String getRestApiKey() {
        return restApiKey;
    }
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    public void setRestApiKey(String restApiKey) {
        this.restApiKey = restApiKey;
    }

    
    
}
