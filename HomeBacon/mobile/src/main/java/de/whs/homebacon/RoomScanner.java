package de.whs.homebacon;

import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
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

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.whs.homebaconcore.BeaconListener;
import de.whs.homebaconcore.BeaconScanner;
import de.whs.homebaconcore.DatabaseHelper;
import de.whs.homebaconcore.Room;
import de.whs.homebaconcore.WatchConnector;

public class RoomScanner extends AppCompatActivity {

    private BeaconScanner mBeaconScanner;
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    private Spinner mSpinner;
    private boolean mIsScanning = false;
    private TextView mScannerView;
    private int mScanCount;
    private BeaconListener mListener;
    private Map<String, Integer> mTagsToIndex = new HashMap<>();
    private TextView mProbView;
    private WatchConnector watchConnector;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "RoomScanner Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://de.whs.homebacon/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "RoomScanner Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://de.whs.homebacon/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    private class Scan {
        private int rssi;
        private long timestamp;

        public Scan(int rssi, long timestamp) {
            this.rssi = rssi;
            this.timestamp = timestamp;
        }

        public int getRssi() {
            return rssi;
        }

        public void setRssi(int rssi) {
            this.rssi = rssi;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }

    private Map<String, Scan> scans = new HashMap<>();

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


        mScannerView = (TextView) findViewById(R.id.scanningTextView);

        Button startButton = (Button) findViewById(R.id.startScanButtton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsScanning = true;
                mScannerView.setText("Scanner: An");
                mScanCount = 0;
            }
        });

        Button stopButton = (Button) findViewById(R.id.stopScanButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsScanning = false;
                mScannerView.setText("Scanner: Aus");
                File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File destFile = new File(downloadDir, "bacon.db");
                File srcFile = new File(getDatabaseDir());
                try {
                    copy(srcFile, destFile);
                } catch (Exception ex) {
                    Log.e("HomeBeacon", ex.getMessage());
                }
            }
        });

        Button deleteButton = (Button) findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDbHelper.deleteScannedTags(mDb);
                mDbHelper.deleteScans(mDb);
            }
        });

        mProbView = (TextView) findViewById(R.id.probabilityText);

        if (mBeaconScanner == null) {
            startBeaconScan();
        } else {
            mIsScanning = savedInstanceState.getBoolean("isScanning");
            if (mIsScanning) {
                mScannerView.setText("Scanner: An");
            } else {
                mScannerView.setText("Scanner: Aus");
            }
            Log.i("HomeBeacon", "Scanner: " + mBeaconScanner);
        }

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                onSaveScans();
            }
        }, 0, 1100);

        mTagsToIndex.put("7C:2F:80:8D:E2:3B", 0);
        mTagsToIndex.put("7C:2F:80:8D:E2:45", 1);
        mTagsToIndex.put("20:C3:8F:99:C1:E7", 2);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void initializeScanToggleButton() {
        ToggleButton scanToggleButton = (ToggleButton) findViewById(R.id.scanToggleButton);
        scanToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    startScan();
                else
                    stoppScan();
            }
        });
    }

    private void startScan() {
        //watchConnector.startScan();
    }

    private void stoppScan() {

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

    private void initializeBackToHomeToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null)
            supportActionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onDestroy() {
        if (mBeaconScanner != null)
            mBeaconScanner.unregister(mListener);
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putBoolean("isScanning", mIsScanning);

        super.onSaveInstanceState(outState, outPersistentState);
    }

    private void startBeaconScan() {
        mBeaconScanner = new BeaconScanner(this);
        mListener = new BeaconListener() {
            @Override
            public void onScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                onBeaconScan(device, rssi);
            }
        };
        mBeaconScanner.register(mListener);
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
        long timestamp = System.currentTimeMillis();
        Scan scan = scans.get(device.getAddress());
        if (scan == null) {
            scans.put(device.getAddress(), new Scan(rssi, timestamp));
        } else {
            scan.setRssi(rssi);
            scan.setTimestamp(timestamp);
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

        if (mIsScanning) {
            ++mScanCount;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mScannerView.setText("Scanner: An (" + room.getName() + ": " + mScanCount + ")");
                }
            });
        }

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
}