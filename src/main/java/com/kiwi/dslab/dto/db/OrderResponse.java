package com.kiwi.dslab.dto.db;

import java.util.List;

public class OrderResponse {
    private boolean success;
    private List<Double> prices;
    private List<String> currencies;

    public OrderResponse(boolean success, List<Double> prices, List<String> currencies) {
        this.success = success;
        this.prices = prices;
        this.currencies = currencies;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<Double> getPrices() {
        return prices;
    }

    public void setPrices(List<Double> prices) {
        this.prices = prices;
    }

    public List<String> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(List<String> currencies) {
        this.currencies = currencies;
    }
}
