package com.example.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.service.AuthorityJwtService;
import com.example.service.SupabaseJwtService;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SupabaseJwtService supabaseJwtService;
    private final AuthorityJwtService authorityJwtService;

    public JwtAuthenticationFilter(SupabaseJwtService supabaseJwtService, AuthorityJwtService authorityJwtService) {
        this.supabaseJwtService = supabaseJwtService;
        this.authorityJwtService = authorityJwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                try {
                    String authorityId = authorityJwtService.getAuthorityId(token);
                    Long communityId = authorityJwtService.getCommunityId(token);
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            authorityId, null, Collections.singletonList(new SimpleGrantedAuthority("AUTHORITY")));
                    auth.setDetails(communityId);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (Exception e) {
                    String userId = supabaseJwtService.getUserId(token);
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            userId, null, Collections.singletonList(new SimpleGrantedAuthority("USER")));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (ExpiredJwtException | SignatureException e) {
            }
        }
        filterChain.doFilter(request, response);
    }

}
