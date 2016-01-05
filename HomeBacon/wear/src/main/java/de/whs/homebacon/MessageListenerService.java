package de.whs.homebacon;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;

import de.whs.homebaconcore.Constants;
import de.whs.homebaconcore.DatabaseHelper;
import de.whs.homebaconcore.Note;
import de.whs.homebaconcore.PhoneListener;
import de.whs.homebaconcore.Serializer;

/**
 * Created by Dennis on 01.12.2015.
 */
public class MessageListenerService extends WearableListenerService implements PhoneListener{

    private RoomScanner mRoomScanner ;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String path = messageEvent.getPath();

        switch (path){
            case Constants.HOME_BACON_NOTE:
                onNote(messageEvent.getData());
                break;

            case Constants.HOME_BACON_SCAN_START:
                onStartScan(messageEvent.getData());
                break;

            case Constants.HOME_BACON_SCAN_STOP:
                onStopScan();
                break;
        }
    }

    @Override
    public void onNote(byte[] noteData) {
        Log.d(Constants.DEBUG_TAG, "Note received");

        try{
            Note note = (Note) Serializer.deserialize(noteData);
            Log.d(Constants.DEBUG_TAG, note.getText());
            saveNoteInDb(note);
            startActivity();
        }
        catch(Exception e){
            Log.e(Constants.DEBUG_TAG, "Note deserialization failed");
            Log.e(Constants.DEBUG_TAG, e.getMessage());
        }
    }

    @Override
    public void onStartScan(byte[] roomIdData) {
        Log.d(Constants.DEBUG_TAG, "Start scan command received");

        try {
            int roomId = (int) Serializer.deserialize(roomIdData);
            mRoomScanner = new RoomScanner(this,roomId);
            mRoomScanner.startBeaconScan();
            Log.d(Constants.DEBUG_TAG, "Start scan for roomId: " + roomId);
        }
        catch (Exception e){
            Log.e(Constants.DEBUG_TAG, "Room deserialization failed");
            Log.e(Constants.DEBUG_TAG, e.getMessage());
        }
    }

    @Override
    public void onStopScan() {
        Log.d(Constants.DEBUG_TAG, "Stop scan command received");

        mRoomScanner.stopBeaconScan();
        Log.d(Constants.DEBUG_TAG, "Scan stopped");

        //TODO load scans from db and send via PhoneConnector
    }

    private void saveNoteInDb(Note note){
        DatabaseHelper mDbHelper = new DatabaseHelper(this);
        SQLiteDatabase mDb = mDbHelper.getWritableDatabase();

        mDbHelper.insertNote(mDb, note, 0); //TODO current room
        mDb.close();
    }

    private void startActivity(){
        Intent startIntent = new Intent(this, MyDisplayActivity.class);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
        startActivity(startIntent);
    }


}


