package de.whs.homebacon;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;

import de.whs.homebaconcore.BeaconScan;
import de.whs.homebaconcore.Constants;
import de.whs.homebaconcore.DatabaseHelper;
import de.whs.homebaconcore.Serializer;
import de.whs.homebaconcore.WatchListener;

/**
 * Created by Dennis on 01.12.2015.
 */
public class MessageListenerService extends WearableListenerService implements WatchListener{

    private RoomScannerActivity mRoomScanner ;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String path = messageEvent.getPath();

        switch (path){
            case Constants.HOME_BACON_SCAN_RESULTS:
                onScanResults(messageEvent.getData());
                break;
        }
    }

    @Override
    public void onScanResults(byte[] scanResultData) {
        Log.d(Constants.DEBUG_TAG, "Scan results received");

        try{
            List<BeaconScan> scanResults = (List<BeaconScan>) Serializer.deserialize(scanResultData);
            Log.d(Constants.DEBUG_TAG, scanResults.size() + " scans received");
            saveScansInDb(scanResults);
        }
        catch(Exception e){
            Log.e(Constants.DEBUG_TAG, "Note deserialization failed");
            Log.e(Constants.DEBUG_TAG, e.getMessage());
        }
    }

    private void saveScansInDb(List<BeaconScan> scans){
        DatabaseHelper mDbHelper = new DatabaseHelper(this);
        SQLiteDatabase mDb = mDbHelper.getWritableDatabase();

        int watchScanId = -1;
        long phoneScanId = 0;
        for(BeaconScan scan : scans){
            if (watchScanId != scan.getScanId()){
                watchScanId = (int) scan.getScanId();
                phoneScanId = mDbHelper.insertScan(mDb, scan.getRoomId());
            }
            mDbHelper.insertScannedTag(mDb, phoneScanId, scan.getAddress(), scan.getRssi());
        }
        mDb.close();
    }
}


