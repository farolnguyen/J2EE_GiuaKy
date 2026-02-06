package fit.hutech.spring.repositories;

import fit.hutech.spring.entities.Book;
import fit.hutech.spring.entities.CartItem;
import fit.hutech.spring.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ICartItemRepository extends JpaRepository<CartItem, Long> {
    
    List<CartItem> findByUserOrderByAddedDateDesc(User user);
    
    Optional<CartItem> findByUserAndBook(User user, Book book);
    
    boolean existsByUserAndBook(User user, Book book);
    
    void deleteByUserAndBook(User user, Book book);
    
    void deleteAllByUser(User user);
    
    Long countByUser(User user);
    
    @Query("SELECT SUM(ci.quantity) FROM CartItem ci WHERE ci.user = ?1")
    Integer getTotalQuantityByUser(User user);
}
