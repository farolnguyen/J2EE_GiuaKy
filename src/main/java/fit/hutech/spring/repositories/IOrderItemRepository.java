package fit.hutech.spring.repositories;

import fit.hutech.spring.entities.Book;
import fit.hutech.spring.entities.Order;
import fit.hutech.spring.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IOrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    List<OrderItem> findByOrder(Order order);
    
    List<OrderItem> findByBook(Book book);
    
    @Query("SELECT oi.book.id, SUM(oi.quantity) as totalSold FROM OrderItem oi " +
           "JOIN oi.order o WHERE o.status != 'CANCELLED' " +
           "GROUP BY oi.book.id ORDER BY totalSold DESC")
    List<Object[]> findTopSellingBooks();
    
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.book = ?1")
    Long getTotalSoldByBook(Book book);
    
    boolean existsByBook(Book book);
    
    @Query("SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END FROM OrderItem oi WHERE oi.book.id = ?1")
    boolean existsByBookId(Long bookId);
}
