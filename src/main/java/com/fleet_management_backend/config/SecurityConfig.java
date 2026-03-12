package com.fleet_management_backend.config;

import com.fleet_management_backend.security.JwtAuthFilter;
import com.fleet_management_backend.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Bean
    public JwtService jwtServiceBean() {
        return new JwtService(jwtSecret, jwtExpirationMs);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtServiceBean,
            UserDetailsService userDetailsService) throws Exception {

        JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(jwtServiceBean, userDetailsService);

        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/admin/trucks/**").hasAnyRole("ADMIN", "LOGISTICS_MANAGER")
                        .requestMatchers("/api/admin/trailers/**").hasAnyRole("ADMIN", "LOGISTICS_MANAGER")
                        .requestMatchers("/api/manager/trips/**").hasAnyRole("ADMIN", "LOGISTICS_MANAGER")
                        .requestMatchers("/api/manager/deliveries/**").hasAnyRole("ADMIN", "LOGISTICS_MANAGER")
                        .requestMatchers("/api/tracking/**").hasAnyRole("DRIVER", "ADMIN")
                        .requestMatchers("/api/maintenances/**").hasAnyRole("ADMIN", "LOGISTICS_MANAGER")
                        .requestMatchers("/api/carburants/**").hasAnyRole("ADMIN", "LOGISTICS_MANAGER")
                        .requestMatchers("/api/analytics/**").hasAnyRole("ADMIN", "LOGISTICS_MANAGER")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/manager/**").hasRole("LOGISTICS_MANAGER")
                        .requestMatchers("/api/admin/clients/**").hasAnyRole("ADMIN", "LOGISTICS_MANAGER")
                        .requestMatchers("/api/admin/create/client").hasAnyRole("ADMIN", "LOGISTICS_MANAGER")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
