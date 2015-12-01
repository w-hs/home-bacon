package de.whs.homebacon;

import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import de.whs.homebaconcore.BeaconListener;
import de.whs.homebaconcore.BeaconScanner;
import de.whs.homebaconcore.DatabaseHelper;
import de.whs.homebaconcore.Room;

public class RoomScanner extends AppCompatActivity {

    private BeaconScanner mBeaconScanner;
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    private Spinner mSpinner;
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
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null)
            supportActionBar.setDisplayHomeAsUpEnabled(true);

        mDbHelper = new DatabaseHelper(getApplicationContext());

        mDb = mDbHelper.getWritableDatabase();
        //mDbHelper.onUpgrade(mDb, 1, 1);

        List<Room> rooms = mDbHelper.getAllRooms(mDb);
        if (rooms.size() == 0) {
            mDbHelper.insertRoom(mDb, "Küche");
            mDbHelper.insertRoom(mDb, "Flur");
            mDbHelper.insertRoom(mDb, "Wohnzimmer");

            rooms = mDbHelper.getAllRooms(mDb);
        }

        mSpinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<Room> listAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, rooms);
        mSpinner.setAdapter(listAdapter);

        final TextView scannerView = (TextView) findViewById(R.id.scanningTextView);

        Button startButton = (Button) findViewById(R.id.startScanButtton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isScanning = true;
                scannerView.setText("Scanner: An");
            }
        });

        Button stopButton = (Button) findViewById(R.id.stopScanButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isScanning = false;
                scannerView.setText("Scanner: Aus");
                File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File destFile = new File(downloadDir, "bacon.db");
                File srcFile = new File(getDatabaseDir());
                try {
                    copy(srcFile, destFile);
                }
                catch (Exception ex) {
                    Log.e("HomeBeacon", ex.getMessage());
                }
            }
        });

        Button deleteButton = (Button) findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDbHelper.deleteScans(mDb);
            }
        });

        startBeaconScan();
    }

    private void startBeaconScan() {
        mBeaconScanner = new BeaconScanner(this);
        mBeaconScanner.register(new BeaconListener() {
            @Override
            public void onScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                onBeaconScan(device, rssi);
            }
        });
    }

    private String getDatabaseDir() {
        PackageManager m = getPackageManager();
        String s = getPackageName();
        try {
            PackageInfo p = m.getPackageInfo(s, 0);
            return p.applicationInfo.dataDir + "/databases/bacon.db";
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }

    private void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
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
