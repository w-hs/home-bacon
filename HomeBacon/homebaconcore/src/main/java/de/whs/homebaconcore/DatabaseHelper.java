package de.whs.homebaconcore;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniel on 24.11.2015.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "bacon.db";

    public static final String TABLE_NOTES_NAME = "notes";
    public static final String COLUMN_NOTES_NAME_NOTEID  = "noteid";
    public static final String COLUMN_NOTES_NAME_TITLE = "title";
    public static final String COLUMN_NOTES_NAME_TEXT = "text";
    public static final String COLUMN_NOTES_NAME_TIMESTAMP = "timestamp";
    public static final String COLUMN_NOTES_NAME_ROOMID = "roomid";
    public static final String COLUMN_NOTES_NAME_EVENT = "event";

    public static final String TABLE_ITEMS_NAME = "item";
    public static final String COLUMN_ITEMS_NAME_BBTAG  = "bbtag";
    public static final String COLUMN_ITEMS_NAME_DESCRIPTION = "description";
    public static final String COLUMN_ITEMS_NAME_ROOMID = "roomid";

    public static final String TABLE_SCAN_NAME = "beaconscan";
    public static final String COLUMN_SCAN_NAME_BBTAG  = "bbtag";
    public static final String COLUMN_SCAN_NAME_RSSI = "rssi";
    public static final String COLUMN_SCAN_NAME_ROOMID = "roomid";

    public static final String TABLE_ROOM_NAME = "room";
    public static final String COLUMN_ROOM_NAME_NAME = "name";
    public static final String COLUMN_ROOM_NAME_ROOMID = "roomid";


    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_NOTES =
            "CREATE TABLE " + TABLE_NOTES_NAME + " (" +
                    COLUMN_NOTES_NAME_NOTEID        + INT_TYPE + " PRIMARY KEY," +
                    COLUMN_NOTES_NAME_TITLE         + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NOTES_NAME_TEXT          + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NOTES_NAME_TIMESTAMP     + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NOTES_NAME_ROOMID        + INT_TYPE + COMMA_SEP +
                    COLUMN_NOTES_NAME_EVENT         + TEXT_TYPE +
            " )";

    private static final String SQL_CREATE_ITEMS =
            "CREATE TABLE " + TABLE_ITEMS_NAME + " (" +
                    COLUMN_ITEMS_NAME_BBTAG           + TEXT_TYPE + " PRIMARY KEY," +
                    COLUMN_ITEMS_NAME_DESCRIPTION     + TEXT_TYPE + COMMA_SEP +
                    COLUMN_ITEMS_NAME_ROOMID          + INT_TYPE +
                    " )";


    private static final String SQL_CREATE_SCANS =
            "CREATE TABLE " + TABLE_SCAN_NAME + " (" +
                    COLUMN_SCAN_NAME_BBTAG           + TEXT_TYPE + COMMA_SEP +
                    COLUMN_SCAN_NAME_RSSI            + INT_TYPE + COMMA_SEP +
                    COLUMN_SCAN_NAME_ROOMID          + INT_TYPE +
                    " )";


    private static final String SQL_CREATE_ROOMS =
            "CREATE TABLE " + TABLE_ROOM_NAME + " (" +
                    COLUMN_ROOM_NAME_ROOMID           + INT_TYPE + " PRIMARY KEY," +
                    COLUMN_ROOM_NAME_NAME + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_NOTES =
            "DROP TABLE IF EXISTS " + TABLE_NOTES_NAME;

    private static final String SQL_DELETE_ITEMS =
            "DROP TABLE IF EXISTS " + TABLE_ITEMS_NAME;

    private static final String SQL_DELETE_SCANS =
            "DROP TABLE IF EXISTS " + TABLE_SCAN_NAME;

    private static final String SQL_DELETE_ROOMS =
            "DROP TABLE IF EXISTS " + TABLE_ROOM_NAME;


    public DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public Room insertRoom(SQLiteDatabase db, String name) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_ROOM_NAME_NAME, name);

        long roomId = db.insert(DatabaseHelper.TABLE_ROOM_NAME, null, values);
        return new Room(roomId, name);
    }

    public List<Room> getAllRooms(SQLiteDatabase db) {
        List<Room> rooms = new ArrayList<>();

        String[] projection = {
                DatabaseHelper.COLUMN_ROOM_NAME_ROOMID,
                DatabaseHelper.COLUMN_ROOM_NAME_NAME
        };

        String sortOrder = DatabaseHelper.COLUMN_ROOM_NAME_ROOMID + " ASC";

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_ROOM_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        if (cursor.moveToFirst()) {

            while (!cursor.isAfterLast()) {
                long roomId = cursor.getLong(0);
                String roomName = cursor.getString(1);
                rooms.add(new Room(roomId, roomName));

                cursor.moveToNext();
            }
        }

        return rooms;
    }


    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_NOTES);
        db.execSQL(SQL_CREATE_ITEMS);

        db.execSQL(SQL_CREATE_ROOMS);
        db.execSQL(SQL_CREATE_SCANS);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_NOTES);
        db.execSQL(SQL_DELETE_ITEMS);
        db.execSQL(SQL_DELETE_SCANS);
        db.execSQL(SQL_DELETE_ROOMS);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}