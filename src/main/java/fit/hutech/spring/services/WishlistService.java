package fit.hutech.spring.services;

import fit.hutech.spring.entities.Book;
import fit.hutech.spring.entities.User;
import fit.hutech.spring.entities.Wishlist;
import fit.hutech.spring.repositories.IWishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class WishlistService {
    private final IWishlistRepository wishlistRepository;
    
    public Wishlist addToWishlist(User user, Book book) {
        Optional<Wishlist> existing = wishlistRepository.findByUserAndBook(user, book);
        if (existing.isPresent()) {
            return existing.get();
        }
        
        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .book(book)
                .build();
        
        return wishlistRepository.save(wishlist);
    }
    
    public void removeFromWishlist(User user, Book book) {
        wishlistRepository.deleteByUserAndBook(user, book);
    }
    
    public void removeById(Long id) {
        wishlistRepository.deleteById(id);
    }
    
    public List<Wishlist> getWishlistByUser(User user) {
        return wishlistRepository.findByUserOrderByAddedDateDesc(user);
    }
    
    public boolean isInWishlist(User user, Book book) {
        return wishlistRepository.existsByUserAndBook(user, book);
    }
    
    public Long getWishlistCount(User user) {
        return wishlistRepository.countByUser(user);
    }
}
