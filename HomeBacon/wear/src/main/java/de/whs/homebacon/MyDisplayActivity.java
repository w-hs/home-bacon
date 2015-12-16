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

public class MyDisplayActivity extends Activity {

    private NotesGridPagerAdapter mNotesAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_display);

        //Start services
        getApplication().startService(new Intent(getApplication(), NoteListenerService.class));

        //Create noteAdapter
        mNotesAdapter = new NotesGridPagerAdapter(this, getFragmentManager());
        final GridViewPager pager = (GridViewPager) findViewById(R.id.pager);
        pager.setAdapter(mNotesAdapter);

        updateCards();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        updateCards();
    }

    private void updateCards(){
        DatabaseHelper mDbHelper = new DatabaseHelper(this);
        SQLiteDatabase mDb = mDbHelper.getReadableDatabase();

        mNotesAdapter.clear();
        List<Note> notes = mDbHelper.getAllNotes(mDb,0); //TODO current room
        mNotesAdapter.addNotes(notes);
        mNotesAdapter.notifyDataSetChanged();

        mDb.close();
    }

}