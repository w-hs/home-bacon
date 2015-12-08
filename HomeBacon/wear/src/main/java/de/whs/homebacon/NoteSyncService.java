package de.whs.homebacon;

import android.os.Handler;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;

import de.whs.homebaconcore.Constants;

/**
 * Created by Dennis on 01.12.2015.
 */
public class NoteSyncService extends WearableListenerService {

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {  }

    public void onMessageReceived(MessageEvent messageEvent) {
        String p = messageEvent.getPath();
        if (p.equals("/count")) {
            Log.d("MESSAGE","received: " + new String(messageEvent.getData()));
//            Intent startIntent = new Intent(this, MainActivity.class);
//            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startIntent.putExtra("VOICE_DATA", messageEvent.getData());
//            startActivity(startIntent);
        }
    }

    public void onPeerConnected(Node peer) { }

    public void onPeerDisconnected(Node peer) {  }
}


