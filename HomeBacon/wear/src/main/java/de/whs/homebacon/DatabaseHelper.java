package de.whs.homebacon;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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
    public static final String COLUMN_ITEMS_NAME_DESCRIPTION = "dec";
    public static final String COLUMN_ITEMS_NAME_ROOMID = "roomid";


    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_NOTES =
            "CREATE TABLE " + TABLE_NOTES_NAME + " (" +
                    COLUMN_NOTES_NAME_NOTEID        + " INTEGER PRIMARY KEY," +
                    COLUMN_NOTES_NAME_TITLE         + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NOTES_NAME_TEXT          + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NOTES_NAME_TIMESTAMP     + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NOTES_NAME_ROOMID        + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NOTES_NAME_EVENT         + TEXT_TYPE +
            " )";

    private static final String SQL_CREATE_ITEMS =
            "CREATE TABLE " + TABLE_ITEMS_NAME + " (" +
                    COLUMN_ITEMS_NAME_BBTAG           + " INTEGER PRIMARY KEY," +
                    COLUMN_ITEMS_NAME_DESCRIPTION     + TEXT_TYPE + COMMA_SEP +
                    COLUMN_ITEMS_NAME_ROOMID          + "INTEGER" +
                    " )";

    private static final String SQL_DELETE_NOTES =
            "DROP TABLE IF EXISTS " + TABLE_NOTES_NAME;

    private static final String SQL_DELETE_ITEMS =
            "DROP TABLE IF EXISTS " + TABLE_ITEMS_NAME;


    public DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    public void onCreate(SQLiteDatabase db) {
        Log.d("SQLSQLSQL________","_________________________Create");
        db.execSQL(SQL_CREATE_NOTES);
        db.execSQL(SQL_CREATE_ITEMS);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_NOTES);
        db.execSQL(SQL_DELETE_ITEMS);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
