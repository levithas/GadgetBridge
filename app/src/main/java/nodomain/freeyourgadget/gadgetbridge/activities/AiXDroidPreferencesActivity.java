package nodomain.freeyourgadget.gadgetbridge.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.devices.AiXDroidFeature;
import nodomain.freeyourgadget.gadgetbridge.externalevents.aixdroid.AiXDroidKey;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;

public class AiXDroidPreferencesActivity extends AbstractSettingsActivityV2 {
    @Override
    protected String fragmentTag() {
        return AiXDroidPreferencesFragment.FRAGMENT_TAG;
    }

    @Override
    protected PreferenceFragmentCompat newFragment() {
        return new AiXDroidPreferencesFragment();
    }

    public static class AiXDroidPreferencesFragment extends AbstractPreferenceFragment {
        static final String FRAGMENT_TAG = "AIXDROID_PREFERENCES_FRAGMENT";


        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            setPreferencesFromResource(R.xml.aixdroid_preferences, rootKey);

            registerDevicePreferencesUpdateListener();
            updateDefaultDevice();
        }

        private void updateDefaultDevice() {
            String defaultDeviceAddr = GBApplication.getPrefs().getString(AiXDroidKey.DEVICE_KEY, "");
            if (!defaultDeviceAddr.isEmpty()) {
                GBDevice device = GBApplication.app().getDeviceManager().getDeviceByAddress(defaultDeviceAddr);
                if (device != null) {
                    updateDevicePreferences(device);
                }
            }
        }

        private void updateDevicePreferences(GBDevice device) {
            Set<AiXDroidFeature> supportedFeatures = device.getDeviceCoordinator().getAiXDroidFeatures();
            Preference pref_movement = findPreference(AiXDroidKey.PREF_KEY_MOVEMENT);
            Preference pref_heart_rate = findPreference(AiXDroidKey.PREF_KEY_HEART_RATE);

            if (pref_movement != null) {
                pref_movement.setEnabled(supportedFeatures.contains(AiXDroidFeature.ACCELEROMETER));
            }
            if (pref_heart_rate != null) {
                pref_heart_rate.setEnabled(supportedFeatures.contains(AiXDroidFeature.HEART_RATE));
            }
        }

        private void registerDevicePreferencesUpdateListener() {
            final ListPreference aiXDroidDevices = findPreference(AiXDroidKey.DEVICE_KEY);
            if (aiXDroidDevices != null) {
                loadDevicesList(aiXDroidDevices);
                aiXDroidDevices.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
                        GBDevice device = GBApplication.app().getDeviceManager().getDeviceByAddress(newValue.toString());
                        if (device != null) {

                            // Set as Default Device
                            GBApplication.getPrefs().getPreferences().edit().putString(AiXDroidKey.DEVICE_KEY, device.getAddress()).apply();

                            updateDevicePreferences(device);
                        }
                        return false;
                    }
                });
            }
        }
    }

    private static void loadDevicesList(ListPreference aiXDroidDevices) {
        List<GBDevice> devices = GBApplication.app().getDeviceManager().getDevices();
        List<String> deviceMACs = new ArrayList<>();
        List<String> deviceNames = new ArrayList<>();
        for (GBDevice dev : devices) {
            if (dev.getDeviceCoordinator().supportsAiXDroid()) {
                deviceMACs.add(dev.getAddress());
                deviceNames.add(dev.getAliasOrName());
            }
        }

        aiXDroidDevices.setEntryValues(deviceMACs.toArray(new String[0]));
        aiXDroidDevices.setEntries(deviceNames.toArray(new String[0]));
    }
}
