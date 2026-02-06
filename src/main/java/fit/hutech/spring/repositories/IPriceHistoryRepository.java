package fit.hutech.spring.repositories;

import fit.hutech.spring.entities.Book;
import fit.hutech.spring.entities.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    
    List<PriceHistory> findByBookOrderByChangeDateDesc(Book book);
    
    List<PriceHistory> findByBookIdOrderByChangeDateDesc(Long bookId);
    
    List<PriceHistory> findTop10ByOrderByChangeDateDesc();
}
