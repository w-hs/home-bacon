package de.whs.homebaconcore;

import java.io.Serializable;

/**
 * Created by pausf on 25.11.2015.
 */
public class BeaconScan implements Serializable {
    private int roomId;
    private int rssi;
    private long timestamp;
    private String address;
    private long scanId;

    public BeaconScan(int roomId, int rssi, long timestamp) {
        this.roomId = roomId;
        this.rssi = rssi;
        this.timestamp = timestamp;
    }

    public BeaconScan(long scanId, int roomId, String address, int rssi) {
        this.scanId = scanId;
        this.roomId = roomId;
        this.address = address;
        this.rssi = rssi;
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

    public long getScanId() {
        return scanId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setScanId(long scanId) {
        this.scanId = scanId;
    }

    public String getAsCSV(){
        return scanId + "," + roomId + "," + address + "," + rssi;
    }
}
