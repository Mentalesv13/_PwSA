package com.example.scanandgo_pwsa.model;

import java.math.BigDecimal;

public class Product {

    private String barcode;
    private String name;
    private BigDecimal price;
    private String promoEnd;
    private String promoStart;
    private BigDecimal discount;
    private int quantity;

    public Product(String barcode, String name, BigDecimal price, String promoEnd, String promoStart, BigDecimal discount, int quantity) {
        this.barcode = barcode;
        this.name = name;
        this.price = price;
        this.promoEnd = promoEnd;
        this.promoStart = promoStart;
        this.discount = discount;
        this.quantity = quantity;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getPromoEnd() {
        return promoEnd;
    }

    public String getPromoStart() {
        return promoStart;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public int getQuantity() {
        return quantity;
    }
}
