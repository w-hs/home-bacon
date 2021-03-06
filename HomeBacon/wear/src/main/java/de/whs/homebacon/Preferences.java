package de.whs.homebacon;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import de.whs.homebaconcore.Constants;

/**
 * Created by pausf on 06.01.2016.
 */
public class Preferences {

    public static int getCurrentRoom(Context context) {
        int currentRoomId = -1;
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            Log.d(Constants.DEBUG_TAG, "Before getting prefs");
            currentRoomId = prefs.getInt(Constants.HOME_BACON_NEW_ROOM, -1);
            Log.d(Constants.DEBUG_TAG, "After getting prefs " + currentRoomId);
        }
        catch (Exception ex) {
            Log.e(Constants.DEBUG_TAG, "Could not load new / current room id from preferences");
            Log.e(Constants.DEBUG_TAG, ex.getMessage());
        }
        return currentRoomId;
    }

    public static int getOldRoom(Context context) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            int oldRoomId = prefs.getInt(Constants.HOME_BACON_OLD_ROOM, -1);
            return oldRoomId;
        }
        catch (Exception ex) {
            Log.e(Constants.DEBUG_TAG, "Could not load old room id from preferences");
            Log.e(Constants.DEBUG_TAG, ex.getMessage());
        }
        return -1;
    }
}
