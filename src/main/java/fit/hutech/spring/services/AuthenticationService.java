package fit.hutech.spring.services;

import fit.hutech.spring.entities.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final UserService userService;
    
    public Optional<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.debug("No authentication found or not authenticated");
            return Optional.empty();
        }
        
        Object principal = auth.getPrincipal();
        log.debug("Principal type: {}", principal.getClass().getName());
        
        if (principal instanceof UserDetails) {
            // Form login - UserDetails
            String username = ((UserDetails) principal).getUsername();
            log.debug("Form login user: {}", username);
            return userService.findByUsername(username);
        } else if (principal instanceof OAuth2User) {
            // OAuth2 login (GitHub, Facebook, Google)
            OAuth2User oauth2User = (OAuth2User) principal;
            String email = oauth2User.getAttribute("email");
            
            // GitHub might not have email, use login@github.oauth
            if (email == null) {
                String login = oauth2User.getAttribute("login");
                if (login != null) {
                    email = login + "@github.oauth";
                    log.debug("GitHub user (no email): {} -> {}", login, email);
                }
            } else {
                log.debug("OAuth2 user email: {}", email);
            }
            
            if (email != null) {
                return userService.findByEmail(email);
            }
        }
        
        log.warn("Unknown principal type: {}", principal.getClass().getName());
        return Optional.empty();
    }
    
    public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String);
    }
}
