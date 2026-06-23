package com.SpringBootMVC.ExpensesTracker.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OperationDTO {

    private String type;
    private String category;
    private BigDecimal amount;
    private LocalDateTime dateTime;
    private String accountName;
    private String accountCurrency;
    private BigDecimal amountInRub;

    public OperationDTO() {
    }

    public OperationDTO(String type, String category, BigDecimal amount, LocalDateTime dateTime) {
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.dateTime = dateTime;
    }

    public OperationDTO(String type, String category, BigDecimal amount, LocalDateTime dateTime,
                        String accountName, String accountCurrency, BigDecimal amountInRub) {
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.dateTime = dateTime;
        this.accountName = accountName;
        this.accountCurrency = accountCurrency;
        this.amountInRub = amountInRub;
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

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountCurrency() {
        return accountCurrency;
    }

    public void setAccountCurrency(String accountCurrency) {
        this.accountCurrency = accountCurrency;
    }

    public BigDecimal getAmountInRub() {
        return amountInRub;
    }

    public void setAmountInRub(BigDecimal amountInRub) {
        this.amountInRub = amountInRub;
    }
}