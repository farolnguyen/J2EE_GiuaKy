package fit.hutech.spring.controllers;

import fit.hutech.spring.entities.Order;
import fit.hutech.spring.entities.OrderStatus;
import fit.hutech.spring.services.AuthenticationService;
import fit.hutech.spring.services.OrderService;
import fit.hutech.spring.services.PdfService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/invoice")
@RequiredArgsConstructor
@Slf4j
public class InvoiceController {
    
    private final OrderService orderService;
    private final PdfService pdfService;
    private final AuthenticationService authService;
    
    @GetMapping("/download/{orderId}")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long orderId) {
        // Get current user
        var userOpt = authService.getCurrentUser();
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        
        // Get order
        Order order = orderService.findById(orderId)
                .orElse(null);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Check if user owns this order or is admin
        var user = userOpt.get();
        boolean isAdmin = user.getRoles().stream().anyMatch(r -> "ADMIN".equals(r.getName()));
        boolean isOwner = order.getUser().getId().equals(user.getId());
        
        if (!isAdmin && !isOwner) {
            return ResponseEntity.status(403).build();
        }
        
        // Check if order is delivered
        if (order.getStatus() != OrderStatus.DELIVERED) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            byte[] pdfBytes = pdfService.generateInvoice(order);
            
            String filename = "Invoice_" + order.getOrderNumber() + ".pdf";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.length)
                    .body(pdfBytes);
                    
        } catch (Exception e) {
            log.error("Error generating invoice PDF for order {}", orderId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
