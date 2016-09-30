package com.example.android.inventory;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;

public class ManipulateQuantity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final int INVENTORY_LOADER = 0;
    int quantity;
    String operation = "";
    int quantityAmount;
    Bundle bundle;
    private Uri mCurrentInventoryUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mCurrentInventoryUri = intent.getData();
        if (!intent.hasExtra("Operation") && !intent.hasExtra("Quantity")) {
        } else if (intent.hasExtra("Operation") && !intent.hasExtra("Quantity")) {
            bundle = intent.getExtras();
            operation = bundle.getString("Operation");
        } else {
            bundle = intent.getExtras();
            operation = bundle.getString("Operation");
            quantityAmount = bundle.getInt("Quantity");
        }
        getLoaderManager().initLoader(0, null, this);

    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY};
        return new CursorLoader(this,   // Parent activity context
                mCurrentInventoryUri,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        return;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1)
            return;
        if (cursor.moveToFirst()) {
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            quantity = cursor.getInt(quantityColumnIndex);
            switch (operation) {
                case "track sale":
                    if ((quantity -= quantityAmount) < 0) {
                        Toast.makeText(this, "Quantity becomes smaller than zero", Toast.LENGTH_SHORT).show();
                        getLoaderManager().destroyLoader(0);
                        finish();
                    } else
                        break;
                case "receive shipment":
                    quantity += quantityAmount;
                    break;
                case "delete item":
                    getContentResolver().delete(mCurrentInventoryUri, null, null);
                    getLoaderManager().destroyLoader(0);
                    finish();
                default:
                    if (quantity > 0) {
                        quantity--;

                    }
            }
            ContentValues content = new ContentValues();
            content.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity);
            getContentResolver().update(mCurrentInventoryUri, content, null, null);
            getLoaderManager().destroyLoader(0);
            finish();

        }
    }

}
