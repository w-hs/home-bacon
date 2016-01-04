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
import de.whs.homebaconcore.Serializer;

/**
 * Created by Dennis on 01.12.2015.
 */
public class NoteListenerService extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String p = messageEvent.getPath();
        if (p.equals(Constants.HOME_BACON_NOTE)) {
            Log.d(Constants.DEBUG_TAG, "Note received");

            try{
                Note note = (Note) Serializer.deserialize(messageEvent.getData());
                Log.d(Constants.DEBUG_TAG, note.getText());
                saveNoteInDb(note);
                startActivity();
            }
            catch(Exception e){
                Log.e(Constants.DEBUG_TAG, "Note deserialization failed");
                Log.e(Constants.DEBUG_TAG, e.getMessage());
            }
        }
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


