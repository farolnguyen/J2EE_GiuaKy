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
        
        // IMPORTANT: Check by EMAIL first, not username
        // Users may have registered with password (different username) but same email
        var userOptional = userRepository.findByEmail(email);
        
        if (userOptional.isPresent()) {
            // User exists - load their roles
            var user = userOptional.get();
            log.info("Existing user found by email: {} with {} roles (username: {})", email, user.getRoles().size(), user.getUsername());
            
            // Check if account is enabled
            if (!user.getEnabled()) {
                log.warn("Account disabled for user: {}", user.getUsername());
                throw new OAuth2AuthenticationException("account_disabled");
            }
            
            // Update provider to GOOGLE if needed
            if (user.getProvider() == null || user.getProvider().equals("LOCAL")) {
                user.setProvider("GOOGLE");
                userRepository.save(user);
                log.info("Updated existing user provider to GOOGLE");
            }
            
            // Load roles and add as authorities
            if (user.getRoles().isEmpty()) {
                // User has no roles - add USER authority as default
                log.warn("User {} has no roles, adding USER authority", user.getUsername());
                authorities.add(new SimpleGrantedAuthority("USER"));
            } else {
                user.getRoles().forEach(role -> {
                    String roleName = role.getName();
                    authorities.add(new SimpleGrantedAuthority(roleName));
                    log.info("Added authority from DB role: '{}'", roleName);
                });
            }
        } else {
            // New user - save them and add USER authority directly
            log.info("New OIDC user, creating with USER role");
            userService.saveOauthUser(email, username);
            // Add USER authority directly
            authorities.add(new SimpleGrantedAuthority("USER"));
            log.info("Added authority: USER (new user)");
        }
        
        // Ensure at least USER authority exists
        if (authorities.isEmpty()) {
            log.warn("No authorities found, adding default USER authority");
            authorities.add(new SimpleGrantedAuthority("USER"));
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