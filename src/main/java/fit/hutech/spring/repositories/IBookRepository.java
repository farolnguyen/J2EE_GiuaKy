package fit.hutech.spring.repositories;

import fit.hutech.spring.entities.Book;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IBookRepository extends 
        PagingAndSortingRepository<Book, Long>, JpaRepository<Book, Long> {
    
    default List<Book> findAllBooks(Integer pageNo, Integer pageSize, String sortBy) {
        return findAll(PageRequest.of(pageNo, pageSize, Sort.by(sortBy))).getContent();
    }
    
    @Query("""
            SELECT b FROM Book b
            WHERE b.enabled = true
            AND (b.title LIKE %?1%
            OR b.author LIKE %?1%
            OR b.category.name LIKE %?1%)
            """)
    List<Book> searchBook(String keyword);
    
    List<Book> findByFeaturedTrue();
    
    List<Book> findByStockGreaterThan(Integer stock);
    
    List<Book> findByStockLessThanEqual(Integer stock);
    
    @Query("SELECT b FROM Book b WHERE b.enabled = true AND b.featured = true ORDER BY b.id DESC LIMIT 8")
    List<Book> findFeaturedBooks();
    
    @Query("SELECT b FROM Book b WHERE b.enabled = true AND b.category.id = ?1")
    List<Book> findByCategory(Long categoryId);
    
    @Query("SELECT b FROM Book b WHERE b.enabled = true AND b.price BETWEEN ?1 AND ?2")
    List<Book> findByPriceRange(Double minPrice, Double maxPrice);
    
    @Query("SELECT b FROM Book b WHERE b.publicationYear = ?1")
    List<Book> findByPublicationYear(Integer year);
    
    @Query("""
            SELECT b FROM Book b
            WHERE b.enabled = true
            AND (:keyword IS NULL OR b.title LIKE %:keyword% OR b.author LIKE %:keyword%)
            AND (:categoryId IS NULL OR b.category.id = :categoryId)
            AND (:minPrice IS NULL OR b.price >= :minPrice)
            AND (:maxPrice IS NULL OR b.price <= :maxPrice)
            AND (:inStock IS NULL OR (:inStock = true AND b.stock > 0) OR (:inStock = false))
            ORDER BY 
                CASE WHEN :sortBy = 'priceAsc' THEN b.price END ASC,
                CASE WHEN :sortBy = 'priceDesc' THEN b.price END DESC,
                CASE WHEN :sortBy = 'titleAsc' THEN b.title END ASC,
                CASE WHEN :sortBy = 'titleDesc' THEN b.title END DESC,
                CASE WHEN :sortBy = 'newest' THEN b.id END DESC,
                b.id DESC
            """)
    List<Book> advancedSearch(
            @org.springframework.data.repository.query.Param("keyword") String keyword,
            @org.springframework.data.repository.query.Param("categoryId") Long categoryId,
            @org.springframework.data.repository.query.Param("minPrice") Double minPrice,
            @org.springframework.data.repository.query.Param("maxPrice") Double maxPrice,
            @org.springframework.data.repository.query.Param("inStock") Boolean inStock,
            @org.springframework.data.repository.query.Param("sortBy") String sortBy
    );
}