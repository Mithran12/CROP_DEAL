package com.cropdeal.report.dto;
import java.util.List;
public class ReportFilterResponse {
 private List<OrderResponse> orders; private long totalOrders; private double totalValue;
 public List<OrderResponse> getOrders(){return orders;} public void setOrders(List<OrderResponse> v){orders=v;}
 public long getTotalOrders(){return totalOrders;} public void setTotalOrders(long v){totalOrders=v;}
 public double getTotalValue(){return totalValue;} public void setTotalValue(double v){totalValue=v;}
}
