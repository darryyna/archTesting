package edu.litviniuk.mongo.config;

/*
  @author darin
  @project mongo
  @class AuditConfiguration
  @version 1.0.0
  @since 18.05.2025 - 13.57
*/

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@EnableMongoAuditing
@Configuration
public class AuditConfiguration {
    @Bean
    public AuditorAware<String> auditorAware() {
        return new AuditorAwareImpl();
    }
}
