package de.whs.homebacon;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.wearable.view.GridViewPager;
import android.widget.TextView;

import de.whs.homebaconcore.DatabaseHelper;
import de.whs.homebaconcore.Note;

public class MyDisplayActivity extends Activity {

    private TextView mTextView;

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

        final NotesGridPagerAdapter adapter = new NotesGridPagerAdapter(this, getFragmentManager());
        adapter.addNote(note);
        adapter.addNote(note1);
        adapter.addNote(note2);
        final GridViewPager pager = (GridViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);
    }
}