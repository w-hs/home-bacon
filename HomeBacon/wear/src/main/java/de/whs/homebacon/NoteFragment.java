package de.whs.homebacon;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.wearable.view.CardFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import de.whs.homebaconcore.DatabaseHelper;
import de.whs.homebaconcore.Note;

/**
 * Created by Daniel on 01.12.2015.
 */
public class NoteFragment extends CardFragment {

    private View mRootView;
    private NotesGridPagerAdapter adapter;
    private Context mContext;

    public void setContext(Context ctx){
        this.mContext = ctx;
    }

    @Override
    public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = (ViewGroup) inflater.inflate(R.layout.note_fragment_view, null);

        Bundle bundle = getArguments();
        final Note note = (Note)bundle.getSerializable("note");
        if(note == null)
            return mRootView;

        TextView title = (TextView)mRootView.findViewById(R.id.textView);
        title.setText(note.getTitle());

        TextView value = (TextView)mRootView.findViewById(R.id.textView2);
        value.setText(note.getText());

        ImageButton button = (ImageButton)mRootView.findViewById(R.id.imageButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.removeNote(note);
                adapter.notifyDataSetChanged();
                deleteNoteFromDB(note);
            }
        });

        return mRootView;
    }

    public void setAdapter(NotesGridPagerAdapter adapter) {
        this.adapter = adapter;
    }

    private void deleteNoteFromDB(Note note){
        DatabaseHelper mDbHelper = new DatabaseHelper(mContext);
        SQLiteDatabase mDb = mDbHelper.getWritableDatabase();

        mDbHelper.deleteNote(mDb, note);
    }
}
