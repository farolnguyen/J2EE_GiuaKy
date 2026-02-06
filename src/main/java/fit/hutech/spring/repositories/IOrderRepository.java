package fit.hutech.spring.repositories;

import fit.hutech.spring.entities.Order;
import fit.hutech.spring.entities.OrderStatus;
import fit.hutech.spring.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface IOrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByUserOrderByOrderDateDesc(User user);
    
    List<Order> findByStatus(OrderStatus status);
    
    Optional<Order> findByOrderNumber(String orderNumber);
    
    Page<Order> findAllByOrderByOrderDateDesc(Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN ?1 AND ?2")
    List<Order> findByDateRange(Date startDate, Date endDate);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = ?1")
    Long countByStatus(OrderStatus status);
    
    @Query("SELECT SUM(o.total) FROM Order o WHERE o.status != 'CANCELLED'")
    Double getTotalRevenue();
    
    @Query("SELECT SUM(o.total) FROM Order o WHERE o.orderDate BETWEEN ?1 AND ?2 AND o.status != 'CANCELLED'")
    Double getRevenueByDateRange(Date startDate, Date endDate);
}
