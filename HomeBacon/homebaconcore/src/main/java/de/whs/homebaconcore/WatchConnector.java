package de.whs.homebaconcore;

/**
 * Created by Daniel on 17.11.2015.
 */
public interface WatchConnector {

    void sendNote(String note);
    void sendNoteWithEvent(String note, EventType event);
}
