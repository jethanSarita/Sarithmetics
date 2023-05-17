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
    private static final String TABLE_NAME = "tblProduct";
    private static final String COL_PRODUCT_ID = "productID";
    private static final String COL_PRODUCT_NAME = "productName";
    private static final String COL_PRODUCT_PRICE = "productPrice";
    private static final String COL_PRODUCT_QTY = "productQty";

    public MyDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " (" + COL_PRODUCT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_PRODUCT_NAME + " TEXT, " + COL_PRODUCT_PRICE + " REAL, " + COL_PRODUCT_QTY + " INTEGER);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    void addItem(String productName, float productPrice, int productQuantity){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COL_PRODUCT_NAME, productName);
        cv.put(COL_PRODUCT_PRICE, productPrice);
        cv.put(COL_PRODUCT_QTY, productQuantity);
        long result = db.insert(TABLE_NAME, null, cv);
        if(result == -1){
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context, "Added", Toast.LENGTH_SHORT).show();
        }
    }
    Cursor readAllData(){
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = null;
        if(db != null){
            db.rawQuery(query, null);
        }
        return cursor;
    }
}
