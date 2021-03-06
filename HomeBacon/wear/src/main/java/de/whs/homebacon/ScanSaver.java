package de.whs.homebacon;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.Map;

import de.whs.homebaconcore.BeaconScan;
import de.whs.homebaconcore.DatabaseHelper;
import de.whs.homebaconcore.ScanListener;
import de.whs.homebaconcore.Scanner;

/**
 * Created by Dennis on 05.01.2016.
 *
 * Speichert Messwerte für einen bestimmten Raum in der Datenbank.
 */
public class ScanSaver implements ScanListener {
    private Context mContext;
    private int mRoomId;
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    public ScanSaver(Context context, int roomId){
        mContext = context;
        mRoomId  = roomId;
    }

    public void startBeaconScan() {
        mDbHelper = new DatabaseHelper(mContext);
        mDb = mDbHelper.getWritableDatabase();
    }

    public void stopBeaconScan() {
        mDb.close();
        mDbHelper.close();
    }

    @Override
    public void onScan(Map<String, BeaconScan> scans) {
        long scanId = mDbHelper.insertScan(mDb, mRoomId);
        for (String address : scans.keySet()) {
            BeaconScan scan = scans.get(address);
            mDbHelper.insertScannedTag(mDb, scanId, address, scan.getRssi());
            Log.i("HomeBeacon", "room=" + mRoomId + ", addr=" + address + ", rssi="
                    + scan.getRssi());
        }
    }
}
