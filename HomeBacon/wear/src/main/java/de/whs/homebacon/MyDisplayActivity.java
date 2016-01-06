package de.whs.homebacon;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.wearable.view.GridViewPager;
import android.util.Base64;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.whs.homebaconcore.Constants;
import de.whs.homebaconcore.DatabaseHelper;
import de.whs.homebaconcore.EventType;
import de.whs.homebaconcore.Note;
import de.whs.homebaconcore.Serializer;

public class MyDisplayActivity extends Activity {

    private GridViewPager mPager;
    private NotesGridPagerAdapter mNotesAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try{
            setContentView(R.layout.activity_display);

            //Start services
            getApplication().startService(new Intent(getApplication(), MessageListenerService.class));

            //Create noteAdapter
            mNotesAdapter = new NotesGridPagerAdapter(this, getFragmentManager());
            mPager = (GridViewPager) findViewById(R.id.pager);
            mPager.setAdapter(mNotesAdapter);

            Intent intent = getIntent();
            String event = intent.getStringExtra(Constants.EVENT);
            updateCards(event);

            //update notes in current room
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String event = intent.getStringExtra(Constants.EVENT);
                    updateCards(event);
                }
            }, new IntentFilter(Constants.HOME_BACON_ROOM_CHANGED));
        }
        catch (Exception e){
            Log.e(Constants.DEBUG_TAG, e.getMessage());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String event = intent.getStringExtra(Constants.EVENT);
        updateCards(event);
        mPager.setCurrentItem(1, 0);
    }

    private void updateCards(String event){
        DatabaseHelper mDbHelper = new DatabaseHelper(this);
        SQLiteDatabase mDb = mDbHelper.getReadableDatabase();

        try{
            List<Note> notes = new ArrayList<>();
            switch (event){
                case Constants.ENTER_LEAVE:
                    int oldRoomId = getOldRoom();
                    int newRoomId = getCurrentRoom();
                    notes.addAll(mDbHelper.getAllNotes(mDb,oldRoomId, EventType.LEAVE.toString()));
                    notes.addAll(mDbHelper.getAllNotes(mDb, newRoomId, EventType.ENTER.toString()));
                    break;

                default:
                    //case Constants.CURRENT_ROOM:
                    int roomId = getCurrentRoom();
                    notes.addAll(mDbHelper.getAllNotes(mDb,roomId, EventType.NONE.toString()));
                    break;

            }

            mNotesAdapter.clear();
            mNotesAdapter.addNotes(notes);
        }
        finally {
            mDb.close();
        }
    }

    private int getOldRoom() {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
            int oldRoomId = prefs.getInt(Constants.HOME_BACON_OLD_ROOM, -1);
            return oldRoomId;
        }
        catch (Exception ex) {
            Log.e(Constants.DEBUG_TAG, "Could not load old room id from preferences");
            Log.e(Constants.DEBUG_TAG, ex.getMessage());
        }
        return -1;
    }

    private int getCurrentRoom() {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
            int oldRoomId = prefs.getInt(Constants.HOME_BACON_NEW_ROOM, -1);
            return oldRoomId;
        }
        catch (Exception ex) {
            Log.e(Constants.DEBUG_TAG, "Could not load new / current room id from preferences");
            Log.e(Constants.DEBUG_TAG, ex.getMessage());
        }
        return -1;
    }
}