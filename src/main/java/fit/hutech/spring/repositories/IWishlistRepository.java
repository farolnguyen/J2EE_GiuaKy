package fit.hutech.spring.repositories;

import fit.hutech.spring.entities.Book;
import fit.hutech.spring.entities.User;
import fit.hutech.spring.entities.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IWishlistRepository extends JpaRepository<Wishlist, Long> {
    
    List<Wishlist> findByUserOrderByAddedDateDesc(User user);
    
    Optional<Wishlist> findByUserAndBook(User user, Book book);
    
    boolean existsByUserAndBook(User user, Book book);
    
    void deleteByUserAndBook(User user, Book book);
    
    Long countByUser(User user);
}
