package de.whs.homebacon;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;

import android.os.Bundle;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import de.whs.homebaconcore.Note;

public class NotesGridPagerAdapter extends FragmentGridPagerAdapter {

    private final List<Note> mNotes = new ArrayList<Note>();
    private final Context mContext;

    public NotesGridPagerAdapter(Context ctx, FragmentManager fm) {
        super(fm);
        mContext = ctx;
    }

    public void addNote(Note note) {
        mNotes.add(0,note);
        notifyDataSetChanged();
    }

    public void removeNote(Note note) {
        mNotes.remove(note);
        notifyDataSetChanged();
    }

    public void addNotes(List<Note> notes) {
        mNotes.addAll(0, notes);
        notifyDataSetChanged();
    }

    @Override
    public Fragment getFragment(int row, int col) throws IndexOutOfBoundsException {
        //no notes -> No notes card
        if (mNotes.size() == 0) {
            return CardFragment.create("Keine \n Notizen","");
        }
        //notes -> note cards
        else {
            NoteFragment f = new NoteFragment();
            Bundle b =new Bundle();

            b.putSerializable("note", mNotes.get(col));
            f.setArguments(b);
            f.setAdapter(this);
            f.setContext(mContext);

            return f;
        }
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

    public void clear() {
        mNotes.clear();
    }


}