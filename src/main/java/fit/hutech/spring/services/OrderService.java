package fit.hutech.spring.services;

import fit.hutech.spring.entities.*;
import fit.hutech.spring.repositories.IOrderRepository;
import fit.hutech.spring.repositories.IOrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final IOrderRepository orderRepository;
    private final IOrderItemRepository orderItemRepository;
    private final BookService bookService;
    
    // Status hierarchy for preventing downgrades
    private static final java.util.Map<OrderStatus, Integer> STATUS_LEVELS = java.util.Map.of(
            OrderStatus.CANCELLED, 0,
            OrderStatus.PENDING, 1,
            OrderStatus.PROCESSING, 2,
            OrderStatus.SHIPPED, 3,
            OrderStatus.DELIVERED, 4
    );
    
    public Order createOrder(User user, List<CartItem> cartItems, 
                            String shippingAddress, String shippingCity,
                            String shippingPostalCode, String shippingCountry,
                            String notes) {
        
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .user(user)
                .shippingAddress(shippingAddress)
                .shippingCity(shippingCity)
                .shippingPostalCode(shippingPostalCode)
                .shippingCountry(shippingCountry)
                .notes(notes)
                .status(OrderStatus.PENDING)
                .build();
        
        double subtotal = 0.0;
        double discountAmount = 0.0;
        
        for (CartItem cartItem : cartItems) {
            Book book = cartItem.getBook();
            double price = book.getPrice();
            double discount = book.getDiscount() != null ? book.getDiscount() : 0.0;
            double itemDiscount = price * discount / 100 * cartItem.getQuantity();
            
            // Reduce stock when order is created
            bookService.reduceStock(book.getId(), cartItem.getQuantity());
            
            OrderItem orderItem = OrderItem.builder()
                    .book(book)
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(price)
                    .discountAtPurchase(discount)
                    .build();
            
            order.addOrderItem(orderItem);
            subtotal += price * cartItem.getQuantity();
            discountAmount += itemDiscount;
        }
        
        order.setSubtotal(subtotal);
        order.setDiscountAmount(discountAmount);
        order.setTotal(subtotal - discountAmount);
        
        return orderRepository.save(order);
    }
    
    private String generateOrderNumber() {
        LocalDate now = LocalDate.now();
        String datePart = String.format("%d%02d%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        String uniquePart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD-" + datePart + "-" + uniquePart;
    }
    
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }
    
    public Optional<Order> findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }
    
    public List<Order> findByUser(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }
    
    public List<Order> findByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }
    
    public Page<Order> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findAllByOrderByOrderDateDesc(pageable);
    }
    
    public Order updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        
        OrderStatus currentStatus = order.getStatus();
        
        // CANCELLED is a terminal state - cannot change status from CANCELLED
        if (currentStatus == OrderStatus.CANCELLED) {
            throw new IllegalStateException(
                "Cannot change status of a cancelled order. Cancelled orders are final.");
        }
        
        // Prevent status downgrade (except to CANCELLED which has special handling)
        if (newStatus != OrderStatus.CANCELLED) {
            int currentLevel = STATUS_LEVELS.getOrDefault(currentStatus, 0);
            int newLevel = STATUS_LEVELS.getOrDefault(newStatus, 0);
            
            if (newLevel < currentLevel) {
                throw new IllegalStateException(
                    "Cannot downgrade order status from " + currentStatus + " to " + newStatus);
            }
        }
        
        // If changing to CANCELLED, restore stock for all items
        if (newStatus == OrderStatus.CANCELLED) {
            for (OrderItem item : order.getOrderItems()) {
                bookService.restoreStock(item.getBook().getId(), item.getQuantity());
            }
        }
        
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }
    
    public void cancelOrder(Long orderId) {
        updateStatus(orderId, OrderStatus.CANCELLED);
    }
    
    public void cancelOrderWithStockRestore(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        
        // Validate: user owns order
        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalStateException("You can only cancel your own orders");
        }
        
        // Validate: order is still pending
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Can only cancel orders in PENDING status");
        }
        
        // Restore stock for each item
        for (OrderItem item : order.getOrderItems()) {
            bookService.restoreStock(item.getBook().getId(), item.getQuantity());
        }
        
        // Update status to cancelled
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
    
    public Long countByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }
    
    public Double getTotalRevenue() {
        Double revenue = orderRepository.getTotalRevenue();
        return revenue != null ? revenue : 0.0;
    }
    
    public Double getRevenueByDateRange(Date startDate, Date endDate) {
        Double revenue = orderRepository.getRevenueByDateRange(startDate, endDate);
        return revenue != null ? revenue : 0.0;
    }
    
    public List<Order> findByDateRange(Date startDate, Date endDate) {
        return orderRepository.findByDateRange(startDate, endDate);
    }
}
