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
import java.util.List;

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
    private WatchConnector watchConnector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_scanner);

        watchConnector = new WatchConnectorImpl(this);
        mDbHelper = new DatabaseHelper(getApplicationContext());
        mDb = mDbHelper.getWritableDatabase();

        initializeBackToHomeToolbar();
        initializeRoomsSpinner();
        initializeScanToggleButton();
        initializeDeleteButton();
        initializeCalculateButton();

        mProbView = (TextView) findViewById(R.id.probabilityText);
/*
        mTagsToIndex.put("7C:2F:80:8D:E2:3B", 0);
        mTagsToIndex.put("7C:2F:80:8D:E2:45", 1);
        mTagsToIndex.put("20:C3:8F:99:C1:E7", 2);
*/
    }

    private void initializeCalculateButton() {
        Button calculateButton = (Button) findViewById(R.id.calculateButton);
        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(Constants.DEBUG_TAG, getScans());
                // TODO Berechnung starten...
                File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File destFile = new File(downloadDir, "bacon.db");
                File srcFile = new File(getDatabaseDir());
                try {
                    copy(srcFile, destFile);
                } catch (Exception ex) {
                    Log.e("HomeBeacon", ex.getMessage());
                }
                try {
                    new AsyncTask<String, Void, PredictionModel>() {
                        private Exception exception;

                        @Override
                        protected PredictionModel doInBackground(String... params) {
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
                                Log.e("HomeBeacon", "Accuracy = " + Float.toString(model.getAccuracy()));
                            } else {
                                Log.e("HomeBeacon", this.exception.getMessage());
                            }
                        }
                    }.execute(PredictionModel.getTestData());
                } catch (Exception ex) {
                    Log.e("HomeBeacon", ex.getMessage());
                }
            }
        });
    }

    private String getScans() {
        List<BeaconScan> scans = mDbHelper.getScans(mDb);
        StringBuilder sb = new StringBuilder();
        sb.append("scan_id,room_id,tag,rssi\n");
        for (BeaconScan scan : scans){
            sb.append(scan.getAsCSV());
            sb.append("\n");
        }
        String s =  sb.toString();
        return s;
    }

    private void initializeDeleteButton() {
        Button deleteButton = (Button) findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDbHelper.deleteScannedTags(mDb);
                mDbHelper.deleteScans(mDb);
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
                    watchConnector.startScan(getSelectedRoom().getId());
                else
                    watchConnector.stopScan();
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

    /*
    // TODO: Code noch nötig?
    private void onSaveScans() {
        // Wenn wir länger als 2 Sekunden nichts mehr vom Beacon gehört haben,
        // gehen wir davon aus, dass der Beacon außer Reichweite ist
        long fadeDurationInMs = 2000;
        long currentTime = System.currentTimeMillis();
        long fadeLimit = currentTime - fadeDurationInMs;

        if (!hasScans(fadeLimit))
            return;

        final Room room = (Room) mSpinner.getSelectedItem();
        long scanId = mDbHelper.insertScan(mDb, room.getId());

        float[] x = {0.0f, 0.0f, 0.0f};

        for (String address : scans.keySet()) {
            Scan scan = scans.get(address);
            if (scan.getTimestamp() > fadeLimit) {
                if (mIsScanning) {
                    mDbHelper.insertScannedTag(mDb, scanId, address, scan.getRssi());
                    Log.i("HomeBeacon", "room=" + room.getName() + ", addr=" + address + ", rssi="
                            + scan.getRssi());
                }

                Integer tagIndex = mTagsToIndex.get(address);
                if (tagIndex != null) {
                    float normalizedRssi = (scan.getRssi() + 100) / 60.0f;
                    x[tagIndex] = normalizedRssi;
                }
            }
        }

        {
            // Nutzen des Scans zur Positionierung
            float[][] W = {
                    {-0.96257174f, -2.28264236f, 3.24521399f},
                    {-1.54085743f, -1.61166883f, 3.15252829f},
                    {-0.95999652f, 11.77016544f, -10.81016445f}
            };
            float[] B = {
                    -3.37038374f, 0.49158058f, 2.8788023f
            };

            float[] mulResult = multiply(W, x);
            float[] addResult = add(mulResult, B);
            final float[] y = softmax(addResult);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    float rounded0 = Math.round(y[0] * 100);
                    float rounded1 = Math.round(y[1] * 100);
                    float rounded2 = Math.round(y[2] * 100);
                    mProbView.setText("[" + rounded0 + ", " + rounded1 + ", " + rounded2 + "]");
                }
            });
            //Log.i("HomeBeacon", "[" + y[0] + ", " + y[1] + ", " + y[2] + "]");
        }
    }

    private boolean hasScans(long fadeLimit) {
        for (Scan scan : scans.values()) {
            if (scan.getTimestamp() > fadeLimit) {
                return true;
            }
        }
        return false;
    }

    private float[] multiply(float[][] W, float[] x_) {
        int height = W.length;
        int width = W[0].length;

        float[] result = new float[height];

        for (int y = 0; y < height; ++y) {
            float sum = 0.0f;
            for (int x = 0; x < width; ++x) {
                sum += W[y][x] * x_[x];
            }
            result[y] = sum;
        }

        return result;
    }

    private float[] add(float[] a, float[] b) {
        float[] result = new float[a.length];
        for (int i = 0; i < a.length; ++i) {
            result[i] = a[i] + b[i];
        }
        return result;
    }

    private float[] softmax(float[] y) {
        float[] result = new float[y.length];

        float sum = 0.0f;
        for (int i = 0; i < y.length; ++i) {
            result[i] = (float) Math.exp(y[i]);
            sum += result[i];
        }
        float invSum = 1.0f / sum;
        for (int i = 0; i < y.length; ++i) {
            result[i] *= invSum;
        }
        return result;
    }
    */
}