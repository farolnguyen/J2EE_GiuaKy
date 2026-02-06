package fit.hutech.spring.controllers;

import fit.hutech.spring.entities.User;
import fit.hutech.spring.services.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/login")
    public String login() {
        return "user/login";
    }

    @GetMapping("/api-login")
    public String apiLogin() {
        return "user/api-login";
    }

    @GetMapping("/oauth-callback")
    public String oauthCallback() {
        return "user/oauth-callback";
    }

    @GetMapping("/register")
    public String register(@NotNull Model model) {
        model.addAttribute("user", new User());
        return "user/register";
    }
    
    @GetMapping("/account-disabled")
    public String accountDisabled() {
        return "error/account-disabled";
    }

    // Password pattern: at least one uppercase, lowercase, number, special char, min 8 chars
    private static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") User user,
            @NotNull BindingResult bindingResult,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {
        // Validate password pattern (must be done here, not in entity, because entity stores encoded password)
        if (user.getPassword() == null || !user.getPassword().matches(PASSWORD_PATTERN)) {
            model.addAttribute("error", "Password must be at least 8 characters and contain uppercase, lowercase, number, and special character (@$!%*?&)");
            return "user/register";
        }
        
        // Check password match first
        if (!user.getPassword().equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            return "user/register";
        }
        
        if (bindingResult.hasErrors()) {
            var errors = bindingResult.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList();
            model.addAttribute("errors", errors);
            return "user/register";
        }
        
        // Check if email already exists with OAuth provider
        var existingUser = userService.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            var existing = existingUser.get();
            if ("GOOGLE".equals(existing.getProvider())) {
                model.addAttribute("error", "This email is already registered with Google. Please login using Google.");
                return "user/register";
            }
            if ("GITHUB".equals(existing.getProvider())) {
                model.addAttribute("error", "This email is already registered with GitHub. Please login using GitHub.");
                return "user/register";
            }
            if ("DISCORD".equals(existing.getProvider())) {
                model.addAttribute("error", "This email is already registered with Discord. Please login using Discord.");
                return "user/register";
            }
            // If LOCAL provider or null, normal duplicate email validation will handle it
        }
        
        userService.save(user);
        userService.setDefaultRole(user.getUsername());
        return "redirect:/login";
    }
}