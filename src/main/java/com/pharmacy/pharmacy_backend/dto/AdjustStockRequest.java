package com.pharmacy.pharmacy_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AdjustStockRequest {
    private Integer adjustment;
    private String reason;

    public AdjustStockRequest() {}


    public AdjustStockRequest(Integer adjustment, String reason) {
        this.adjustment = adjustment;
        this.reason = reason;
    }

    public Integer getAdjustment() {
        return adjustment;
    }

    @JsonProperty
    public void setAdjustment(Integer adjustment) {
        this.adjustment = adjustment;
    }

    public String getReason() {
        return reason;
    }

    @JsonProperty
    public void setReason(String reason) {
        this.reason = reason;
    }
}