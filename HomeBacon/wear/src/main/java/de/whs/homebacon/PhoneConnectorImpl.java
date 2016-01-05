package de.whs.homebacon;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.concurrent.TimeUnit;

import de.whs.homebaconcore.BeaconScan;
import de.whs.homebaconcore.Constants;
import de.whs.homebaconcore.Note;
import de.whs.homebaconcore.PhoneConnector;
import de.whs.homebaconcore.Serializer;

/**
 * Created by Dennis on 05.01.2016.
 */
public class PhoneConnectorImpl implements PhoneConnector {

    private GoogleApiClient mGoogleApiClient;

    public PhoneConnectorImpl (Context context) {

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();
    }

    @Override
    public void sendScanResults(final List<BeaconScan> scans) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mGoogleApiClient.blockingConnect(Constants.CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                    NodeApi.GetConnectedNodesResult nodesResult = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                    List<Node> nodes = nodesResult.getNodes();
                    byte[] serializedScans = Serializer.serialize(scans);
                    if (nodes != null && nodes.size() > 0) {
                        for (Node node : nodes) {
                            Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(),
                                    Constants.HOME_BACON_SCAN_RESULTS, serializedScans).setResultCallback(
                                    new ResultCallback() {
                                        @Override
                                        public void onResult(Result result) {
                                            if (!result.getStatus().isSuccess())
                                                // Failed to send message
                                                Log.e(Constants.DEBUG_TAG, "Send message failed");
                                            else
                                                Log.d(Constants.DEBUG_TAG, "send successfully");
                                        }
                                    });
                        }
                    } else {
                        Log.d(Constants.DEBUG_TAG, "nothing connected");
                    }
                }
                catch (Exception e){

                }
                finally {
                    mGoogleApiClient.disconnect();
                }
            }
        }).start();
    }
}
