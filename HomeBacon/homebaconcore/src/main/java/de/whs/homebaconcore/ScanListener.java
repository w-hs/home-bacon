package de.whs.homebaconcore;

import java.util.Map;

/**
 * Created by pausf on 05.01.2016.
 */
public interface ScanListener {
    void onScan(Map<String, BeaconScan> scan);
}
