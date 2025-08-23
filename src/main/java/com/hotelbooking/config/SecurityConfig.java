package com.hotelbooking.config;

import com.hotelbooking.security.JwtAuthFilter;
import com.hotelbooking.service.CustomUserDetailsService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


@Configuration
@EnableMethodSecurity // ✅ Enables @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig  {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> {}) // ✅ enable CORS with WebMvcConfigurer
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // ✅ Public endpoints
                .requestMatchers(
                    "/api/auth/**",
                    "/api/hotels/all",
                    "/api/rooms/all",
                    "/api/rooms/available",
                    "/api/blogs/all",
                    "/uploads/**",
                    "/images/**",
                    "/api/hotels/search"
                ).permitAll()
                // Booking endpoints → require USER role
                .requestMatchers("/api/bookings/**").hasAnyRole("USER","ADMIN")

                // Admin
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // User endpoints
                .requestMatchers("/api/user/**").hasAnyRole("USER","ADMIN")

//                // ✅ Role-based endpoints
//                .requestMatchers("/api/admin/**").hasRole("ADMIN")
//                .requestMatchers("/api/user/**").hasAnyRole("USER","ADMIN")

                // ✅ Everything else needs login
                .anyRequest().authenticated()
            )
            .userDetailsService(customUserDetailsService)
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ✅ Custom authentication provider
    @Bean
    public AuthenticationProvider authenticationProvider() {
        return new AuthenticationProvider() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                String username = authentication.getName();
                String password = authentication.getCredentials().toString();

                UserDetails user = customUserDetailsService.loadUserByUsername(username);

                if (passwordEncoder().matches(password, user.getPassword())) {
                    return new UsernamePasswordAuthenticationToken(user, password, user.getAuthorities());
                } else {
                    throw new RuntimeException("Invalid username or password");
                }
            }

            @Override
            public boolean supports(Class<?> authentication) {
                return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
            }
        };
    }


    
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:5173") // or 3000, depending on Vite/React
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowCredentials(true); // ✅ important for cookies
            }
        };
        
        
    }
    // ✅ Global CORS config
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowCredentials(true); // ✅ allow cookies
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        
        configuration.setExposedHeaders(List.of("Authorization", "Set-Cookie"));

        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // ✅ Authentication manager bean
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
 
}
