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
import java.util.List;

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



        Button startButton = (Button)  mRootView.findViewById(R.id.button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsScanning = true;
               // mScannerView.setText("Scanner: An");
                mScanCount = 0;
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



        if (savedInstanceState == null) {
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
/*
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
*/
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

        Room room = (Room) mSpinner.getSelectedItem();
        Log.i("HomeBeacon", "room=" + room.getName() + ", addr=" + device.getAddress() + ", rssi="
                + rssi);
        ++mScanCount;
        mScannerView.setText("Scanner: An (" + room.getName() + ": " + mScanCount + ")");
        mDbHelper.insertScan(mDb, room.getId(), device.getAddress(), rssi);
    }

}
