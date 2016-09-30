package com.example.android.inventory;

import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;

public class ProductDetail extends AppCompatActivity implements
        LoaderCallbacks<Cursor>, OnClickListener {
    String sendEmailTo = "";
    boolean flag = true;
    Bundle b;
    int count = 0;
    private Uri mCurrentInventoryUri;
    private ImageView productImage;
    private TextView productDescription;
    private TextView productQuantity;
    private TextView productSupplierName;
    private TextView productSupplierEmail;
    private Button trackSale;
    private Button receiveShipment;
    private Button order;
    private Button delete;
    private int quantityAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = savedInstanceState;
        setContentView(R.layout.activity_product_detail);
        Intent intent = getIntent();
        mCurrentInventoryUri = intent.getData();
        productImage = (ImageView) findViewById(R.id.img_view_desc);
        productDescription = (TextView) findViewById(R.id.prod_detail);
        productQuantity = (TextView) findViewById(R.id.quan_detail);
        productSupplierName = (TextView) findViewById(R.id.sup_detail);
        productSupplierEmail = (TextView) findViewById(R.id.sup_email);
        trackSale = (Button) findViewById(R.id.tracksale);
        receiveShipment = (Button) findViewById(R.id.receiveshipment);
        order = (Button) findViewById(R.id.order);
        delete = (Button) findViewById(R.id.delete);
        trackSale.setOnClickListener(this);
        receiveShipment.setOnClickListener(this);
        order.setOnClickListener(this);
        delete.setOnClickListener(this);
        getLoaderManager().initLoader(0, null, this);

    }

    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, null, this);

    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.order:
                placeOrder();
                break;
            case R.id.delete:
                deleteDialog();
                break;
            case R.id.tracksale:
                trackSaleAlertDialog();
                break;
            case R.id.receiveshipment:
                trackReceiveShipmentDialog();
                break;

        }
    }

    public void deleteDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete");
        builder.setMessage("Are you sure? ");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                performItemDelete();
                finish();
            }
        });
        builder.setNegativeButton("Dissmis", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void performItemDelete() {
        Intent intent = new Intent(ProductDetail.this, ManipulateQuantity.class);
        intent.putExtra("Operation", "delete item");
        intent.setData(mCurrentInventoryUri);
        startActivity(intent);
    }

    public void placeOrder() {

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{sendEmailTo});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Placement of order");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }

    }

    public void trackReceiveShipmentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Receive Shipment");
        builder.setMessage("Increase Quantity by? ");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setRawInputType(Configuration.KEYBOARD_12KEY);
        builder.setView(input);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                quantityAmount = Integer.parseInt(input.getText().toString());
                performShipmentReceive();
            }
        });
        builder.setNegativeButton("Dissmis", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void performShipmentReceive() {
        Intent intent = new Intent(ProductDetail.this, ManipulateQuantity.class);

        intent.putExtra("Quantity", quantityAmount);
        intent.putExtra("Operation", "receive shipment");
        intent.setData(mCurrentInventoryUri);
        startActivity(intent);
    }

    public void trackSaleAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Track Sale");
        builder.setMessage("Decrese Quantity by? ");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setRawInputType(Configuration.KEYBOARD_12KEY);
        builder.setView(input);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                quantityAmount = Integer.parseInt(input.getText().toString());
                performSalesTracking();
            }
        });
        builder.setNegativeButton("Dissmis", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void performSalesTracking() {
        Intent intent = new Intent(ProductDetail.this, ManipulateQuantity.class);

        intent.putExtra("Quantity", quantityAmount);
        intent.putExtra("Operation", "track sale");
        intent.setData(mCurrentInventoryUri);

        startActivity(intent);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_IMAGE,
                InventoryEntry.COLUMN_PRODUCT_DESCRIPTION,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_SUPPLIER_NAME,
                InventoryEntry.COLUMN_SUPPLIER_EMAIL};
        return new CursorLoader(this,   // Parent activity context
                mCurrentInventoryUri,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            int nameImageIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_IMAGE);
            int nameDescriptionIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_DESCRIPTION);
            int nameQuantityIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierNameIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_NAME);
            int supplierEmailIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_EMAIL);
            byte[] imageArray = cursor.getBlob(nameImageIndex);
            Bitmap theImage = BitmapFactory.decodeByteArray(imageArray, 0, imageArray.length);
            productImage.setImageBitmap(theImage);
            productImage.setMaxHeight(10);
            productImage.setMaxWidth(10);
            String description = cursor.getString(nameDescriptionIndex);
            int quantity = cursor.getInt(nameQuantityIndex);
            String supplierName = cursor.getString(supplierNameIndex);
            String supplierEmail = cursor.getString(supplierEmailIndex);
            sendEmailTo = supplierEmail;
            productDescription.setText(description);
            productQuantity.setText("" + quantity);
            productSupplierName.setText(supplierName);
            productSupplierEmail.setText(supplierEmail);
            getLoaderManager().destroyLoader(0);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


}
