package de.whs.homebaconcore;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by pausf on 05.01.2016.
 *
 * Allgemeine Scanner-Klasse für die initiale Messung und die
 * Bestimmung der Position.
 */
public class Scanner implements BeaconListener {
    private Map<String, BeaconScan> scans = new HashMap<>();
    private  BeaconScanner mBeaconScanner;
    private Timer mTimer = new Timer();
    private int mRoomId;
    private List<ScanListener> mListeners = new ArrayList<>();

    public Scanner(int roomId) {
        mRoomId = roomId;
    }

    public void start() {
        mBeaconScanner = new BeaconScanner(null);
        mBeaconScanner.register(this);
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                onSaveScans();
            }
        }, 0, 1100);
    }

    public void stop() {
        mBeaconScanner.unregister(this);
        mTimer.cancel();
        //mBeaconScanner.stopBeaconScan(); //TODO
    }

    public void register(ScanListener listener) {
        mListeners.add(listener);
    }

    public void unregister(ScanListener listener) {
        mListeners.remove(listener);
    }

    @Override
    public void onScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        long timestamp = System.currentTimeMillis();
        BeaconScan scan = scans.get(device.getAddress());
        if (scan == null) {
            scans.put(device.getAddress(), new BeaconScan(mRoomId, rssi, timestamp));
        }
        else {
            scan.setRssi(rssi);
            scan.setTimestamp(timestamp);
        }
    }

    private boolean hasScans(long fadeLimit) {
        for (BeaconScan scan : scans.values()) {
            if (scan.getTimestamp() > fadeLimit) {
                return true;
            }
        }
        return false;
    }

    private void onSaveScans() {
        // Wenn wir länger als 2 Sekunden nichts mehr vom Beacon gehört haben,
        // gehen wir davon aus, dass der Beacon außer Reichweite ist
        long fadeDurationInMs = 2000;
        long currentTime = System.currentTimeMillis();
        long fadeLimit = currentTime - fadeDurationInMs;

        if (!hasScans(fadeLimit))
            return;

        for (String address : scans.keySet()) {
            BeaconScan scan = scans.get(address);
            if (scan.getTimestamp() < fadeLimit) {
                scans.remove(address);
            }
        }

        for (ScanListener listener : mListeners) {
            listener.onScan(scans);
        }
    }
}
