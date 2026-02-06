package fit.hutech.spring.services;

import fit.hutech.spring.entities.Book;
import fit.hutech.spring.entities.CartItem;
import fit.hutech.spring.entities.User;
import fit.hutech.spring.repositories.ICartItemRepository;
import fit.hutech.spring.repositories.IBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartItemService {
    private final ICartItemRepository cartItemRepository;
    private final IBookRepository bookRepository;
    
    public CartItem addToCart(User user, Book book, int quantity) {
        Optional<CartItem> existing = cartItemRepository.findByUserAndBook(user, book);
        
        if (existing.isPresent()) {
            CartItem cartItem = existing.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            return cartItemRepository.save(cartItem);
        }
        
        CartItem cartItem = CartItem.builder()
                .user(user)
                .book(book)
                .quantity(quantity)
                .build();
        
        return cartItemRepository.save(cartItem);
    }
    
    public CartItem addToCart(User user, Long bookId, int quantity) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
        return addToCart(user, book, quantity);
    }
    
    public void removeFromCart(User user, Book book) {
        cartItemRepository.deleteByUserAndBook(user, book);
    }
    
    public void removeFromCart(User user, Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
        removeFromCart(user, book);
    }
    
    public void removeById(Long id) {
        cartItemRepository.deleteById(id);
    }
    
    public CartItem updateQuantity(User user, Book book, int quantity) {
        CartItem cartItem = cartItemRepository.findByUserAndBook(user, book)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));
        
        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
            return null;
        }
        
        cartItem.setQuantity(quantity);
        return cartItemRepository.save(cartItem);
    }
    
    public CartItem updateQuantity(Long cartItemId, int quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));
        
        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
            return null;
        }
        
        cartItem.setQuantity(quantity);
        return cartItemRepository.save(cartItem);
    }
    
    public List<CartItem> getCartByUser(User user) {
        return cartItemRepository.findByUserOrderByAddedDateDesc(user);
    }
    
    public void clearCart(User user) {
        cartItemRepository.deleteAllByUser(user);
    }
    
    public Long getCartItemCount(User user) {
        return cartItemRepository.countByUser(user);
    }
    
    public Integer getTotalQuantity(User user) {
        Integer total = cartItemRepository.getTotalQuantityByUser(user);
        return total != null ? total : 0;
    }
    
    public Double getCartTotal(User user) {
        List<CartItem> cartItems = getCartByUser(user);
        return cartItems.stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();
    }
    
    public boolean isInCart(User user, Book book) {
        return cartItemRepository.existsByUserAndBook(user, book);
    }
}
