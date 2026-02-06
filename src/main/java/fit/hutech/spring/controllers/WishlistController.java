package fit.hutech.spring.controllers;

import fit.hutech.spring.entities.User;
import fit.hutech.spring.services.AuthenticationService;
import fit.hutech.spring.services.BookService;
import fit.hutech.spring.services.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {
    
    private final WishlistService wishlistService;
    private final BookService bookService;
    private final AuthenticationService authService;
    
    @GetMapping
    public String viewWishlist(Model model) {
        Optional<User> userOpt = authService.getCurrentUser();
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOpt.get();
        
        model.addAttribute("wishlistItems", wishlistService.getWishlistByUser(user));
        model.addAttribute("wishlistCount", wishlistService.getWishlistCount(user));
        return "wishlist/index";
    }
    
    @GetMapping("/add/{bookId}")
    public String addToWishlist(@PathVariable Long bookId, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = authService.getCurrentUser();
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOpt.get();
        
        var book = bookService.getBookById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
        
        wishlistService.addToWishlist(user, book);
        redirectAttributes.addFlashAttribute("success", "Added to wishlist!");
        return "redirect:/books/" + bookId;
    }
    
    @GetMapping("/remove/{bookId}")
    public String removeFromWishlist(@PathVariable Long bookId, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = authService.getCurrentUser();
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOpt.get();
        
        var book = bookService.getBookById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
        
        wishlistService.removeFromWishlist(user, book);
        redirectAttributes.addFlashAttribute("success", "Removed from wishlist");
        return "redirect:/wishlist";
    }
    
    @PostMapping("/move-to-cart/{bookId}")
    public String moveToCart(@PathVariable Long bookId, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = authService.getCurrentUser();
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        User user = userOpt.get();
        
        var book = bookService.getBookById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
        
        wishlistService.removeFromWishlist(user, book);
        redirectAttributes.addFlashAttribute("success", "Moved to cart! Add it from the books page.");
        return "redirect:/books/" + bookId;
    }
}
