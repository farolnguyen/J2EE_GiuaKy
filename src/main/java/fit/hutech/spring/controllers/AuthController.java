package fit.hutech.spring.controllers;

import fit.hutech.spring.dto.LoginRequest;
import fit.hutech.spring.dto.LoginResponse;
import fit.hutech.spring.services.UserService;
import fit.hutech.spring.utils.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getUsername());
        
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );
        } catch (BadCredentialsException e) {
            log.warn("Invalid credentials for user: {}", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid username or password"));
        }

        final UserDetails userDetails = userService.loadUserByUsername(loginRequest.getUsername());
        final String token = jwtUtil.generateToken(userDetails);

        var roles = userDetails.getAuthorities().stream()
            .map(auth -> auth.getAuthority())
            .toList();

        log.info("Login successful for user: {}, roles: {}", loginRequest.getUsername(), roles);

        return ResponseEntity.ok(LoginResponse.builder()
            .token(token)
            .type("Bearer")
            .username(userDetails.getUsername())
            .roles(roles)
            .expiresIn(jwtExpiration)
            .build());
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("valid", false, "error", "No token provided"));
        }

        String token = authHeader.substring(7);
        try {
            String username = jwtUtil.extractUsername(token);
            UserDetails userDetails = userService.loadUserByUsername(username);
            
            if (jwtUtil.validateToken(token, userDetails)) {
                return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "username", username,
                    "roles", userDetails.getAuthorities().stream()
                        .map(auth -> auth.getAuthority())
                        .toList()
                ));
            }
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("valid", false, "error", "Invalid or expired token"));
    }

    @GetMapping("/oauth-token")
    public ResponseEntity<?> generateOAuthToken() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            log.info("Generating OAuth token for: {}", authentication.getName());
            
            // Extract email from OAuth2 or OIDC user
            String email = null;
            Object principal = authentication.getPrincipal();
            
            if (principal instanceof org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser) {
                // Google uses OIDC
                var oidcUser = (org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser) principal;
                email = oidcUser.getEmail();
                log.info("OAuth user email from OIDC (Google): {}", email);
            } else if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
                // GitHub/Facebook use OAuth2
                var oauth2User = (org.springframework.security.oauth2.core.user.OAuth2User) principal;
                email = oauth2User.getAttribute("email");
                log.info("OAuth user email from OAuth2 (GitHub/Facebook): {}", email);
            }
            
            if (email == null) {
                log.error("No email found in OAuth authentication");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "No email in OAuth response"));
            }
            
            var userOptional = userService.findByEmail(email);
            if (userOptional.isEmpty()) {
                log.error("User not found: {}", email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
            }
            
            var user = userOptional.get();
            String username = user.getUsername();
            log.info("Generating OAuth JWT token for user: {} (email: {})", username, email);
            
            UserDetails userDetails = userService.loadUserByUsername(username);
            String token = jwtUtil.generateToken(userDetails);
            
            List<String> roles = userDetails.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .toList();
            
            log.info("OAuth JWT token generated for user: {}", username);
            
            return ResponseEntity.ok(LoginResponse.builder()
                .token(token)
                .username(username)
                .roles(roles)
                .type("Bearer")
                .expiresIn(jwtExpiration)
                .build());
        } catch (Exception e) {
            log.error("Failed to generate OAuth token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to generate token"));
        }
    }
}
