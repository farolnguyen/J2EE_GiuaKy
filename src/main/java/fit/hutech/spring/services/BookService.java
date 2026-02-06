package fit.hutech.spring.services;

import fit.hutech.spring.entities.Book;
import fit.hutech.spring.entities.PriceHistory;
import fit.hutech.spring.entities.User;
import fit.hutech.spring.repositories.IBookRepository;
import fit.hutech.spring.repositories.IOrderItemRepository;
import fit.hutech.spring.repositories.IPriceHistoryRepository;
import lombok.RequiredArgsConstructor;

import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = { Exception.class, Throwable.class })
public class BookService {
    private final IBookRepository bookRepository;
    private final IOrderItemRepository orderItemRepository;
    private final IPriceHistoryRepository priceHistoryRepository;

    public List<Book> getAllBooks(Integer pageNo,
            Integer pageSize,
            String sortBy) {

        return bookRepository.findAllBooks(pageNo, pageSize, sortBy);
    }

    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    public void addBook(Book book) {
        bookRepository.save(book);
    }

    public void updateBook(@NotNull Book book) {
        updateBook(book, null);
    }
    
    public void updateBook(@NotNull Book book, User changedBy) {
        Book existingBook = bookRepository.findById(book.getId())
                .orElse(null);
        Objects.requireNonNull(existingBook);
        
        // Record price change if price has changed
        if (existingBook.getPrice() != null && book.getPrice() != null 
                && !existingBook.getPrice().equals(book.getPrice())) {
            PriceHistory priceHistory = PriceHistory.builder()
                    .book(existingBook)
                    .oldPrice(existingBook.getPrice())
                    .newPrice(book.getPrice())
                    .changedBy(changedBy)
                    .build();
            priceHistoryRepository.save(priceHistory);
        }
        
        existingBook.setTitle(book.getTitle());
        existingBook.setAuthor(book.getAuthor());
        existingBook.setPrice(book.getPrice());
        existingBook.setCategory(book.getCategory());
        existingBook.setDescription(book.getDescription());
        existingBook.setPublisher(book.getPublisher());
        existingBook.setPublicationYear(book.getPublicationYear());
        existingBook.setImageUrl(book.getImageUrl());
        existingBook.setDiscount(book.getDiscount());
        existingBook.setStock(book.getStock());
        existingBook.setFeatured(book.getFeatured());
        bookRepository.save(existingBook);
    }
    
    public List<PriceHistory> getPriceHistory(Long bookId) {
        return priceHistoryRepository.findByBookIdOrderByChangeDateDesc(bookId);
    }

    public void deleteBookById(Long id) {
        // Check if book has orders
        if (orderItemRepository.existsByBookId(id)) {
            throw new IllegalStateException("Cannot delete book with existing orders. Disable the book instead.");
        }
        bookRepository.deleteById(id);
    }
    
    public boolean hasOrders(Long bookId) {
        return orderItemRepository.existsByBookId(bookId);
    }
    
    public void setEnabled(Long bookId, Boolean enabled) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
        book.setEnabled(enabled);
        bookRepository.save(book);
    }

    public List<Book> searchBook(String keyword) {
        return bookRepository.searchBook(keyword);
    }

    public List<Book> getFeaturedBooks() {
        return bookRepository.findFeaturedBooks();
    }

    public List<Book> getBestSellerBooks() {
        return getFeaturedBooks();
    }

    public List<Book> getBooksByCategory(Long categoryId) {
        return bookRepository.findByCategory(categoryId);
    }

    public List<Book> getBooksByPriceRange(Double minPrice, Double maxPrice) {
        return bookRepository.findByPriceRange(minPrice, maxPrice);
    }

    public List<Book> getLowStockBooks(Integer threshold) {
        return bookRepository.findByStockLessThanEqual(threshold);
    }

    public void updateStock(Long bookId, Integer newStock) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
        book.setStock(newStock);
        
        // Auto-disable if stock is 0, auto-enable if stock > 0 (per user requirement)
        if (newStock <= 0) {
            book.setEnabled(false);
        } else if (book.getEnabled() == null || !book.getEnabled()) {
            book.setEnabled(true);
        }
        
        bookRepository.save(book);
    }
    
    public void reduceStock(Long bookId, Integer quantity) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
        
        int newStock = book.getStock() - quantity;
        if (newStock < 0) {
            throw new IllegalStateException("Insufficient stock for book: " + book.getTitle());
        }
        
        book.setStock(newStock);
        
        // Auto-disable if stock reaches 0
        if (newStock == 0) {
            book.setEnabled(false);
        }
        
        bookRepository.save(book);
    }
    
    public void restoreStock(Long bookId, Integer quantity) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
        
        int newStock = book.getStock() + quantity;
        book.setStock(newStock);
        
        // Auto-enable if stock > 0 (per user requirement)
        if (newStock > 0 && (book.getEnabled() == null || !book.getEnabled())) {
            book.setEnabled(true);
        }
        
        bookRepository.save(book);
    }

    public void setFeatured(Long bookId, Boolean featured) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
        book.setFeatured(featured);
        bookRepository.save(book);
    }

    public List<Book> advancedSearch(String keyword, Long categoryId, 
                                     Double minPrice, Double maxPrice, 
                                     Boolean inStock, String sortBy) {
        return bookRepository.advancedSearch(keyword, categoryId, minPrice, maxPrice, inStock, sortBy);
    }
}