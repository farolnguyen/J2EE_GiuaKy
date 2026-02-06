package fit.hutech.spring.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.Hibernate;

import java.util.Date;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "cart_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "book_id"})
})
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    @ToString.Exclude
    @NotNull(message = "User is required")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", referencedColumnName = "id", nullable = false)
    @ToString.Exclude
    @NotNull(message = "Book is required")
    private Book book;

    @Column(name = "quantity", nullable = false)
    @Positive(message = "Quantity must be positive")
    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "added_date", nullable = false)
    @Builder.Default
    private Date addedDate = new Date();

    public Double getSubtotal() {
        if (book == null || book.getPrice() == null) return 0.0;
        double price = book.getPrice();
        double discount = book.getDiscount() != null ? book.getDiscount() : 0.0;
        double discountedPrice = price * (1 - discount / 100);
        return discountedPrice * quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CartItem cartItem = (CartItem) o;
        return getId() != null && Objects.equals(getId(), cartItem.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
