package com.orderapp.order_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin2.reporter.Sender;
import zipkin2.reporter.urlconnection.URLConnectionSender;

@Configuration
public class TracingConfig {

    @Value("${management.zipkin.tracing.endpoint}")
    private String zipkinEndpoint;

    @Bean
    public Sender zipkinSender() {
        return URLConnectionSender.create(zipkinEndpoint);
    }
}
