package fit.hutech.spring.repositories;

import fit.hutech.spring.entities.AuditLog;
import fit.hutech.spring.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface IAuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    List<AuditLog> findByPerformedByOrderByTimestampDesc(User user);
    
    List<AuditLog> findByActionOrderByTimestampDesc(String action);
    
    List<AuditLog> findByEntityTypeOrderByTimestampDesc(String entityType);
    
    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, Long entityId);
    
    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);
    
    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(Date start, Date end);
    
    List<AuditLog> findTop50ByOrderByTimestampDesc();
}
