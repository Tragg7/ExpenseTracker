package com.SpringBootMVC.ExpensesTracker.service;

import com.SpringBootMVC.ExpensesTracker.DTO.CBRFullResponse;
import com.SpringBootMVC.ExpensesTracker.DTO.CBRValuteResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ExchangeRateServiceImpl implements ExchangeRateService {

    private static final String CBR_API_URL = "https://www.cbr-xml-daily.ru/daily_json.js";

    private final RestTemplate restTemplate;

    private final Map<String, BigDecimal> rateCache = new ConcurrentHashMap<>();

    private static final Map<String, BigDecimal> FALLBACK_RATES = Map.of(
            "USD", BigDecimal.valueOf(95.50),
            "EUR", BigDecimal.valueOf(104.20),
            "CNY", BigDecimal.valueOf(13.10),
            "GBP", BigDecimal.valueOf(121.30),
            "RUB", BigDecimal.ONE
    );

    @Autowired
    public ExchangeRateServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void initRates() {
        System.out.println("🔄 Инициализация курсов валют...");
        refreshRates();
        if (rateCache.isEmpty()) {
            rateCache.putAll(FALLBACK_RATES);
            System.out.println("📦 Используются fallback-курсы (API недоступен)");
        }
    }

    @Override
    @Scheduled(fixedRate = 3600000)
    public void refreshRates() {
        try {
            System.out.println("📡 Загрузка курсов валют с ЦБ РФ...");
            ResponseEntity<CBRFullResponse> response = restTemplate.getForEntity(CBR_API_URL, CBRFullResponse.class);

            if (response.getBody() != null && response.getBody().getValute() != null) {
                Map<String, CBRValuteResponse> valutes = response.getBody().getValute();

                for (Map.Entry<String, CBRValuteResponse> entry : valutes.entrySet()) {
                    CBRValuteResponse val = entry.getValue();

                    BigDecimal ratePerOne = val.getValue()
                            .divide(val.getNominal(), 6, RoundingMode.HALF_UP);

                    rateCache.put(val.getCharCode(), ratePerOne);
                }

                System.out.println("✅ Курсы валют успешно обновлены. Всего: " + rateCache.size() + " валют");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Ошибка загрузки курсов ЦБ: " + e.getMessage());
        }
    }

    @Override
    public BigDecimal getRateToRub(String currency) {
        if (currency == null) return BigDecimal.ONE;
        if ("RUB".equalsIgnoreCase(currency)) return BigDecimal.ONE;

        BigDecimal rate = rateCache.get(currency.toUpperCase());
        if (rate == null) {
            System.out.println("⚠️ Курс для " + currency + " не найден, используется fallback");
            return FALLBACK_RATES.getOrDefault(currency.toUpperCase(), BigDecimal.ONE);
        }
        return rate;
    }

    @Override
    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return amount;
        }

        BigDecimal rubAmount = amount.multiply(getRateToRub(fromCurrency))
                .setScale(4, RoundingMode.HALF_UP);

        BigDecimal convertedAmount = rubAmount.divide(getRateToRub(toCurrency), 4, RoundingMode.HALF_UP);

        return convertedAmount;
    }

    @Override
    public BigDecimal convertToRub(BigDecimal amount, String fromCurrency) {
        return convert(amount, fromCurrency, "RUB");
    }

    @Override
    public Map<String, BigDecimal> getAllRates() {
        return rateCache;
    }
}