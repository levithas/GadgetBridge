package nodomain.freeyourgadget.gadgetbridge.service;

import android.app.Service;
import android.content.Intent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.devices.AiXDroidFeature;
import nodomain.freeyourgadget.gadgetbridge.devices.SleepAsAndroidFeature;
import nodomain.freeyourgadget.gadgetbridge.externalevents.aixdroid.AiXDroidKey;
import nodomain.freeyourgadget.gadgetbridge.externalevents.aixdroid.AiXDroidReceiver;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;

public class AiXDroidSender {

    private final Logger LOG = LoggerFactory.getLogger(AiXDroidReceiver.class);

    private GBDevice device;
    private Set<AiXDroidFeature> features;


    private ScheduledExecutorService trackingService;
    private boolean trackingActive = false;

    private float maxMagnitudeAcceleration;
    private float heartRate;
    private long lastUpdateTime;

    public AiXDroidSender(GBDevice gbDevice) {
        this.device = gbDevice;
        this.features = gbDevice.getDeviceCoordinator().getAiXDroidFeatures();

        this.trackingService = Executors.newSingleThreadScheduledExecutor();
        this.trackingService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                syncData();
                requestData();
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    private void syncData() {
        LOG.info("Synching Data with AiXDroid!");

        if(isFeatureEnabled(AiXDroidFeature.ACCELEROMETER)) {
            Map<Long, Float> dataAcc = new HashMap<>();
            dataAcc.put(System.currentTimeMillis(), maxMagnitudeAcceleration);
            sendWriteDataIntent("Movement_BangleJS", dataAcc);
            this.maxMagnitudeAcceleration = 0;
        }

        if(isFeatureEnabled(AiXDroidFeature.HEART_RATE)) {
            Map<Long, Float> dataHr = new HashMap<>();
            dataHr.put(System.currentTimeMillis(), heartRate);
            sendWriteDataIntent("HeartRate_BangleJS", dataHr);
            this.heartRate = 0;
        }
    }

    private void requestData() {
        LOG.info("Request Data from AiXDroid!");

        sendReadDataIntent("BangleJS_Sleeplabels", System.currentTimeMillis(), 600);
    }

    private boolean isFeatureEnabled(AiXDroidFeature feature) {
        switch (feature) {
            case ACCELEROMETER:
                return GBApplication.getPrefs().getBoolean(AiXDroidKey.PREF_KEY_MOVEMENT, false);
            case HEART_RATE:
                return GBApplication.getPrefs().getBoolean(AiXDroidKey.PREF_KEY_HEART_RATE, false);

            default:
                return false;
        }
    }

    public boolean isValidAction(String action) {
        return action.equals(AiXDroidKey.RESPONSE_ACTION_READ_DATA);
    }

    public void onAccelChanged(float x, float y, float z) {
        if (isDeviceDefault() && isFeatureEnabled(AiXDroidFeature.ACCELEROMETER) && features.contains(AiXDroidFeature.ACCELEROMETER) && trackingActive)
        {
            float magnitude = (float) Math.sqrt(x * x + y * y + z * z);
            if (magnitude > maxMagnitudeAcceleration) {
                maxMagnitudeAcceleration = magnitude;
            }
            this.lastUpdateTime = System.currentTimeMillis() / 1000;
        }
    }

    public void onHeartRateChanged(float heartRate, long lastTimeStamp) {
        if (isDeviceDefault() && isFeatureEnabled(AiXDroidFeature.HEART_RATE) && features.contains(AiXDroidFeature.HEART_RATE) && trackingActive
            && heartRate != 0)
        {
            this.heartRate = heartRate;
            this.lastUpdateTime = lastTimeStamp;
        }
    }

    private void sendReadDataIntent(String dataName, long lastTimeValue, int dataCount) {
        Intent intent = new Intent(AiXDroidKey.ACTION_READ_DATA);
        intent.putExtra("seriesName", dataName);
        intent.putExtra("lastTimeValue", lastTimeValue);
        intent.putExtra("dataCount", dataCount);
        LOG.debug("Sending Read_Data!");
        broadcastToAiXDroid(intent);
    }

    private void sendWriteDataIntent(String dataName, Map<Long, Float> dataDict) {
        Intent intent = new Intent(AiXDroidKey.ACTION_WRITE_DATA);
        intent.putExtra("seriesName", dataName);
        intent.putExtra("timeValues", getLongArrayFromEnumerator(dataDict.keySet()));
        intent.putExtra("dataValues", getFloatArrayFromEnumerator(dataDict.values()));
        LOG.debug("Sending Write_Data!");
        broadcastToAiXDroid(intent);
    }

    public boolean isAiXDroidEnabled() {
        return GBApplication.getPrefs().getBoolean(AiXDroidKey.PREF_KEY_AIXDROID_ENABLED, false);
    }

    public boolean isDeviceDefault() {
        if (device == null || !device.isInitialized()) return false;
        if (isAiXDroidEnabled()) {
            return device.getAddress().equals(GBApplication.getPrefs().getString(AiXDroidKey.DEVICE_KEY, ""));
        }
        return false;
    }

    private void broadcastToAiXDroid(Intent intent) {
        if (!isDeviceDefault()) return;
        intent.setPackage(AiXDroidKey.PACKAGE_AIXDROID);
        LOG.info("Send broadcast to " + intent.getPackage());
        GBApplication.getContext().sendBroadcast(intent);
    }

    private long[] getLongArrayFromEnumerator(Set<Long> keySet) {
        return keySet.stream().mapToLong(Long::longValue).toArray();
    }


    private float[] getFloatArrayFromEnumerator(Collection<Float> valueCollection) {
        List<Float> valueList = new ArrayList<>(valueCollection);
        float[] floatArray = new float[valueList.size()];
        for(int i = 0;i<floatArray.length;i++) {
            floatArray[i] = valueList.get(i);
        }
        return floatArray;
    }
}
