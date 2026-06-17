package com.SpringBootMVC.ExpensesTracker.service;

import java.math.BigDecimal;
import java.util.Map;

public interface ExchangeRateService {

    BigDecimal getRateToRub(String currency);

    BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency);

    BigDecimal convertToRub(BigDecimal amount, String fromCurrency);

    Map<String, BigDecimal> getAllRates();

    void refreshRates();
}