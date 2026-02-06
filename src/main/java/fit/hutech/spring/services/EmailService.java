package fit.hutech.spring.services;

import fit.hutech.spring.entities.Order;
import fit.hutech.spring.entities.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:noreply@storystation.com}")
    private String fromEmail;

    @Value("${app.name:Story Station}")
    private String appName;

    @Async
    public void sendWelcomeEmail(User user) {
        try {
            Context context = new Context();
            context.setVariable("username", user.getUsername());
            context.setVariable("appName", appName);

            String htmlContent = templateEngine.process("email/welcome", context);
            sendHtmlEmail(user.getEmail(), "Welcome to " + appName + "!", htmlContent);
            log.info("Welcome email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    @Async
    public void sendOrderConfirmationEmail(Order order) {
        try {
            Context context = new Context();
            context.setVariable("order", order);
            context.setVariable("user", order.getUser());
            context.setVariable("appName", appName);

            String htmlContent = templateEngine.process("email/order-confirmation", context);
            sendHtmlEmail(order.getUser().getEmail(), 
                    appName + " - Order Confirmation #" + order.getOrderNumber(), 
                    htmlContent);
            log.info("Order confirmation email sent for order: {}", order.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to send order confirmation email for order {}: {}", 
                    order.getOrderNumber(), e.getMessage());
        }
    }

    @Async
    public void sendOrderStatusUpdateEmail(Order order) {
        try {
            Context context = new Context();
            context.setVariable("order", order);
            context.setVariable("user", order.getUser());
            context.setVariable("status", order.getStatus().getDisplayName());
            context.setVariable("appName", appName);

            String htmlContent = templateEngine.process("email/order-status-update", context);
            sendHtmlEmail(order.getUser().getEmail(),
                    appName + " - Order #" + order.getOrderNumber() + " Status Update",
                    htmlContent);
            log.info("Order status update email sent for order: {}", order.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to send order status email for order {}: {}", 
                    order.getOrderNumber(), e.getMessage());
        }
    }

    @Async
    public void sendShippingNotificationEmail(Order order) {
        try {
            Context context = new Context();
            context.setVariable("order", order);
            context.setVariable("user", order.getUser());
            context.setVariable("appName", appName);

            String htmlContent = templateEngine.process("email/shipping-notification", context);
            sendHtmlEmail(order.getUser().getEmail(),
                    appName + " - Your Order #" + order.getOrderNumber() + " Has Shipped!",
                    htmlContent);
            log.info("Shipping notification email sent for order: {}", order.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to send shipping notification email for order {}: {}", 
                    order.getOrderNumber(), e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetEmail(User user, String resetToken) {
        try {
            Context context = new Context();
            context.setVariable("username", user.getUsername());
            context.setVariable("resetToken", resetToken);
            context.setVariable("appName", appName);

            String htmlContent = templateEngine.process("email/password-reset", context);
            sendHtmlEmail(user.getEmail(), appName + " - Password Reset Request", htmlContent);
            log.info("Password reset email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    @Async
    public void sendAccountLockedEmail(User user) {
        try {
            String htmlContent = String.format(
                "<html><body>" +
                "<h2>Account Locked - %s</h2>" +
                "<p>Dear %s,</p>" +
                "<p>Your account has been locked due to <strong>5 failed login attempts</strong>.</p>" +
                "<p>Your account will be automatically unlocked after <strong>24 hours</strong>.</p>" +
                "<p>If you did not attempt to login, please contact our support team immediately.</p>" +
                "<br><p>Best regards,<br>%s Team</p>" +
                "</body></html>",
                appName, user.getUsername(), appName
            );
            sendHtmlEmail(user.getEmail(), appName + " - Account Locked", htmlContent);
            log.info("Account locked email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send account locked email to {}: {}", user.getEmail(), e.getMessage());
        }
    }
    
    @Async
    public void sendLowStockAlertEmail(String adminEmail, String bookTitle, int currentStock) {
        try {
            String htmlContent = String.format(
                "<html><body>" +
                "<h2>Low Stock Alert - %s</h2>" +
                "<p>The following book has low stock:</p>" +
                "<p><strong>Book:</strong> %s</p>" +
                "<p><strong>Current Stock:</strong> %d</p>" +
                "<p>Please consider restocking this item.</p>" +
                "<br><p>Best regards,<br>%s Inventory System</p>" +
                "</body></html>",
                appName, bookTitle, currentStock, appName
            );
            sendHtmlEmail(adminEmail, appName + " - Low Stock Alert: " + bookTitle, htmlContent);
            log.info("Low stock alert email sent for book: {}", bookTitle);
        } catch (Exception e) {
            log.error("Failed to send low stock alert email: {}", e.getMessage());
        }
    }
    
    @Async
    public void sendOrderConfirmationWithInvoice(Order order, byte[] pdfInvoice) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            Context context = new Context();
            context.setVariable("order", order);
            context.setVariable("user", order.getUser());
            context.setVariable("appName", appName);
            
            String htmlContent = templateEngine.process("email/order-confirmation", context);
            
            helper.setFrom(fromEmail);
            helper.setTo(order.getUser().getEmail());
            helper.setSubject(appName + " - Order Confirmation #" + order.getOrderNumber());
            helper.setText(htmlContent, true);
            
            // Attach PDF invoice
            if (pdfInvoice != null) {
                helper.addAttachment("Invoice_" + order.getOrderNumber() + ".pdf", 
                        () -> new java.io.ByteArrayInputStream(pdfInvoice));
            }
            
            mailSender.send(message);
            log.info("Order confirmation with invoice sent for order: {}", order.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to send order confirmation with invoice for order {}: {}", 
                    order.getOrderNumber(), e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetOtp(String email, String otp) {
        try {
            Context context = new Context();
            context.setVariable("otp", otp);
            context.setVariable("appName", appName);
            context.setVariable("validityMinutes", 10);

            String htmlContent = templateEngine.process("email/password-reset-otp", context);
            sendHtmlEmail(email, "Password Reset OTP - " + appName, htmlContent);
            log.info("Password reset OTP sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset OTP to {}: {}", email, e.getMessage());
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}
