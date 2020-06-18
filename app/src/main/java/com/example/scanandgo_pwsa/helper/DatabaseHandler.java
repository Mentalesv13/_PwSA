package com.example.scanandgo_pwsa.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.scanandgo_pwsa.model.Product;
import com.example.scanandgo_pwsa.model.ShoppingList;

import java.math.BigDecimal;
import java.util.HashMap;

public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "localdatabase_ScanAndGo_PwSA";

    // Login table name
    private static final String TABLE_LOGIN = "login";
    private static final String TABLE_PRODUCTS = "products";
    private static final String TABLE_SHOP = "shops";
    private static final String TABLE_LIST = "list";
    private static final String TABLE_SAG_LIST = "sag_list";
    private static final String TABLE_CATEGORY = "category";

    // Login Table Columns names
    private static final String KEY_FNAME = "firstname";
    private static final String KEY_LNAME = "lastname";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_UID = "uid";

    // Category Table Columns names
    private static final String KEY_CATEGORY_MAIN = "categorymain";
    private static final String KEY_CATEGORY_SEC = "categorysec";

    //Product Table Columns names
    private static final String KEY_BARCODE = "barcode";
    private static final String KEY_PNAME = "productname";
    private static final String KEY_PRICE = "price";
    private static final String KEY_PROMOSTART = "promoStart";
    private static final String KEY_PROMOEND = "promoEnd";
    private static final String KEY_DISCOUNT = "discount";
    private static final String KEY_QUANTITY = "quantity";
    private static final String KEY_CATEGORY1 = "category1";
    private static final String KEY_CATEGORY2 = "category2";

    //Shops Table Columns names
    private static final String KEY_SNAME = "sname";
    private static final String KEY_LOCALIZATION = "localization";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_SCODE = "shopcode";
    private static final String KEY_SHOPID = "shopID";

    //Shoping List Table Columns name

    private static final String KEY_LIST_PNAME = "productname";
    private static final String KEY_AMOUNT = "amount";
    private static final String KEY_BOUGHT = "isBought";
    private static final String KEY_SLBARCODE = "slbarcode";
    private static final String KEY_SLPRICE = "slprice";

    //ScanAndGo List Table Columns name

    private static final String KEY_SAG_LIST_PNAME = "sag_productname";
    private static final String KEY_SAG_AMOUNT = "sag_amount";
    private static final String KEY_SAG_SLBARCODE = "sag_slbarcode";
    private static final String KEY_SAG_SLPRICE = "sag_slprice";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Table Create Statement
    private static final String CREATE_LOGIN_TABLE = " CREATE TABLE " + TABLE_LOGIN + "("
            + KEY_FNAME + " TEXT,"
            + KEY_LNAME + " TEXT,"
            + KEY_EMAIL + " TEXT UNIQUE,"
            + KEY_PHONE + " TEXT UNIQUE,"
            + KEY_UID + " TEXT UNIQUE" + ")";

    private static final String CREATE_PRODUCTS_TABLE = " CREATE TABLE " + TABLE_PRODUCTS + "("
            + KEY_BARCODE + " TEXT UNIQUE,"
            + KEY_PNAME + " TEXT,"
            + KEY_PRICE + " TEXT,"
            + KEY_PROMOEND + " TEXT,"
            + KEY_PROMOSTART + " TEXT,"
            + KEY_DISCOUNT + " TEXT,"
            + KEY_QUANTITY + " TEXT,"
            + KEY_CATEGORY1 + " TEXT,"
            + KEY_CATEGORY2 + " TEXT"
            + ")";

    private static final String CREATE_SHOP_TABLE = " CREATE TABLE " + TABLE_SHOP + "("
            + KEY_SNAME + " TEXT UNIQUE,"
            + KEY_LOCALIZATION + " TEXT,"
            + KEY_ADDRESS + " TEXT,"
            + KEY_SCODE + " TEXT,"
            + KEY_SHOPID + " TEXT"+ ")";

    private static final String CREATE_LIST_TABLE = " CREATE TABLE " + TABLE_LIST + "("
            + KEY_LIST_PNAME + " TEXT UNIQUE,"
            + KEY_AMOUNT + " TEXT,"
            + KEY_BOUGHT + " TEXT,"
            + KEY_SLBARCODE + " TEXT,"
            + KEY_SLPRICE + " TEXT" + ")";

    private static final String CREATE_SAG_LIST_TABLE = " CREATE TABLE " + TABLE_SAG_LIST + "("
            + KEY_SAG_LIST_PNAME + " TEXT UNIQUE,"
            + KEY_SAG_AMOUNT + " TEXT,"
            + KEY_SAG_SLBARCODE + " TEXT,"
            + KEY_SAG_SLPRICE + " TEXT" + ")";

    private static final String CREATE_CATEGORY_TABLE = " CREATE TABLE " + TABLE_CATEGORY + "("
            + KEY_CATEGORY_MAIN + " TEXT,"
            + KEY_CATEGORY_SEC + " TEXT" + ")";

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_LOGIN_TABLE);
        db.execSQL(CREATE_PRODUCTS_TABLE);
        db.execSQL(CREATE_SHOP_TABLE);
        db.execSQL(CREATE_LIST_TABLE);
        db.execSQL(CREATE_SAG_LIST_TABLE);
        db.execSQL(CREATE_CATEGORY_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGIN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHOP);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LIST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAG_LIST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
        // Create tables again
        onCreate(db);
    }

    /**
     * Storing user details in database
     * */
    public void addUser(String fname, String lname, String email, String phone, String uid) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_FNAME, fname); // FirstName
        values.put(KEY_LNAME, lname); // LastName
        values.put(KEY_EMAIL, email); // Email
        values.put(KEY_PHONE, phone); // Phone
        values.put(KEY_UID, uid); // uid

        // Inserting Row
        db.insert(TABLE_LOGIN, null, values);
        db.close(); // Closing database connection
    }

    public void addCategory(String category1, String category2) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CATEGORY_MAIN, category1);
        values.put(KEY_CATEGORY_SEC, category2);

        // Inserting Row
        db.insert(TABLE_CATEGORY, null, values);
        db.close(); // Closing database connection
    }

    public void addShop(String sname, String localization, String address, String scode, String shopID) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_SNAME, sname); // FirstName
        values.put(KEY_LOCALIZATION, localization); // LastName
        values.put(KEY_ADDRESS, address); // Email
        values.put(KEY_SCODE, scode); // Phone
        values.put(KEY_SHOPID, shopID);

        // Inserting Row
        db.insert(TABLE_SHOP, null, values);
        db.close(); // Closing database connection
    }


    public void addProduct(String barcode, String pName, String price, String promoEnd, String promoStart, String discount, String quantity, String category1, String category2) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_BARCODE, barcode.trim());
        values.put(KEY_PNAME, pName.trim());
        values.put(KEY_PRICE, price.trim());
        values.put(KEY_PROMOEND, promoEnd.trim());
        values.put(KEY_PROMOSTART, promoStart.trim());
        values.put(KEY_DISCOUNT, discount.trim());
        values.put(KEY_QUANTITY, quantity.trim());
        values.put(KEY_CATEGORY1, category1.trim());
        values.put(KEY_CATEGORY2, category2.trim());

        // Inserting Row
        db.insert(TABLE_PRODUCTS, null, values);
        db.close(); // Closing database connection
    }

    public void addToList(String pName, String amount, String isBought, String barcode, String price ) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_LIST_PNAME, pName);
        values.put(KEY_AMOUNT, amount);
        values.put(KEY_BOUGHT, isBought);
        values.put(KEY_SLBARCODE, barcode);
        values.put(KEY_SLPRICE, price);

        // Inserting Row
        db.insert(TABLE_LIST, null, values);
        db.close(); // Closing database connection
    }

    public void addToShopAndGoList(String pName, String amount, String barcode, String price ) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_SAG_LIST_PNAME, pName);
        values.put(KEY_SAG_AMOUNT, amount);
        values.put(KEY_SAG_SLBARCODE, barcode);
        values.put(KEY_SAG_SLPRICE, price);

        // Inserting Row
        db.insert(TABLE_SAG_LIST, null, values);
        db.close(); // Closing database connection
    }


    /**
     * Getting user data from database
     * */

    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_LOGIN;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            user.put("fname", cursor.getString(0));
            user.put("lname", cursor.getString(1));
            user.put("email", cursor.getString(2));
            user.put("phone", cursor.getString(3));
            user.put("uid", cursor.getString(4));
        }
        cursor.close();
        db.close();
        // return user
        return user;
    }

    public HashMap<String, String> getCategoryDetails(){
        HashMap<String, String> category = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_CATEGORY;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            //Log.e("TAG",cursor.getString(0) + " " + cursor.getString(1));
            category.put(cursor.getString(0),  cursor.getString(1));
        }
        cursor.close();
        db.close();
        // return category
        return category;
    }


    public HashMap<String, Product> getProductsDetail(){
        HashMap<String, Product> products = new HashMap<>();
        String selectQuery = "SELECT * FROM " + TABLE_PRODUCTS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        do
        {
            if(cursor.getCount() > 0) {
                String barcode = cursor.getString(0);
                String name = cursor.getString(1);
                BigDecimal price = BigDecimal.valueOf(Double.parseDouble(cursor.getString(2)));
                String promoEnd = cursor.getString(3);
                String promoStart = cursor.getString(4);
                BigDecimal discount = BigDecimal.valueOf(Double.parseDouble(cursor.getString(5)));
                int quantity = Integer.parseInt(cursor.getString(6));
                products.put(name, new Product(barcode, name, price, promoEnd, promoStart, discount, quantity));
            }
        } while (cursor.moveToNext());
        cursor.close();
        db.close();
        // return events
        return products;
    }

    public HashMap<String, Product> getProductsDetails(){
        HashMap<String, Product> products = new HashMap<>();
        String selectQuery = "SELECT * FROM " + TABLE_PRODUCTS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        do
        {
            if(cursor.getCount() > 0) {
                String barcode = cursor.getString(0);
                String name = cursor.getString(1);
                BigDecimal price = BigDecimal.valueOf(Double.parseDouble(cursor.getString(2)));
                String promoEnd = cursor.getString(3);
                String promoStart = cursor.getString(4);
                BigDecimal discount = BigDecimal.valueOf(Double.parseDouble(cursor.getString(5)));
                int quantity = Integer.parseInt(cursor.getString(6));
                products.put(barcode, new Product(barcode, name, price, promoEnd, promoStart, discount, quantity));
            }
        } while (cursor.moveToNext());
        cursor.close();
        db.close();
        // return events
        return products;
    }

    public HashMap<String, Product> getProductsDetailsWithCategory1(String category){
        HashMap<String, Product> products = new HashMap<>();
        String selectQuery = "SELECT * FROM products WHERE category1 = "+ "'"+category  + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        do
        {
            if(cursor.getCount() > 0) {
                String barcode = cursor.getString(0);
                String name = cursor.getString(1);
                BigDecimal price = BigDecimal.valueOf(Double.parseDouble(cursor.getString(2)));
                String promoEnd = cursor.getString(3);
                String promoStart = cursor.getString(4);
                BigDecimal discount = BigDecimal.valueOf(Double.parseDouble(cursor.getString(5)));
                int quantity = Integer.parseInt(cursor.getString(6));
                products.put(name, new Product(barcode, name, price, promoEnd, promoStart, discount, quantity));
            }
        } while (cursor.moveToNext());
        cursor.close();
        db.close();
        // return events
        return products;
    }

    public HashMap<String, Product> getProductsDetailsWithCategory2(String category){
        HashMap<String, Product> products = new HashMap<>();
        String selectQuery = "SELECT * FROM products WHERE category2 = "+ "'"+category  + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        do
        {
            if(cursor.getCount() > 0) {
                String barcode = cursor.getString(0);
                String name = cursor.getString(1);
                BigDecimal price = BigDecimal.valueOf(Double.parseDouble(cursor.getString(2)));
                String promoEnd = cursor.getString(3);
                String promoStart = cursor.getString(4);
                BigDecimal discount = BigDecimal.valueOf(Double.parseDouble(cursor.getString(5)));
                int quantity = Integer.parseInt(cursor.getString(6));
                products.put(name, new Product(barcode, name, price, promoEnd, promoStart, discount, quantity));
            }
        } while (cursor.moveToNext());
        cursor.close();
        db.close();
        // return events
        return products;
    }

    public HashMap<String, String> getShopDetails(){
        HashMap<String, String> shop = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_SHOP;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            shop.put("shopname", cursor.getString(0));
            shop.put("localization", cursor.getString(1));
            shop.put("address", cursor.getString(2));
            shop.put("shopcode", cursor.getString(3));
            shop.put("shopID", cursor.getString(4));
        }
        cursor.close();
        db.close();
        // return user
        return shop;
    }

    public HashMap<String, ShoppingList> getShoppingList(){
        HashMap<String, ShoppingList> list = new HashMap<>();
        String selectQuery = "SELECT  * FROM " + TABLE_LIST;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        do
        {
            if(cursor.getCount() > 0) {
                String name = cursor.getString(0);
                int amount = Integer.parseInt(cursor.getString(1));
                boolean isBought = Boolean.parseBoolean(cursor.getString(2));
                String barcode = cursor.getString(3);
                String price = cursor.getString(4);

                list.put(barcode, new ShoppingList(name,amount,isBought,barcode,price));
            }
        } while (cursor.moveToNext());
        cursor.close();
        db.close();
        // return user
        return list;
    }

    public HashMap<String, ShoppingList> getShopList(){
        HashMap<String, ShoppingList> list = new HashMap<>();
        String selectQuery = "SELECT  * FROM " + TABLE_LIST;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        do
        {
            if(cursor.getCount() > 0) {
                String name = cursor.getString(0);
                int amount = Integer.parseInt(cursor.getString(1));
                boolean isBought = Boolean.parseBoolean(cursor.getString(2));
                String barcode = cursor.getString(3);
                String price = cursor.getString(4);

                list.put(name, new ShoppingList(name,amount,isBought,barcode,price));
            }
        } while (cursor.moveToNext());
        cursor.close();
        db.close();
        // return user
        return list;
    }

    public HashMap<String, ShoppingList> getScanAndGoShoppingList(){
        HashMap<String, ShoppingList> list = new HashMap<>();
        String selectQuery = "SELECT  * FROM " + TABLE_SAG_LIST;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        do
        {
            if(cursor.getCount() > 0) {
                String name = cursor.getString(0);
                int amount = Integer.parseInt(cursor.getString(1));
                String barcode = cursor.getString(2);
                String price = cursor.getString(3);

                list.put(barcode, new ShoppingList(name,amount,barcode,price));
            }
        } while (cursor.moveToNext());
        cursor.close();
        db.close();
        // return user
        return list;
    }

    public HashMap<String, ShoppingList> getScanAndGoShopList(){
        HashMap<String, ShoppingList> list = new HashMap<>();
        String selectQuery = "SELECT  * FROM " + TABLE_SAG_LIST;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        do
        {
            if(cursor.getCount() > 0) {
                String name = cursor.getString(0);
                int amount = Integer.parseInt(cursor.getString(1));
                String barcode = cursor.getString(2);
                String price = cursor.getString(3);

                list.put(name, new ShoppingList(name,amount,barcode,price));
            }
        } while (cursor.moveToNext());
        cursor.close();
        db.close();
        // return user
        return list;
    }


    private static boolean containsIgnoreCase(String str, String subString) {
        return str.toLowerCase().contains(subString.toLowerCase());
    }


    public void updateIsBought(String name, String isBought, String amount, String barcode, String price)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("productname",name);
        contentValues.put("amount",amount);
        contentValues.put("isBought",isBought);
        contentValues.put("slbarcode", barcode);
        contentValues.put("slprice", price);

        db.update("list",contentValues,"slbarcode=?",new String[]{barcode});
    }

    public void updateIsBoughtByName(String name, String isBought, String amount, String price)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("productname",name);
        contentValues.put("amount",amount);
        contentValues.put("isBought",isBought);
        contentValues.put("slprice", price);

        db.update("list",contentValues,"productname=?",new String[]{name});
    }

    public void deleteFromShoppingList(String name)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete("list","slbarcode=?",new String[]{name});

    }

    public void updateScanAndGoList(String name, String amount, String barcode, String price)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("sag_productname",name);
        contentValues.put("sag_amount",amount);
        contentValues.put("sag_slbarcode", barcode);
        contentValues.put("sag_slprice", price);

        db.update("sag_list",contentValues,"sag_slbarcode=?",new String[]{barcode});
    }

    public void deleteFromScanAndGoList(String name)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete("sag_list","sag_slbarcode=?",new String[]{name});

    }

    /**
     * Recreate database
     * Delete all tables and create them again
     * */
    public void resetLogin() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_LOGIN, null, null);
        db.close();
    }

    public void resetProducts() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_PRODUCTS, null, null);
        db.close();
    }

    public void resetShop() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_SHOP, null, null);
        db.close();
    }
    public void resetList() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_LIST, null, null);
        db.close();
    }
    public void resetShopAndGoList() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_SAG_LIST, null, null);
        db.close();
    }

    public void resetCategory() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_CATEGORY, null, null);
        db.close();
    }
}
