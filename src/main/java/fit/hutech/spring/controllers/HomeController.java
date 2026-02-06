package fit.hutech.spring.controllers;

import fit.hutech.spring.services.BookService;
import fit.hutech.spring.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class HomeController {
    
    private final BookService bookService;
    private final CategoryService categoryService;
    
    @GetMapping
    public String home(Model model) {
        model.addAttribute("featuredBooks", bookService.getFeaturedBooks());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "home/index";
    }
}