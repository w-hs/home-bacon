package de.whs.homebacon;

import android.content.Intent;
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

import de.whs.homebaconcore.EventType;
import de.whs.homebaconcore.NavigationService;
import de.whs.homebaconcore.NavigationServiceImpl;
import de.whs.homebaconcore.WatchConnector;

public class MainActivity extends AppCompatActivity {

    private final WatchConnector watchConnector = new WatchConnectorImpl();
    private NavigationService mNavService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
}
