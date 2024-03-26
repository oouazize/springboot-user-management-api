package com.example.demo.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws JwtException, ServletException, IOException {
        try {
            if (request.getParameter("username") != null && request.getParameter("password") != null) {
                UserDetails userdetails = this.userDetailsService.loadUserByUsername(request.getParameter("username"));
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(  // for creating a Token
                        userdetails,
                        null,
                        userdetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
                HashMap<String, Object> hm = new HashMap<>();
                hm.put("Roles", userdetails.getAuthorities());
                String token = this.jwtService.generateToken(hm, userdetails);
                Cookie cookie = new Cookie("JWT_TOKEN", token);
                cookie.setPath("/");
                response.addCookie(cookie);
            }

            final String jwt = request.getHeader("Authorization");
            if (jwt != null) {
                if (!this.jwtService.isTokenExpired(jwt)) {
                    String username = this.jwtService.extractUsername(jwt);
                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UserDetails userdetails = this.userDetailsService.loadUserByUsername(username);
                        if (this.jwtService.isTokenValid(jwt, userdetails)) {
                            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(  // for creating a Token
                                    userdetails,
                                    null,
                                    userdetails.getAuthorities()
                            );
                            authToken.setDetails(
                                    new WebAuthenticationDetailsSource().buildDetails(request)
                            );
                            SecurityContextHolder.getContext().setAuthentication(authToken);
                        }
                    }
                }
            }
            filterChain.doFilter(request, response);
        }
        catch (Exception err)
        {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("JWT_TOKEN".equals(cookie.getName())) {
                        cookie.setMaxAge(0);
                        response.addCookie(cookie);
                        break;
                    }
                }
            }
            filterChain.doFilter(request, response);
        }
    }
}