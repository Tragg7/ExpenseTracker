package com.SpringBootMVC.ExpensesTracker.controller;

import com.SpringBootMVC.ExpensesTracker.service.ExchangeRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Controller
@RequestMapping("/exchange")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @Autowired
    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping("/test")
    @ResponseBody
    public String testExchangeRates() {
        StringBuilder sb = new StringBuilder();
        sb.append("<h2>Курсы валют (к RUB)</h2>");
        sb.append("<ul>");

        Map<String, BigDecimal> rates = exchangeRateService.getAllRates();
        for (Map.Entry<String, BigDecimal> entry : rates.entrySet()) {
            sb.append("<li>")
                    .append(entry.getKey())
                    .append(" = ")
                    .append(entry.getValue().setScale(2, RoundingMode.HALF_UP))
                    .append(" RUB</li>");
        }

        sb.append("</ul>");

        sb.append("<h3>Тест конвертации:</h3>");
        sb.append("<p>100 USD = ")
                .append(exchangeRateService.convert(BigDecimal.valueOf(100), "USD", "RUB")
                        .setScale(2, RoundingMode.HALF_UP))
                .append(" RUB</p>");

        sb.append("<p>100 EUR = ")
                .append(exchangeRateService.convert(BigDecimal.valueOf(100), "EUR", "RUB")
                        .setScale(2, RoundingMode.HALF_UP))
                .append(" RUB</p>");

        return sb.toString();
    }

    @GetMapping("/convert")
    @ResponseBody
    public String convert(
            @RequestParam BigDecimal amount,
            @RequestParam String from,
            @RequestParam String to) {

        BigDecimal result = exchangeRateService.convert(amount, from, to);
        return String.format("%.2f %s = %.2f %s",
                amount, from,
                result.setScale(2, RoundingMode.HALF_UP), to);
    }
}