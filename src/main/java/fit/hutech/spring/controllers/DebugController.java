package fit.hutech.spring.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class DebugController {
    
    @GetMapping("/debug")
    public org.springframework.web.servlet.ModelAndView debugPage(HttpServletRequest request) {
        org.springframework.web.servlet.ModelAndView mav = new org.springframework.web.servlet.ModelAndView("debug");
        
        // Pass session info as model attribute (Spring Boot 3.x compatibility)
        HttpSession session = request.getSession(false);
        if (session != null) {
            mav.addObject("sessionId", session.getId());
        } else {
            mav.addObject("sessionId", "NO SESSION");
        }
        
        return mav;
    }
    
    @GetMapping("/api/debug/auth")
    public Map<String, Object> debugAuth(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        // Session info
        HttpSession session = request.getSession(false);
        if (session != null) {
            result.put("sessionId", session.getId());
            result.put("sessionCreationTime", session.getCreationTime());
            result.put("sessionMaxInactiveInterval", session.getMaxInactiveInterval());
        } else {
            result.put("session", "NO SESSION");
        }
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null) {
            result.put("authenticated", auth.isAuthenticated());
            result.put("principal", auth.getPrincipal().toString());
            result.put("principalClass", auth.getPrincipal().getClass().getName());
            result.put("name", auth.getName());
            result.put("authClass", auth.getClass().getName());
            
            // List authorities in detail
            var authList = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
            result.put("authorities", authList);
            result.put("authoritiesCount", authList.size());
            
            // Check specific authorities
            result.put("hasUSER", auth.getAuthorities().stream()
                .anyMatch(a -> "USER".equals(a.getAuthority())));
            result.put("hasADMIN", auth.getAuthorities().stream()
                .anyMatch(a -> "ADMIN".equals(a.getAuthority())));
            result.put("hasROLE_USER", auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_USER".equals(a.getAuthority())));
            
            log.info("Debug Auth - authenticated: {}, name: {}, authorities: {}", 
                auth.isAuthenticated(), auth.getName(), authList);
        } else {
            result.put("authenticated", false);
            result.put("error", "No authentication in context");
            log.warn("Debug Auth - No authentication found");
        }
        
        return result;
    }
}
