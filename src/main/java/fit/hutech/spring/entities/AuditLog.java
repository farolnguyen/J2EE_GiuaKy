package fit.hutech.spring.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "audit_log")
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by")
    @ToString.Exclude
    private User performedBy;
    
    @Column(name = "action", nullable = false, length = 100)
    private String action;
    
    @Column(name = "entity_type", length = 50)
    private String entityType;
    
    @Column(name = "entity_id")
    private Long entityId;
    
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;
    
    @Column(name = "timestamp", nullable = false)
    @Builder.Default
    private Date timestamp = new Date();
    
    @Column(name = "ip_address", length = 50)
    private String ipAddress;
    
    @Column(name = "success")
    @Builder.Default
    private Boolean success = true;
}
