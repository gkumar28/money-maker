package strategy.engine.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .defaultHeader(HttpHeaders.USER_AGENT, "MoneyMaker/1.0")
            .defaultHeader(HttpHeaders.CONNECTION, "keep-alive")
            .defaultHeader(HttpHeaders.ACCEPT, "*/*")
            .build();
    }
}
