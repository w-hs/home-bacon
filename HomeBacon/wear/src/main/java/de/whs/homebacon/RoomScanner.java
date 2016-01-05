package de.whs.homebacon;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.whs.homebaconcore.BeaconListener;
import de.whs.homebaconcore.BeaconScan;
import de.whs.homebaconcore.BeaconScanner;
import de.whs.homebaconcore.DatabaseHelper;

/**
 * Created by Dennis on 05.01.2016.
 */
public class RoomScanner implements BeaconListener{
    private Map<String, BeaconScan> scans = new HashMap<>();
    private Context mContext;
    private  BeaconScanner mBeaconScanner;
    private int mRoomId;
    private Timer mSaveTimer = new Timer();
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    public RoomScanner (Context context, int roomId){
        mContext = context;
        mRoomId  = roomId;
    }

    public void startBeaconScan() {
        mDbHelper = new DatabaseHelper(mContext);
        mDb = mDbHelper.getWritableDatabase();

        mBeaconScanner = new BeaconScanner(null);
        mBeaconScanner.register(this);
        mSaveTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                onSaveScans();
            }
        }, 0, 1100);
    }

    public void stopBeaconScan() {
        mBeaconScanner.unregister(this);
        mSaveTimer.cancel();
        //mBeaconScanner.stopBeaconScan(); //TODO

        mDb.close();
        mDbHelper.close();
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

        long scanId = mDbHelper.insertScan(mDb, mRoomId);

        for (String address : scans.keySet()) {
            BeaconScan scan = scans.get(address);
            if (scan.getTimestamp() > fadeLimit) {
                mDbHelper.insertScannedTag(mDb, scanId, address, scan.getRssi());
                Log.i("HomeBeacon", "room=" + mRoomId + ", addr=" + address + ", rssi="
                        + scan.getRssi());
            }
        }

    }
}
