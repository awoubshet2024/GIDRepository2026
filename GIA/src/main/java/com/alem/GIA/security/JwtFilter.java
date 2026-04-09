package com.alem.GIA.security;

import com.alem.GIA.service.JwtService;
import com.alem.GIA.service.MyUserDetailsService;
import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger =
            LoggerFactory.getLogger(JwtFilter.class);

    private final JwtService jwtService;
    private final MyUserDetailsService myUserDetailsService;

    public JwtFilter(JwtService jwtService,
                     MyUserDetailsService myUserDetailsService) {
        this.jwtService = jwtService;
        this.myUserDetailsService = myUserDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader =
                request.getHeader("Authorization");

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String username =
                    jwtService.extractUsername(jwt);

            if (username != null &&
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication() == null) {

               /* UserDetails userDetails =
                        myUserDetailsService
                                .loadUserByUsername(username);
                System.out.println("Authorities: " + userDetails.getAuthorities());


                if (jwtService.validateToken(jwt, userDetails)) {

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities() // ✅ USE DB AUTHORITIES
                            );

*/
                if (jwtService.validateToken(jwt,
                        myUserDetailsService.loadUserByUsername(username))) {

                    List<String> roles =
                            jwtService.extractAuthorities(jwt);

                    List<String> permissions =
                            jwtService.extractPermissions(jwt);

                    List<GrantedAuthority> authorities =
                            new ArrayList<>();

                    roles.forEach(r ->
                            authorities.add(new SimpleGrantedAuthority(r)));

                    permissions.forEach(p ->
                            authorities.add(new SimpleGrantedAuthority(p)));

                    System.out.println("JWT Authorities = " + authorities);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    authorities
                            );
                    /*UserDetails userDetails =
                            myUserDetailsService.loadUserByUsername(username);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,   // ✅ principal is UserDetails
                                    null,
                                    authorities
                            );*/


                    authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request));

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authToken);
                }

            }

        } catch (Exception ex) {
            logger.error("JWT auth failed", ex);
        }

        filterChain.doFilter(request, response);
    }
}
