package com.SpringBootMVC.ExpensesTracker.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OperationDTO {

    private String type;
    private String category;
    private BigDecimal amount;
    private LocalDateTime dateTime;

    public OperationDTO() {
    }

    public OperationDTO(
            String type,
            String category,
            BigDecimal amount,
            LocalDateTime dateTime) {

        this.type = type;
        this.category = category;
        this.amount = amount;
        this.dateTime = dateTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
}