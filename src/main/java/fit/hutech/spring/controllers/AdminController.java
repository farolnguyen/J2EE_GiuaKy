package fit.hutech.spring.controllers;

import fit.hutech.spring.entities.Book;
import fit.hutech.spring.entities.Category;
import fit.hutech.spring.entities.Order;
import fit.hutech.spring.entities.OrderStatus;
import fit.hutech.spring.services.BookService;
import fit.hutech.spring.services.CategoryService;
import fit.hutech.spring.services.ExcelExportService;
import fit.hutech.spring.services.OrderService;
import fit.hutech.spring.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import java.security.Principal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final BookService bookService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final UserService userService;
    private final ExcelExportService excelExportService;

    @GetMapping
    public String adminHome() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("currentPage", "dashboard");
        model.addAttribute("pageTitle", "Dashboard");
        
        // Main Statistics
        var allBooks = bookService.getAllBooks(0, Integer.MAX_VALUE, "id");
        model.addAttribute("totalBooks", allBooks.size());
        model.addAttribute("pendingOrders", orderService.countByStatus(OrderStatus.PENDING));
        model.addAttribute("totalRevenue", orderService.getTotalRevenue());
        model.addAttribute("lowStockBooks", bookService.getLowStockBooks(5).size());
        
        // Additional Statistics
        model.addAttribute("totalCategories", categoryService.getAllCategories().size());
        model.addAttribute("totalUsers", userService.getAllUsers().size());
        model.addAttribute("processingOrders", orderService.countByStatus(OrderStatus.PROCESSING));
        model.addAttribute("completedOrders", orderService.countByStatus(OrderStatus.DELIVERED));
        model.addAttribute("featuredBooks", bookService.getFeaturedBooks().size());
        model.addAttribute("outOfStockBooks", bookService.getLowStockBooks(0).size());
        
        // Recent orders
        Page<Order> recentOrders = orderService.findAll(0, 5);
        model.addAttribute("recentOrders", recentOrders.getContent());
        
        // Low stock alert
        model.addAttribute("lowStockList", bookService.getLowStockBooks(5));
        
        // Top categories by book count
        model.addAttribute("categories", categoryService.getAllCategories());
        
        return "admin/dashboard";
    }

    // ==================== BOOKS MANAGEMENT ====================
    
    @GetMapping("/books")
    public String listBooks(Model model,
                           @RequestParam(defaultValue = "0") Integer page,
                           @RequestParam(defaultValue = "50") Integer size) {
        model.addAttribute("currentPage", "books");
        model.addAttribute("pageTitle", "Manage Books");
        
        var allBooks = bookService.getAllBooks(0, Integer.MAX_VALUE, "id");
        int totalBooks = allBooks.size();
        int totalPages = Math.max(1, (int) Math.ceil(totalBooks / (double) size));
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalBooks);
        var books = (startIndex < totalBooks) ? allBooks.subList(startIndex, endIndex) : allBooks;
        
        model.addAttribute("books", books);
        model.addAttribute("currentPageNum", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalBooks", totalBooks);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/books";
    }

    @GetMapping("/books/add")
    public String addBookForm(Model model) {
        model.addAttribute("currentPage", "books");
        model.addAttribute("pageTitle", "Add New Book");
        model.addAttribute("book", new Book());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/book-form";
    }

    @PostMapping("/books/add")
    public String addBook(@Valid @ModelAttribute("book") Book book,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("currentPage", "books");
            model.addAttribute("pageTitle", "Add New Book");
            model.addAttribute("categories", categoryService.getAllCategories());
            return "admin/book-form";
        }
        bookService.addBook(book);
        redirectAttributes.addFlashAttribute("success", "Book added successfully!");
        return "redirect:/admin/books";
    }

    @GetMapping("/books/edit/{id}")
    public String editBookForm(@PathVariable Long id, Model model) {
        Book book = bookService.getBookById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
        model.addAttribute("currentPage", "books");
        model.addAttribute("pageTitle", "Edit Book");
        model.addAttribute("book", book);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/book-form";
    }

    @PostMapping("/books/edit")
    public String editBook(@Valid @ModelAttribute("book") Book book,
                          @RequestParam(defaultValue = "0") Integer addStock,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("currentPage", "books");
            model.addAttribute("pageTitle", "Edit Book");
            model.addAttribute("categories", categoryService.getAllCategories());
            return "admin/book-form";
        }
        
        // Handle additive stock: add to existing stock instead of replacing
        if (addStock > 0) {
            Book existingBook = bookService.getBookById(book.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Book not found"));
            book.setStock(existingBook.getStock() + addStock);
        }
        
        bookService.updateBook(book);
        redirectAttributes.addFlashAttribute("success", "Book updated successfully!");
        return "redirect:/admin/books";
    }

    @GetMapping("/books/delete/{id}")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookService.deleteBookById(id);
            redirectAttributes.addFlashAttribute("success", "Book deleted successfully!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/books";
    }
    
    @PostMapping("/books/toggle-enabled/{id}")
    public String toggleEnabled(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Book book = bookService.getBookById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
        bookService.setEnabled(id, !book.getEnabled());
        String status = book.getEnabled() ? "disabled" : "enabled";
        redirectAttributes.addFlashAttribute("success", "Book " + status + " successfully!");
        return "redirect:/admin/books";
    }

    @PostMapping("/books/toggle-featured/{id}")
    public String toggleFeatured(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Book book = bookService.getBookById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
        bookService.setFeatured(id, !book.getFeatured());
        redirectAttributes.addFlashAttribute("success", "Featured status updated!");
        return "redirect:/admin/books";
    }

    // ==================== CATEGORIES MANAGEMENT ====================
    
    @GetMapping("/categories")
    public String listCategories(Model model) {
        model.addAttribute("currentPage", "categories");
        model.addAttribute("pageTitle", "Manage Categories");
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("newCategory", new Category());
        return "admin/categories";
    }

    @PostMapping("/categories/add")
    public String addCategory(@Valid @ModelAttribute("newCategory") Category category,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Invalid category name");
            return "redirect:/admin/categories";
        }
        categoryService.addCategory(category);
        redirectAttributes.addFlashAttribute("success", "Category added successfully!");
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategoryById(id);
            redirectAttributes.addFlashAttribute("success", "Category deleted successfully!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    // ==================== ORDERS MANAGEMENT ====================
    
    @GetMapping("/orders")
    public String listOrders(Model model,
                            @RequestParam(defaultValue = "0") Integer page,
                            @RequestParam(required = false) String status) {
        model.addAttribute("currentPage", "orders");
        model.addAttribute("pageTitle", "Manage Orders");
        
        if (status != null && !status.isEmpty()) {
            model.addAttribute("orders", orderService.findByStatus(OrderStatus.valueOf(status)));
            model.addAttribute("selectedStatus", status);
        } else {
            Page<Order> orders = orderService.findAll(page, 20);
            model.addAttribute("orders", orders.getContent());
            model.addAttribute("totalPages", orders.getTotalPages());
        }
        
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/orders";
    }

    @GetMapping("/orders/{id}")
    public String viewOrder(@PathVariable Long id, Model model) {
        Order order = orderService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        model.addAttribute("currentPage", "orders");
        model.addAttribute("pageTitle", "Order #" + order.getOrderNumber());
        model.addAttribute("order", order);
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/order-detail";
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id,
                                   @RequestParam OrderStatus status,
                                   RedirectAttributes redirectAttributes) {
        try {
            orderService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("success", "Order status updated!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }

    // ==================== USERS MANAGEMENT ====================
    
    @GetMapping("/users")
    public String listUsers(Model model, Principal principal) {
        model.addAttribute("currentPage", "users");
        model.addAttribute("pageTitle", "Manage Users");
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("firstAdminId", userService.findFirstAdmin().map(u -> u.getId()).orElse(null));
        
        // Add current logged-in user ID to hide disable button for their own account
        if (principal != null) {
            userService.findByUsername(principal.getName())
                .ifPresent(user -> model.addAttribute("currentUserId", user.getId()));
        }
        
        return "admin/users";
    }
    
    @PostMapping("/users/{id}/toggle-enabled")
    public String toggleUserEnabled(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        var user = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        userService.setEnabled(id, !user.getEnabled());
        String status = user.getEnabled() ? "disabled" : "enabled";
        redirectAttributes.addFlashAttribute("success", "User " + status + " successfully!");
        return "redirect:/admin/users";
    }
    
    @PostMapping("/users/{id}/unlock")
    public String unlockUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.unlockAccount(id);
        redirectAttributes.addFlashAttribute("success", "User account unlocked!");
        return "redirect:/admin/users";
    }
    
    @PostMapping("/users/{id}/change-password")
    public String changeUserPassword(@PathVariable Long id, 
                                    @RequestParam String newPassword,
                                    RedirectAttributes redirectAttributes) {
        if (newPassword == null || newPassword.length() < 8) {
            redirectAttributes.addFlashAttribute("error", "Password must be at least 8 characters");
            return "redirect:/admin/users/" + id;
        }
        userService.changePassword(id, newPassword);
        redirectAttributes.addFlashAttribute("success", "Password changed successfully!");
        return "redirect:/admin/users/" + id;
    }
    
    @PostMapping("/users/{id}/demote")
    public String demoteAdmin(@PathVariable Long id, 
                             @AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser,
                             RedirectAttributes redirectAttributes) {
        try {
            var admin = userService.findByUsername(currentUser.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Admin not found"));
            userService.demoteAdmin(admin.getId(), id);
            redirectAttributes.addFlashAttribute("success", "User demoted from admin successfully!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }
    
    @PostMapping("/users/{id}/promote")
    public String promoteToAdmin(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.promoteToAdmin(id);
        redirectAttributes.addFlashAttribute("success", "User promoted to admin successfully!");
        return "redirect:/admin/users";
    }

    // ==================== REPORTS ====================
    
    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("currentPage", "reports");
        model.addAttribute("pageTitle", "Sales Reports");
        model.addAttribute("totalRevenue", orderService.getTotalRevenue());
        model.addAttribute("pendingOrders", orderService.countByStatus(OrderStatus.PENDING));
        model.addAttribute("completedOrders", orderService.countByStatus(OrderStatus.DELIVERED));
        model.addAttribute("cancelledOrders", orderService.countByStatus(OrderStatus.CANCELLED));
        model.addAttribute("lowStockBooks", bookService.getLowStockBooks(10));
        return "admin/reports";
    }
    
    @GetMapping("/reports/export")
    public ResponseEntity<byte[]> exportReports() {
        try {
            byte[] excelBytes = excelExportService.generateSalesReport(null, null);
            
            String filename = "SalesReport_" + new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".xlsx";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(excelBytes.length)
                    .body(excelBytes);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
