package com.ecom.config;

import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.servlet.MultipartConfigElement;

@Configuration
public class MultipartUploadConfig {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024 * 1024; // 10 GB
    private static final long MAX_REQUEST_SIZE = 10L * 1024 * 1024 * 1024; // 10 GB
    private static final int FILE_SIZE_THRESHOLD = 2 * 1024 * 1024; // 2 MB

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        return new MultipartConfigElement(
                System.getProperty("java.io.tmpdir"),
                MAX_FILE_SIZE,
                MAX_REQUEST_SIZE,
                FILE_SIZE_THRESHOLD
        );
    }

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> factory.addConnectorCustomizers(connector -> {
            connector.setMaxPostSize(-1);
        });
    }
}
