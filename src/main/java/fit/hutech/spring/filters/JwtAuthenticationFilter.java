package fit.hutech.spring.filters;

import fit.hutech.spring.services.UserService;
import fit.hutech.spring.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        final String requestPath = request.getRequestURI();
        final String authHeader = request.getHeader("Authorization");
        
        // For /api/v1/** endpoints, JWT is REQUIRED (no session fallback)
        if (requestPath.startsWith("/api/v1/")) {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("JWT required for {} but no Bearer token provided", requestPath);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"JWT token required\",\"message\":\"Authorization header with Bearer token is required for API endpoints\"}");
                return;
            }
            
            String jwt = authHeader.substring(7);
            try {
                String username = jwtUtil.extractUsername(jwt);
                UserDetails userDetails = userService.loadUserByUsername(username);
                
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("JWT authenticated user: {}", username);
                } else {
                    log.warn("Invalid JWT token for {}", requestPath);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Invalid token\",\"message\":\"JWT token is invalid or expired\"}");
                    return;
                }
            } catch (Exception e) {
                log.warn("JWT authentication failed for {}: {}", requestPath, e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Authentication failed\",\"message\":\"" + e.getMessage() + "\"}");
                return;
            }
        } else {
            // For other endpoints, JWT is optional (can use session)
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwt = authHeader.substring(7);
                try {
                    String username = jwtUtil.extractUsername(jwt);
                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UserDetails userDetails = userService.loadUserByUsername(username);
                        if (jwtUtil.validateToken(jwt, userDetails)) {
                            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authToken);
                            log.debug("JWT authenticated user: {}", username);
                        }
                    }
                } catch (Exception e) {
                    log.warn("JWT authentication failed: {}", e.getMessage());
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
