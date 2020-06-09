package com.example.scanandgo_pwsa.model;

public class ShoppingList {

    private String productName;
    private int amount;
    private boolean isBought;
    private String barcode;
    private String price;


    public ShoppingList(String productName, int amount, boolean isBought, String barcode, String price) {
        this.productName = productName;
        this.amount = amount;
        this.isBought = isBought;
        this.barcode = barcode;
        this.price = price;
    }

    public ShoppingList(String productName, int amount, String barcode, String price) {
        this.productName = productName;
        this.amount = amount;
        this.barcode = barcode;
        this.price = price;
    }

    public boolean isBought() {
        return isBought;
    }

    public String getProductName() {
        return productName;
    }

    public int getAmount() {
        return amount;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getPrice() {
        return price;
    }
}
