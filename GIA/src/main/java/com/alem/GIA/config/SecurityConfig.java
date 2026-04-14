package com.alem.GIA.config;


import com.alem.GIA.security.AuditSecurityFilter;
import com.alem.GIA.security.JwtFilter;
import com.alem.GIA.service.MyUserDetailsService;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.filter.AbstractRequestLoggingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;




import java.util.List;

import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtFilter jwtFilter;
    private final AuditSecurityFilter auditSecurityFilter;
    private final MyUserDetailsService myUserDetailsService;


    public SecurityConfig(JwtFilter jwtFilter, AuditSecurityFilter auditSecurityFilter, MyUserDetailsService myUserDetailsService) {
        this.jwtFilter = jwtFilter;
        this.auditSecurityFilter = auditSecurityFilter;
        this.myUserDetailsService = myUserDetailsService;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        //configuration.setAllowedOrigins(List.of("*")); // Or your specific origins
       configuration.setAllowedOrigins(List.of("http://localhost:4200",
                "http://gia-angular-frontend.s3-website-us-east-1.amazonaws.com"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // Inner class definition
    public static class RequestLoggingFilter extends AbstractRequestLoggingFilter {
        public RequestLoggingFilter() {
            setIncludeQueryString(true);
            setIncludePayload(true);
            setMaxPayloadLength(10000);
            setIncludeHeaders(true);
            setAfterMessagePrefix("REQUEST DATA: ");
        }

        @Override
        protected void beforeRequest(HttpServletRequest request, String message) {
            logger.info(message);
        }

        @Override
        protected void afterRequest(HttpServletRequest request, String message) {
            logger.info(message);
        }
    }

    @Bean
    public FilterRegistrationBean<RequestLoggingFilter> loggingFilter() {
        FilterRegistrationBean<RequestLoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestLoggingFilter());
        registration.addUrlPatterns("/api/accounts/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<Filter> filterDebug() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter((request, response, chain) -> {
            System.out.println("Current filter: " + ((HttpServletRequest) request).getRequestURI());
            chain.doFilter(request, response);
        });
        registration.setOrder(Ordered.LOWEST_PRECEDENCE);
        return registration;
    }



    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> {
                    auth
                            // ✅ Fixed the wildcard here
                            .requestMatchers("/", "/index.html", "/static/**", "/*.html").permitAll()
                            .requestMatchers("/api/accounts/**","/api/auth/**", "/css/**", "/js/**").permitAll()

                            .requestMatchers(GET, "/api/members/me").hasAnyRole("USER", "ADMIN")

                            // USER
                            .requestMatchers(POST, "/api/members/self-register").hasAnyRole("USER","ADMIN")
                            .requestMatchers("/api/payments/**").hasAnyRole("USER","ADMIN")
                            .requestMatchers(GET,"/api/billing/**").hasAnyRole("USER","ADMIN")
                            .requestMatchers(GET, "/api/members/by-user/**").hasAnyRole("USER","ADMIN")

                            // ✅ Change this to match the whole path or use a double wildcard at the END
                            .requestMatchers(POST, "/api/members/**").hasAnyAuthority("ROLE_USER","ROLE_ADMIN","ADMIN:WRITE", "USER:WRITE")

                            // ADMIN
                            .requestMatchers(POST, "/api/admin/members/register").hasAuthority("ADMIN:WRITE")
                            .requestMatchers("/api/beneficiaries/**").hasAuthority("ADMIN:WRITE")
                            .requestMatchers("/api/roles/**", "/api/permissions/**").hasAnyAuthority("ADMIN:READ","ADMIN:WRITE")
                            .requestMatchers(GET,"/api/admin/audit/**").hasAuthority("ADMIN:READ")

                            .anyRequest().authenticated();
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(auditSecurityFilter, JwtFilter.class)
                .build();
    }
//    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
//        return http
//
//                .csrf(AbstractHttpConfigurer::disable)
//                //.cors(Customizer.withDefaults())
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//
//
//                .authorizeHttpRequests(auth -> {
//                    auth
//
//                            // ✅ allow UI
//                            .requestMatchers("/", "/index.html", "/**/*.html").permitAll()
//                            .requestMatchers("/api/accounts/**","/api/auth/**", "/css/**", "/js/**").permitAll()
//                            // Move this right after permitAll
//                            .requestMatchers(GET, "/api/members/me").hasAnyRole("USER", "ADMIN")
//
//                            // USER
//                            .requestMatchers(POST, "/api/members/self-register")
//
//                            .hasAnyRole("USER","ADMIN")
//
//                            .requestMatchers("/api/payments/**")
//                            .hasAnyRole("USER","ADMIN")
//                            // ✅ ADD BILLING ACCESS HERE
//                            .requestMatchers(GET,"/api/billing/**")
//                            .hasAnyRole("USER","ADMIN")
//
//
//                            .requestMatchers(GET, "/api/members/by-user/**")
//                            .hasAnyRole("USER","ADMIN")
//
//                            // ✅ ADD THIS
//
//                            .requestMatchers(POST, "/api/members/*/image")
//                           .hasAnyAuthority("ROLE_USER","ROLE_ADMIN","ADMIN:WRITE", "USER:WRITE")
//
//
//                            // ADMIN
//                            .requestMatchers(POST, "/api/admin/members/register")
//                            .hasAuthority("ADMIN:WRITE")
//                            // ADMIN
//                            .requestMatchers(POST, "/api/beneficiaries/**")
//                            .hasAuthority("ADMIN:WRITE")
//                            .requestMatchers(GET, "/api/beneficiaries/**")
//                            .hasAuthority("ADMIN:WRITE")
//
//                            .requestMatchers("/api/roles/**", "/api/permissions/**")
//                            .hasAnyAuthority("ADMIN:READ","ADMIN:WRITE")
//                            .requestMatchers(GET,"/api/admin/audit/**")
//                            .hasAuthority("ADMIN:READ")
//
//                            .anyRequest().authenticated();
//                })
//
//
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
//                .addFilterAfter(auditSecurityFilter, JwtFilter.class)
//
//
//                .build();
//    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }


    @Bean
    public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration config) throws Exception {

        return config.getAuthenticationManager();
    }


    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider daoAuth = new DaoAuthenticationProvider();
        daoAuth.setUserDetailsService(myUserDetailsService);
        daoAuth.setPasswordEncoder(passwordEncoder());
        return daoAuth;
    }

}
