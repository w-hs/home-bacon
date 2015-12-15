package de.whs.homebacon;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.GridViewPager;
import android.util.Log;
import android.widget.TextView;

import java.util.List;

import de.whs.homebaconcore.Constants;
import de.whs.homebaconcore.DatabaseHelper;
import de.whs.homebaconcore.Note;
import de.whs.homebaconcore.PhoneListener;

public class MyDisplayActivity extends Activity implements PhoneListener {

    private TextView mTextView;
    private NotesGridPagerAdapter mNotesAdapter;
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_display);

        //Start wearable/note listener service
        getApplication().startService(new Intent(getApplication(), NoteListenerService.class));

        //Create DatabaseHelper and db
        mDbHelper = new DatabaseHelper(this.getApplicationContext());
        mDb = mDbHelper.getWritableDatabase();

        //Create noteAdapter
        mNotesAdapter = new NotesGridPagerAdapter(this, getFragmentManager());
        final GridViewPager pager = (GridViewPager) findViewById(R.id.pager);
        pager.setAdapter(mNotesAdapter);

        //on new note
        Intent intent = getIntent();
        if (intent != null ){
            Note note = (Note) intent.getSerializableExtra(Constants.HOME_BACON_NOTE);
            if (note != null) this.onNote(note);
        }

        //load notes from db
        List<Note> notes = mDbHelper.getAllNotes(mDb,0); //TODO current room
        mNotesAdapter.addNotes(notes);

        mDb.close();
    }

    @Override
    public void onNote(Note note) {
        Log.d(Constants.DEBUG_TAG, "note in MyDisplayActivity receieved - " + note.getText());

        //save note in db
        mDbHelper.insertNote(mDb, note, 0); //TODO current room

        //add to adapter
        //mNotesAdapter.addNote(note);
    }
}