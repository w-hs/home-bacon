package de.whs.homebaconcore;

/**
 * Created by pausf on 25.11.2015.
 */
public class BeaconScan {
    private int roomId;
    private int rssi;
    private long timestamp;

    public BeaconScan(int roomId, int rssi, long timestamp) {
        this.roomId = roomId;
        this.rssi = rssi;
        this.timestamp = timestamp;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
