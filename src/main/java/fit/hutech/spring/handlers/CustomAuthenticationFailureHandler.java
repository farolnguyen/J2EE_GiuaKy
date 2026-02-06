package fit.hutech.spring.handlers;

import fit.hutech.spring.services.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    
    private final UserService userService;
    
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, 
                                       HttpServletResponse response,
                                       AuthenticationException exception) throws IOException, ServletException {
        
        String username = request.getParameter("username");
        String errorMessage;
        
        if (exception instanceof LockedException) {
            errorMessage = "locked";
            log.warn("Login attempt for locked account: {}", username);
        } else if (exception instanceof DisabledException) {
            errorMessage = "disabled";
            log.warn("Login attempt for disabled account: {}", username);
        } else if (exception instanceof BadCredentialsException) {
            // Increment failed login attempts
            if (username != null && !username.isEmpty()) {
                userService.incrementFailedAttempts(username);
                int attempts = userService.getFailedAttempts(username);
                
                if (attempts >= 5) {
                    errorMessage = "locked";
                    log.warn("Account locked after 5 failed attempts: {}", username);
                } else if (attempts >= 2) {
                    errorMessage = "warning&attempts=" + attempts;
                    log.warn("Warning: {} failed login attempts for user: {}", attempts, username);
                } else {
                    errorMessage = "bad_credentials";
                }
            } else {
                errorMessage = "bad_credentials";
            }
        } else {
            errorMessage = "error";
            log.error("Authentication failure: {}", exception.getMessage());
        }
        
        setDefaultFailureUrl("/login?error=" + errorMessage);
        super.onAuthenticationFailure(request, response, exception);
    }
}
