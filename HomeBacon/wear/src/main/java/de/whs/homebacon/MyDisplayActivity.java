package de.whs.homebacon;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.GridViewPager;
import android.widget.TextView;

import de.whs.homebaconcore.DatabaseHelper;

public class MyDisplayActivity extends Activity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        mTextView = (TextView) findViewById(R.id.text);

        DatabaseHelper mDbHelper = new DatabaseHelper(this.getApplicationContext());

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        mDbHelper.onUpgrade(db, 1, 1);

      // Cursor cursor2 = db.
       //         rawQuery(".schema notes;",null);



        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NOTES_NAME_NOTEID, 1);
        values.put(DatabaseHelper.COLUMN_NOTES_NAME_TITLE, "Hallo");
        values.put(DatabaseHelper.COLUMN_NOTES_NAME_TEXT, "Eine super notiz");
        values.put(DatabaseHelper.COLUMN_NOTES_NAME_TIMESTAMP,  System.currentTimeMillis());
        values.put(DatabaseHelper.COLUMN_NOTES_NAME_EVENT, "null");
        values.put(DatabaseHelper.COLUMN_NOTES_NAME_ROOMID, 100);

        long newRowId;
        newRowId = db.insert(
                DatabaseHelper.TABLE_NOTES_NAME,
                null,
                values);





        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DatabaseHelper.COLUMN_NOTES_NAME_NOTEID,
                DatabaseHelper.COLUMN_NOTES_NAME_TITLE,
                DatabaseHelper.COLUMN_NOTES_NAME_TEXT,
                DatabaseHelper.COLUMN_NOTES_NAME_TIMESTAMP,
                DatabaseHelper.COLUMN_NOTES_NAME_EVENT,
                DatabaseHelper.COLUMN_NOTES_NAME_ROOMID
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DatabaseHelper.COLUMN_NOTES_NAME_TIMESTAMP + " DESC";

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_NOTES_NAME, // The table to query
                projection,                      // The columns to return
                null,                            // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                            // don't group the rows
                null,                            // don't filter by row groups
                sortOrder                        // The sort order
        );



        cursor.moveToFirst();
        long itemId = cursor.getLong(
                cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTES_NAME_TITLE)
        );

       // mTextView.setText("" + cursor.getString(2));





        final GridViewPager pager = (GridViewPager) findViewById(R.id.pager);
        pager.setAdapter(new SampleGridPagerAdapter(this, getFragmentManager()));





    }



}
