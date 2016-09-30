package com.example.android.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;

/**
 * Created by baba on 9/23/2016.
 */
public class InventoryDBHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG = InventoryDBHelper.class.getSimpleName();
    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "inventory.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    public InventoryDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the inventories table
        String SQL_CREATE_INVENTORIES_TABLE = "CREATE TABLE " + InventoryEntry.TABLE_NAME + " ("
                + InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL, "
                + InventoryEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL, "
                + InventoryEntry.COLUMN_PRODUCT_DESCRIPTION + " TEXT NOT NULL,"
                + InventoryEntry.COLUMN_PRODUCT_IMAGE + " BLOB NOT NULL,"
                + InventoryEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL,"
                + InventoryEntry.COLUMN_SUPPLIER_EMAIL + " TEXT NOT NULL);";
        // Execute the SQL statement
        db.execSQL(SQL_CREATE_INVENTORIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
