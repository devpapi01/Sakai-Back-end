package com.pfe.code.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class JWTAuthorizationFilter extends OncePerRequestFilter {

    private final String jwtSecret;

    public JWTAuthorizationFilter(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String jwt =request.getHeader("Authorization");

        if (jwt==null || !jwt.startsWith(SecParams.prefix))
        {
            filterChain.doFilter(request, response);
            return;
        }

        try{
            JWTVerifier verifier =
                    JWT.require(Algorithm.HMAC256(jwtSecret)).build();

            //enlever le préfixe Bearer du  jwt
            jwt = jwt.substring(SecParams.prefix.length());

            DecodedJWT decodedJWT = verifier.verify(jwt);
            String username = decodedJWT.getSubject();
            String role = decodedJWT.getClaim("role").asString();
            Collection<GrantedAuthority> authorities =
                    Collections.singletonList(new SimpleGrantedAuthority(role));


            UsernamePasswordAuthenticationToken user =
                    new
                            UsernamePasswordAuthenticationToken(username, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(user);
        }
         catch (Exception e) {
            // Token invalide/expiré/corrompu
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Token invalide ou expiré\"}");
            return; // ⚠️ Important : ne pas continuer la chaîne
        }

        filterChain.doFilter(request, response);
    }
}