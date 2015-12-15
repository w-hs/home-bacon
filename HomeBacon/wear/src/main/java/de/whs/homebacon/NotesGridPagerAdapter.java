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
    private final CardFragment defaultFragment = CardFragment.create("keine notizen","");
    private final CardFragment settingsFragment = new SettingsFragment();

    public NotesGridPagerAdapter(Context ctx, FragmentManager fm) {
        super(fm);
        mContext = ctx;

        mNotes.add(new Note("s",""));
    }

    public void addNote(Note note) {
        mNotes.add(0,note);
        notifyDataSetChanged();
    }

    public void removeNote(Note note) {

        if(mNotes.size()!=1)
        mNotes.remove(note);
    }

    public void addNotes(List<Note> notes) {
        mNotes.addAll(0,notes);
    }

    @Override
    public Fragment getFragment(int row, int col) {
        if (mNotes.size() == 0) {

            return defaultFragment;
        }
        else
        {


        }

        if(mNotes.get(col).getTitle()=="s")
        {

            return settingsFragment;
        }

        //CardFragment f = CardFragment.create("sdfsdf", "dfgdfgdf dfgdfgdf gdfgdfg dfgdfgdfg dfgdfgdf gdfgdfgdfg dfgd");

        NoteFragment f = new NoteFragment();
        Bundle b =new Bundle();

        b.putSerializable("note", mNotes.get(col));
        f.setArguments(b);
        f.setAdapter(this);
        f.setContext(mContext);

        return f;
    }

    // Obtain the background image for the row
   // @Override
   // public Drawable getBackgroundForRow(int row) {
   //     return mContext.getResources().getDrawable(R.drawable.bg);
   // }

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