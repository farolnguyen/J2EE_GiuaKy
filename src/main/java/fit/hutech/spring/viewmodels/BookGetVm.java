package fit.hutech.spring.viewmodels;

import fit.hutech.spring.entities.Book;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record BookGetVm(
        Long id,
        String title,
        String author,
        Double price,
        String category,
        String imageUrl,
        String description,
        String publisher,
        Integer publicationYear,
        Integer stock,
        Double discount,
        Boolean featured,
        Double discountedPrice
) {
    public static BookGetVm from(@NotNull Book book) {
        Double originalPrice = book.getPrice() != null ? book.getPrice() : 0.0;
        Double discountPercent = book.getDiscount() != null ? book.getDiscount() : 0.0;
        Double calculatedDiscountedPrice = originalPrice * (1 - discountPercent / 100);
        
        return BookGetVm.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .price(book.getPrice())
                .category(book.getCategory() != null ? book.getCategory().getName() : null)
                .imageUrl(book.getImageUrl())
                .description(book.getDescription())
                .publisher(book.getPublisher())
                .publicationYear(book.getPublicationYear())
                .stock(book.getStock())
                .discount(book.getDiscount())
                .featured(book.getFeatured())
                .discountedPrice(calculatedDiscountedPrice)
                .build();
    }
}
