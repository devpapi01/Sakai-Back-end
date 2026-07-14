package com.pfe.code.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfe.code.entities.Utilisateur;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final String jwtSecret;
    private final long jwtExpirationMs;

    public JWTAuthenticationFilter(AuthenticationManager authenticationManager, String jwtSecret, long jwtExpirationMs) {
        this.authenticationManager = authenticationManager;
        this.jwtSecret = jwtSecret;
        this.jwtExpirationMs = jwtExpirationMs;
        // ✅ CORRECTION : Définir l'AuthenticationManager pour le parent
        super.setAuthenticationManager(authenticationManager);
        // ✅ Optionnel : définir l'URL du endpoint de login
        setFilterProcessesUrl("/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        Utilisateur utilisateur;
        try {
            utilisateur = new ObjectMapper().readValue(request.getInputStream(), Utilisateur.class);
        } catch (JsonParseException e) {
            throw new AuthenticationServiceException("Invalid login payload", e);
        } catch (JsonMappingException e) {
            throw new AuthenticationServiceException("Invalid login payload", e);
        } catch (IOException e) {
            throw new AuthenticationServiceException("Unable to read login payload", e);
        }

        if (utilisateur.getEmail() == null || utilisateur.getPassword() == null) {
            throw new AuthenticationServiceException("Email and password are required");
        }

        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(utilisateur.getEmail(), utilisateur.getPassword()));
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {

        org.springframework.security.core.userdetails.User springUser =
                (org.springframework.security.core.userdetails.User) authResult.getPrincipal();
        String role = springUser.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse(null);

        String jwt = JWT.create()
                .withSubject(springUser.getUsername())
                .withClaim("role", role)
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .sign(Algorithm.HMAC256(jwtSecret));

        response.addHeader("Authorization", "Bearer " + jwt);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response, AuthenticationException failed)
            throws IOException, ServletException {
        if (failed instanceof DisabledException) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            Map<String, Object> data = new HashMap<>();

            data.put("errorCause", "désactivé");
            data.put("message", "L'utilisateur n'est pas connecté !");
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(data);
            PrintWriter writer = response.getWriter();
            writer.println(json);
            writer.flush();
        } else if (failed instanceof AuthenticationServiceException) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            Map<String, Object> data = new HashMap<>();
            data.put("errorCause", "invalid_request");
            data.put("message", failed.getMessage());

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(data);
            PrintWriter writer = response.getWriter();
            writer.println(json);
            writer.flush();

        } else {
            super.unsuccessfulAuthentication(request, response, failed);
        }
    }
}
