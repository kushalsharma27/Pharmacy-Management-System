package com.pharmacy.pharmacy_backend.dto;

public class SaleSummaryResponse {

    private Long totalSales;
    private Double totalRevenue;
    private Double averageOrderValue;
    private Long totalItems;
    private Long completedSales;
    private Long cancelledSales;
    private String date;

    // Constructors
    public SaleSummaryResponse() {}

    public SaleSummaryResponse(Long totalSales, Double totalRevenue, Double averageOrderValue,
                               Long totalItems, Long completedSales, Long cancelledSales, String date) {
        this.totalSales = totalSales;
        this.totalRevenue = totalRevenue;
        this.averageOrderValue = averageOrderValue;
        this.totalItems = totalItems;
        this.completedSales = completedSales;
        this.cancelledSales = cancelledSales;
        this.date = date;
    }

    // Getters and Setters
    public Long getTotalSales() { return totalSales; }
    public void setTotalSales(Long totalSales) { this.totalSales = totalSales; }

    public Double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }

    public Double getAverageOrderValue() { return averageOrderValue; }
    public void setAverageOrderValue(Double averageOrderValue) { this.averageOrderValue = averageOrderValue; }

    public Long getTotalItems() { return totalItems; }
    public void setTotalItems(Long totalItems) { this.totalItems = totalItems; }

    public Long getCompletedSales() { return completedSales; }
    public void setCompletedSales(Long completedSales) { this.completedSales = completedSales; }

    public Long getCancelledSales() { return cancelledSales; }
    public void setCancelledSales(Long cancelledSales) { this.cancelledSales = cancelledSales; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}