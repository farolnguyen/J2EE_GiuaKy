package fit.hutech.spring.services;

import fit.hutech.spring.entities.Book;
import fit.hutech.spring.entities.CartItem;
import fit.hutech.spring.entities.Invoice;
import fit.hutech.spring.entities.ItemInvoice;
import fit.hutech.spring.entities.User;
import fit.hutech.spring.repositories.IBookRepository;
import fit.hutech.spring.repositories.ICartItemRepository;
import fit.hutech.spring.repositories.IInvoiceRepository;
import fit.hutech.spring.repositories.IItemInvoiceRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {
    private final ICartItemRepository cartItemRepository;
    private final IBookRepository bookRepository;
    private final IInvoiceRepository invoiceRepository;
    private final IItemInvoiceRepository itemInvoiceRepository;

    public List<CartItem> getCartItems(@NotNull User user) {
        return cartItemRepository.findByUserOrderByAddedDateDesc(user);
    }

    public void addToCart(@NotNull User user, @NotNull Book book, int quantity) {
        // Check stock availability
        if (quantity > book.getStock()) {
            throw new IllegalStateException("Cannot add " + quantity + " items. Only " + book.getStock() + " available in stock.");
        }
        
        var existingItem = cartItemRepository.findByUserAndBook(user, book);
        if (existingItem.isPresent()) {
            var item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;
            
            // Validate total quantity doesn't exceed stock
            if (newQuantity > book.getStock()) {
                throw new IllegalStateException("Cannot add more items. Maximum available: " + book.getStock() + ". You already have " + item.getQuantity() + " in cart.");
            }
            
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            var cartItem = CartItem.builder()
                    .user(user)
                    .book(book)
                    .quantity(quantity)
                    .addedDate(new Date())
                    .build();
            cartItemRepository.save(cartItem);
        }
    }

    public void removeFromCart(@NotNull User user, @NotNull Long bookId) {
        var book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
        cartItemRepository.deleteByUserAndBook(user, book);
    }

    public void updateCartItemQuantity(@NotNull User user, @NotNull Long bookId, int quantity) {
        var book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
        var cartItem = cartItemRepository.findByUserAndBook(user, book)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));
        
        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            // Validate quantity doesn't exceed stock
            if (quantity > book.getStock()) {
                throw new IllegalStateException("Cannot update quantity to " + quantity + ". Only " + book.getStock() + " available in stock.");
            }
            
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }
    }

    public void clearCart(@NotNull User user) {
        cartItemRepository.deleteAllByUser(user);
    }

    public int getTotalQuantity(@NotNull User user) {
        Integer total = cartItemRepository.getTotalQuantityByUser(user);
        return total != null ? total : 0;
    }

    public double getTotalPrice(@NotNull User user) {
        return getCartItems(user).stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();
    }

    public Long getCartItemCount(@NotNull User user) {
        return cartItemRepository.countByUser(user);
    }

    public void saveCart(@NotNull User user) {
        var cartItems = getCartItems(user);
        if (cartItems.isEmpty())
            return;
        var invoice = new Invoice();
        invoice.setInvoiceDate(new Date(new Date().getTime()));
        invoice.setPrice(getTotalPrice(user));
        invoiceRepository.save(invoice);
        cartItems.forEach(item -> {
            var items = new ItemInvoice();
            items.setInvoice(invoice);
            items.setQuantity(item.getQuantity());
            items.setBook(item.getBook());
            itemInvoiceRepository.save(items);
        });
        clearCart(user);
    }
}