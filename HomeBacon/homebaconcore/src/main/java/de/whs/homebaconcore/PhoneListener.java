package de.whs.homebaconcore;

/**
 * Created by Daniel on 17.11.2015.
 */
public interface PhoneListener {
    void onNote(byte[] noteData);
    void onStartScan(byte[] roomId);
    void onStopScan();
    void onSendModel(byte[] model);
}
