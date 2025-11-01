package edu.litviniuk.mongo.config;

/*
  @author darin
  @project mongo
  @class SecurityConfig
  @version 1.0.0
  @since 01.11.2025 - 14.15
*/

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(req -> req
                        .requestMatchers("/index.html").permitAll()

                        .requestMatchers("/api/v1/movies/hello/user").hasRole("USER")
                        .requestMatchers("/api/v1/movies/hello/admin").hasRole("ADMIN")
                        .requestMatchers("/api/v1/movies/hello/root").hasRole("ROOT")

                        // GET — ADMIN або ROOT
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/movies/**")
                        .hasAnyRole("ADMIN", "ROOT")

                        // POST — тільки ADMIN
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/movies/**")
                        .hasRole("ADMIN")

                        // PUT — тільки ROOT
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/v1/movies/**")
                        .hasRole("ROOT")

                        // DELETE — тільки ROOT
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/v1/movies/**")
                        .hasRole("ROOT")

                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
                .username("admin").
                password(passwordEncoder().
                encode("daryna")).
                roles("ADMIN").build();

        UserDetails user = User.builder()
                .username("user").
                password(passwordEncoder().
                        encode("12345")).
                roles("USER").build();

        UserDetails root = User.builder()
                .username("root").
                password(passwordEncoder().
                        encode("root")).
                roles("ROOT").build();
        return new InMemoryUserDetailsManager(admin, user, root);
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
