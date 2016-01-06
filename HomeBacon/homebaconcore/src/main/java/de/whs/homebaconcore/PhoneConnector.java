package de.whs.homebaconcore;

import java.util.List;

/**
 * Created by Dennis on 05.01.2016.
 */
public interface PhoneConnector {

    void sendScanResults(List<BeaconScan> scans);

}
