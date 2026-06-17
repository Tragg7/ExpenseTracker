package com.SpringBootMVC.ExpensesTracker.DTO;

import java.math.BigDecimal;
import java.util.Map;

public class DashboardDTO {

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance;

    private Map<String, BigDecimal> incomeCategories;
    private Map<String, BigDecimal> expenseCategories;

    public DashboardDTO() {
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(BigDecimal totalExpense) {
        this.totalExpense = totalExpense;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Map<String, BigDecimal> getIncomeCategories() {
        return incomeCategories;
    }

    public void setIncomeCategories(Map<String, BigDecimal> incomeCategories) {
        this.incomeCategories = incomeCategories;
    }

    public Map<String, BigDecimal> getExpenseCategories() {
        return expenseCategories;
    }

    public void setExpenseCategories(Map<String, BigDecimal> expenseCategories) {
        this.expenseCategories = expenseCategories;
    }
}