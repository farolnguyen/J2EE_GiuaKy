package fit.hutech.spring.services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import fit.hutech.spring.entities.Order;
import fit.hutech.spring.entities.OrderItem;
import fit.hutech.spring.entities.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfService {
    
    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
    private static final Font BOLD_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
    private static final Font SMALL_FONT = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.GRAY);
    
    public byte[] generateInvoice(Order order) {
        // Only allow invoice generation for delivered orders
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalStateException("Invoice can only be generated for delivered orders");
        }
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();
            
            // Add header
            addHeader(document, order);
            
            // Add customer info
            addCustomerInfo(document, order);
            
            // Add items table
            addItemsTable(document, order);
            
            // Add totals
            addTotals(document, order);
            
            // Add footer
            addFooter(document);
            
            document.close();
            
            log.info("Generated invoice PDF for order: {}", order.getOrderNumber());
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Error generating invoice PDF", e);
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }
    
    private void addHeader(Document document, Order order) throws DocumentException {
        // Company name
        Paragraph title = new Paragraph("STORY STATION", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        
        // Invoice title
        Paragraph invoice = new Paragraph("INVOICE", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD));
        invoice.setAlignment(Element.ALIGN_CENTER);
        invoice.setSpacingBefore(10);
        document.add(invoice);
        
        // Order number and date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Paragraph orderInfo = new Paragraph();
        orderInfo.setAlignment(Element.ALIGN_CENTER);
        orderInfo.add(new Chunk("Order #: " + order.getOrderNumber(), NORMAL_FONT));
        orderInfo.add(Chunk.NEWLINE);
        orderInfo.add(new Chunk("Date: " + sdf.format(order.getOrderDate()), NORMAL_FONT));
        orderInfo.setSpacingAfter(20);
        document.add(orderInfo);
        
        // Separator line
        LineSeparator line = new LineSeparator();
        line.setLineColor(BaseColor.LIGHT_GRAY);
        document.add(new Chunk(line));
    }
    
    private void addCustomerInfo(Document document, Order order) throws DocumentException {
        Paragraph customerTitle = new Paragraph("BILL TO:", BOLD_FONT);
        customerTitle.setSpacingBefore(15);
        document.add(customerTitle);
        
        Paragraph customerInfo = new Paragraph();
        customerInfo.add(new Chunk(order.getUser().getUsername(), NORMAL_FONT));
        customerInfo.add(Chunk.NEWLINE);
        customerInfo.add(new Chunk(order.getUser().getEmail(), NORMAL_FONT));
        customerInfo.add(Chunk.NEWLINE);
        
        // Shipping address
        if (order.getShippingAddress() != null) {
            customerInfo.add(new Chunk(order.getShippingAddress(), NORMAL_FONT));
            customerInfo.add(Chunk.NEWLINE);
        }
        if (order.getShippingCity() != null) {
            customerInfo.add(new Chunk(order.getShippingCity(), NORMAL_FONT));
            if (order.getShippingPostalCode() != null) {
                customerInfo.add(new Chunk(", " + order.getShippingPostalCode(), NORMAL_FONT));
            }
            customerInfo.add(Chunk.NEWLINE);
        }
        if (order.getShippingCountry() != null) {
            customerInfo.add(new Chunk(order.getShippingCountry(), NORMAL_FONT));
        }
        customerInfo.setSpacingAfter(20);
        document.add(customerInfo);
    }
    
    private void addItemsTable(Document document, Order order) throws DocumentException {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 1, 1.5f, 1, 1.5f});
        
        // Header row
        addTableHeader(table, "Item");
        addTableHeader(table, "Qty");
        addTableHeader(table, "Price");
        addTableHeader(table, "Discount");
        addTableHeader(table, "Subtotal");
        
        // Data rows
        for (OrderItem item : order.getOrderItems()) {
            // Item name
            PdfPCell nameCell = new PdfPCell(new Phrase(item.getBook().getTitle(), NORMAL_FONT));
            nameCell.setPadding(8);
            nameCell.setBorderColor(BaseColor.LIGHT_GRAY);
            table.addCell(nameCell);
            
            // Quantity
            PdfPCell qtyCell = new PdfPCell(new Phrase(String.valueOf(item.getQuantity()), NORMAL_FONT));
            qtyCell.setPadding(8);
            qtyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            qtyCell.setBorderColor(BaseColor.LIGHT_GRAY);
            table.addCell(qtyCell);
            
            // Price
            PdfPCell priceCell = new PdfPCell(new Phrase(String.format("$%.2f", item.getPriceAtPurchase()), NORMAL_FONT));
            priceCell.setPadding(8);
            priceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            priceCell.setBorderColor(BaseColor.LIGHT_GRAY);
            table.addCell(priceCell);
            
            // Discount
            String discountText = item.getDiscountAtPurchase() != null && item.getDiscountAtPurchase() > 0 
                    ? String.format("%.0f%%", item.getDiscountAtPurchase()) : "-";
            PdfPCell discountCell = new PdfPCell(new Phrase(discountText, NORMAL_FONT));
            discountCell.setPadding(8);
            discountCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            discountCell.setBorderColor(BaseColor.LIGHT_GRAY);
            table.addCell(discountCell);
            
            // Subtotal
            PdfPCell subtotalCell = new PdfPCell(new Phrase(String.format("$%.2f", item.getSubtotal()), NORMAL_FONT));
            subtotalCell.setPadding(8);
            subtotalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            subtotalCell.setBorderColor(BaseColor.LIGHT_GRAY);
            table.addCell(subtotalCell);
        }
        
        document.add(table);
    }
    
    private void addTableHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setBackgroundColor(new BaseColor(52, 73, 94));
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }
    
    private void addTotals(Document document, Order order) throws DocumentException {
        PdfPTable totalsTable = new PdfPTable(2);
        totalsTable.setWidthPercentage(40);
        totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalsTable.setSpacingBefore(10);
        
        // Subtotal
        addTotalRow(totalsTable, "Subtotal:", String.format("$%.2f", order.getSubtotal()));
        
        // Discount
        if (order.getDiscountAmount() != null && order.getDiscountAmount() > 0) {
            addTotalRow(totalsTable, "Discount:", String.format("-$%.2f", order.getDiscountAmount()));
        }
        
        // Total
        PdfPCell totalLabelCell = new PdfPCell(new Phrase("TOTAL:", BOLD_FONT));
        totalLabelCell.setBorder(Rectangle.TOP);
        totalLabelCell.setPadding(8);
        totalsTable.addCell(totalLabelCell);
        
        PdfPCell totalValueCell = new PdfPCell(new Phrase(String.format("$%.2f", order.getTotal()), BOLD_FONT));
        totalValueCell.setBorder(Rectangle.TOP);
        totalValueCell.setPadding(8);
        totalValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalsTable.addCell(totalValueCell);
        
        document.add(totalsTable);
    }
    
    private void addTotalRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, NORMAL_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(5);
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }
    
    private void addFooter(Document document) throws DocumentException {
        Paragraph footer = new Paragraph();
        footer.setSpacingBefore(30);
        footer.add(new Chunk("Thank you for your purchase!", BOLD_FONT));
        footer.add(Chunk.NEWLINE);
        footer.add(new Chunk("Story Station - Your Book Destination", SMALL_FONT));
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }
}
