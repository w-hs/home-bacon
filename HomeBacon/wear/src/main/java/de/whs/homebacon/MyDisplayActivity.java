package de.whs.homebacon;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.wearable.view.GridViewPager;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.whs.homebaconcore.DatabaseHelper;
import de.whs.homebaconcore.Note;

public class MyDisplayActivity extends Activity {

    private GridViewPager mPager;
    private NotesGridPagerAdapter mNotesAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_display);

        //Start services
        getApplication().startService(new Intent(getApplication(), NoteListenerService.class));

        //Create noteAdapter
        mNotesAdapter = new NotesGridPagerAdapter(this, getFragmentManager());
        mPager = (GridViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mNotesAdapter);

        updateCards();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        updateCards();
        mPager.setCurrentItem(1, 0);
    }

    private void updateCards(){
        DatabaseHelper mDbHelper = new DatabaseHelper(this);
        SQLiteDatabase mDb = mDbHelper.getReadableDatabase();

        mNotesAdapter.clear();
        List<Note> notes = mDbHelper.getAllNotes(mDb,0); //TODO current room
        mNotesAdapter.addNotes(notes);

        mDb.close();
    }

}