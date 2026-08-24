package com.vsbec.feedback.config;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder.mixIn(Object.class, HibernateProxyMixIn.class);
    }

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private interface HibernateProxyMixIn {}
}
