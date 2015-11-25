package de.whs.homebacon;

import android.bluetooth.BluetoothDevice;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import java.util.ArrayList;
import java.util.List;

import de.whs.homebaconcore.BeaconListener;
import de.whs.homebaconcore.BeaconScanner;
import de.whs.homebaconcore.DatabaseHelper;
import de.whs.homebaconcore.Room;

public class RoomScanner extends AppCompatActivity {

    private BeaconScanner mBeaconScanner;
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    private List<Room> mRooms = new ArrayList<>();
    private Spinner mSpinner;
    private ArrayAdapter<Room> mListAdapter;
    private boolean isScanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_scanner);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDbHelper = new DatabaseHelper(getApplicationContext());

        mDb = mDbHelper.getWritableDatabase();
        mDbHelper.onUpgrade(mDb, 1, 1);

        mRooms = mDbHelper.getAllRooms(mDb);
        if (mRooms.size() == 0) {
            mDbHelper.insertRoom(mDb, "Küche");
            mDbHelper.insertRoom(mDb, "Flur");
            mDbHelper.insertRoom(mDb, "Wohnzimmer");

            mRooms = mDbHelper.getAllRooms(mDb);
        }

        mSpinner = (Spinner) findViewById(R.id.spinner);
        mListAdapter = new ArrayAdapter<Room>(this,
                android.R.layout.simple_list_item_1, mRooms);
        mSpinner.setAdapter(mListAdapter);

        Button startButton = (Button) findViewById(R.id.startScanButtton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isScanning = true;
            }
        });

        Button stopButton = (Button) findViewById(R.id.stopScanButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isScanning = false;
            }
        });

        mBeaconScanner = new BeaconScanner(this);
        mBeaconScanner.register(new BeaconListener() {
            @Override
            public void onScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                onBeaconScan(device, rssi);
            }
        });
    }

    private void onBeaconScan(BluetoothDevice device, int rssi) {

        if (!isScanning)
            return;

        Room room = (Room) mSpinner.getSelectedItem();
        Log.i("HomeBeacon", "room=" + room.getName() + ", addr=" + device.getAddress() + ", rssi="
                + rssi);
        mDbHelper.insertScan(mDb, room.getId(), device.getAddress(), rssi);
    }

}
