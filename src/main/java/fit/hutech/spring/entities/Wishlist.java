package fit.hutech.spring.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "wishlist", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "book_id"})
})
public class Wishlist {
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

    @Column(name = "added_date", nullable = false)
    @Builder.Default
    private Date addedDate = new Date();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Wishlist wishlist = (Wishlist) o;
        return getId() != null && Objects.equals(getId(), wishlist.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
