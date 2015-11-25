package de.whs.homebaconcore;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pausf on 25.11.2015.
 */
public class BeaconScanner {
    private BluetoothAdapter mBluetoothAdapter;
    private List<BeaconListener> mListeners = new ArrayList<>();

    public BeaconScanner(Activity activity) {

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Phone does not support Bluetooth so let the user know and exit.
        if (mBluetoothAdapter == null) {
            new android.support.v7.app.AlertDialog.Builder(activity)
                    .setTitle("Not compatible")
                    .setMessage("Your phone does not support Bluetooth")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                int REQUEST_BLUETOOTH = 1;
                activity.startActivityForResult(enableBT, REQUEST_BLUETOOTH);
            }

            if (mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.startLeScan(new BluetoothAdapter.LeScanCallback() {
                    @Override
                    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                        onBeaconScan(device, rssi, scanRecord);
                    }
                });
            }
        }
    }

    public void register(BeaconListener listener) {
        mListeners.add(listener);
    }

    private void onBeaconScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        for (BeaconListener listener : mListeners) {
            listener.onScan(device, rssi, scanRecord);
        }
    }
}
