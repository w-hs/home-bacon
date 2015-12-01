package de.whs.homebacon;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.view.Gravity;
import android.view.View;

import java.util.List;

import de.whs.homebaconcore.Note;


// Using it as "1D-Picker": http://developer.android.com/design/wear/structure.html

public class NotesGridPagerAdapter extends FragmentGridPagerAdapter {

    private final Context mContext;
    private List mRows;

    public NotesGridPagerAdapter(Context ctx, FragmentManager fm) {
        super(fm);
        mContext = ctx;
    }

    @Override
    public Fragment getFragment(int row, int col) {
        Note note = notes[row][col];
        CardFragment fragment = CardFragment.create(note.getTitle(), note.getText());

        // Advanced settings (card gravity, card expansion/scrolling)
        fragment.setCardGravity(Gravity.CENTER);
        fragment.setExpansionEnabled(true);
        // fragment.setExpansionDirection(Path.Direction.NONE);
        // fragment.setExpansionFactor(page.expansionFactor);


        NoteFragment f = new NoteFragment();
        Bundle b =new Bundle();
        b.putSerializable("note",note);
        f.setArguments(b);
       // f.setNote(note);

        return f;
    }

    // Obtain the background image for the row
    @Override
    public Drawable getBackgroundForRow(int row) {
        return mContext.getResources().getDrawable(R.drawable.card_background);
    }

    // Obtain the number of pages (vertical)
    @Override
    public int getRowCount() {
        return notes.length;
    }

    // Obtain the number of pages (horizontal)
    @Override
    public int getColumnCount(int rowNum) {
        return notes[rowNum].length;
    }



    // Create a static set of pages in a 2D array
    private final Note[][] notes = {
            {new Note("1", "1"), new Note("2", "1"), new Note("3", "1")}};
}