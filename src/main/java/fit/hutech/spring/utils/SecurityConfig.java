package fit.hutech.spring.utils;

import fit.hutech.spring.filters.JwtAuthenticationFilter;
import fit.hutech.spring.handlers.CustomAuthenticationFailureHandler;
import fit.hutech.spring.handlers.CustomAuthenticationSuccessHandler;
import fit.hutech.spring.services.CustomOAuth2UserService;
import fit.hutech.spring.services.OAuthService;
import fit.hutech.spring.services.UserService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private final OAuthService oAuthService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final UserService userService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationFailureHandler authenticationFailureHandler;
    private final CustomAuthenticationSuccessHandler authenticationSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(@NotNull HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**")
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/", "/images/**",
                                "/oauth/**", "/register", "/error",
                                "/api/auth/**", "/api-login", "/oauth-callback",
                                "/api/debug/**", "/debug", "/account-disabled",
                                "/forgot-password", "/forgot-password/**")
                        .permitAll()

                        .requestMatchers("/books", "/books/{id}")
                        .permitAll()

                        .requestMatchers("/admin/**")
                        .hasAuthority("ADMIN")

                        .requestMatchers("/books/edit/**",
                                "/books/add", "/books/delete")
                        .hasAnyAuthority("ADMIN")
                        .requestMatchers("/books/api-add", "/books/api-edit/**")
                        .hasAuthority("ADMIN")
                        .requestMatchers("/books/api-list")
                        .hasAnyAuthority("ADMIN", "USER")
                        .requestMatchers("/cart", "/cart/**",
                                "/wishlist", "/wishlist/**", "/checkout", "/checkout/**",
                                "/orders", "/orders/**", "/profile", "/profile/**")
                        .hasAnyAuthority("ADMIN", "USER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/books/**")
                        .hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/books/**")
                        .hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/books/**")
                        .hasAuthority("ADMIN")
                        .requestMatchers("/api/**")
                        .hasAnyAuthority("ADMIN", "USER")
                        .anyRequest().authenticated())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll())
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(authenticationSuccessHandler)
                        .failureHandler(authenticationFailureHandler)
                        .permitAll())
                .oauth2Login(
                        oauth2Login -> oauth2Login
                                .loginPage("/login")
                                .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint
                                        .oidcUserService(oAuthService)
                                        .userService(customOAuth2UserService))
                                .defaultSuccessUrl("/", true)
                                .failureHandler((request, response, exception) -> {
                                    System.err.println("OAuth2 Login FAILED: " + exception.getMessage());
                                    exception.printStackTrace();

                                    // Check if account is disabled (check error code, not message)
                                    if (exception instanceof org.springframework.security.oauth2.core.OAuth2AuthenticationException) {
                                        var oauth2Exception = (org.springframework.security.oauth2.core.OAuth2AuthenticationException) exception;
                                        if ("account_disabled".equals(oauth2Exception.getError().getErrorCode())) {
                                            response.sendRedirect("/account-disabled");
                                            return;
                                        }
                                    }
                                    
                                    response.sendRedirect("/login?error=oauth_failed&message=" + exception.getMessage());
                                })
                                .permitAll()
                ).rememberMe(rememberMe -> rememberMe
                        .key("hutech")
                        .rememberMeCookieName("hutech")
                        .tokenValiditySeconds(24 * 60 * 60)
                        .userDetailsService(userService))
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedPage("/403"))
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().migrateSession()
                        .maximumSessions(1)
                        .expiredUrl("/login"))
                .httpBasic(httpBasic -> httpBasic.disable())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}