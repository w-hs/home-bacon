package de.whs.homebacon;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import de.whs.homebaconcore.BeaconScan;
import de.whs.homebaconcore.PredictionModel;
import de.whs.homebaconcore.ScanListener;

/**
 * Created by pausf on 06.01.2016.
 */
public class RoomDetector implements ScanListener {
    PredictionModel mModel;

    String mCurrentRoom;
    Map<String, Integer> mWinnerCount = new HashMap<>();
    Queue<String> mLastWinners = new ArrayDeque<>();

    public RoomDetector(PredictionModel model) {
        mModel = model;
    }

    @Override
    public void onScan(Map<String, BeaconScan> scan) {
        Map<String, Float> predictions = mModel.predict(scan);

        String winner = "";
        float winnerProb = 0.0f;
        for (String room : predictions.keySet()) {

        }

    }
}
