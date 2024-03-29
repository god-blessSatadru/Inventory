package com.example.android.inventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.util.Patterns;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;

/**
 * Created by baba on 9/23/2016.
 */
public class InventoryProvider extends ContentProvider {
    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the inventories table
     */
    private static final int INVENTORIES = 100;

    /**
     * URI matcher code for the content URI for a single product in the inventories table
     */
    private static final int INVENTORY_ID = 101;
    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    /**
     * Database helper object
     */
    private static InventoryDBHelper mDbHelper;

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.inventories/inventories" will map to the
        // integer code {@link #INVENTORIES}. This URI is used to provide access to MULTIPLE rows
        // of the inventories table.
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORIES, INVENTORIES);
        // The content URI of the form "content://com.example.android.inventory/inventories/#" will map to the
        // integer code {@link #INVENTORY_ID}. This URI is used to provide access to ONE single row
        // of the inventories table.

        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.android.inventories/inventories/3" matches, but
        // "content://com.example.android.inventory/inventories" (without a number at the end) doesn't match.
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORIES + "/#", INVENTORY_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        // This cursor will hold the result of the query
        Cursor cursor;
        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORIES:
                // For the INVENTORIES code, query the inventories table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the inventories table.
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case INVENTORY_ID:
                // For the INVENTORY_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.inventories/inventories/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                // This will perform a query on the inventories table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;

    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORIES:
                return insertInventory(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a product into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertInventory(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Product requires a name");
        }
        // Check that the price is valid
        Integer price = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_PRICE);
        if (price == null || price <= 0) {
            throw new IllegalArgumentException("Product requires valid price");
        }
        // If the quantity is provided, check that it's greater than or equal to 0
        Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Product requires valid quantity");
        }
        // If the description is provided, check that it's not null
        String description = values.getAsString(InventoryEntry.COLUMN_PRODUCT_DESCRIPTION);
        if (description == null || description.isEmpty())
            throw new IllegalArgumentException("Product requires a valid description");
        byte[] image = values.getAsByteArray(InventoryEntry.COLUMN_PRODUCT_IMAGE);
        if (image == null || image.length <= 0)
            throw new IllegalArgumentException("Product requires a valid image");
        String supplier = values.getAsString(InventoryEntry.COLUMN_SUPPLIER_NAME);
        if (supplier == null || supplier.isEmpty())
            throw new IllegalArgumentException("Product requires a valid supplier");
        String supplierEmail = values.getAsString(InventoryEntry.COLUMN_SUPPLIER_EMAIL);
        if (supplierEmail == null || !Patterns.EMAIL_ADDRESS.matcher(supplierEmail).matches())
            throw new IllegalArgumentException("There should be valid suppler email address");

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new inventory with the given values
        long id = database.insert(InventoryEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the inventory content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);

    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {

                // For the INVENTORY_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateInventory(uri, contentValues, selection, selectionArgs);
    }

    /**
     * Update inventory in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more products).
     * Return the number of rows that were successfully updated.
     */
    private int updateInventory(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // If the {@link InventoryEntry#COLUMN_PRODUCT_QUANTITY} key is present,
        // check that the weight value is valid.
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_QUANTITY)) {
            Log.v(LOG_TAG, "Contains quantity key");
            Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            if (quantity == null && quantity < 0) {
                throw new IllegalArgumentException("Product requires valid quantity");
            }
            Log.v(LOG_TAG, "" + quantity);

        }
        //  get writable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORIES:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case INVENTORY_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }


}



