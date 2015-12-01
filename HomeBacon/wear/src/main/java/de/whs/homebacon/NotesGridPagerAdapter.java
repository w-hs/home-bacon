package de.whs.homebacon;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;

import android.graphics.drawable.Drawable;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import de.whs.homebaconcore.Note;

public class NotesGridPagerAdapter extends FragmentGridPagerAdapter {

    private final List<Note> mNotes = new ArrayList<Note>();
    private final Context mContext;
    private List mRows;

    public NotesGridPagerAdapter(Context ctx, FragmentManager fm) {
        super(fm);
        mContext = ctx;
    }

    public void addNote(Note note) {
        mNotes.add(note);
    }

    public void addNotes(List<Note> notes) {
        mNotes.addAll(notes);
    }

    @Override
    public Fragment getFragment(int row, int col) {

        Log.d("fragment", "GetFragment called. Row=" + row + " Col=" + col);

        Note note = mNotes.get(col);//notes[row][col];
        CardFragment fragment = CardFragment.create(note.getTitle(), note.getText());


        // Advanced settings (card gravity, card expansion/scrolling)
        fragment.setCardGravity(Gravity.CENTER);
        fragment.setExpansionEnabled(true);
        // fragment.setExpansionDirection(Path.Direction.NONE);
        // fragment.setExpansionFactor(page.expansionFactor);
        return fragment;
    }

    // Obtain the background image for the row
    @Override
    public Drawable getBackgroundForRow(int row) {
        return mContext.getResources().getDrawable(R.drawable.card_background);
    }

    // Obtain the number of pages (vertical)
    @Override
    public int getRowCount() {
        return 1;
    }

    // Obtain the number of pages (horizontal)
    @Override
    public int getColumnCount(int rowNum) {
        return mNotes.size();
    }
}