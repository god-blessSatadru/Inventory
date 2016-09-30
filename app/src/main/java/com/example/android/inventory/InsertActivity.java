package com.example.android.inventory;

import android.app.AlertDialog;
import android.content.CursorLoader;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.LoaderManager;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;

import java.io.ByteArrayOutputStream;

public class InsertActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    public static final String LOG_TAG = InsertActivity.class.getSimpleName();
    /**
     * Identifier for the inventory data loader
     */
    private static final int INVENTORY_LOADER = 0;
    private static final int CAMERA_REQUEST = 1;

    /**
     * Identifier for the inventory data loader
     */
    private static final int EXISTING_INVENTORY_LOADER = 0;
    Bitmap yourImage;
    byte imageInByte[];
    InventoryCursorAdapter mCursorAdapter;
    /**
     * EditText field to enter the product's name
     */
    private EditText mNameEditText;
    /**
     * EditText field to enter the product quamtity
     */
    private EditText mQuantityEditText;
    /**
     * EditText field to enter the product's price
     */
    private EditText mPriceEditText;
    /**
     * EditText field to enter the product's description
     */
    private EditText mDescriptionEditText;
    /* Button to add product image*/
    private Button mAddImage;
    /* EditText field to enter the supplier name */
    private EditText mSupplierEditText;
    /*Edit Text field to enter supplier email*/
    private EditText mSupplierEmailAddress;
    private Button mInsertIntoDB;
    private boolean mInventoryHasChanged = false;
    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mInventoryHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mInventoryHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert);
        // Find all relevant views that we will need to read user input from
        final String[] option = new String[]{"Take from Camera"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.select_dialog_item, option);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Option");
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                Log.e("Selected Item", String.valueOf(which));
                if (which == 0) {
                    callCamera();
                }
            }
        });
        mCursorAdapter = new InventoryCursorAdapter(this, null);
        getLoaderManager().initLoader(0, null, this);
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mQuantityEditText = (EditText) findViewById(R.id.edit_product_quantity);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mDescriptionEditText = (EditText) findViewById(R.id.edit_product_description);
        final AlertDialog dialog = builder.create();
        mAddImage = (Button) findViewById(R.id.add_image);
        mAddImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.show();
            }
        });
        mSupplierEditText = (EditText) findViewById(R.id.edit_supplier_name);
        mSupplierEmailAddress = (EditText) findViewById(R.id.edit_supplier_email);
        mInsertIntoDB = (Button) findViewById(R.id.insert_into_db);
        mInsertIntoDB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                insertData();
            }
        });
        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mDescriptionEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mSupplierEmailAddress.setOnTouchListener(mTouchListener);
        mAddImage.setOnTouchListener(mTouchListener);
    }

    @Override
    public void onBackPressed() {
        // If the inventory hasn't changed, continue with handling back button press
        if (!mInventoryHasChanged) {
            super.onBackPressed();
            Toast.makeText(this, "" + mInventoryHasChanged, Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "" + mInventoryHasChanged, Toast.LENGTH_SHORT).show();

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure to quit without saving");
        builder.setPositiveButton("Yes", discardButtonClickListener);
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the inventory.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_DESCRIPTION,
                InventoryEntry.COLUMN_PRODUCT_IMAGE};
        // This loader will execute the ContentProvider's query method on a background thread
        // This loader will execute the ContentProvider's query method on a background thread
        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                InventoryEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link InventoryCursorAdapter} with this new cursor containing updated inventory data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }

    /**
     * open camera method
     */
    public void callCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST);
        intent.setType("image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 0);
        intent.putExtra("aspectY", 0);
        intent.putExtra("outputX", 50);
        intent.putExtra("outputY", 50);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
            case CAMERA_REQUEST:

                Bundle extras = data.getExtras();

                if (extras != null) {
                    yourImage = extras.getParcelable("data");
                    // convert bitmap to byte
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    yourImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    imageInByte = stream.toByteArray();
                    Toast.makeText(this, "Image taken successfully", Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }

    public void insertData() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        int quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
        int price = Integer.parseInt(mPriceEditText.getText().toString().trim());
        String mDescription = mDescriptionEditText.getText().toString();
        String mSupplierName = mSupplierEditText.getText().toString().trim();
        String mSupplierEmail = mSupplierEmailAddress.getText().toString();
        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, price);
        values.put(InventoryEntry.COLUMN_PRODUCT_DESCRIPTION, mDescription);
        values.put(InventoryEntry.COLUMN_PRODUCT_IMAGE, imageInByte);
        values.put(InventoryEntry.COLUMN_SUPPLIER_NAME, mSupplierName);
        values.put(InventoryEntry.COLUMN_SUPPLIER_EMAIL, mSupplierEmail);
        // This is a NEW product, so insert a new product into the provider,
        // returning the content URI for the new product.
        Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
        if (newUri == null) {
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(this, "Product insertion unsuccessful",
                    Toast.LENGTH_SHORT).show();
        } else {
            finish();
        }

    }

}

