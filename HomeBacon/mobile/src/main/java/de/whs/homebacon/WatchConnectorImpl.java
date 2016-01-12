package de.whs.homebacon;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

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

import org.xmlpull.v1.XmlSerializer;

import java.util.List;
import java.util.concurrent.TimeUnit;

import de.whs.homebaconcore.Constants;
import de.whs.homebaconcore.EventType;
import de.whs.homebaconcore.Note;
import de.whs.homebaconcore.PredictionModel;
import de.whs.homebaconcore.Serializer;
import de.whs.homebaconcore.WatchConnector;

/**
 * Created by Daniel on 17.11.2015.
 */
public class WatchConnectorImpl implements WatchConnector{

    private GoogleApiClient mGoogleApiClient;
    private Activity mActivity;

    public WatchConnectorImpl (Activity activity) {

        mActivity= activity;
        mGoogleApiClient = new GoogleApiClient.Builder(mActivity.getApplicationContext())
                .addApi(Wearable.API)
                .build();
    }


    @Override
    public void sendNote(final Note note) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mGoogleApiClient.blockingConnect(Constants.CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                    NodeApi.GetConnectedNodesResult nodesResult = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                    List<Node> nodes = nodesResult.getNodes();
                    byte[] serializedNote = Serializer.serialize(note);
                    if (nodes != null && nodes.size() > 0) {
                        for (Node node : nodes) {
                            Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(),
                                    Constants.HOME_BACON_NOTE, serializedNote).setResultCallback(
                                    new ResultCallback() {
                                        @Override
                                        public void onResult(Result result) {
                                            if (!result.getStatus().isSuccess()) {
                                                // Failed to send message

                                                Toast("Failed to send note to watch", Toast.LENGTH_LONG);
                                                Log.e(Constants.DEBUG_TAG, "Send message failed");
                                            } else {
                                                Toast("Note sent successfully", Toast.LENGTH_SHORT);
                                                Log.d(Constants.DEBUG_TAG, "send successfully");
                                            }
                                        }
                                    });
                        }
                    } else {
                        Toast("No watch connected", Toast.LENGTH_SHORT);
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

    @Override
    public void startScan(final long roomId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mGoogleApiClient.blockingConnect(Constants.CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                    NodeApi.GetConnectedNodesResult nodesResult = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                    List<Node> nodes = nodesResult.getNodes();
                    byte[] serializedRoomId = Serializer.serialize(roomId);
                    if (nodes != null && nodes.size() > 0) {
                        for (Node node : nodes) {
                            Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(),
                                    Constants.HOME_BACON_SCAN_START, serializedRoomId).setResultCallback(
                                    new ResultCallback() {
                                        @Override
                                        public void onResult(Result result) {
                                            if (!result.getStatus().isSuccess()) {
                                                // Failed to send message
                                                Toast("Failed to start scan", Toast.LENGTH_LONG);
                                                Log.e(Constants.DEBUG_TAG, "Start scan failed");
                                            } else
                                                Toast("Scan started successfully", Toast.LENGTH_SHORT);
                                            Log.d(Constants.DEBUG_TAG, "scan started successfully");
                                        }
                                    });
                        }
                    } else {
                        Toast("No watch connected", Toast.LENGTH_SHORT);
                    }
                }
                catch (Exception e){
                    Log.e(Constants.DEBUG_TAG, "Error while sending start scan command");
                    e.printStackTrace();
                }
                finally {
                    mGoogleApiClient.disconnect();
                }
            }
        }).start();
    }

    @Override
    public void stopScan() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mGoogleApiClient.blockingConnect(Constants.CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                    NodeApi.GetConnectedNodesResult nodesResult = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                    List<Node> nodes = nodesResult.getNodes();
                    if (nodes != null && nodes.size() > 0) {
                        for (Node node : nodes) {
                            Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(),
                                    Constants.HOME_BACON_SCAN_STOP, null).setResultCallback(
                                    new ResultCallback() {
                                        @Override
                                        public void onResult(Result result) {
                                            if (!result.getStatus().isSuccess()) {
                                                // Failed to send message
                                                Toast("Failed to stop scan", Toast.LENGTH_LONG);
                                                Log.e(Constants.DEBUG_TAG, "Stop scan failed");
                                            } else
                                                Toast("Scan stopped successfully", Toast.LENGTH_SHORT);
                                            Log.d(Constants.DEBUG_TAG, "scan stopped successfully");
                                        }
                                    });
                        }
                    } else {
                        Toast("No watch connected", Toast.LENGTH_SHORT);
                    }
                }
                catch (Exception e){
                    Log.e(Constants.DEBUG_TAG, "Error while sending stop scan command");
                    e.printStackTrace();
                }
                finally {
                    mGoogleApiClient.disconnect();
                }
            }
        }).start();
    }

    @Override
    public void sendModel(final PredictionModel model) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mGoogleApiClient.blockingConnect(Constants.CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                    NodeApi.GetConnectedNodesResult nodesResult = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                    List<Node> nodes = nodesResult.getNodes();
                    byte[] serializedModel = Serializer.serialize(model);
                    if (nodes != null && nodes.size() > 0) {
                        for (Node node : nodes) {
                            Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(),
                                    Constants.HOME_BACON_SEND_MODEL, serializedModel).setResultCallback(
                                    new ResultCallback() {
                                        @Override
                                        public void onResult(Result result) {
                                            if (!result.getStatus().isSuccess()) {
                                                // Failed to send message
                                                Toast("Failed to send model", Toast.LENGTH_LONG);
                                                Log.e(Constants.DEBUG_TAG, "Stop scan failed");
                                            } else
                                                Toast("Model sent successfully", Toast.LENGTH_SHORT);
                                            Log.d(Constants.DEBUG_TAG, "scan stopped successfully");
                                        }
                                    });
                        }
                    } else {
                        Toast("No watch connected", Toast.LENGTH_SHORT);
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

    public void Toast(final String text, final int toastLength){
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mActivity.getApplicationContext(), text, toastLength).show();
            }
        });
    }
}
