package de.whs.homebaconcore;

/**
 * Created by pausf on 25.11.2015.
 */
public class BeaconScan {
    private String address;
    private int rssi;

    public BeaconScan(String address, int rssi) {
        this.address = address;
        this.rssi = rssi;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String id) {
        this.address = address;
    }

    @Override
    public String toString() {
        return address + ": " + rssi;
    }
}
