package fit.hutech.spring.controllers;

import fit.hutech.spring.entities.User;
import fit.hutech.spring.services.AuthenticationService;
import fit.hutech.spring.services.CartService;
import fit.hutech.spring.services.OrderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {
    
    private final CartService cartService;
    private final OrderService orderService;
    private final AuthenticationService authService;
    
    @GetMapping
    public String checkoutShipping(Model model) {
        Optional<User> userOpt = authService.getCurrentUser();
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOpt.get();
        
        var cartItems = cartService.getCartItems(user);
        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }
        
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", cartService.getTotalPrice(user));
        model.addAttribute("totalQuantity", cartService.getTotalQuantity(user));
        model.addAttribute("user", user);
        model.addAttribute("step", "shipping");
        
        return "checkout/shipping";
    }
    
    @PostMapping("/payment")
    public String checkoutPayment(HttpSession session,
                                 @RequestParam String shippingAddress,
                                 @RequestParam String shippingCity,
                                 @RequestParam String shippingPostalCode,
                                 @RequestParam String shippingCountry,
                                 @RequestParam(required = false) String notes,
                                 Model model) {
        Optional<User> userOpt = authService.getCurrentUser();
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOpt.get();
        
        var cartItems = cartService.getCartItems(user);
        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }
        
        session.setAttribute("checkout_shipping_address", shippingAddress);
        session.setAttribute("checkout_shipping_city", shippingCity);
        session.setAttribute("checkout_shipping_postal", shippingPostalCode);
        session.setAttribute("checkout_shipping_country", shippingCountry);
        session.setAttribute("checkout_notes", notes);
        
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", cartService.getTotalPrice(user));
        model.addAttribute("totalQuantity", cartService.getTotalQuantity(user));
        model.addAttribute("shippingAddress", shippingAddress);
        model.addAttribute("shippingCity", shippingCity);
        model.addAttribute("shippingPostalCode", shippingPostalCode);
        model.addAttribute("shippingCountry", shippingCountry);
        model.addAttribute("step", "payment");
        
        return "checkout/payment";
    }
    
    @PostMapping("/confirm")
    public String checkoutConfirm(HttpSession session,
                                 @RequestParam String paymentMethod,
                                 Model model) {
        Optional<User> userOpt = authService.getCurrentUser();
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOpt.get();
        
        var cartItems = cartService.getCartItems(user);
        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }
        
        String shippingAddress = (String) session.getAttribute("checkout_shipping_address");
        String shippingCity = (String) session.getAttribute("checkout_shipping_city");
        String shippingPostalCode = (String) session.getAttribute("checkout_shipping_postal");
        String shippingCountry = (String) session.getAttribute("checkout_shipping_country");
        String notes = (String) session.getAttribute("checkout_notes");
        
        session.setAttribute("checkout_payment_method", paymentMethod);
        
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", cartService.getTotalPrice(user));
        model.addAttribute("totalQuantity", cartService.getTotalQuantity(user));
        model.addAttribute("shippingAddress", shippingAddress);
        model.addAttribute("shippingCity", shippingCity);
        model.addAttribute("shippingPostalCode", shippingPostalCode);
        model.addAttribute("shippingCountry", shippingCountry);
        model.addAttribute("notes", notes);
        model.addAttribute("paymentMethod", paymentMethod);
        model.addAttribute("step", "confirm");
        
        return "checkout/confirm";
    }
    
    @PostMapping("/place-order")
    public String placeOrder(HttpSession session, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = authService.getCurrentUser();
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOpt.get();
        
        var cartItems = cartService.getCartItems(user);
        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }
        
        // Get shipping info from session
        String shippingAddress = (String) session.getAttribute("checkout_shipping_address");
        String shippingCity = (String) session.getAttribute("checkout_shipping_city");
        String shippingPostalCode = (String) session.getAttribute("checkout_shipping_postal");
        String shippingCountry = (String) session.getAttribute("checkout_shipping_country");
        String notes = (String) session.getAttribute("checkout_notes");
        
        // Create order using OrderService
        orderService.createOrder(user, cartItems, shippingAddress, shippingCity, 
                                shippingPostalCode, shippingCountry, notes);
        
        // Clear cart
        cartService.clearCart(user);
        
        // Clear checkout session data
        session.removeAttribute("checkout_shipping_address");
        session.removeAttribute("checkout_shipping_city");
        session.removeAttribute("checkout_shipping_postal");
        session.removeAttribute("checkout_shipping_country");
        session.removeAttribute("checkout_notes");
        session.removeAttribute("checkout_payment_method");
        
        redirectAttributes.addFlashAttribute("success", "Order placed successfully! Thank you for your purchase.");
        return "redirect:/checkout/success";
    }
    
    @GetMapping("/success")
    public String orderSuccess(Model model) {
        return "checkout/success";
    }
}
