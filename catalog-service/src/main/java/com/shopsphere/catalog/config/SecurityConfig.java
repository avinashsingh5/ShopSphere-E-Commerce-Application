package com.shopsphere.catalog.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        @Autowired
        private RoleHeaderFilter roleHeaderFilter;

        @Autowired
        private ServiceTokenFilter serviceTokenFilter;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .formLogin(form -> form.disable())
                                .httpBasic(basic -> basic.disable())
                                .authorizeHttpRequests(auth -> auth
                                        .requestMatchers(
                                                "/v3/api-docs/**",
                                                "/swagger-ui.html",
                                                "/swagger-ui/**"
                                        ).permitAll()
                                                .requestMatchers(HttpMethod.GET, "/catalog/products",
                                                                "/catalog/products/**")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET, "/catalog/featured").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/catalog/categories",
                                                                "/catalog/categories/**")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                // ServiceTokenFilter runs first — authenticates internal service calls
                                .addFilterBefore(serviceTokenFilter, UsernamePasswordAuthenticationFilter.class)
                                // RoleHeaderFilter runs after — authenticates user calls forwarded by API Gateway
                                .addFilterBefore(roleHeaderFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}