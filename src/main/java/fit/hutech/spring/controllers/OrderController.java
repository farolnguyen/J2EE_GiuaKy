package fit.hutech.spring.controllers;

import fit.hutech.spring.entities.User;
import fit.hutech.spring.services.AuthenticationService;
import fit.hutech.spring.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    private final AuthenticationService authService;
    
    @GetMapping
    public String orderHistory(Model model) {
        Optional<User> userOpt = authService.getCurrentUser();
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOpt.get();
        
        model.addAttribute("orders", orderService.findByUser(user));
        return "orders/index";
    }
    
    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        Optional<User> userOpt = authService.getCurrentUser();
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOpt.get();
        
        var order = orderService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        
        // Security check - users can only view their own orders
        if (!order.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Access denied");
        }
        
        model.addAttribute("order", order);
        return "orders/detail";
    }
    
    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = authService.getCurrentUser();
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOpt.get();
        
        try {
            orderService.cancelOrderWithStockRestore(id, user.getId());
            redirectAttributes.addFlashAttribute("success", "Order cancelled successfully. Stock has been restored.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/orders/" + id;
    }
}
