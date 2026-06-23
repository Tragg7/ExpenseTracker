package com.SpringBootMVC.ExpensesTracker.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransferDTO {

    @NotNull(message = "Счёт отправителя обязателен")
    private Integer fromAccountId;

    @NotNull(message = "Счёт получателя обязателен")
    private Integer toAccountId;

    @NotNull(message = "Сумма обязательна")
    @Min(value = 1, message = "Сумма должна быть больше 0")
    private BigDecimal amount;

    private String description;

    private LocalDateTime dateTime;

    private int clientId;

    public Integer getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(Integer fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public Integer getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(Integer toAccountId) {
        this.toAccountId = toAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }
}