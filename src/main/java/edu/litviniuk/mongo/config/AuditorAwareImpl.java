package edu.litviniuk.mongo.config;

/*
  @author darin
  @project mongo
  @class AuditorAwareImpl
  @version 1.0.0
  @since 18.05.2025 - 13.56
*/

import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class AuditorAwareImpl  implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of(System.getProperty("user.name"));
    }
}