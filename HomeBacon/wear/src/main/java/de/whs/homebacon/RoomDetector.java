package de.whs.homebacon;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import de.whs.homebaconcore.BeaconScan;
import de.whs.homebaconcore.PredictionModel;
import de.whs.homebaconcore.ScanListener;

/**
 * Created by pausf on 06.01.2016.
 */
public class RoomDetector implements ScanListener {
    private PredictionModel mModel;
    private String mCurrentRoom = "-1";
    private Queue<String> mLastWinners = new ArrayDeque<>();
    private List<RoomChangeListener> mListeners = new ArrayList<>();

    private static int MAX_WINNER_COUNT = 10;

    public RoomDetector(PredictionModel model) {
        mModel = model;
    }

    public void setModel(PredictionModel mModel) {
        this.mModel = mModel;
    }

    @Override
    public void onScan(Map<String, BeaconScan> scan) {
        if (mModel == null || scan.isEmpty())
            return;

        Map<String, Float> predictions = mModel.predict(scan);
        String winner = getWinnerFromPredictions(predictions);
        pushWinner(winner);
        Map<String, Integer> winnerCounts = getWinnerCounts();
        String newCurrentRoom = getNewCurrentRoom(winnerCounts);
        checkForRoomChange(newCurrentRoom);
    }

    private void checkForRoomChange(String newCurrentRoom) {
        if (!newCurrentRoom.equals(mCurrentRoom)) {
            int oldRoomId = Integer.parseInt(mCurrentRoom);
            int newRoomId = Integer.parseInt(newCurrentRoom);

            for (RoomChangeListener listener : mListeners) {
                listener.onChange(oldRoomId, newRoomId);
            }
            mCurrentRoom = newCurrentRoom;
        }
    }

    private String getNewCurrentRoom(Map<String, Integer> winnerCounts) {
        String newCurrentRoom = "";
        int maxCount = 0;
        for (String room : winnerCounts.keySet()) {
            int count = winnerCounts.get(room);
            if (count >= maxCount) {
                newCurrentRoom = room;
                maxCount = count;
            }
        }
        return newCurrentRoom;
    }

    private void pushWinner(String winner) {
        mLastWinners.add(winner);
        if (mLastWinners.size() > MAX_WINNER_COUNT) {
            mLastWinners.remove();
        }
    }

    private Map<String, Integer> getWinnerCounts() {
        Map<String, Integer> winnerCounts = new HashMap<>();
        for (String lastWinner : mLastWinners) {
            Integer winnerCount = winnerCounts.get(lastWinner);
            if (winnerCount == null) {
                winnerCount = 0;
            }
            winnerCounts.put(lastWinner, winnerCount + 1);
        }
        return winnerCounts;
    }

    private String getWinnerFromPredictions(Map<String, Float> predictions) {
        String winner = "";
        float winnerProb = 0.0f;
        for (String room : predictions.keySet()) {
            float roomProb = predictions.get(room);
            if (roomProb >= winnerProb) {
                winner = room;
                winnerProb = roomProb;
            }
        }
        return winner;
    }

    public void register(RoomChangeListener listener) {
        mListeners.add(listener);
    }

    public void unregister(RoomChangeListener listener) {
        mListeners.remove(listener);
    }
}
