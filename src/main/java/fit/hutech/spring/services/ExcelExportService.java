package fit.hutech.spring.services;

import fit.hutech.spring.entities.Book;
import fit.hutech.spring.entities.Order;
import fit.hutech.spring.entities.OrderStatus;
import fit.hutech.spring.repositories.IBookRepository;
import fit.hutech.spring.repositories.IOrderItemRepository;
import fit.hutech.spring.repositories.IOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelExportService {
    
    private final IOrderRepository orderRepository;
    private final IOrderItemRepository orderItemRepository;
    private final IBookRepository bookRepository;
    private final OrderService orderService;
    
    public byte[] generateSalesReport(Date startDate, Date endDate) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            
            // Sheet 1: Summary
            createSummarySheet(workbook, titleStyle, headerStyle, currencyStyle, startDate, endDate);
            
            // Sheet 2: Top 10 Best Sellers
            createTopSellersSheet(workbook, titleStyle, headerStyle);
            
            // Sheet 3: Top 10 Highest Stock
            createHighestStockSheet(workbook, titleStyle, headerStyle);
            
            // Sheet 4: Orders List
            createOrdersSheet(workbook, titleStyle, headerStyle, currencyStyle, startDate, endDate);
            
            workbook.write(baos);
            log.info("Generated Excel sales report");
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Error generating Excel report", e);
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }
    
    private void createSummarySheet(Workbook workbook, CellStyle titleStyle, CellStyle headerStyle, 
                                    CellStyle currencyStyle, Date startDate, Date endDate) {
        Sheet sheet = workbook.createSheet("Summary");
        sheet.setColumnWidth(0, 8000);
        sheet.setColumnWidth(1, 6000);
        
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("SALES REPORT - STORY STATION");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));
        
        // Date range
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Row dateRow = sheet.createRow(rowNum++);
        dateRow.createCell(0).setCellValue("Report Period:");
        dateRow.createCell(1).setCellValue(
                (startDate != null ? sdf.format(startDate) : "All time") + " - " + 
                (endDate != null ? sdf.format(endDate) : "Present"));
        
        rowNum++; // Empty row
        
        // Revenue Summary
        Row revenueHeader = sheet.createRow(rowNum++);
        revenueHeader.createCell(0).setCellValue("REVENUE SUMMARY");
        revenueHeader.getCell(0).setCellStyle(headerStyle);
        
        Double totalRevenue = startDate != null && endDate != null 
                ? orderService.getRevenueByDateRange(startDate, endDate)
                : orderService.getTotalRevenue();
        
        Row revenueRow = sheet.createRow(rowNum++);
        revenueRow.createCell(0).setCellValue("Total Revenue:");
        Cell revenueCell = revenueRow.createCell(1);
        revenueCell.setCellValue(totalRevenue != null ? totalRevenue : 0.0);
        revenueCell.setCellStyle(currencyStyle);
        
        rowNum++; // Empty row
        
        // Order Statistics
        Row orderHeader = sheet.createRow(rowNum++);
        orderHeader.createCell(0).setCellValue("ORDER STATISTICS");
        orderHeader.getCell(0).setCellStyle(headerStyle);
        
        Row pendingRow = sheet.createRow(rowNum++);
        pendingRow.createCell(0).setCellValue("Pending Orders:");
        pendingRow.createCell(1).setCellValue(orderService.countByStatus(OrderStatus.PENDING));
        
        Row processingRow = sheet.createRow(rowNum++);
        processingRow.createCell(0).setCellValue("Processing Orders:");
        processingRow.createCell(1).setCellValue(orderService.countByStatus(OrderStatus.PROCESSING));
        
        Row shippedRow = sheet.createRow(rowNum++);
        shippedRow.createCell(0).setCellValue("Shipped Orders:");
        shippedRow.createCell(1).setCellValue(orderService.countByStatus(OrderStatus.SHIPPED));
        
        Row deliveredRow = sheet.createRow(rowNum++);
        deliveredRow.createCell(0).setCellValue("Delivered Orders:");
        deliveredRow.createCell(1).setCellValue(orderService.countByStatus(OrderStatus.DELIVERED));
        
        Row cancelledRow = sheet.createRow(rowNum++);
        cancelledRow.createCell(0).setCellValue("Cancelled Orders:");
        cancelledRow.createCell(1).setCellValue(orderService.countByStatus(OrderStatus.CANCELLED));
        
        rowNum++; // Empty row
        
        // Generated timestamp
        Row timestampRow = sheet.createRow(rowNum);
        timestampRow.createCell(0).setCellValue("Generated on:");
        timestampRow.createCell(1).setCellValue(sdf.format(new Date()) + " " + 
                new SimpleDateFormat("HH:mm:ss").format(new Date()));
    }
    
    private void createTopSellersSheet(Workbook workbook, CellStyle titleStyle, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("Top 10 Best Sellers");
        sheet.setColumnWidth(0, 2000);
        sheet.setColumnWidth(1, 10000);
        sheet.setColumnWidth(2, 6000);
        sheet.setColumnWidth(3, 4000);
        
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("TOP 10 BEST SELLING BOOKS");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
        
        rowNum++; // Empty row
        
        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Rank", "Book Title", "Author", "Total Sold"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data
        List<Object[]> topSellers = orderItemRepository.findTopSellingBooks();
        int rank = 1;
        for (Object[] item : topSellers) {
            if (rank > 10) break;
            
            Long bookId = (Long) item[0];
            Long totalSold = (Long) item[1];
            
            Book book = bookRepository.findById(bookId).orElse(null);
            if (book == null) continue;
            
            Row dataRow = sheet.createRow(rowNum++);
            dataRow.createCell(0).setCellValue(rank++);
            dataRow.createCell(1).setCellValue(book.getTitle());
            dataRow.createCell(2).setCellValue(book.getAuthor());
            dataRow.createCell(3).setCellValue(totalSold);
        }
    }
    
    private void createHighestStockSheet(Workbook workbook, CellStyle titleStyle, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("Top 10 Highest Stock");
        sheet.setColumnWidth(0, 2000);
        sheet.setColumnWidth(1, 10000);
        sheet.setColumnWidth(2, 6000);
        sheet.setColumnWidth(3, 4000);
        sheet.setColumnWidth(4, 4000);
        
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("TOP 10 HIGHEST STOCK BOOKS");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
        
        rowNum++; // Empty row
        
        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Rank", "Book Title", "Author", "Stock", "Status"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data - get all books sorted by stock descending
        List<Book> allBooks = bookRepository.findAll();
        allBooks.sort((b1, b2) -> Integer.compare(
                b2.getStock() != null ? b2.getStock() : 0,
                b1.getStock() != null ? b1.getStock() : 0
        ));
        
        int rank = 1;
        for (Book book : allBooks) {
            if (rank > 10) break;
            
            Row dataRow = sheet.createRow(rowNum++);
            dataRow.createCell(0).setCellValue(rank++);
            dataRow.createCell(1).setCellValue(book.getTitle());
            dataRow.createCell(2).setCellValue(book.getAuthor());
            dataRow.createCell(3).setCellValue(book.getStock() != null ? book.getStock() : 0);
            dataRow.createCell(4).setCellValue(book.getEnabled() != null && book.getEnabled() ? "Active" : "Disabled");
        }
    }
    
    private void createOrdersSheet(Workbook workbook, CellStyle titleStyle, CellStyle headerStyle,
                                   CellStyle currencyStyle, Date startDate, Date endDate) {
        Sheet sheet = workbook.createSheet("Orders");
        sheet.setColumnWidth(0, 6000);
        sheet.setColumnWidth(1, 5000);
        sheet.setColumnWidth(2, 6000);
        sheet.setColumnWidth(3, 4000);
        sheet.setColumnWidth(4, 4000);
        
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("ORDERS LIST");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
        
        rowNum++; // Empty row
        
        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Order Number", "Date", "Customer", "Status", "Total"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data
        List<Order> orders = startDate != null && endDate != null 
                ? orderService.findByDateRange(startDate, endDate)
                : orderRepository.findAll();
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (Order order : orders) {
            Row dataRow = sheet.createRow(rowNum++);
            dataRow.createCell(0).setCellValue(order.getOrderNumber());
            dataRow.createCell(1).setCellValue(sdf.format(order.getOrderDate()));
            dataRow.createCell(2).setCellValue(order.getUser().getUsername());
            dataRow.createCell(3).setCellValue(order.getStatus().getDisplayName());
            Cell totalCell = dataRow.createCell(4);
            totalCell.setCellValue(order.getTotal());
            totalCell.setCellStyle(currencyStyle);
        }
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
    
    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
    
    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("$#,##0.00"));
        return style;
    }
}
