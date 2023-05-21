package com.example.sarithmetics;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    private Context context;
    private static final String DATABASE_NAME = "dbSarithemtics";
    private static final int DATABASE_VERSION = 1;
    /*tblProduct*/
    private static final String TABLE_PRODUCT = "tblProduct";
    private static final String COL_PRODUCT_ID = "productID";
    private static final String COL_PRODUCT_NAME = "productName";
    private static final String COL_PRODUCT_PRICE = "productPrice";
    private static final String COL_PRODUCT_QTY = "productQty";

    /*tblProfile*/
    private static final String TABLE_PROFILE = "tblProfile";
    private static final String COL_PROFILE_ID = "profileID";
    private static final String COL_PROFILE_FIRST_NAME = "profileFirstName";
    private static final String COL_PROFILE_LAST_NAME = "profileLastName";
    private static final String COL_PROFILE_PASSWORD = "profilePassword";
    private static final String COL_PROFILE_HIGH_SCORE = "profileHighScore";
    private static final String COL_PROFILE_CURRENCY = "profileCurrency";

    public MyDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_PRODUCT + " (" + COL_PRODUCT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_PRODUCT_NAME + " TEXT, " + COL_PRODUCT_PRICE + " REAL, " + COL_PRODUCT_QTY + " INTEGER);";
        db.execSQL(query);
        query = "CREATE TABLE " + TABLE_PROFILE + " (" + COL_PROFILE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_PROFILE_FIRST_NAME + " TEXT, " + COL_PROFILE_LAST_NAME + " TEXT, " + COL_PROFILE_PASSWORD + " TEXT, " + COL_PROFILE_HIGH_SCORE + " INTEGER, " + COL_PROFILE_CURRENCY + " INTEGER);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILE);
        onCreate(db);
    }

    void addItem(String productName, float productPrice, int productQuantity){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COL_PRODUCT_NAME, productName);
        cv.put(COL_PRODUCT_PRICE, productPrice);
        cv.put(COL_PRODUCT_QTY, productQuantity);
        long result = db.insert(TABLE_PRODUCT, null, cv);
        if(result == -1){
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context, "Added", Toast.LENGTH_SHORT).show();
        }
    }
    boolean loginAccount(String firstName, String password){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        String query = "SELECT * FROM " + TABLE_PROFILE + " WHERE profileFirstName = '" + firstName + "' AND profilePassword = '" + password + "';";
        if(db == null){
            Toast.makeText(context, "no db", Toast.LENGTH_SHORT).show();
        }else{
            cursor = db.rawQuery(query, null);
        }
        if(cursor.getCount() != 0){
            return true;
        }
        return false;
    }
    boolean registerAccount(String firstName, String lastName, String password){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COL_PROFILE_FIRST_NAME, firstName);
        cv.put(COL_PROFILE_LAST_NAME, lastName);
        cv.put(COL_PROFILE_PASSWORD, password);
        cv.put(COL_PROFILE_HIGH_SCORE, 0);
        cv.put(COL_PROFILE_CURRENCY, 0);

        long result = db.insert(TABLE_PROFILE, null, cv);

        if(result == -1){
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
            return false;
        }else{
            Toast.makeText(context, "Added", Toast.LENGTH_SHORT).show();
            return true;
        }
    }
    Cursor readAllProductData(){
        String query = "SELECT * FROM " + TABLE_PRODUCT;
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = null;
        if(db != null){
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }
}
