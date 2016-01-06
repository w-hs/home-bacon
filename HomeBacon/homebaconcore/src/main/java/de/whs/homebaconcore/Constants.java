package de.whs.homebaconcore;

/**
 * Created by Dennis on 01.12.2015.
 */
public class Constants {

    public static final int CONNECTION_TIME_OUT_MS = 1000;
    public static final String DEBUG_TAG = "HOME-BACON";

    //phone to watch paths
    public static final String HOME_BACON_NOTE = "/HOME_BACON_NOTE";
    public static final String HOME_BACON_SCAN_START = "/HOME_BACON_SCAN_START";
    public static final String HOME_BACON_SCAN_STOP = "/HOME_BACON_SCAN_STOP";
    public static final String HOME_BACON_SEND_MODEL= "/HOME_BACON_SEND_MODEL";

    //watch to phone paths
    public static final String HOME_BACON_SCAN_RESULTS = "/HOME_BACON_SCAN_RESULTS";

    //watch service -  service to app
    public static final String EVENT = "EVENT";
    public static final String ENTER_LEAVE = "EnterLeave";
    public static final String CURRENT_ROOM = "CurrentRoom";
    public static final String HOME_BACON_ROOM_CHANGED = "HOME_BACON_ROOM_CHANGED";
    public static final String HOME_BACON_OLD_ROOM = "HOME_BACON_OLD_ROOM";
    public static final String HOME_BACON_NEW_ROOM = "HOME_BACON_NEW_ROOM";
}
