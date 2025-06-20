package kr.hhplus.be.server.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties(prefix = "naver.api")
public class NaverApiProperties implements ApiProperties{
    private String baseUrl;
    private String clientId;
    private String clientSecret;

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }
    public String getClientId() {
        return clientId;
    }
    public String getClientSecret() {
        return clientSecret;
    }
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    
    
}
