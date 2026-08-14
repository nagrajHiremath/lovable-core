package com.lovable.services.workspace_service.security;

import com.lovable.services.common_lib.security.JwtFilter;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

  private final JwtFilter jwtFilter;
  private final HandlerExceptionResolver handlerExceptionResolver;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) {
    httpSecurity
        .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
        .sessionManagement(
            sessionConfig -> sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth -> auth
                    .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()
                    .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        // /internal/** - service-to-service calls (e.g. intelligence-service's
                        // project-permission check) that don't carry a forwarded JWT, matching
                        // the same allowance account-service already has for its /internal/** .
                        // /public/** - anonymous project-sharing endpoints; the gateway also
                        // skips its own JWT check for these, so no Authorization header ever
                        // reaches this filter chain for them either.
                        .requestMatchers("/actuator/**", "/internal/**", "/public/**").permitAll()
                    .anyRequest()
                    .authenticated())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(exceptionHandlingConfigurer ->
                    exceptionHandlingConfigurer.accessDeniedHandler((request, response, accessDeniedException) -> {
                      handlerExceptionResolver.resolveException(request, response, null, accessDeniedException);
                    }));

    return httpSecurity.build();
  }
}
