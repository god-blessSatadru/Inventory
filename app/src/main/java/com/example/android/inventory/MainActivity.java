package com.example.android.inventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int INVENTORY_LOADER = 0;
    /**
     * Identifier for the Inventory data loader
     */
    int count = 0;
    Uri uri = InventoryEntry.CONTENT_URI;
    /**
     * Adapter for the ListView
     */
    InventoryCursorAdapter mCursorAdapter;
    boolean flag = true;
    ListView inventoryListView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Find the ListView which will be populated with the inventory data
        inventoryListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        inventoryListView.setEmptyView(emptyView);
        Button bt = (Button) findViewById(R.id.empty_subtitle_text);
        bt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, InsertActivity.class);
                startActivity(intent);
            }
        });


        // Setup an Adapter to create a list item for each row of inventory data in the Cursor.
        // There is no inventory data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new InventoryCursorAdapter(this, null);
        inventoryListView.setAdapter(mCursorAdapter);
        // Setup the item click listener
        inventoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ProductDetail.class);
                Uri currentInventoryUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentInventoryUri);

                // Launch the {@link EditorActivity} to display the data for the current inventory.
                startActivity(intent);
            }
        });
// Kick off the loader
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);

    }


    public void decreaseQuantity(View v) {
        Button b = (Button) v;
        int i = Integer.parseInt(b.getTag().toString());
        uri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, i);
        Intent intent = new Intent(MainActivity.this, ManipulateQuantity.class);
        intent.setData(uri);
        startActivity(intent);
        uri = null;
        return;


    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_PRODUCT_PRICE};
        // This loader will execute the ContentProvider's query method on a background thread
        // This loader will execute the ContentProvider's query method on a background thread
        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                uri,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        mCursorAdapter.swapCursor(cursor);

    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
