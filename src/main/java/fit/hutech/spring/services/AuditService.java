package fit.hutech.spring.services;

import fit.hutech.spring.entities.AuditLog;
import fit.hutech.spring.entities.User;
import fit.hutech.spring.repositories.IAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {
    
    private final IAuditLogRepository auditLogRepository;
    
    // Action constants
    public static final String ACTION_LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String ACTION_LOGIN_FAILED = "LOGIN_FAILED";
    public static final String ACTION_ACCOUNT_LOCKED = "ACCOUNT_LOCKED";
    public static final String ACTION_ACCOUNT_UNLOCKED = "ACCOUNT_UNLOCKED";
    public static final String ACTION_USER_ENABLED = "USER_ENABLED";
    public static final String ACTION_USER_DISABLED = "USER_DISABLED";
    public static final String ACTION_PASSWORD_CHANGED = "PASSWORD_CHANGED";
    public static final String ACTION_ROLE_CHANGED = "ROLE_CHANGED";
    public static final String ACTION_BOOK_CREATED = "BOOK_CREATED";
    public static final String ACTION_BOOK_UPDATED = "BOOK_UPDATED";
    public static final String ACTION_BOOK_DELETED = "BOOK_DELETED";
    public static final String ACTION_BOOK_DELETE_BLOCKED = "BOOK_DELETE_BLOCKED";
    public static final String ACTION_BOOK_ENABLED = "BOOK_ENABLED";
    public static final String ACTION_BOOK_DISABLED = "BOOK_DISABLED";
    public static final String ACTION_CATEGORY_DELETED = "CATEGORY_DELETED";
    public static final String ACTION_CATEGORY_DELETE_BLOCKED = "CATEGORY_DELETE_BLOCKED";
    public static final String ACTION_ORDER_STATUS_CHANGED = "ORDER_STATUS_CHANGED";
    public static final String ACTION_ORDER_CANCELLED = "ORDER_CANCELLED";
    public static final String ACTION_STOCK_UPDATED = "STOCK_UPDATED";
    public static final String ACTION_PRICE_CHANGED = "PRICE_CHANGED";
    
    @Transactional
    public void log(User performedBy, String action, String entityType, Long entityId, String details) {
        log(performedBy, action, entityType, entityId, details, null, true);
    }
    
    @Transactional
    public void log(User performedBy, String action, String entityType, Long entityId, 
                   String details, String ipAddress, boolean success) {
        AuditLog auditLog = AuditLog.builder()
                .performedBy(performedBy)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .ipAddress(ipAddress)
                .success(success)
                .build();
        
        auditLogRepository.save(auditLog);
        log.info("Audit: {} by {} on {}:{} - {}", 
                action, 
                performedBy != null ? performedBy.getUsername() : "SYSTEM",
                entityType, 
                entityId, 
                details);
    }
    
    @Transactional
    public void logAction(User performedBy, String action, String details) {
        log(performedBy, action, null, null, details, null, true);
    }
    
    @Transactional
    public void logFailedAction(User performedBy, String action, String entityType, 
                                Long entityId, String details) {
        log(performedBy, action, entityType, entityId, details, null, false);
    }
    
    public List<AuditLog> getRecentLogs(int limit) {
        return auditLogRepository.findTop50ByOrderByTimestampDesc();
    }
    
    public Page<AuditLog> getAllLogs(int page, int size) {
        return auditLogRepository.findAllByOrderByTimestampDesc(PageRequest.of(page, size));
    }
    
    public List<AuditLog> getLogsByEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId);
    }
    
    public List<AuditLog> getLogsByUser(User user) {
        return auditLogRepository.findByPerformedByOrderByTimestampDesc(user);
    }
    
    public List<AuditLog> getLogsByAction(String action) {
        return auditLogRepository.findByActionOrderByTimestampDesc(action);
    }
}
