package de.whs.homebaconcore;

import android.bluetooth.BluetoothDevice;

/**
 * Created by pausf on 25.11.2015.
 */
public interface BeaconListener {

    void onScan(BluetoothDevice device, int rssi, byte[] scanRecord);
}
