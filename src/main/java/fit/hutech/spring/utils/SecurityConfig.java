package fit.hutech.spring.utils;

import fit.hutech.spring.filters.JwtAuthenticationFilter;
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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private final OAuthService oAuthService;
    private final UserService userService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

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
                        .requestMatchers("/css/**", "/js/**", "/",
                                "/oauth/**", "/register", "/error",
                                "/api/auth/**", "/api-login", "/oauth-callback")
                        .permitAll()

                        .requestMatchers("/books/edit/**",

                                "/books/add", "/books/delete")
                        .hasAnyAuthority("ADMIN")
                        .requestMatchers("/books/api-add", "/books/api-edit/**")
                        .hasAuthority("ADMIN")
                        .requestMatchers("/books/api-list")
                        .hasAnyAuthority("ADMIN", "USER")
                        .requestMatchers("/books", "/cart", "/cart/**")
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
                        .defaultSuccessUrl("/")
                        .failureUrl("/login?error")
                        .permitAll())
                .oauth2Login(
                        oauth2Login -> oauth2Login
                                .loginPage("/login")
                                .failureUrl("/login?error")
                                .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint
                                        .oidcUserService(oAuthService))

                                .successHandler(

                                        (request, response,
                                                authentication) -> {
                                            var oidcUser = (DefaultOidcUser) authentication
                                                    .getPrincipal();
                                            userService.saveOauthUser(
                                                    oidcUser.getEmail(),
                                                    oidcUser.getName());
                                            response.sendRedirect("/oauth-callback");
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
                        .maximumSessions(1)
                        .expiredUrl("/login"))
                .httpBasic(httpBasic -> httpBasic
                        .realmName("hutech"))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}