package com.netki;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Product {

    private String id;
    private String name;
    private String currentTierName;
    private Map<String, Integer> currentPrice = new HashMap<String, Integer>();
    private int term;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurrentTierName() {
        return currentTierName;
    }

    public void setCurrentTierName(String currentTierName) {
        this.currentTierName = currentTierName;
    }

    public Map<String, Integer> getCurrentPrice() {
        return currentPrice;
    }

    public List<String> getCountries() {
        return new ArrayList<String>(currentPrice.keySet());
    }

    public Integer getCurrentPrice(String country) {
        return currentPrice.get(country);
    }

    public void setCurrentPrice(Map<String, Integer> currentPrice) {
        this.currentPrice = currentPrice;
    }

    public void setCurrentPrice(String country, Integer price) {
        this.currentPrice.put(country, price);
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }
}
