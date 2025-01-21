package nodomain.freeyourgadget.gadgetbridge.externalevents.aixdroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;

public class AiXDroidReceiver extends BroadcastReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(AiXDroidReceiver.class);

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (GBApplication.getPrefs().getBoolean("pref_key_aixdroid_enable", false)) {
            GBApplication.deviceService().onAiXDroidAction(action, intent.getExtras());
        }
    }
}
