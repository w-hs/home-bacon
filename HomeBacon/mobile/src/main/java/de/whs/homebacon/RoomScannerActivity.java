package de.whs.homebacon;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.whs.homebaconcore.BeaconScan;
import de.whs.homebaconcore.DatabaseHelper;
import de.whs.homebaconcore.PredictionModel;
import de.whs.homebaconcore.Room;
import de.whs.homebaconcore.WatchConnector;

public class RoomScannerActivity extends AppCompatActivity {

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    private Spinner mSpinner;
    private TextView mProbView;
    private WatchConnector mWatchConnector;
    private PredictionModel mModel;

    private Handler handler = new Handler();
    TimerTask mTask;
    Timer mTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_scanner);
        mModel = PredictionModel.loadFromPreferences(this);
        mProbView = (TextView) findViewById(R.id.probabilityText);
        mWatchConnector = new WatchConnectorImpl(this);
        mDbHelper = new DatabaseHelper(getApplicationContext());
        mDb = mDbHelper.getWritableDatabase();

        initializeTimer();
        initializeBackToHomeToolbar();
        initializeRoomsSpinner();
        initializeScanToggleButton();
        initializeDeleteButton();
        initializeCalculateButton();


    }

    private void initializeTimer() {
        mTask = new TimerTask() {
            private int time = 1;
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        String result = String.format("%02d:%02d", time / 60, time % 60);
                        mProbView.setText(result);
                        time++;
                    }
                });
            }
        };
        mTimer = new Timer();
    }

    private void initializeCalculateButton() {
        Button calculateButton = (Button) findViewById(R.id.calculateButton);
        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                List<String> scansCSV = getScans();
                if (scansCSV.size() <= 1)
                    return;
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
                mWatchConnector.clearModel();
                Toast.makeText(getApplicationContext(), "room " + getSelectedRoom().getId() + " scans successfully deleted", Toast.LENGTH_SHORT).show();
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
                if (isChecked) {
                    mTimer.schedule(mTask,1000, 1000);
                    mWatchConnector.startScan(getSelectedRoom().getId());
                }
                else {
                    mTimer.cancel();
                    mTimer.purge();
                    initializeTimer();
                    mWatchConnector.stopScan();
                }
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