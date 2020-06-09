package com.example.scanandgo_pwsa.model;

public class Shop {
    private String address;
    private String shopCode;
    private Double longitude;
    private Double latitude;
    private String name;
    private int distance;
    private String documentID;

    public Shop(String address, String shopCode, Double longitude, Double latitude, String name, int distance, String documentID) {
        this.address = address;
        this.shopCode = shopCode;
        this.longitude = longitude;
        this.latitude = latitude;
        this.name = name;
        this.distance = distance;
        this.documentID = documentID;
    }

    public String getDocumentID() {
        return documentID;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public String getAddress() {
        return address;
    }

    public String getShopCode() {
        return shopCode;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public String getName() {
        return name;
    }

    public int getDistance() {
        return distance;
    }
}


