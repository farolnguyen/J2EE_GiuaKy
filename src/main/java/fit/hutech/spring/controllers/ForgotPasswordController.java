package fit.hutech.spring.controllers;

import fit.hutech.spring.services.EmailService;
import fit.hutech.spring.services.OtpService;
import fit.hutech.spring.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/forgot-password")
@RequiredArgsConstructor
@Slf4j
public class ForgotPasswordController {
    
    private final UserService userService;
    private final OtpService otpService;
    private final EmailService emailService;
    
    @GetMapping
    public String showForgotPasswordPage() {
        return "user/forgot-password";
    }
    
    @PostMapping("/send-otp")
    public String sendOtp(@RequestParam String email, 
                         RedirectAttributes redirectAttributes) {
        var userOpt = userService.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No account found with this email address");
            return "redirect:/forgot-password";
        }
        
        var user = userOpt.get();
        
        if (user.getProvider() != null && !user.getProvider().equals("LOCAL")) {
            redirectAttributes.addFlashAttribute("error", 
                "This account uses " + user.getProvider() + " login. Password reset is not available.");
            return "redirect:/forgot-password";
        }
        
        String otp = otpService.generateOtp(email);
        emailService.sendPasswordResetOtp(email, otp);
        
        log.info("Password reset OTP sent to: {}", email);
        redirectAttributes.addFlashAttribute("email", email);
        redirectAttributes.addFlashAttribute("success", "OTP has been sent to your email");
        return "redirect:/forgot-password/verify-otp";
    }
    
    @GetMapping("/verify-otp")
    public String showVerifyOtpPage(Model model) {
        if (!model.containsAttribute("email")) {
            return "redirect:/forgot-password";
        }
        return "user/verify-otp";
    }
    
    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String email,
                           @RequestParam String otp,
                           RedirectAttributes redirectAttributes) {
        if (!otpService.validateOtp(email, otp)) {
            int remaining = otpService.getRemainingAttempts(email);
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("error", 
                "Invalid OTP. " + (remaining > 0 ? remaining + " attempts remaining" : "Maximum attempts exceeded"));
            return "redirect:/forgot-password/verify-otp";
        }
        
        redirectAttributes.addFlashAttribute("email", email);
        redirectAttributes.addFlashAttribute("success", "OTP verified successfully");
        return "redirect:/forgot-password/reset";
    }
    
    @GetMapping("/reset")
    public String showResetPasswordPage(Model model) {
        if (!model.containsAttribute("email")) {
            return "redirect:/forgot-password";
        }
        return "user/reset-password";
    }
    
    @PostMapping("/reset")
    public String resetPassword(@RequestParam String email,
                               @RequestParam String newPassword,
                               @RequestParam String confirmPassword,
                               RedirectAttributes redirectAttributes) {
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("error", "Passwords do not match");
            return "redirect:/forgot-password/reset";
        }
        
        String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        if (!newPassword.matches(PASSWORD_PATTERN)) {
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("error", 
                "Password must be at least 8 characters and contain uppercase, lowercase, number, and special character");
            return "redirect:/forgot-password/reset";
        }
        
        var userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User not found");
            return "redirect:/forgot-password";
        }
        
        userService.changePassword(userOpt.get().getId(), newPassword);
        log.info("Password reset successful for email: {}", email);
        
        redirectAttributes.addFlashAttribute("success", 
            "Password reset successfully! You can now login with your new password");
        return "redirect:/login";
    }
    
    @GetMapping("/resend-otp")
    public String resendOtp(@RequestParam String email,
                           RedirectAttributes redirectAttributes) {
        otpService.clearOtp(email);
        String otp = otpService.generateOtp(email);
        emailService.sendPasswordResetOtp(email, otp);
        
        log.info("Password reset OTP resent to: {}", email);
        redirectAttributes.addFlashAttribute("email", email);
        redirectAttributes.addFlashAttribute("success", "New OTP has been sent to your email");
        return "redirect:/forgot-password/verify-otp";
    }
}
