package de.whs.homebaconcore;

/**
 * Created by Daniel on 17.11.2015.
 */
public interface PhoneListener {

    void onNote(String note);
    void onNoteWithEvent (String note, EventType event);

}
