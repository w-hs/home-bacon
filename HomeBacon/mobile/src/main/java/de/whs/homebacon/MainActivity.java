package de.whs.homebacon;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import de.whs.homebaconcore.Note;
import de.whs.homebaconcore.WatchConnector;

public class MainActivity extends AppCompatActivity {

    private WatchConnector watchConnector;
    private NavigationService mNavService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        watchConnector = new WatchConnectorImpl(this);
        getApplication().startService(new Intent(getApplication(), MessageListenerService.class));

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);

        final EditText noteTitleEditText = (EditText) findViewById(R.id.notizTitel);
        final EditText noteTextEditText = (EditText)findViewById(R.id.notizText);
        final Spinner spinner = (Spinner) findViewById(R.id.eventSpinner);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Note note = new Note();
                note.setTitle(noteTitleEditText.getText().toString());
                note.setText(noteTextEditText.getText().toString());

                switch (spinner.getSelectedItemPosition()){
                    case 0:
                        note.setEventType(EventType.NONE);
                        break;

                    case 1:
                        note.setEventType(EventType.ENTER);
                        break;

                    case 2:
                        note.setEventType(EventType.LEAVE);
                        break;
                }

                watchConnector.sendNote(note);
            }
        });


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.eventTypes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
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
            Intent intent = new Intent(this, RoomScannerActivity.class);
            startActivityForResult(intent, 0);
        }

        return super.onOptionsItemSelected(item);
    }
}
