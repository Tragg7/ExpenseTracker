package com.SpringBootMVC.ExpensesTracker.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CBRFullResponse {

    @JsonProperty("Valute")
    private Map<String, CBRValuteResponse> valute;

    public CBRFullResponse() {
    }

    public Map<String, CBRValuteResponse> getValute() {
        return valute;
    }

    public void setValute(Map<String, CBRValuteResponse> valute) {
        this.valute = valute;
    }
}