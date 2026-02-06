package fit.hutech.spring.controllers;

import fit.hutech.spring.entities.User;
import fit.hutech.spring.services.BookService;
import fit.hutech.spring.services.CartService;
import fit.hutech.spring.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final UserService userService;
    private final BookService bookService;

    private Optional<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }
        
        Object principal = auth.getPrincipal();
        
        if (principal instanceof UserDetails) {
            return userService.findByUsername(((UserDetails) principal).getUsername());
        } else if (principal instanceof OAuth2User) {
            String email = ((OAuth2User) principal).getAttribute("email");
            if (email == null) {
                String login = ((OAuth2User) principal).getAttribute("login");
                if (login != null) {
                    email = login + "@github.oauth";
                }
            }
            return userService.findByEmail(email);
        }
        
        return Optional.empty();
    }

    @GetMapping
    public String showCart(Model model) {
        Optional<User> userOpt = getCurrentUser();
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOpt.get();
        
        model.addAttribute("cartItems", cartService.getCartItems(user));
        model.addAttribute("totalPrice", cartService.getTotalPrice(user));
        model.addAttribute("totalQuantity", cartService.getTotalQuantity(user));
        return "book/cart";
    }

    @GetMapping("/remove/{id}")
    public String removeFromCart(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = getCurrentUser();
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOpt.get();
        
        cartService.removeFromCart(user, id);
        redirectAttributes.addFlashAttribute("success", "Item removed from cart");
        return "redirect:/cart";
    }

    @GetMapping("/update/{id}/{quantity}")
    public String updateCart(@PathVariable Long id,
                            @PathVariable int quantity,
                            RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = getCurrentUser();
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOpt.get();
        
        try {
            cartService.updateCartItemQuantity(user, id, quantity);
            redirectAttributes.addFlashAttribute("success", "Cart updated");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    @GetMapping("/clear")
    public String clearCart(RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = getCurrentUser();
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOpt.get();
        
        cartService.clearCart(user);
        redirectAttributes.addFlashAttribute("success", "Cart cleared");
        return "redirect:/cart";
    }

    @GetMapping("/add/{bookId}")
    public String addToCart(@PathVariable Long bookId,
                           HttpServletRequest request,
                           RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = getCurrentUser();
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOpt.get();
        
        var book = bookService.getBookById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
        
        try {
            cartService.addToCart(user, book, 1);
            redirectAttributes.addFlashAttribute("success", "Successfully added to cart!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/books");
    }

    @PostMapping("/add")
    public String addToCartPost(@RequestParam Long id,
                                @RequestParam(defaultValue = "1") int quantity,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = getCurrentUser();
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOpt.get();
        
        var book = bookService.getBookById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
        
        try {
            cartService.addToCart(user, book, quantity);
            redirectAttributes.addFlashAttribute("success", "Successfully added " + quantity + " item(s) to cart!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/books");
    }
}