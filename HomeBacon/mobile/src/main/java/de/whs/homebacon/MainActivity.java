package de.whs.homebacon;

import android.content.Intent;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import de.whs.homebaconcore.DatabaseHelper;
import de.whs.homebaconcore.EventType;
import de.whs.homebaconcore.NavigationService;
import de.whs.homebaconcore.NavigationServiceImpl;
import de.whs.homebaconcore.WatchConnector;

public class MainActivity extends AppCompatActivity {

    private WatchConnector watchConnector;
    private NavigationService mNavService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        watchConnector = new WatchConnectorImpl(this);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);

        final EditText noticeTextbox = (EditText) findViewById(R.id.notizText);
        final Spinner spinner = (Spinner) findViewById(R.id.eventSpinner);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Notiz hinterlegt", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                if(spinner.getSelectedItemPosition() == 0)
                {
                    watchConnector.sendNote(noticeTextbox.getText().toString());
                }
                if(spinner.getSelectedItemPosition() == 1)
                {
                    watchConnector.sendNoteWithEvent(noticeTextbox.getText().toString(), EventType.ENTER);
                }
                if(spinner.getSelectedItemPosition() == 2)
                {
                    watchConnector.sendNoteWithEvent(noticeTextbox.getText().toString(), EventType.LEAVE);
                }

                noticeTextbox.setText("");
                spinner.setSelection(0);

                DatabaseHelper mDbHelper = new DatabaseHelper(getApplicationContext());

                // Gets the data repository in write mode
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                mDbHelper.onUpgrade(db, 1, 1);

                // Cursor cursor2 = db.
                //         rawQuery(".schema notes;",null);



                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_NOTES_NAME_NOTE_ID, 1);
                values.put(DatabaseHelper.COLUMN_NOTES_NAME_TITLE, "Hallo");
                values.put(DatabaseHelper.COLUMN_NOTES_NAME_TEXT, "Eine super notiz");
                values.put(DatabaseHelper.COLUMN_NOTES_NAME_TIMESTAMP,  System.currentTimeMillis());
                values.put(DatabaseHelper.COLUMN_NOTES_NAME_EVENT, "null");
                values.put(DatabaseHelper.COLUMN_NOTES_NAME_ROOM_ID, 100);

                long newRowId;
                newRowId = db.insert(
                        DatabaseHelper.TABLE_NOTES_NAME,
                        null,
                        values);





                // Define a projection that specifies which columns from the database
                // you will actually use after this query.
                String[] projection = {
                        DatabaseHelper.COLUMN_NOTES_NAME_NOTE_ID,
                        DatabaseHelper.COLUMN_NOTES_NAME_TITLE,
                        DatabaseHelper.COLUMN_NOTES_NAME_TEXT,
                        DatabaseHelper.COLUMN_NOTES_NAME_TIMESTAMP,
                        DatabaseHelper.COLUMN_NOTES_NAME_EVENT,
                        DatabaseHelper.COLUMN_NOTES_NAME_ROOM_ID
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
              //  long itemId = cursor.getLong(
               //         cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTES_NAME_TITLE)
                //);

                //mTextView.setText(""+cursor.getString(2));
                noticeTextbox.setText(""+cursor.getString(2));
            }
        });


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.eventTypes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        //Bluetooth
        mNavService = new NavigationServiceImpl(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if (id == R.id.action_measure) {
            Intent intent = new Intent(this, RoomScanner.class);
            startActivityForResult(intent, 0);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        watchConnector.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        watchConnector.disconnect();
    }
}
