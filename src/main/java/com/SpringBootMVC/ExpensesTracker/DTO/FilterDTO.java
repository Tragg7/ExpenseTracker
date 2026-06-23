package com.SpringBootMVC.ExpensesTracker.DTO;

public class FilterDTO {
    private String category;
    private Integer from;
    private Integer to;
    private String month;
    private String year;
    private Integer accountId;

    public FilterDTO() {
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getFrom() {
        return from;
    }

    public void setFrom(Integer from) {
        this.from = from;
    }

    public Integer getTo() {
        return to;
    }

    public void setTo(Integer to) {
        this.to = to;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return "Filter{" +
                "category='" + category + '\'' +
                ", from=" + from +
                ", to=" + to +
                ", month='" + month + '\'' +
                ", year='" + year + '\'' +
                '}';
    }
}
