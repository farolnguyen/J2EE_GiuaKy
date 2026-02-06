package fit.hutech.spring.services;

import fit.hutech.spring.constants.Role;
import fit.hutech.spring.entities.User;
import fit.hutech.spring.repositories.IRoleRepository;
import fit.hutech.spring.repositories.IUserRepository;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserService implements UserDetailsService {
    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private IRoleRepository roleRepository;
    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = { Exception.class, Throwable.class })
    public void save(@NotNull User user) {
        log.info("Saving user: {}", user.getUsername());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        log.info("User saved successfully");
    }

    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = { Exception.class, Throwable.class })
    public void setDefaultRole(String username) {
        log.info("Setting default role for user: {}", username);
        userRepository.findByUsername(username).ifPresent(user -> {
            user.getRoles().add(roleRepository.findRoleById(Role.USER.value));
            userRepository.save(user);
        });
    }

    public Optional<User> findByUsername(String username) {
        log.info("Finding user by username: {}", username);
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        log.info("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        log.info("Loading user by username: {}", username);
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        log.info("User found: {}, roles: {}, enabled: {}, locked: {}", 
            user.getUsername(), user.getRoles().size(), user.isEnabled(), !user.isAccountNonLocked());
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getAuthorities())
                .accountExpired(false)
                .accountLocked(!user.isAccountNonLocked())
                .credentialsExpired(false)
                .disabled(!user.isEnabled())
                .build();
    }

    @Transactional
    public void saveOauthUser(String email, @NotNull String username) {
        saveOauthUser(email, username, "GOOGLE");
    }
    
    @Transactional
    public void saveOauthUser(String email, @NotNull String username, String provider) {
        // Check if user with this email already exists
        var existingUserByEmail = userRepository.findByEmail(email);
        if (existingUserByEmail.isPresent()) {
            var user = existingUserByEmail.get();
            log.info("User with email {} already exists (username: {}, provider: {})", 
                email, user.getUsername(), user.getProvider());
            
            // If user registered with password (LOCAL or null provider), REJECT OAuth login
            if (user.getProvider() == null || user.getProvider().equals("LOCAL")) {
                log.warn("Email {} already registered with password. OAuth login rejected.", email);
                throw new IllegalStateException("EMAIL_ALREADY_REGISTERED_WITH_PASSWORD");
            }
            
            // If already registered with any OAuth provider, just return (already exists)
            if (user.getProvider().equals("GOOGLE") || 
                user.getProvider().equals("GITHUB") || 
                user.getProvider().equals("FACEBOOK")) {
                log.info("User already registered with {} OAuth, continuing...", user.getProvider());
                return;
            }
        }
        
        // Check if username already exists (shouldn't happen but safe check)
        if (userRepository.findByUsername(username).isPresent()) {
            log.info("OAuth user with username already exists: {}", username);
            return;
        }
        
        log.info("Saving new OAuth user: {} with email: {} (provider: {})", username, email, provider);
        var user = new User();
        user.setUsername(username);
        user.setEmail(email);
        
        // Generate a password that meets validation criteria (OAuth users don't use password)
        // Pattern: at least one uppercase, one lowercase, one number, one special char, min 8 chars
        String oauthPassword = "OAuth@" + username.hashCode() + "!";
        user.setPassword(passwordEncoder.encode(oauthPassword));
        user.setProvider(provider);
        
        // Add USER role directly before saving
        var userRole = roleRepository.findRoleById(Role.USER.value);
        user.getRoles().add(userRole);
        
        userRepository.save(user);
        log.info("OAuth user saved successfully with USER role");
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = { Exception.class, Throwable.class })
    public void updateUser(@NotNull User user) {
        log.info("Updating user: {}", user.getUsername());
        userRepository.save(user);
        log.info("User updated successfully");
    }
    
    // ==================== USER MANAGEMENT METHODS ====================
    
    @Transactional
    public void incrementFailedAttempts(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            int attempts = (user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0) + 1;
            user.setFailedLoginAttempts(attempts);
            log.warn("Failed login attempt #{} for user: {}", attempts, username);
            
            // Lock account after 5 failed attempts for 24 hours
            if (attempts >= 5) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.HOUR, 24);
                user.setLockedUntil(cal.getTime());
                log.warn("Account locked for 24 hours: {}", username);
            }
            
            userRepository.save(user);
        });
    }
    
    @Transactional
    public void resetFailedAttempts(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            if (user.getFailedLoginAttempts() != null && user.getFailedLoginAttempts() > 0) {
                user.setFailedLoginAttempts(0);
                user.setLockedUntil(null);
                userRepository.save(user);
                log.info("Reset failed login attempts for user: {}", username);
            }
        });
    }
    
    @Transactional
    public void unlockAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setLockedUntil(null);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        log.info("Account unlocked: {}", user.getUsername());
    }
    
    @Transactional
    public void setEnabled(Long userId, Boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setEnabled(enabled);
        userRepository.save(user);
        log.info("User {} {}", user.getUsername(), enabled ? "enabled" : "disabled");
    }
    
    @Transactional
    public void changePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed for user: {}", user.getUsername());
    }
    
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    public Optional<User> findFirstAdmin() {
        // Find the admin with the earliest creation date
        return userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> "ADMIN".equals(r.getName())))
                .min((u1, u2) -> {
                    Date d1 = u1.getCreatedAt() != null ? u1.getCreatedAt() : new Date(0);
                    Date d2 = u2.getCreatedAt() != null ? u2.getCreatedAt() : new Date(0);
                    return d1.compareTo(d2);
                });
    }
    
    public boolean isFirstAdmin(Long userId) {
        return findFirstAdmin().map(admin -> admin.getId().equals(userId)).orElse(false);
    }
    
    @Transactional
    public void demoteAdmin(Long adminId, Long targetUserId) {
        // Only first admin can demote other admins
        if (!isFirstAdmin(adminId)) {
            throw new IllegalStateException("Only the first admin can demote other admins");
        }
        
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Cannot demote yourself (the first admin)
        if (targetUserId.equals(adminId)) {
            throw new IllegalStateException("Cannot demote yourself");
        }
        
        // Remove ADMIN role, keep USER role
        var adminRole = roleRepository.findRoleById(Role.ADMIN.value);
        targetUser.getRoles().remove(adminRole);
        
        // Ensure user has at least USER role
        var userRole = roleRepository.findRoleById(Role.USER.value);
        if (!targetUser.getRoles().contains(userRole)) {
            targetUser.getRoles().add(userRole);
        }
        
        userRepository.save(targetUser);
        log.info("User {} demoted from ADMIN to USER", targetUser.getUsername());
    }
    
    @Transactional
    public void promoteToAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        var adminRole = roleRepository.findRoleById(Role.ADMIN.value);
        user.getRoles().add(adminRole);
        userRepository.save(user);
        log.info("User {} promoted to ADMIN", user.getUsername());
    }
    
    public int getFailedAttempts(String username) {
        return userRepository.findByUsername(username)
                .map(u -> u.getFailedLoginAttempts() != null ? u.getFailedLoginAttempts() : 0)
                .orElse(0);
    }
}