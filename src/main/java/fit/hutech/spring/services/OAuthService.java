package fit.hutech.spring.services;

import fit.hutech.spring.repositories.IUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class OAuthService extends OidcUserService {
    private final IUserRepository userRepository;
    private final UserService userService;
    
    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // Load user from Google OIDC
        OidcUser oidcUser = super.loadUser(userRequest);
        
        // Get user details
        String username = oidcUser.getName();
        String email = oidcUser.getEmail();
        log.info("Loading OIDC user: {} ({})", username, email);
        
        Set<GrantedAuthority> authorities = new HashSet<>();
        
        // Check if user exists in database
        var userOptional = userRepository.findByUsername(username);
        
        if (userOptional.isPresent()) {
            // User exists - load their roles
            var user = userOptional.get();
            log.info("Existing OIDC user found with {} roles", user.getRoles().size());
            user.getRoles().forEach(role -> {
                authorities.add(new SimpleGrantedAuthority(role.getName()));
                log.info("Added authority: {}", role.getName());
            });
        } else {
            // New user - save them and add USER authority directly
            log.info("New OIDC user, creating with USER role");
            userService.saveOauthUser(email, username);
            // Add USER authority directly
            authorities.add(new SimpleGrantedAuthority("USER"));
            log.info("Added authority: USER (new user)");
        }
        
        log.info("Returning OidcUser with {} authorities", authorities.size());
        
        // Return OidcUser with authorities
        return new DefaultOidcUser(
            authorities,
            oidcUser.getIdToken(),
            oidcUser.getUserInfo()
        );
    }
}