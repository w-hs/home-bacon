package de.whs.homebacon;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import de.whs.homebaconcore.Constants;
import de.whs.homebaconcore.EventType;
import de.whs.homebaconcore.WatchConnector;

/**
 * Created by Daniel on 17.11.2015.
 */
public class WatchConnectorImpl implements
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        WatchConnector{

    private GoogleApiClient mGoogleApiClient;

    public WatchConnectorImpl (Context context) {

             mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }


    @Override
    public void sendNote(final String note) {
//        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/count");
//        putDataMapReq.getDataMap().putString(Constants.NOTE, note);
//        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
//        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);

        new Thread(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                for (Node node : nodes.getNodes()) {
                    Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(),
                            "/count", note.getBytes()).setResultCallback(
                            new ResultCallback() {
                                @Override
                                public void onResult(Result result) {
                                    if (!result.getStatus().isSuccess()) {
                                        // Failed to send message
                                        Log.e("MESSAGE","Send message failed");
                                    }
                                    else
                                        Log.d("MESSAGE", "send successfully");

                                }
                            });
                }
            }
        }).start();
    }

    @Override
    public void sendNoteWithEvent(String note, EventType event) {
//        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/count");
//        putDataMapReq.getDataMap().putString(Constants.NOTEWITHEVENT, "Hallo Uhrnutzer");
//        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
//        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
    }

    @Override
    public void connect() {
        Log.d("WatchConnector: ", "Try to connect");
        mGoogleApiClient.connect();
    }

    @Override
    public void disconnect() {
         Log.d("WatchConnector: ", "disconnect");
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("WatchConnector: ", "Connection established");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("WatchConnector: ", "Connection suspended - Code: " + i);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        Log.d("WatchConnector: ", "Data changed");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("WatchConnector: ", "Connection failed");
    }
}
