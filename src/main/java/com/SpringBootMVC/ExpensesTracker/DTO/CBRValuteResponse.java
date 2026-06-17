package com.SpringBootMVC.ExpensesTracker.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CBRValuteResponse {

    @JsonProperty("CharCode")
    private String charCode;

    @JsonProperty("Value")
    private BigDecimal value;

    @JsonProperty("Nominal")
    private BigDecimal nominal;

    public CBRValuteResponse() {
    }

    public String getCharCode() {
        return charCode;
    }

    public void setCharCode(String charCode) {
        this.charCode = charCode;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getNominal() {
        return nominal;
    }

    public void setNominal(BigDecimal nominal) {
        this.nominal = nominal;
    }

    @Override
    public String toString() {
        return "CBRValuteResponse{" +
                "charCode='" + charCode + '\'' +
                ", value=" + value +
                ", nominal=" + nominal +
                '}';
    }
}