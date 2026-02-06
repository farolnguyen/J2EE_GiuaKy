package fit.hutech.spring.controllers;

import fit.hutech.spring.entities.User;
import fit.hutech.spring.services.AuthenticationService;
import fit.hutech.spring.services.OrderService;
import fit.hutech.spring.services.UserService;
import fit.hutech.spring.services.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final OrderService orderService;
    private final WishlistService wishlistService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationService authService;

    @GetMapping
    public String profile(Model model) {
        Optional<User> userOpt = authService.getCurrentUser();
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOpt.get();

        model.addAttribute("user", user);
        model.addAttribute("orderCount", orderService.findByUser(user).size());
        model.addAttribute("wishlistCount", wishlistService.getWishlistByUser(user).size());

        return "profile/index";
    }

    @GetMapping("/edit")
    public String editProfile(Model model) {
        Optional<User> userOpt = authService.getCurrentUser();
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOpt.get();

        model.addAttribute("user", user);
        return "profile/edit";
    }

    @PostMapping("/edit")
    public String updateProfile(@RequestParam String email,
                               @RequestParam(required = false) String phone,
                               RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = authService.getCurrentUser();
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOpt.get();

        user.setEmail(email);
        user.setPhone(phone);
        userService.updateUser(user);

        redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        return "redirect:/profile";
    }

    @GetMapping("/password")
    public String changePasswordForm(Model model) {
        return "profile/password";
    }

    @PostMapping("/password")
    public String changePassword(@RequestParam String currentPassword,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = authService.getCurrentUser();
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOpt.get();

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Current password is incorrect");
            return "redirect:/profile/password";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "New passwords do not match");
            return "redirect:/profile/password";
        }

        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Password must be at least 6 characters");
            return "redirect:/profile/password";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userService.updateUser(user);

        redirectAttributes.addFlashAttribute("success", "Password changed successfully!");
        return "redirect:/profile";
    }

    @GetMapping("/address")
    public String addressForm(Model model) {
        Optional<User> userOpt = authService.getCurrentUser();
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOpt.get();

        model.addAttribute("user", user);
        return "profile/address";
    }

    @PostMapping("/address")
    public String updateAddress(@RequestParam String shippingAddress,
                               @RequestParam String shippingCity,
                               @RequestParam String shippingPostalCode,
                               @RequestParam String shippingCountry,
                               RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = authService.getCurrentUser();
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOpt.get();

        user.setShippingAddress(shippingAddress);
        user.setShippingCity(shippingCity);
        user.setShippingPostalCode(shippingPostalCode);
        user.setShippingCountry(shippingCountry);
        userService.updateUser(user);

        redirectAttributes.addFlashAttribute("success", "Shipping address updated successfully!");
        return "redirect:/profile";
    }
}
