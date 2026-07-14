package com.pfe.code.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final MyUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    public SecurityConfig(MyUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    private static final String[] AUTH_WHITELIST = {
            "/",
            "/error",
            "/login",
            "/register",
            "/marchands/register",
            "/marchands/verifyEmail/**",
            "/fournisseurs/**",
            "/api-docs/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    // Lecture publique du catalogue (categories/sous-categories) : ecriture reservee a ADMIN, voir ADMIN[]
    private static final String[] CATALOG_READ_WHITELIST = {
            "/categories/allcats",
            "/categories/allnoms",
            "/categories/getByid/**",
            "/categories/nomc/**",
            "/souscategories/allsc",
            "/souscategories/getssc/**",
            "/souscategories/getbycatid/**",
            "/souscategories/allnomss",
            "/souscategories/ssnomc/**",
            "/souscategories/getbyid/**",
            "/produits/allprods",
            "/produits/filtre",
            "/produits/search",
            "/produits/getncont/**",
            "/produits/prodcat/**",
            "/produits/detailprod/**",
            "/produits/prodcatnom/**",
            "/produits/fournisseur/**",
            "/produits/prodcatacs",
            "/produits/prix/**",
            "/produits/prixasc",
            "/produits/prixdes",
            "/produits/nomasc",
            "/produits/nomdesc"
    };

    private static final String[] ADMIN = {
            "/users/all",
            "/users/deleteUser/**",
            "/users/nomcont/**",
            "/marchands/all",
            "/marchands/getnc/**",
            "/marchands/nomASC",
            "/marchands/nomDESc",
            "/marchands/preASC",
            "/marchands/preDESC",
            "/marchands/find/**",
            "/categories/addcat",
            "/categories/updatecat",
            "/categories/deletecat/**",
            "/souscategories/addsscat/**",
            "/souscategories/updatecat",
            "/souscategories/deletessc/**",
            "/serviceslivraison/addSl",
            "/commandes/all"
    };

    private static final String[] SERVICE = {
            "/serviceslivraison/getSl/**",
            "/serviceslivraison/deleteSL/**",
            "/serviceslivraison/updateSl",
            "/livreurs/addlivreur/**",
            "/commandes/setlivreur/**",
            "/commandes/getbySL/**"
    };

    private static final String[] COMMUN = {
            "/users/changepassword/**",
            "/users/updateinfosuser",
            "/users/email/**",
            "/commandes/getbyM/**",
            "/commandes/getbyref/**"
    };

    private static final String[] LIVREUR = {
            "/livreurs/deletelivreur/**",
            "/commandes/getbylivreur/**"
    };

    private static final String[] FOURNISSEUR = {
            "/produits/addprod",
            "/produits/update",
            "/produits/supprimer/**"
    };

    private static final String[] ACHETEUR = {
            "/marchands/updateinfos",
            "/marchands/delete/**",
            "/commandes/newcommande",
            "/commandes/deletecom/**",
            "/commandes/panier/**",
            "/commandes/*/payer",
            "/marchands/adresses/**"
    };

    private static final String[] SERVICE_OR_LIVREUR = {
            "/livreurs/updateL",
            "/livreurs/getforsl/**",
            "/commandes/etatcom/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
        AuthenticationManager authenticationManager = authBuilder.build();

        http
                .authenticationManager(authenticationManager)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\":\"Unauthorized - Token manquant ou invalide\"}");
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\":\"Forbidden - Vous n'avez pas les permissions\"}");
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(AUTH_WHITELIST).permitAll()
                        .requestMatchers(HttpMethod.GET, CATALOG_READ_WHITELIST).permitAll()
                        .requestMatchers(COMMUN).authenticated()
                        .requestMatchers(ADMIN).hasAuthority("ADMIN")
                        .requestMatchers(SERVICE).hasAuthority("SERVICE_LIVRAISON")
                        .requestMatchers(SERVICE_OR_LIVREUR).hasAnyAuthority("SERVICE_LIVRAISON", "LIVREUR")
                        .requestMatchers(LIVREUR).hasAuthority("LIVREUR")
                        .requestMatchers(FOURNISSEUR).hasAuthority("FOURNISSEUR")
                        .requestMatchers(ACHETEUR).hasAuthority("ACHETEUR")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        new JWTAuthorizationFilter(jwtSecret),
                        UsernamePasswordAuthenticationFilter.class
                )
                .addFilter(new JWTAuthenticationFilter(authenticationManager, jwtSecret, jwtExpirationMs));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Collections.singletonList("http://localhost:4200"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setExposedHeaders(Collections.singletonList("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
