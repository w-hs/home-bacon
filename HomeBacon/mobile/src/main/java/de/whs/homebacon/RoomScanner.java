package de.whs.homebacon;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.whs.homebaconcore.BeaconScan;
import de.whs.homebaconcore.Constants;
import de.whs.homebaconcore.DatabaseHelper;
import de.whs.homebaconcore.PredictionModel;
import de.whs.homebaconcore.Room;
import de.whs.homebaconcore.WatchConnector;

public class RoomScanner extends AppCompatActivity {

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    private Spinner mSpinner;
    private TextView mProbView;
    private WatchConnector mWatchConnector;
    private PredictionModel mModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_scanner);
        mModel = PredictionModel.loadFromPreferences(this);

        mWatchConnector = new WatchConnectorImpl(this);
        mDbHelper = new DatabaseHelper(getApplicationContext());
        mDb = mDbHelper.getWritableDatabase();

        initializeBackToHomeToolbar();
        initializeRoomsSpinner();
        initializeScanToggleButton();
        initializeDeleteButton();
        initializeCalculateButton();

        mProbView = (TextView) findViewById(R.id.probabilityText);
    }

    private void initializeCalculateButton() {
        Button calculateButton = (Button) findViewById(R.id.calculateButton);
        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                List<String> scansCSV = getScans();
                try {
                    new AsyncTask<List<String>, Void, PredictionModel>() {
                        private Exception exception;

                        @Override
                        protected PredictionModel doInBackground(List<String>... params) {
                            try {
                                return PredictionModel.getPredictionModelFor(params[0]);
                            } catch (Exception ex) {
                                this.exception = ex;
                                return null;
                            }
                        }

                        @Override
                        protected void onPostExecute(PredictionModel model) {
                            if (this.exception == null) {
                                model.saveToPreferences(v.getContext());
                                mWatchConnector.sendModel(model);

                                // TODO: Wirkliche Messwerte mit dem Modell zur Vorhersage nutzen
                                Log.e("HomeBeacon", "Accuracy = " + Float.toString(model.getAccuracy()));
                                Map<String, BeaconScan> test = new HashMap<>();
                                test.put("7C:2F:80:99:DE:CD", new BeaconScan(3, -98, 0));
                                test.put("7C:2F:80:99:DE:25", new BeaconScan(3, -88, 0));
                                test.put("7C:2F:80:99:DE:B1", new BeaconScan(3, -90, 0));
                                model.predict(test);
                            }
                            else {
                                Log.e("HomeBeacon", this.exception.getMessage());
                            }
                        }
                    }.execute(scansCSV);
                } catch (Exception ex) {
                    Log.e("HomeBeacon", ex.getMessage());
                }
            }
        });
    }

    private List<String> getScans() {
        List<BeaconScan> scans = mDbHelper.getScans(mDb);
        List<String> lines = new ArrayList<>();
        lines.add("scan_id,room_id,tag,rssi\n");
        for (BeaconScan scan : scans){
            lines.add(scan.getAsCSV() + "\n");
        }
        return lines;
    }

    private void initializeDeleteButton() {
        Button deleteButton = (Button) findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDbHelper.deleteScannedTagsAndScans(mDb, getSelectedRoom().getId());
            }
        });
    }

    private void downloadDatabase() {
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File destFile = new File(downloadDir, "bacon.db");
        File srcFile = new File(getDatabaseDir());
        try {
            copy(srcFile, destFile);
        } catch (Exception ex) {
            Log.e("HomeBeacon", ex.getMessage());
        }
    }

    private void initializeScanToggleButton() {
        ToggleButton scanToggleButton = (ToggleButton) findViewById(R.id.scanToggleButton);
        scanToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    mWatchConnector.startScan(getSelectedRoom().getId());
                else
                    mWatchConnector.stopScan();
            }
        });
    }

    private void initializeRoomsSpinner() {
        List<Room> rooms = getRoomsFromDatabase();
        mSpinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<Room> listAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, rooms);
        mSpinner.setAdapter(listAdapter);
        mSpinner.setSaveEnabled(true);
    }

    private List<Room> getRoomsFromDatabase() {
        List<Room> rooms = mDbHelper.getAllRooms(mDb);
        if (rooms.size() == 0) {
            mDbHelper.insertRoom(mDb, "Küche");
            mDbHelper.insertRoom(mDb, "Flur");
            mDbHelper.insertRoom(mDb, "Wohnzimmer");

            rooms = mDbHelper.getAllRooms(mDb);
        }
        return rooms;
    }

    private Room getSelectedRoom() {
        return (Room) mSpinner.getSelectedItem();
    }

    private void initializeBackToHomeToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null)
            supportActionBar.setDisplayHomeAsUpEnabled(true);
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
}