package de.whs.homebaconcore;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniel on 24.11.2015.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "bacon.db";

    public static final String TABLE_NOTES_NAME = "notes";
    public static final String COLUMN_NOTES_NAME_NOTE_ID  = "note_id";
    public static final String COLUMN_NOTES_NAME_TITLE = "title";
    public static final String COLUMN_NOTES_NAME_TEXT = "text";
    public static final String COLUMN_NOTES_NAME_TIMESTAMP = "timestamp";
    public static final String COLUMN_NOTES_NAME_ROOM_ID = "room_id";
    public static final String COLUMN_NOTES_NAME_EVENT = "event";

    public static final String TABLE_ITEMS_NAME = "items";
    public static final String COLUMN_ITEMS_NAME_TAG  = "tag";
    public static final String COLUMN_ITEMS_NAME_DESCRIPTION = "description";
    public static final String COLUMN_ITEMS_NAME_ROOM_ID = "room_id";

    public static final String TABLE_SCAN_NAME = "scans";
    public static final String COLUMN_SCAN_ID = "scan_id";
    public static final String COLUMN_SCAN_NAME_ROOM_ID = "room_id";

    public static final String TABLE_SCANNED_TAGS_NAME = "scanned_tags";
    public static final String COLUMN_SCANNED_TAGS_NAME_SCAN_ID  = "scan_id";
    public static final String COLUMN_SCANNED_TAGS_NAME_TAG  = "tag";
    public static final String COLUMN_SCANNED_TAGS_NAME_RSSI = "rssi";

    public static final String TABLE_ROOM_NAME = "rooms";
    public static final String COLUMN_ROOM_NAME_NAME = "name";
    public static final String COLUMN_ROOM_NAME_ROOM_ID = "room_id";


    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_NOTES =
            "CREATE TABLE " + TABLE_NOTES_NAME + " (" +
                    COLUMN_NOTES_NAME_NOTE_ID        + INT_TYPE + " PRIMARY KEY," +
                    COLUMN_NOTES_NAME_TITLE         + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NOTES_NAME_TEXT          + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NOTES_NAME_TIMESTAMP     + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NOTES_NAME_ROOM_ID        + INT_TYPE + COMMA_SEP +
                    COLUMN_NOTES_NAME_EVENT         + TEXT_TYPE +
            " )";

    private static final String SQL_CREATE_ITEMS =
            "CREATE TABLE " + TABLE_ITEMS_NAME + " (" +
                    COLUMN_ITEMS_NAME_TAG             + TEXT_TYPE + " PRIMARY KEY," +
                    COLUMN_ITEMS_NAME_DESCRIPTION     + TEXT_TYPE + COMMA_SEP +
                    COLUMN_ITEMS_NAME_ROOM_ID         + INT_TYPE +
                    " )";


    private static final String SQL_CREATE_SCANS =
            "CREATE TABLE " + TABLE_SCAN_NAME + " (" +
                    COLUMN_SCAN_ID                   + INT_TYPE + " PRIMARY KEY" + COMMA_SEP +
                    COLUMN_SCAN_NAME_ROOM_ID          + INT_TYPE +
                    " )";

    private static final String SQL_CREATE_SCANNED_TAGS =
            "CREATE TABLE " + TABLE_SCANNED_TAGS_NAME + " (" +
                    COLUMN_SCANNED_TAGS_NAME_SCAN_ID + INT_TYPE + COMMA_SEP +
                    COLUMN_SCANNED_TAGS_NAME_TAG     + TEXT_TYPE + COMMA_SEP +
                    COLUMN_SCANNED_TAGS_NAME_RSSI    + INT_TYPE +
                    " )";

    private static final String SQL_CREATE_ROOMS =
            "CREATE TABLE " + TABLE_ROOM_NAME + " (" +
                    COLUMN_ROOM_NAME_ROOM_ID           + INT_TYPE + " PRIMARY KEY," +
                    COLUMN_ROOM_NAME_NAME + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_NOTES =
            "DROP TABLE IF EXISTS " + TABLE_NOTES_NAME;

    private static final String SQL_DELETE_ITEMS =
            "DROP TABLE IF EXISTS " + TABLE_ITEMS_NAME;

    private static final String SQL_DELETE_SCANS =
            "DROP TABLE IF EXISTS " + TABLE_SCAN_NAME;

    private static final String SQL_DELETE_SCANNED_TAGS =
            "DROP TABLE IF EXISTS " + TABLE_SCANNED_TAGS_NAME;

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
                DatabaseHelper.COLUMN_ROOM_NAME_ROOM_ID,
                DatabaseHelper.COLUMN_ROOM_NAME_NAME
        };

        String sortOrder = DatabaseHelper.COLUMN_ROOM_NAME_ROOM_ID + " ASC";

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

        cursor.close();

        return rooms;
    }

    public long insertScan(SQLiteDatabase db, long roomId) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_SCAN_NAME_ROOM_ID, roomId);

        return db.insert(DatabaseHelper.TABLE_SCAN_NAME, null, values);
    }

    public void insertScannedTag(SQLiteDatabase db, long scanId, String tag, int rssi) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_SCANNED_TAGS_NAME_SCAN_ID, scanId);
        values.put(DatabaseHelper.COLUMN_SCANNED_TAGS_NAME_TAG, tag);
        values.put(DatabaseHelper.COLUMN_SCANNED_TAGS_NAME_RSSI, rssi);

        db.insert(DatabaseHelper.TABLE_SCANNED_TAGS_NAME, null, values);
    }

    public void insertNote(SQLiteDatabase db, Note note, int roomID) {
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_NOTES_NAME_TITLE, note.getTitle());
        values.put(DatabaseHelper.COLUMN_NOTES_NAME_TEXT, note.getText());
        values.put(DatabaseHelper.COLUMN_NOTES_NAME_TIMESTAMP,  System.currentTimeMillis());
        values.put(DatabaseHelper.COLUMN_NOTES_NAME_EVENT, note.getEventType().toString());
        values.put(DatabaseHelper.COLUMN_NOTES_NAME_ROOM_ID, roomID);

        db.insert(DatabaseHelper.TABLE_NOTES_NAME, null, values);
    }

    public void deleteNote(SQLiteDatabase db, Note note){
        db.delete(TABLE_NOTES_NAME, COLUMN_NOTES_NAME_NOTE_ID + "=" + note.getNoteId(), null);
    }

    public List<Note> getAllNotes(SQLiteDatabase db, long roomId){
        List<Note> notes = new ArrayList<>();

        String[] projection = {
                DatabaseHelper.COLUMN_NOTES_NAME_NOTE_ID,
                DatabaseHelper.COLUMN_NOTES_NAME_TITLE,
                DatabaseHelper.COLUMN_NOTES_NAME_TEXT,
                DatabaseHelper.COLUMN_NOTES_NAME_TIMESTAMP,
                DatabaseHelper.COLUMN_NOTES_NAME_EVENT,
                DatabaseHelper.COLUMN_NOTES_NAME_ROOM_ID
        };

        String sortOrder = DatabaseHelper.COLUMN_NOTES_NAME_TIMESTAMP + " DESC";
        String selection = DatabaseHelper.COLUMN_NOTES_NAME_ROOM_ID + " == " + roomId;

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_NOTES_NAME,
                projection,
                selection,
                null,
                null,
                null,
                sortOrder
        );

        if (cursor.moveToFirst()) {

            while (!cursor.isAfterLast()) {
                int noteID = cursor.getInt(0);
                String title = cursor.getString(1);
                String text = cursor.getString(2);
                long timestamp = cursor.getLong(3);
                EventType eventType = EventType.valueOf(cursor.getString(4));

                notes.add(new Note(noteID, title, text, eventType, timestamp));

                cursor.moveToNext();
            }
        }

        cursor.close();

        return notes;
    }


    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_NOTES);
        db.execSQL(SQL_CREATE_ITEMS);

        db.execSQL(SQL_CREATE_ROOMS);
        db.execSQL(SQL_CREATE_SCANNED_TAGS);
        db.execSQL(SQL_CREATE_SCANS);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_NOTES);
        db.execSQL(SQL_DELETE_ITEMS);
        db.execSQL(SQL_DELETE_SCANNED_TAGS);
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
                DatabaseHelper.COLUMN_NOTES_NAME_NOTE_ID,
                DatabaseHelper.COLUMN_NOTES_NAME_TITLE,
                DatabaseHelper.COLUMN_NOTES_NAME_TEXT,
                DatabaseHelper.COLUMN_NOTES_NAME_TIMESTAMP,
                DatabaseHelper.COLUMN_NOTES_NAME_EVENT,
                DatabaseHelper.COLUMN_NOTES_NAME_ROOM_ID
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
        cursor.getLong(
                cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTES_NAME_TITLE)
        );
        cursor.close();
    }

    public void deleteScannedTagsAndScans(SQLiteDatabase database, long roomId) {
        String sql = "DELETE FROM scanned_tags WHERE scan_id=(SELECT scan_id FROM scans WHERE room_id=" + roomId;
        database.rawQuery(sql, null);

        sql = "DELETE FROM scans where room_id=" + roomId;
        database.rawQuery(sql, null);
    }

    public void deleteScannedTags(SQLiteDatabase db) {
        int deletedRows = db.delete(DatabaseHelper.TABLE_SCANNED_TAGS_NAME, "1", null);
        Log.i("HomeBeacon", "Deleted rows: " + deletedRows);
    }

    public void deleteScans(SQLiteDatabase db) {
        int deletedRows = db.delete(DatabaseHelper.TABLE_SCAN_NAME, "1", null);
        Log.i("HomeBeacon", "Deleted rows: " + deletedRows);
    }

    public List<BeaconScan> getScans(SQLiteDatabase db) {

        List<BeaconScan> scans = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT s.scan_id, s.room_id, t.tag, t.rssi " +
                "FROM scans s " +
                "INNER JOIN scanned_tags t ON (s.scan_id = t.scan_id) " +
                "ORDER BY s.scan_id", null);


        if (cursor.moveToFirst()) {

            while (!cursor.isAfterLast()) {
                long scanId = cursor.getLong(0);
                int roomId = cursor.getInt(1);
                String address = cursor.getString(2);
                int rssi = cursor.getInt(3);

                scans.add(new BeaconScan(scanId, roomId, address, rssi));
                cursor.moveToNext();
            }
        }

        cursor.close();

        return scans;
    }
}
