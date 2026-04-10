package com.orderapp.user_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin2.reporter.Sender;
import zipkin2.reporter.urlconnection.URLConnectionSender;

@Configuration
public class TracingConfig {

    @Bean
    public Sender zipkinSender() {
        return URLConnectionSender.create("http://localhost:9411/api/v2/spans");
    }
}
