package com.example.android.inventory;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;

/**
 * Created by baba on 9/23/2016.
 */
public class InventoryCursorAdapter extends CursorAdapter
{
    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }
    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        Button sale =(Button)view.findViewById(R.id.sale);
        sale.setTag(""+cursor.getInt(cursor.getColumnIndex(InventoryEntry._ID)));

        // Find the columns of inventory attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
        int priceColumnIndex=cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);

        // Read the product attributes from the Cursor for the current inventory
        String productName = cursor.getString(nameColumnIndex);
        Integer productQuantity = cursor.getInt(quantityColumnIndex);
        Integer productPrice=cursor.getInt(priceColumnIndex);

        // Update the TextViews with the attributes for the current pet
        nameTextView.setText(productName);
        quantityTextView.setText("quantity:"+productQuantity);
        priceTextView.setText("Price:"+productPrice);
    }
}
