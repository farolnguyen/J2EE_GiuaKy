package fit.hutech.spring.services;

import fit.hutech.spring.repositories.IUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final IUserRepository userRepository;
    private final UserService userService;
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Load user from OAuth2 provider (GitHub, Microsoft)
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        // Get provider (github or microsoft)
        String provider = userRequest.getClientRegistration().getRegistrationId().toUpperCase();
        log.info("Loading OAuth2 user from provider: {}", provider);
        log.info("OAuth2 user attributes: {}", oauth2User.getAttributes());
        
        // Extract email and username based on provider
        String email = oauth2User.getAttribute("email");
        String username;
        String nameAttributeKey;
        
        if ("GITHUB".equals(provider)) {
            // GitHub uses 'id' as the unique identifier
            username = oauth2User.getAttribute("login"); // GitHub username
            nameAttributeKey = "id";
            
            // GitHub email might be null if not public - need to fetch from emails API
            if (email == null) {
                log.warn("GitHub email is null - user may not have public email. Using username@github.oauth as placeholder");
                email = username + "@github.oauth";
            }
            log.info("GitHub user: {} (email: {})", username, email);
        } else if ("DISCORD".equals(provider)) {
            // Discord uses 'id' as the unique identifier
            username = oauth2User.getAttribute("username");
            if (username == null) {
                username = oauth2User.getAttribute("id");
            }
            nameAttributeKey = "id";
            
            // Discord provides email directly
            log.info("Discord user: {} (email: {})", username, email);
        } else {
            // Default
            username = oauth2User.getName();
            nameAttributeKey = "id";
        }
        
        Set<GrantedAuthority> authorities = new HashSet<>();
        
        // Check by EMAIL first
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
            
            // Update provider if needed
            if (user.getProvider() == null || user.getProvider().equals("LOCAL")) {
                user.setProvider(provider);
                userRepository.save(user);
                log.info("Updated existing user provider to {}", provider);
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
            // New user - save them
            log.info("New OAuth2 user, creating with USER role");
            userService.saveOauthUser(email, username, provider);
            authorities.add(new SimpleGrantedAuthority("USER"));
            log.info("Added authority: USER (new user)");
        }
        
        // Ensure at least USER authority exists
        if (authorities.isEmpty()) {
            log.warn("No authorities found, adding default USER authority");
            authorities.add(new SimpleGrantedAuthority("USER"));
        }
        
        log.info("Returning OAuth2User with {} authorities", authorities.size());
        
        // Return OAuth2User with authorities
        return new DefaultOAuth2User(
            authorities,
            oauth2User.getAttributes(),
            nameAttributeKey
        );
    }
}
