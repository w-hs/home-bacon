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

import de.whs.homebaconcore.DatabaseHelper;
import de.whs.homebaconcore.Note;

public class MyDisplayActivity extends Activity {

    private TextView mTextView;
    private Intent mServiceIntent;
    private NotesGridPagerAdapter mNotesAdapter;

    BroadcastReceiver mReceiver;

    private void createReceiver() {

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Log.w("broadcast", "receive Intent:"+ intent.getAction().toString());
                if (mNotesAdapter == null) {
                    Log.w("broadcast", "mNotesAdapter is null!");
                    return;
                }

                if (intent.getAction().equals(IntentIds.NewNoteId)) {
                    Note note = (Note)intent.getSerializableExtra("note");
                    mNotesAdapter.addNote(note);
                }
                else {
                    Log.w("broadcast", "intent action not match: " + intent.getAction());
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver((mReceiver), new IntentFilter(IntentIds.NewNoteId));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        mTextView = (TextView) findViewById(R.id.text);

        DatabaseHelper mDbHelper = new DatabaseHelper(this.getApplicationContext());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        mDbHelper.onUpgrade(db, 1, 1);

        Note note = new Note("Notiz", "Eine tolle erste Notiz");
        Note note1 = new Note("Notiz 1", "blajdgb");
        Note note2 = new Note("Notiz 2", "ajbgj");

        mDbHelper.insertNote(db, note);

        mNotesAdapter = new NotesGridPagerAdapter(this, getFragmentManager());
        mNotesAdapter.addNote(note);
        mNotesAdapter.addNote(note1);
        mNotesAdapter.addNote(note2);
        final GridViewPager pager = (GridViewPager) findViewById(R.id.pager);
        pager.setAdapter(mNotesAdapter);

        createReceiver();

        mServiceIntent = new Intent(getApplication(), DanielsService.class);
        getApplication().startService(mServiceIntent);


    }
}