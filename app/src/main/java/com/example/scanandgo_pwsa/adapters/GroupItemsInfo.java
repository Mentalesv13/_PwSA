package com.example.scanandgo_pwsa.adapters;

import java.util.ArrayList;

public class GroupItemsInfo {

    private String listName;
    private String shopAddress;
    private String totalPrice;
    private ArrayList<ChildItemsInfo> list = new ArrayList<ChildItemsInfo>();

    public String getName() {
        return listName;
    }

    public void setName(String songListName) {
        this.listName = songListName;
    }

    public ArrayList<ChildItemsInfo> getSongName() {
        return list;
    }

    public void setPlayerName(ArrayList<ChildItemsInfo> productName) {
        this.list = productName;
    }

    public String getShopAddress() {
        return shopAddress;
    }

    public void setShopAddress(String shopAddress) {
        this.shopAddress = shopAddress;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }
}
