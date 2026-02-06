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
@Table(name = "price_history")
public class PriceHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    @ToString.Exclude
    private Book book;
    
    @Column(name = "old_price", nullable = false)
    private Double oldPrice;
    
    @Column(name = "new_price", nullable = false)
    private Double newPrice;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    @ToString.Exclude
    private User changedBy;
    
    @Column(name = "change_date", nullable = false)
    @Builder.Default
    private Date changeDate = new Date();
    
    @Column(name = "reason", length = 255)
    private String reason;
}
