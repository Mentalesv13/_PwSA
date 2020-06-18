package com.example.scanandgo_pwsa.adapters;

public class ChildItemsInfo {

    private String songName = "";
    private String price = "";
    private String amount = "";

    public String getName() {
        return songName;
    }

    public void setName(String productName) {
        this.songName = productName;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}