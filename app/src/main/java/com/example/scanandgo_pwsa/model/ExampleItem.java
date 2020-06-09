package com.example.scanandgo_pwsa.model;

public class ExampleItem {
    private int mImageResource;
    private String mText1;
    private String mText2;
    private String mDistance;
    private String documentID;

    public ExampleItem(int imageResource, String text1, String text2, String distance, String documentID) {
        mImageResource = imageResource;
        mText1 = text1;
        mText2 = text2;
        mDistance = distance;
        this.documentID = documentID;
    }

    public int getImageResource() {
        return mImageResource;
    }

    public String getText1() {
        return mText1;
    }

    public String getText2() {
        return mText2;
    }

    public String getmDistance() {
        return mDistance;
    }

    public String getDocumentID() {
        return documentID;
    }
}
