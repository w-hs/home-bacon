package de.whs.homebaconcore;

/**
 * Created by Daniel on 17.11.2015.
 */
public interface PhoneListener {
    void onNote(Note note);
    void onNoteWithEvent (Note note, EventType event);
}
