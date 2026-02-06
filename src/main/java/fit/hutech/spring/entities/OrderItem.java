package fit.hutech.spring.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.Hibernate;

import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", referencedColumnName = "id", nullable = false)
    @ToString.Exclude
    @NotNull(message = "Order is required")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", referencedColumnName = "id", nullable = false)
    @ToString.Exclude
    @NotNull(message = "Book is required")
    private Book book;

    @Column(name = "quantity", nullable = false)
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    @Column(name = "price_at_purchase", nullable = false)
    @Positive(message = "Price must be positive")
    private Double priceAtPurchase;

    @Column(name = "discount_at_purchase")
    @Builder.Default
    private Double discountAtPurchase = 0.0;

    public Double getSubtotal() {
        double discountedPrice = priceAtPurchase * (1 - discountAtPurchase / 100);
        return discountedPrice * quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        OrderItem orderItem = (OrderItem) o;
        return getId() != null && Objects.equals(getId(), orderItem.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
