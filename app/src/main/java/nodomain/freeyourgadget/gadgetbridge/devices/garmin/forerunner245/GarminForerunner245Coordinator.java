package nodomain.freeyourgadget.gadgetbridge.devices.garmin.forerunner245;

import java.util.regex.Pattern;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.devices.garmin.GarminCoordinator;

public class GarminForerunner245Coordinator extends GarminCoordinator {
    @Override
    protected Pattern getSupportedDeviceName() {
        return Pattern.compile("^Forerunner 245$");
    }

    @Override
    public int getDeviceNameResource() {
        return R.string.devicetype_garmin_forerunner_245;
    }
}
