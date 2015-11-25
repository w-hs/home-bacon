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

    public void insertScan(SQLiteDatabase db, long roomId, String address, int rssi) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_SCAN_NAME_BBTAG, address);
        values.put(DatabaseHelper.COLUMN_SCAN_NAME_ROOMID, roomId);
        values.put(DatabaseHelper.COLUMN_SCAN_NAME_RSSI, rssi);

        db.insert(DatabaseHelper.TABLE_SCAN_NAME, null, values);
    }

    public void insertNote(SQLiteDatabase db, Note note) {
        ContentValues values = new ContentValues();

        // Genereate ID?!
        // values.put(DatabaseHelper.COLUMN_NOTES_NAME_NOTEID, 1);


        values.put(DatabaseHelper.COLUMN_NOTES_NAME_TITLE, note.getTitle());
        values.put(DatabaseHelper.COLUMN_NOTES_NAME_TEXT, note.getText());
        values.put(DatabaseHelper.COLUMN_NOTES_NAME_TIMESTAMP,  System.currentTimeMillis());
        values.put(DatabaseHelper.COLUMN_NOTES_NAME_EVENT, "null");
        values.put(DatabaseHelper.COLUMN_NOTES_NAME_ROOMID, 100);

        long noteId = db.insert(DatabaseHelper.TABLE_NOTES_NAME, null, values);
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

    public void test(SQLiteDatabase db) {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DatabaseHelper.COLUMN_NOTES_NAME_NOTEID,
                DatabaseHelper.COLUMN_NOTES_NAME_TITLE,
                DatabaseHelper.COLUMN_NOTES_NAME_TEXT,
                DatabaseHelper.COLUMN_NOTES_NAME_TIMESTAMP,
                DatabaseHelper.COLUMN_NOTES_NAME_EVENT,
                DatabaseHelper.COLUMN_NOTES_NAME_ROOMID
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DatabaseHelper.COLUMN_NOTES_NAME_TIMESTAMP + " DESC";

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_NOTES_NAME, // The table to query
                projection,                      // The columns to return
                null,                            // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                            // don't group the rows
                null,                            // don't filter by row groups
                sortOrder                        // The sort order
        );

        cursor.moveToFirst();
        long itemId = cursor.getLong(
                cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTES_NAME_TITLE)
        );
    }

}
