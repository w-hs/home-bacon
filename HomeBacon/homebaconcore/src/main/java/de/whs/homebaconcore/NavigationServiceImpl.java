package de.whs.homebaconcore;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.ArrayAdapter;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

/**
 * Created by Dennis on 24.11.2015.
 */
public class NavigationServiceImpl implements NavigationService {

    private BluetoothAdapter BTAdapter;
    private BroadcastReceiver mReceiver;
    private ArrayAdapter<String> mArrayAdapter;

    public static int REQUEST_BLUETOOTH = 1;

    public NavigationServiceImpl (Activity activity){
        BTAdapter = BluetoothAdapter.getDefaultAdapter();

        // Phone does not support Bluetooth so let the user know and exit.
        if (BTAdapter == null) {
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

        if (!BTAdapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBT, REQUEST_BLUETOOTH);
        }

        if (BTAdapter.isEnabled()) {
            BTAdapter.startDiscovery();
            mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();

                    //Finding devices
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        // Get the BluetoothDevice object from the Intent
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                        // Add the name and address to an array adapter to show in a ListView
                        String msg = device.getName() + "\n" + device.getAddress() + "\n" + rssi;
                        //mArrayAdapter.add(msg);
                        Log.d("BLUETOOTH", msg);
                    }
                }
            };
        };

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        activity.registerReceiver(mReceiver, filter);
    }

    @Override
    public Room getCurrentPosition() {



        return null;
    }
}
