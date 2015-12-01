package de.whs.homebacon;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
    private final CardFragment defaultFragment = CardFragment.create("keine notizen","");
    public NotesGridPagerAdapter(Context ctx, FragmentManager fm) {
        super(fm);
        mContext = ctx;
    }

    public void addNote(Note note) {
        mNotes.add(note);
        notifyDataSetChanged();
    }

    public void removeNote(Note note) {
        mNotes.remove(note);
    }

    public void addNotes(List<Note> notes) {
        mNotes.addAll(notes);
    }

    @Override
    public Fragment getFragment(int row, int col) {
        if (mNotes.size() == 0)
            return defaultFragment;

        //CardFragment f = CardFragment.create("sdfsdf", "dfgdfgdf dfgdfgdf gdfgdfg dfgdfgdfg dfgdfgdf gdfgdfgdfg dfgd");

        NoteFragment f = new NoteFragment();
        Bundle b =new Bundle();

        b.putSerializable("note", mNotes.get(col));
        f.setArguments(b);
        f.setAdapter(this);

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
        return 1;
    }

    // Obtain the number of pages (horizontal)
    @Override
    public int getColumnCount(int rowNum) {
        return mNotes.size() > 0 ? mNotes.size() : 1;
    }
}