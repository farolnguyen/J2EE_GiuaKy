package fit.hutech.spring.entities;

import fit.hutech.spring.validators.annotations.ValidCategoryId;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.Hibernate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "book")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", length = 100, nullable = false)
    @Size(min = 1, max = 100, message = "Title must be between 1 and 100 characters")
    @NotBlank(message = "Title must not be blank")
    private String title;

    @Column(name = "author", length = 100, nullable = false)
    @Size(min = 1, max = 100, message = "Author must be between 1 and 100 characters")
    @NotBlank(message = "Author must not be blank")
    private String author;

    @Column(name = "price")
    @Positive(message = "Price must be greater than 0")
    private Double price;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "publisher", length = 100)
    private String publisher;

    @Column(name = "publication_year")
    private Integer publicationYear;

    @Column(name = "stock")
    @Builder.Default
    private Integer stock = 0;

    @Column(name = "discount")
    @Builder.Default
    private Double discount = 0.0;

    @Column(name = "featured")
    @Builder.Default
    private Boolean featured = false;

    @Column(name = "enabled")
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "stock_alert_threshold")
    @Builder.Default
    private Integer stockAlertThreshold = 10;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    @ValidCategoryId
    @ToString.Exclude
    private Category category;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    @ToString.Exclude
    @Builder.Default
    private List<ItemInvoice> itemInvoices = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o))
            return false;
        Book book = (Book) o;
        return getId() != null && Objects.equals(getId(),
                book.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}