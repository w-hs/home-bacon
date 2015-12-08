package de.whs.homebacon;

import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.wearable.view.CardFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

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
import de.whs.homebaconcore.Note;
import de.whs.homebaconcore.Room;

/**
 * Created by Daniel on 08.12.2015.
 */
public class SettingsFragment extends CardFragment {

    private View mRootView;
    private NotesGridPagerAdapter adapter;

    private BeaconScanner mBeaconScanner;
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    private Spinner mSpinner;
    private boolean mIsScanning = false;
    TextView mScannerView;
    private int mScanCount;
    private BeaconListener mListener;




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
    public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = (ViewGroup) inflater.inflate(R.layout.settings_fragment_view, null);

        /*Bundle bundle = getArguments();
        final Note note = (Note)bundle.getSerializable("note");
        if(note == null)
            return mRootView;*/



        mSpinner = (Spinner) mRootView.findViewById(R.id.spinner);
        mScannerView = (TextView)mRootView.findViewById(R.id.textView);

        Button button = (Button)mRootView.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // adapter.removeNote(note);
               // adapter.notifyDataSetChanged();
            }
        });

        mDbHelper = new DatabaseHelper(this.getActivity());
        mDb = mDbHelper.getWritableDatabase();

        List<Room> rooms = mDbHelper.getAllRooms(mDb);
        if (rooms.size() == 0) {
            mDbHelper.insertRoom(mDb, "Küche");
            mDbHelper.insertRoom(mDb, "Flur");
            mDbHelper.insertRoom(mDb, "Wohnzimmer");

            rooms = mDbHelper.getAllRooms(mDb);
        }

        mSpinner = (Spinner) mRootView.findViewById(R.id.spinner);
        ArrayAdapter<Room> listAdapter = new ArrayAdapter<Room>(this.getActivity(), android.R.layout.simple_expandable_list_item_1, rooms);
        mSpinner.setAdapter(listAdapter);
        mSpinner.setSaveEnabled(true);



        final Button startButton = (Button)  mRootView.findViewById(R.id.button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsScanning)
                {
                    startButton.setText("start");
                    mIsScanning = false;
                    mScannerView.setText("Scanner: Aus");
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
                else
                {
                    startButton.setText("stop");
                    mIsScanning = true;
                    mScannerView.setText("Scanner: An");
                    mScanCount = 0;
                }
            }
        });

      /*  Button stopButton = (Button)  mRootView.findViewById(R.id.stopScanButton);
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
        });*/



        if (mBeaconScanner == null) {
            startBeaconScan();
        } else {
            mIsScanning = savedInstanceState.getBoolean("isScanning");
            if (mIsScanning) {
                mScannerView.setText("Scanner: An");
            }
            else {
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


        return mRootView;
    }

    public void setAdapter(NotesGridPagerAdapter adapter) {
        this.adapter = adapter;
    }

    private void startBeaconScan() {
        mBeaconScanner = new BeaconScanner(this.getActivity());
        mListener = new BeaconListener() {
            @Override
            public void onScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                onBeaconScan(device, rssi);
            }
        };
        mBeaconScanner.register(mListener);
    }


    private String getDatabaseDir() {
        PackageManager m = this.getActivity().getPackageManager();
        String s = this.getActivity().getPackageName();
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

        if (!mIsScanning)
            return;

        long timestamp = System.currentTimeMillis();
        Scan scan = scans.get(device.getAddress());
        if (scan == null) {
            scans.put(device.getAddress(), new Scan(rssi, timestamp));
        }
        else {
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
        if (!mIsScanning)
            return;

        // Wenn wir länger als 2 Sekunden nichts mehr vom Beacon gehört haben,
        // gehen wir davon aus, dass der Beacon außer Reichweite ist
        long fadeDurationInMs = 2000;
        long currentTime = System.currentTimeMillis();
        long fadeLimit = currentTime - fadeDurationInMs;

        if (!hasScans(fadeLimit))
            return;

        final Room room = (Room) mSpinner.getSelectedItem();
        long scanId = mDbHelper.insertScan(mDb, room.getId());

        ++mScanCount;
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mScannerView.setText("Scanner: An (" + room.getName() + ": " + mScanCount + ")");
            }
        });

        for (String address : scans.keySet()) {
            Scan scan = scans.get(address);
            if (scan.getTimestamp() > fadeLimit) {
                mDbHelper.insertScannedTag(mDb, scanId, address, scan.getRssi());
                Log.i("HomeBeacon", "room=" + room.getName() + ", addr=" + address + ", rssi="
                        + scan.getRssi());
            }
        }

    }


}
