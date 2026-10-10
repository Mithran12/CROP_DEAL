package com.cropdeal.report.service;

import com.cropdeal.report.dto.AdminReportResponse;
import com.cropdeal.report.dto.DealerPurchaseReportResponse;
import com.cropdeal.report.dto.FarmerSalesReportResponse;
import com.cropdeal.report.dto.InvoiceResponse;
import com.cropdeal.report.dto.OrderReportResponse;
import com.cropdeal.report.dto.PaymentReportResponse;
import com.cropdeal.report.dto.ReceiptResponse;
import com.cropdeal.report.dto.ReportFilterResponse;
import java.time.LocalDateTime;

public interface ReportService {

    FarmerSalesReportResponse getFarmerSalesReport(Long farmerId);
    
    DealerPurchaseReportResponse getDealerPurchaseReport(Long dealerId);
    
    OrderReportResponse getOrderReport();
    
    PaymentReportResponse getPaymentReport();	
    
    AdminReportResponse getAdminReport();
    
    InvoiceResponse getInvoice(Long orderId);
    
    ReceiptResponse getReceipt(Long paymentId);
    
    byte[] exportOrderReport();
    ReportFilterResponse searchOrders(String orderNumber, String status, LocalDateTime from, LocalDateTime to, Long userId, String business, String location);
    byte[] exportFilteredOrderReport(String orderNumber, String status, LocalDateTime from, LocalDateTime to, Long userId, String business, String location);
}