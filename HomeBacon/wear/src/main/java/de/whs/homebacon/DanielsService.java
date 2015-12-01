package de.whs.homebacon;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.util.Timer;
import java.util.TimerTask;

import de.whs.homebaconcore.Note;

/**
 * Created by Chris on 01.12.2015.
 */
public class DanielsService extends IntentService {
    public DanielsService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Timer timer = new Timer();
        final int intervallInMS = 10 * 1000;
        timer.schedule(new SayDaniel(getApplicationContext()), 0, intervallInMS);
    }

    private class SayDaniel extends TimerTask {
        private final Context applicationContext;

        public SayDaniel(Context applicationContext) {
            this.applicationContext = applicationContext;
        }

        public void run() {
            System.out.println("Hello Daniel!");
            Intent intent = new Intent(IntentIds.NewNoteId);
            intent.putExtra("note", new Note("Notiz 1", "janjon"));
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent);
        }
    }
}
