package fit.hutech.spring.controllers;

import fit.hutech.spring.entities.Book;

import fit.hutech.spring.services.BookService;

import fit.hutech.spring.services.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.antlr.v4.runtime.misc.NotNull;

import org.springframework.context.support.DefaultMessageSourceResolvable;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.*;

@Controller

@RequestMapping("/books")

@RequiredArgsConstructor

public class BookController {

        private final BookService bookService;

        private final CategoryService categoryService;

        @GetMapping
        public String showAllBooks(@NotNull Model model,
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) Long categoryId,
                        @RequestParam(required = false) Double minPrice,
                        @RequestParam(required = false) Double maxPrice,
                        @RequestParam(required = false) Boolean inStock,
                        @RequestParam(defaultValue = "newest") String sortBy,
                        @RequestParam(defaultValue = "0") Integer pageNo,
                        @RequestParam(defaultValue = "12") Integer pageSize) {

                var allBooks = bookService.advancedSearch(keyword, categoryId, minPrice, maxPrice, inStock, sortBy);
                
                int totalBooks = allBooks.size();
                int totalPages = Math.max(1, (int) Math.ceil(totalBooks / (double) pageSize));
                int startIndex = pageNo * pageSize;
                int endIndex = Math.min(startIndex + pageSize, totalBooks);
                
                var books = (startIndex < totalBooks) ? allBooks.subList(startIndex, endIndex) : allBooks;
                
                model.addAttribute("books", books);
                model.addAttribute("currentPage", pageNo);
                model.addAttribute("totalPages", totalPages);
                model.addAttribute("categories", categoryService.getAllCategories());
                
                model.addAttribute("keyword", keyword);
                model.addAttribute("selectedCategory", categoryId);
                model.addAttribute("minPrice", minPrice);
                model.addAttribute("maxPrice", maxPrice);
                model.addAttribute("inStock", inStock);
                model.addAttribute("sortBy", sortBy);
                model.addAttribute("resultsCount", totalBooks);

                return "book/list";
        }

        @GetMapping("/{id}")
        public String showBookDetail(@PathVariable Long id, Model model) {
                var book = bookService.getBookById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Book not found"));
                model.addAttribute("book", book);
                model.addAttribute("relatedBooks", bookService.getBooksByCategory(book.getCategory().getId())
                        .stream().filter(b -> !b.getId().equals(id)).limit(4).toList());
                return "book/detail";
        }

        @GetMapping("/api-list")
        public String showApiList() {
                return "book/api-list";
        }

        @GetMapping("/api-add")
        public String showApiAdd() {
                return "book/api-add";
        }

        @GetMapping("/api-edit/{id}")
        public String showApiEdit() {
                return "book/api-edit";
        }

        @GetMapping("/add")

        public String addBookForm(@NotNull Model model) {

                model.addAttribute("book", new Book());

                model.addAttribute("categories",

                                categoryService.getAllCategories());

                return "book/add";

        }

        @PostMapping("/add")
        public String addBook(
                        @Valid @ModelAttribute("book") Book book,
                        @NotNull BindingResult bindingResult,
                        Model model) {
                if (bindingResult.hasErrors()) {
                        var errors = bindingResult.getAllErrors()
                                        .stream()
                                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                                        .toArray(String[]::new);
                        model.addAttribute("errors", errors);
                        model.addAttribute("categories",
                                        categoryService.getAllCategories());
                        return "book/add";
                }
                bookService.addBook(book);
                return "redirect:/books";
        }


        @GetMapping("/delete/{id}")
        public String deleteBook(@PathVariable long id) {
                bookService.getBookById(id)
                                .ifPresentOrElse(
                                                book -> bookService.deleteBookById(id),
                                                () -> {
                                                        throw new IllegalArgumentException("Book notfound");
                                                });
                return "redirect:/books";
        }

        @GetMapping("/edit/{id}")
        public String editBookForm(@NotNull Model model, @PathVariable long id) {
                var book = bookService.getBookById(id);
                model.addAttribute("book", book.orElseThrow(() -> new IllegalArgumentException("Book not found")));
                model.addAttribute("categories",
                                categoryService.getAllCategories());
                return "book/edit";
        }

        @PostMapping("/edit")
        public String editBook(@Valid @ModelAttribute("book") Book book,
                        @NotNull BindingResult bindingResult,
                        Model model) {
                if (bindingResult.hasErrors()) {
                        var errors = bindingResult.getAllErrors()
                                        .stream()
                                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                                        .toArray(String[]::new);
                        model.addAttribute("errors", errors);
                        model.addAttribute("categories",
                                        categoryService.getAllCategories());
                        return "book/edit";
                }
                bookService.updateBook(book);
                return "redirect:/books";
        }

        @GetMapping("/search")
        public String searchBook(@RequestParam String keyword) {
                return "redirect:/books?keyword=" + keyword;
        }
}
