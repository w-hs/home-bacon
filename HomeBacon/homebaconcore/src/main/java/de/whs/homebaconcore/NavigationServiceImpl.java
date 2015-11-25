package de.whs.homebaconcore;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

/**
 * Created by Dennis on 24.11.2015.
 */
public class NavigationServiceImpl implements NavigationService {

    private BeaconScanner mBeaconScanner;

    public NavigationServiceImpl (final Activity activity){
        mBeaconScanner = new BeaconScanner(activity);
        mBeaconScanner.register(new BeaconListener() {
            @Override
            public void onScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                onBeaconScan(device, rssi);
            }
        });
    }

    private void onBeaconScan(BluetoothDevice device, int rssi) {
        // TODO: Aktuelle Schätzung für Position updaten
    }

    @Override
    public Room getCurrentPosition() {



        return null;
    }
}
