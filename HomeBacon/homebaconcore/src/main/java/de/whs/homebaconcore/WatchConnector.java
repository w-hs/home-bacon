package de.whs.homebaconcore;

/**
 * Created by Daniel on 17.11.2015.
 */
public interface WatchConnector {

    void sendNote(Note note);
    void startScan(int roomId);
    void stopScan();
}
