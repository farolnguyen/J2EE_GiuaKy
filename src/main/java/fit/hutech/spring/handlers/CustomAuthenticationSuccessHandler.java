package fit.hutech.spring.handlers;

import fit.hutech.spring.services.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    private final UserService userService;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                       HttpServletResponse response,
                                       Authentication authentication) throws IOException, ServletException {
        
        String username = authentication.getName();
        
        // Reset failed login attempts on successful login
        userService.resetFailedAttempts(username);
        log.info("Successful login for user: {}", username);
        
        setDefaultTargetUrl("/");
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
