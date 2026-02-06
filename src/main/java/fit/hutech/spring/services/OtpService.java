package fit.hutech.spring.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class OtpService {
    
    private static class OtpData {
        String otp;
        LocalDateTime expiryTime;
        int attempts;
        
        OtpData(String otp, LocalDateTime expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
            this.attempts = 0;
        }
    }
    
    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();
    private static final int OTP_LENGTH = 6;
    private static final int OTP_VALIDITY_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 3;
    private final SecureRandom random = new SecureRandom();
    
    public String generateOtp(String email) {
        String otp = String.format("%06d", random.nextInt(1000000));
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES);
        
        otpStorage.put(email.toLowerCase(), new OtpData(otp, expiryTime));
        log.info("OTP generated for email: {} (expires at {})", email, expiryTime);
        
        return otp;
    }
    
    public boolean validateOtp(String email, String otp) {
        email = email.toLowerCase();
        OtpData otpData = otpStorage.get(email);
        
        if (otpData == null) {
            log.warn("No OTP found for email: {}", email);
            return false;
        }
        
        if (LocalDateTime.now().isAfter(otpData.expiryTime)) {
            log.warn("OTP expired for email: {}", email);
            otpStorage.remove(email);
            return false;
        }
        
        if (otpData.attempts >= MAX_ATTEMPTS) {
            log.warn("Max OTP attempts exceeded for email: {}", email);
            otpStorage.remove(email);
            return false;
        }
        
        otpData.attempts++;
        
        if (!otpData.otp.equals(otp)) {
            log.warn("Invalid OTP for email: {} (attempt {}/{})", email, otpData.attempts, MAX_ATTEMPTS);
            return false;
        }
        
        log.info("OTP validated successfully for email: {}", email);
        otpStorage.remove(email);
        return true;
    }
    
    public void clearOtp(String email) {
        otpStorage.remove(email.toLowerCase());
        log.info("OTP cleared for email: {}", email);
    }
    
    public boolean hasValidOtp(String email) {
        email = email.toLowerCase();
        OtpData otpData = otpStorage.get(email);
        return otpData != null && LocalDateTime.now().isBefore(otpData.expiryTime);
    }
    
    public int getRemainingAttempts(String email) {
        email = email.toLowerCase();
        OtpData otpData = otpStorage.get(email);
        if (otpData == null) {
            return 0;
        }
        return Math.max(0, MAX_ATTEMPTS - otpData.attempts);
    }
}
